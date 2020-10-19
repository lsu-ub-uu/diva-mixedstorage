/*
 * Copyright 2018, 2019, 2020 Uppsala University Library
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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.db.ConversionException;

public class DivaDbToCoraOrganisationConverterTest {

	private DivaDbToCoraOrganisationConverter converter;
	private Map<String, Object> rowFromDb;
	private DataGroupFactorySpy dataGroupFactorySpy;
	private DataAtomicFactorySpy dataAtomicFactorySpy;

	@BeforeMethod
	public void beforeMethod() {
		dataGroupFactorySpy = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactorySpy);
		dataAtomicFactorySpy = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactorySpy);
		rowFromDb = new HashMap<>();
		rowFromDb.put("id", "someOrgId");
		rowFromDb.put("type_code", "unit");
		converter = new DivaDbToCoraOrganisationConverter();
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation to Cora organisation: Map does not contain value for id")
	public void testEmptyMap() {
		rowFromDb = new HashMap<>();
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertNull(organisation);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation to Cora organisation: Map does not contain value for id")
	public void testMapWithEmptyValueThrowsError() {
		rowFromDb = new HashMap<>();
		rowFromDb.put("id", "");
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation to Cora organisation: Map does not contain value for id")
	public void testMapWithNonEmptyValueANDEmptyValueThrowsError() {
		Map<String, Object> rowFromDb = new HashMap<>();
		rowFromDb.put("defaultname", "someName");
		rowFromDb.put("id", "");
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation to Cora organisation: Map does not contain value for id")
	public void mapDoesNotContainOrganisationIdValue() {
		rowFromDb = new HashMap<>();
		rowFromDb.put("defaultname", "someName");
		converter.fromMap(rowFromDb);
	}

	@Test
	public void testMinimalValuesReturnsDataGroupWithCorrectRecordInfo() {
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getNameInData(), "organisation");
		assertCorrectRecordInfoWithIdAndRecordType(organisation, "someOrgId", "commonOrganisation");

		DataGroupSpy factoredOrganisation = dataGroupFactorySpy.factoredDataGroups.get(0);
		assertEquals(factoredOrganisation.nameInData, "organisation");

		DataGroupSpy factoredRecordInfo = dataGroupFactorySpy.factoredDataGroups.get(1);
		assertEquals(factoredRecordInfo.nameInData, "recordInfo");
		assertSame(factoredRecordInfo, organisation.getFirstChildWithNameInData("recordInfo"));

		DataAtomicSpy factoredDataAtomicForId = getFactoredDataAtomicByNumber(0);
		assertEquals(factoredDataAtomicForId.nameInData, "id");
		assertEquals(factoredDataAtomicForId.value, "someOrgId");
	}

	private void assertCorrectRecordInfoWithIdAndRecordType(DataGroup organisation, String id,
			String recordType) {
		DataGroup recordInfo = organisation.getFirstGroupWithNameInData("recordInfo");
		assertEquals(recordInfo.getFirstAtomicValueWithNameInData("id"), id);

		DataGroup type = recordInfo.getFirstGroupWithNameInData("type");
		assertEquals(type.getFirstAtomicValueWithNameInData("linkedRecordType"), "recordType");
		assertEquals(type.getFirstAtomicValueWithNameInData("linkedRecordId"), recordType);

		DataGroup dataDivider = recordInfo.getFirstGroupWithNameInData("dataDivider");
		assertEquals(dataDivider.getFirstAtomicValueWithNameInData("linkedRecordType"), "system");
		assertEquals(dataDivider.getFirstAtomicValueWithNameInData("linkedRecordId"), "diva");

		assertCorrectCreatedAndUpdatedInfo(recordInfo);
	}

	private DataAtomicSpy getFactoredDataAtomicByNumber(int noFactored) {
		return dataAtomicFactorySpy.factoredDataAtomics.get(noFactored);
	}

	private DataAtomicSpy getFactoredDataAtomicByNameInData(String nameInData) {
		return dataAtomicFactorySpy.factoredDataAtomicsMap.get(nameInData);
	}

	@Test
	public void testCorrectRecordTypeWhenOrganisationTypeIsRoot() {
		rowFromDb.put("type_code", "root");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getNameInData(), "organisation");
		assertCorrectRecordInfoWithIdAndRecordType(organisation, "someOrgId", "rootOrganisation");

	}

	@Test
	public void testOrganisationDomain() throws Exception {
		rowFromDb.put("domain", "uu");

		DataGroup organisation = converter.fromMap(rowFromDb);

		DataAtomicSpy factoredDataAtomicForId = getFactoredDataAtomicByNumber(11);
		assertEquals(factoredDataAtomicForId.nameInData, "domain");
		assertEquals(factoredDataAtomicForId.value, "uu");

		assertEquals(organisation.getFirstAtomicValueWithNameInData("domain"), "uu");
	}

	@Test
	public void testOrganisationName() {
		rowFromDb.put("defaultname", "Java-fakulteten");
		rowFromDb.put("organisation_name_locale", "sv");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getNameInData(), "organisation");

		assertCorrectValuesForNameWasFactored();

		DataGroup nameGroup = organisation.getFirstGroupWithNameInData("name");
		assertEquals(nameGroup.getFirstAtomicValueWithNameInData("organisationName"),
				"Java-fakulteten");
		assertEquals(nameGroup.getFirstAtomicValueWithNameInData("language"), "sv");
	}

	private void assertCorrectValuesForNameWasFactored() {
		DataAtomicSpy factoredDataAtomicForName = getFactoredDataAtomicByNumber(12);
		assertEquals(factoredDataAtomicForName.nameInData, "organisationName");
		assertEquals(factoredDataAtomicForName.value, "Java-fakulteten");
		DataAtomicSpy factoredDataAtomicForLanguage = getFactoredDataAtomicByNumber(13);
		assertEquals(factoredDataAtomicForLanguage.nameInData, "language");
		assertEquals(factoredDataAtomicForLanguage.value, "sv");
	}

	@Test
	public void testTypeCode() {
		String typeCode = "university";
		rowFromDb.put("type_code", typeCode);

		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getFirstAtomicValueWithNameInData("organisationType"), typeCode);
	}

	@Test
	public void testAlternativeName() {
		rowFromDb.put("alternative_name", "Java Faculty");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertTrue(organisation.containsChildWithNameInData("alternativeName"));
		DataGroup alternativeName = organisation.getFirstGroupWithNameInData("alternativeName");
		assertEquals(alternativeName.getFirstAtomicValueWithNameInData("language"), "en");
		assertEquals(alternativeName.getFirstAtomicValueWithNameInData("organisationName"),
				"Java Faculty");
	}

	@Test
	public void testOrganisationNotEligible() {
		rowFromDb.put("not_eligible", true);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getFirstAtomicValueWithNameInData("eligible"), "no");
	}

	@Test
	public void testOrganisationEligible() {
		rowFromDb.put("not_eligible", false);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getFirstAtomicValueWithNameInData("eligible"), "yes");
	}

	private void assertCorrectCreatedAndUpdatedInfo(DataGroup recordInfo) {
		assertEquals(recordInfo.getFirstAtomicValueWithNameInData("tsCreated"),
				"2017-01-01T00:00:00.000000Z");

		DataGroup createdBy = recordInfo.getFirstGroupWithNameInData("createdBy");
		assertEquals(createdBy.getFirstAtomicValueWithNameInData("linkedRecordType"), "coraUser");
		assertEquals(createdBy.getFirstAtomicValueWithNameInData("linkedRecordId"),
				"coraUser:4412982402853626");

		assertEquals(recordInfo.getAllGroupsWithNameInData("updated").size(), 1);
		DataGroup updated = recordInfo.getFirstGroupWithNameInData("updated");
		assertEquals(updated.getFirstAtomicValueWithNameInData("tsUpdated"),
				"2017-01-01T00:00:00.000000Z");
		assertEquals(updated.getRepeatId(), "0");

		DataGroup updatedBy = updated.getFirstGroupWithNameInData("updatedBy");
		assertEquals(updatedBy.getFirstAtomicValueWithNameInData("linkedRecordType"), "coraUser");
		assertEquals(updatedBy.getFirstAtomicValueWithNameInData("linkedRecordId"),
				"coraUser:4412982402853626");

	}

	@Test
	public void testAdressMissing() {
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("city"));
		assertFalse(organisation.containsChildWithNameInData("street"));
		assertFalse(organisation.containsChildWithNameInData("box"));
		assertFalse(organisation.containsChildWithNameInData("postcode"));
		assertFalse(organisation.containsChildWithNameInData("country"));

	}

	@Test
	public void testAdress() {
		rowFromDb.put("city", "uppsala");
		rowFromDb.put("street", "Övre slottsgatan 1");
		rowFromDb.put("postbox", "Box5435");
		rowFromDb.put("postnumber", "345 34");
		rowFromDb.put("country_code", "fi");

		DataGroup organisation = converter.fromMap(rowFromDb);
		DataAtomicSpy factoredDataAtomicForCity = getFactoredDataAtomicByNameInData("city");
		assertEquals(factoredDataAtomicForCity.nameInData, "city");
		assertEquals(factoredDataAtomicForCity.value, "uppsala");

		DataAtomicSpy factoredDataAtomicForStreet = getFactoredDataAtomicByNameInData("street");
		assertEquals(factoredDataAtomicForStreet.nameInData, "street");
		assertEquals(factoredDataAtomicForStreet.value, "Övre slottsgatan 1");

		DataAtomicSpy factoredDataAtomicForBox = getFactoredDataAtomicByNameInData("box");
		assertEquals(factoredDataAtomicForBox.nameInData, "box");
		assertEquals(factoredDataAtomicForBox.value, "Box5435");

		DataAtomicSpy factoredDataAtomicForPostcode = getFactoredDataAtomicByNameInData("postcode");
		assertEquals(factoredDataAtomicForPostcode.nameInData, "postcode");
		assertEquals(factoredDataAtomicForPostcode.value, "345 34");

		DataAtomicSpy factoredDataAtomicForCountry = getFactoredDataAtomicByNameInData("country");
		assertEquals(factoredDataAtomicForCountry.nameInData, "country");
		assertEquals(factoredDataAtomicForCountry.value, "FI");

		assertEquals(organisation.getFirstAtomicValueWithNameInData("city"), "uppsala");
		assertEquals(organisation.getFirstAtomicValueWithNameInData("street"),
				"Övre slottsgatan 1");
		assertEquals(organisation.getFirstAtomicValueWithNameInData("box"), "Box5435");
		assertEquals(organisation.getFirstAtomicValueWithNameInData("postcode"), "345 34");
		assertEquals(organisation.getFirstAtomicValueWithNameInData("country"), "FI");

	}

	@Test
	public void testOrganisationNumberMissing() {
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("organisationNumber"));
	}

	@Test
	public void testOrganisationNumberIsnull() {
		rowFromDb.put("orgnumber", null);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("organisationNumber"));
	}

	@Test
	public void testOrganisationNumberIsEmpty() {
		rowFromDb.put("orgnumber", "");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("organisationNumber"));
	}

	@Test
	public void testOrganisationNumber() {
		rowFromDb.put("orgnumber", "540002");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getFirstAtomicValueWithNameInData("organisationNumber"),
				"540002");
	}

	@Test
	public void testOrganisationCodeMissing() {
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("organisationCode"));
	}

	@Test
	public void testOrganisationCodeIsNull() {
		rowFromDb.put("organisation_code", null);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("organisationCode"));
	}

	@Test
	public void testOrganisationCodeIsEmpty() {
		rowFromDb.put("organisation_code", "");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("organisationCode"));
	}

	@Test
	public void testOrganisationCode() {
		rowFromDb.put("organisation_code", "56783545");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getFirstAtomicValueWithNameInData("organisationCode"),
				"56783545");
	}

	@Test
	public void testOrganisationUrlMissing() {
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("URL"));
	}

	@Test
	public void testOrganisationUrlIsNull() {
		rowFromDb.put("organisation_homepage", null);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("URL"));
	}

	@Test
	public void testOrganisationUrlIsEmpty() {
		rowFromDb.put("organisation_homepage", "");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("URL"));
	}

	@Test
	public void testOrganisationURL() {
		rowFromDb.put("organisation_homepage", "www.something.org");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getFirstAtomicValueWithNameInData("URL"), "www.something.org");
	}

	@Test
	public void testOrganisationClosedDateMissing() {
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("closedDate"));
	}

	@Test
	public void testOrganisationClosedDateIsnull() {
		rowFromDb.put("closed_date", null);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("closedDate"));
	}

	@Test
	public void testOrganisationClosedDateIsEmpty() {
		rowFromDb.put("closed_date", "");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("closedDate"));
	}

	@Test
	public void testOrganisationClosedDate() {
		Date date = Date.valueOf("2018-12-31");
		rowFromDb.put("closed_date", date);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getFirstAtomicValueWithNameInData("closedDate"), "2018-12-31");
	}

	@Test
	public void testOrganisationLibrisIdMissing() {
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("librisId"));
	}

	@Test
	public void testOrganisationLibrisIdIsNull() {
		rowFromDb.put("libris_code", null);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("librisId"));
	}

	@Test
	public void testOrganisationLibrisIdIsEmpty() {
		rowFromDb.put("libris_code", "");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("librisId"));
	}

	@Test
	public void testOrganisationLibrisId() {
		rowFromDb.put("libris_code", "uuLibrisCode");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getFirstAtomicValueWithNameInData("librisId"), "uuLibrisCode");
	}

	@Test
	public void testOrganisationShowInDefenceFalse() {
		rowFromDb.put("show_in_defence", false);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getFirstAtomicValueWithNameInData("showInDefence"), "no");
	}

	@Test
	public void testOrganisationShowInDefenceTrue() {
		rowFromDb.put("show_in_defence", true);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getFirstAtomicValueWithNameInData("showInDefence"), "yes");
	}

	@Test
	public void testOrganisationTopLevelFalse() {
		rowFromDb.put("top_level", false);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getFirstAtomicValueWithNameInData("topLevel"), "no");
	}

	@Test
	public void testOrganisationTopLevelTrue() {
		rowFromDb.put("top_level", true);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getFirstAtomicValueWithNameInData("topLevel"), "yes");
	}

	@Test
	public void testOrganisationShowInPortalFalse() {
		rowFromDb.put("show_in_portal", false);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getFirstAtomicValueWithNameInData("showInPortal"), "no");
	}

	@Test
	public void testOrganisationShowInPortalTrue() {
		rowFromDb.put("show_in_portal", true);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getFirstAtomicValueWithNameInData("showInPortal"), "yes");
	}

}
