/*
  * Copyright 2019, 2021 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.diva.mixedstorage.db.organisation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbTranslater;
import se.uu.ub.cora.diva.mixedstorage.db.DbException;

public class OrganisationDataToDbTranslaterTest {

	private DataToDbTranslater translater;
	private SqlDatabaseFactorySpy sqlDatabaseFactory;

	@BeforeMethod
	public void setUp() {
		sqlDatabaseFactory = new SqlDatabaseFactorySpy();
		translater = new OrganisationDataToDbTranslater(sqlDatabaseFactory);
	}

	@Test
	public void testConditions() {
		DataGroup dataGroup = createDataGroupWithIdTypeAndOrgType("56", "subOrganisation", "unit");
		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 56);
	}

	private DataGroup createDataGroupWithIdTypeAndOrgType(String id, String recordType,
			String orgType) {
		DataGroup dataGroup = new DataGroupSpy("organisation");
		DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", id));
		DataGroupSpy type = createType(recordType);
		recordInfo.addChild(type);
		dataGroup.addChild(recordInfo);
		dataGroup.addChild(new DataAtomicSpy("organisationType", orgType));

		DataGroup organisationName = new DataGroupSpy("organisationName");
		organisationName.addChild(new DataAtomicSpy("name", "someChangedName"));
		organisationName.addChild(new DataAtomicSpy("language", "sv"));
		dataGroup.addChild(organisationName);

		return dataGroup;
	}

	private DataGroupSpy createType(String recordType) {
		DataGroupSpy type = new DataGroupSpy("type");
		type.addChild(new DataAtomicSpy("linkedRecordType", "recordType"));
		type.addChild(new DataAtomicSpy("linkedRecordId", recordType));
		return type;
	}

	@Test
	public void testLastUpdated() {
		DataGroup dataGroup = createDataGroupWithIdTypeAndOrgType("45", "subOrganisation", "unit");

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");

		Timestamp lastUpdated = (Timestamp) translater.getValues().get("last_updated");
		String lastUpdatedString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
				.format(lastUpdated);
		assertTrue(lastUpdatedString
				.matches("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\.\\d{1,3}"));
	}

	@Test
	public void testOrganisationNameInValues() {
		DataGroup dataGroup = createDataGroupWithIdTypeAndOrgType("45", "subOrganisation", "unit");

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		assertEquals(translater.getValues().get("organisation_name_locale"), "sv");
	}

	@Test
	public void testUpdateAllAtomicChildrenInOrganisation() throws Exception {
		DataGroup dataGroup = createDataGroupWithIdTypeAndOrgType("45", "subOrganisation", "unit");
		dataGroup.addChild(new DataAtomicSpy("closedDate", "2017-10-31"));
		dataGroup.addChild(new DataAtomicSpy("organisationCode", "1235"));
		dataGroup.addChild(new DataAtomicSpy("organisationNumber", "78979-45654"));
		dataGroup.addChild(new DataAtomicSpy("URL", "www.someaddress.se"));

		translater.translate(dataGroup);

		assertEquals(translater.getConditions().get("organisation_id"), 45);

		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		Date closedDate = (Date) translater.getValues().get("closed_date");
		assertEquals(closedDate, Date.valueOf("2017-10-31"));
		assertEquals(translater.getValues().get("organisation_code"), "1235");
		assertEquals(translater.getValues().get("orgnumber"), "78979-45654");
		assertEquals(translater.getValues().get("organisation_homepage"), "www.someaddress.se");
	}

	@Test
	public void testUpdateEmptyDataAtomicsAreSetToNullInQuery() throws Exception {
		DataGroup dataGroup = createDataGroupWithIdTypeAndOrgType("45", "subOrganisation", "unit");

		translater.translate(dataGroup);

		assertEquals(translater.getConditions().get("organisation_id"), 45);

		assertEquals(translater.getValues().get("closed_date"), null);
		assertEquals(translater.getValues().get("organisation_code"), null);
		assertEquals(translater.getValues().get("orgnumber"), null);
		assertEquals(translater.getValues().get("organisation_homepage"), null);
	}

	@Test
	public void testValuesAndConditionsAreOverwrittenWhenNewTranslateIsCalled() {
		DataGroup dataGroup = createDataGroupWithIdTypeAndOrgType("45", "subOrganisation", "unit");

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().size(), 1);
		assertEquals(translater.getValues().size(), 12);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		Timestamp lastUpdated = (Timestamp) translater.getValues().get("last_updated");

		DataGroup dataGroup2 = createDataGroupWithIdTypeAndOrgType("4500", "subOrganisation",
				"unit");
		DataGroup nameGroup = dataGroup2.getFirstGroupWithNameInData("organisationName");
		nameGroup.removeFirstChildWithNameInData("name");
		nameGroup.addChild(new DataAtomicSpy("name", "someOtherChangedName"));

		translater.translate(dataGroup2);
		assertEquals(translater.getConditions().size(), 1);
		assertEquals(translater.getValues().size(), 12);

		assertEquals(translater.getConditions().get("organisation_id"), 4500);
		assertEquals(translater.getValues().get("organisation_name"), "someOtherChangedName");
		Timestamp lastUpdated2 = (Timestamp) translater.getValues().get("last_updated");
		assertNotSame(lastUpdated, lastUpdated2);

	}

	@Test(expectedExceptions = DbException.class)
	public void testUpdateOrganisationIdNotAnInt() throws Exception {
		DataGroup dataGroup = createDataGroupWithIdTypeAndOrgType("notAnInt", "subOrganisation",
				"unit");
		translater.translate(dataGroup);

	}

	@Test
	public void testOrganisationNotSelectable() {
		DataGroup dataGroup = createDataGroupWithIdTypeAndOrgType("45", "subOrganisation", "unit");
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("selectable", "no"));

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		assertEquals(translater.getValues().get("not_eligible"), true);
	}

	@Test
	public void testOrganisationEligable() {
		DataGroup dataGroup = createDataGroupWithIdTypeAndOrgType("45", "subOrganisation", "unit");
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("selectable", "yes"));

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		assertEquals(translater.getValues().get("not_eligible"), false);
	}

	@Test
	public void testOrganisationShowInPortalTrueWhenTopOrganisation() {
		DataGroup dataGroup = createDataGroupWithIdTypeAndOrgType("45", "topOrganisation", "unit");

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		assertEquals(translater.getValues().get("show_in_portal"), true);
	}

	private DataGroup createDataGroupAddChildWithNameInDataAndValue(String nameInData,
			String value) {
		DataGroup dataGroup = createDataGroupWithIdTypeAndOrgType("45", "subOrganisation", "unit");
		dataGroup.addChild(new DataAtomicSpy(nameInData, value));
		return dataGroup;
	}

	@Test
	public void testOrganisationShowInPortalFalseWhenSubOrganisation() {
		DataGroup dataGroup = createDataGroupWithIdTypeAndOrgType("45", "subOrganisation", "unit");

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		assertEquals(translater.getValues().get("show_in_portal"), false);
	}

	@Test
	public void testOrganisationShowInPortalFalseWhenRootOrganisation() {
		DataGroup dataGroup = createDataGroupWithIdTypeAndOrgType("45", "rootOrganisation", "unit");

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		assertEquals(translater.getValues().get("show_in_portal"), false);
	}

	@Test
	public void testOrganisationShowInDefenceTrue() {
		DataGroup dataGroup = createDataGroupAddChildWithNameInDataAndValue("doctoralDegreeGrantor",
				"yes");

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		assertEquals(translater.getValues().get("show_in_defence"), true);
	}

	@Test
	public void testOrganisationShowInDefenceFalse() {
		DataGroup dataGroup = createDataGroupAddChildWithNameInDataAndValue("doctoralDegreeGrantor",
				"no");

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		assertEquals(translater.getValues().get("show_in_defence"), false);
	}

	@Test
	public void testOrganisationTopLevelTrueForTopOrganisation() {
		DataGroup dataGroup = createDataGroupWithIdTypeAndOrgType("45", "topOrganisation", "unit");

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		assertEquals(translater.getValues().get("top_level"), true);
	}

	@Test
	public void testOrganisationTopLevelFalseForSubOrganisation() {
		DataGroup dataGroup = createDataGroupWithIdTypeAndOrgType("45", "subOrganisation", "unit");

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		assertEquals(translater.getValues().get("top_level"), false);
	}

	@Test
	public void testOrganisationTopLevelFalseForRootOrganisation() {
		DataGroup dataGroup = createDataGroupWithIdTypeAndOrgType("45", "rootOrganisation", "unit");

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		assertEquals(translater.getValues().get("top_level"), false);
	}

	@Test
	public void testOrganisationTypeWhenSubOrganisation() {
		RowSpy rowToReturnForOrgType = new RowSpy();
		sqlDatabaseFactory.rowToReturn = rowToReturnForOrgType;
		translater = new OrganisationDataToDbTranslater(sqlDatabaseFactory);

		rowToReturnForOrgType.addColumnWithValue("organisation_type_id", 52);
		DataGroup dataGroup = createDataGroupWithIdTypeAndOrgType("45", "subOrganisation", "unit");

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertOrgTypeIsReadFromDbAndUsed();
	}

	private void assertOrgTypeIsReadFromDbAndUsed() {
		assertEquals(sqlDatabaseFactory.tableName, "organisation_type");
		TableQuerySpy tableQuery = sqlDatabaseFactory.factoredTableQuery;
		assertEquals(tableQuery.conditions.get("organisation_type_code"), "unit");
		TableFacadeSpy tableFacade = sqlDatabaseFactory.factoredTableFacade;
		assertSame(tableFacade.tableQuery, tableQuery);

		assertEquals(translater.getValues().get("organisation_type_id"), 52);
	}

	@Test
	public void testOrganisationTypeWhenRootOrganisation() {
		DataGroup dataGroup = createDataGroupWithIdTypeAndOrgType("45", "rootOrganisation", "unit");

		translater.translate(dataGroup);
		assertEquals(translater.getValues().get("organisation_type_id"), 49);
	}

}
