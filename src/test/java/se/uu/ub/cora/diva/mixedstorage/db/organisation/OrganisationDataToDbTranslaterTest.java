/*
  * Copyright 2019 Uppsala University Library
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
import se.uu.ub.cora.diva.mixedstorage.db.RecordReaderSpy;

public class OrganisationDataToDbTranslaterTest {

	private RecordReaderSpy recordReader;
	private DataToDbTranslater translater;

	@BeforeMethod
	public void setUp() {
		recordReader = new RecordReaderSpy();
		translater = new OrganisationDataToDbTranslater(recordReader);
	}

	@Test
	public void testConditions() {
		DataGroup dataGroup = createDataGroupWithId("56");
		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 56);
	}

	private DataGroup createDataGroupWithId(String id) {
		DataGroup dataGroup = new DataGroupSpy("organisation");
		DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", id));
		dataGroup.addChild(recordInfo);
		dataGroup.addChild(new DataAtomicSpy("organisationType", "unit"));

		DataGroup organisationName = new DataGroupSpy("name");
		organisationName.addChild(new DataAtomicSpy("organisationName", "someChangedName"));
		organisationName.addChild(new DataAtomicSpy("language", "sv"));
		dataGroup.addChild(organisationName);

		return dataGroup;
	}

	@Test
	public void testLastUpdated() {
		DataGroup dataGroup = createDataGroupWithId("45");
		dataGroup.addChild(new DataAtomicSpy("organisationName", "someChangedName"));

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
		DataGroup dataGroup = createDataGroupWithId("45");

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		assertEquals(translater.getValues().get("organisation_name_locale"), "sv");
	}

	@Test
	public void testUpdateAllAtomicChildrenInOrganisation() throws Exception {
		DataGroup dataGroup = createDataGroupWithId("45");
		dataGroup.addChild(new DataAtomicSpy("organisationName", "someChangedName"));
		dataGroup.addChild(new DataAtomicSpy("closedDate", "2017-10-31"));
		dataGroup.addChild(new DataAtomicSpy("organisationCode", "1235"));
		dataGroup.addChild(new DataAtomicSpy("organisationNumber", "78979-45654"));
		dataGroup.addChild(new DataAtomicSpy("URL", "www.someaddress.se"));
		dataGroup.addChild(new DataAtomicSpy("librisId", "someLibrisId"));

		translater.translate(dataGroup);

		assertEquals(translater.getConditions().get("organisation_id"), 45);

		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		Date closedDate = (Date) translater.getValues().get("closed_date");
		assertEquals(closedDate, Date.valueOf("2017-10-31"));
		assertEquals(translater.getValues().get("organisation_code"), "1235");
		assertEquals(translater.getValues().get("orgnumber"), "78979-45654");
		assertEquals(translater.getValues().get("organisation_homepage"), "www.someaddress.se");
		assertEquals(translater.getValues().get("libris_code"), "someLibrisId");
	}

	@Test
	public void testUpdateEmptyDataAtomicsAreSetToNullInQuery() throws Exception {
		DataGroup dataGroup = createDataGroupWithId("45");

		translater.translate(dataGroup);

		assertEquals(translater.getConditions().get("organisation_id"), 45);

		assertEquals(translater.getValues().get("closed_date"), null);
		assertEquals(translater.getValues().get("organisation_code"), null);
		assertEquals(translater.getValues().get("orgnumber"), null);
		assertEquals(translater.getValues().get("organisation_homepage"), null);
	}

	@Test
	public void testValuesAndConditionsAreOverwrittenWhenNewTranslateIsCalled() {
		DataGroup dataGroup = createDataGroupWithId("45");

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().size(), 1);
		assertEquals(translater.getValues().size(), 13);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		Timestamp lastUpdated = (Timestamp) translater.getValues().get("last_updated");

		DataGroup dataGroup2 = createDataGroupWithId("4500");
		DataGroup nameGroup = dataGroup2.getFirstGroupWithNameInData("name");
		nameGroup.removeFirstChildWithNameInData("organisationName");
		nameGroup.addChild(new DataAtomicSpy("organisationName", "someOtherChangedName"));

		translater.translate(dataGroup2);
		assertEquals(translater.getConditions().size(), 1);
		assertEquals(translater.getValues().size(), 13);

		assertEquals(translater.getConditions().get("organisation_id"), 4500);
		assertEquals(translater.getValues().get("organisation_name"), "someOtherChangedName");
		Timestamp lastUpdated2 = (Timestamp) translater.getValues().get("last_updated");
		assertNotSame(lastUpdated, lastUpdated2);

	}

	@Test(expectedExceptions = DbException.class)
	public void testUpdateOrganisationIdNotAnInt() throws Exception {
		DataGroup dataGroup = createDataGroupWithId("notAnInt");
		translater.translate(dataGroup);

	}

	@Test
	public void testOrganisationNotEligable() {
		DataGroup dataGroup = createDataGroupWithId("45");
		dataGroup.addChild(new DataAtomicSpy("organisationName", "someChangedName"));
		dataGroup.addChild(new DataAtomicSpy("eligible", "no"));

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		assertEquals(translater.getValues().get("not_eligible"), true);
	}

	@Test
	public void testOrganisationEligable() {
		DataGroup dataGroup = createDataGroupWithId("45");
		dataGroup.addChild(new DataAtomicSpy("organisationName", "someChangedName"));
		dataGroup.addChild(new DataAtomicSpy("eligible", "yes"));

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		assertEquals(translater.getValues().get("not_eligible"), false);
	}

	@Test
	public void testOrganisationShowInPortalTrue() {
		DataGroup dataGroup = createDataGroupAddChildWithNameInDataAndValue("showInPortal", "yes");

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		assertEquals(translater.getValues().get("show_in_portal"), true);
	}

	private DataGroup createDataGroupAddChildWithNameInDataAndValue(String nameInData,
			String value) {
		DataGroup dataGroup = createDataGroupWithId("45");
		dataGroup.addChild(new DataAtomicSpy("organisationName", "someChangedName"));
		dataGroup.addChild(new DataAtomicSpy(nameInData, value));
		return dataGroup;
	}

	@Test
	public void testOrganisationShowInPortalFalse() {
		DataGroup dataGroup = createDataGroupAddChildWithNameInDataAndValue("showInPortal", "no");

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		assertEquals(translater.getValues().get("show_in_portal"), false);
	}

	@Test
	public void testOrganisationShowInDefenceTrue() {
		DataGroup dataGroup = createDataGroupAddChildWithNameInDataAndValue("showInDefence", "yes");

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		assertEquals(translater.getValues().get("show_in_defence"), true);
	}

	@Test
	public void testOrganisationShowInDefenceFalse() {
		DataGroup dataGroup = createDataGroupAddChildWithNameInDataAndValue("showInDefence", "no");

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		assertEquals(translater.getValues().get("show_in_defence"), false);
	}

	@Test
	public void testOrganisationTopLevelTrue() {
		DataGroup dataGroup = createDataGroupAddChildWithNameInDataAndValue("topLevel", "yes");

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		assertEquals(translater.getValues().get("top_level"), true);
	}

	@Test
	public void testOrganisationTopLevelFalse() {
		DataGroup dataGroup = createDataGroupAddChildWithNameInDataAndValue("topLevel", "no");

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		assertEquals(translater.getValues().get("top_level"), false);
	}

	@Test
	public void testOrganisationType() {
		DataGroup dataGroup = createDataGroupWithId("45");
		dataGroup.addChild(new DataAtomicSpy("organisationName", "someChangedName"));

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertOrgTypeIsReadFromDbAndUsed();
	}

	private void assertOrgTypeIsReadFromDbAndUsed() {
		assertEquals(recordReader.usedTableNames.size(), 1);
		assertEquals(recordReader.usedTableNames.get(0), "organisation_type");
		assertEquals(recordReader.usedConditions.get("organisation_type_code"), "unit");

		assertEquals(recordReader.oneRowRead.get("organisation_type_id"),
				translater.getValues().get("organisation_type_id"));
	}

	@Test
	public void testOrganisationRootOrganisationFalseUsesOrganisationType() {
		DataGroup dataGroup = createDataGroupAddChildWithNameInDataAndValue("rootOrganisation",
				"no");

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		assertOrgTypeIsReadFromDbAndUsed();
	}

}
