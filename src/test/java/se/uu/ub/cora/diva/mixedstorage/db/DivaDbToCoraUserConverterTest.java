/*
 * Copyright 2020 Uppsala University Library
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
package se.uu.ub.cora.diva.mixedstorage.db;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

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

public class DivaDbToCoraUserConverterTest {

	private DivaDbToCoraUserConverter converter;
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
		rowFromDb.put("db_id", 678);
		converter = new DivaDbToCoraUserConverter();
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting user to Cora user: Map does not contain value for id")
	public void testEmptyMap() {
		rowFromDb = new HashMap<>();
		DataGroup user = converter.fromMap(rowFromDb);
		assertNull(user);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting user to Cora user: Map does not contain value for id")
	public void testMapWithEmptyValueThrowsError() {
		rowFromDb = new HashMap<>();
		rowFromDb.put("db_id", "");
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting user to Cora user: Map does not contain value for id")
	public void testMapWithNonEmptyValueANDEmptyValueThrowsError() {
		Map<String, Object> rowFromDb = new HashMap<>();
		rowFromDb.put("first_name", "someName");
		rowFromDb.put("db_id", "");
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting user to Cora user: Map does not contain value for id")
	public void mapDoesNotContainUserIdValue() {
		rowFromDb = new HashMap<>();
		rowFromDb.put("first_name", "someName");
		converter.fromMap(rowFromDb);
	}

	@Test
	public void testReturnsDataGroupWithCorrectRecordInfo() {
		DataGroup user = converter.fromMap(rowFromDb);
		assertEquals(user.getNameInData(), "user");
		assertEquals(user.getAttribute("type").getValue(), "coraUser");

		DataGroupSpy firstFactoredGroup = dataGroupFactorySpy.factoredDataGroups.get(0);
		assertSame(user, firstFactoredGroup);

		assertEquals(dataGroupFactorySpy.usedNameInDatas.get(0), "user");
		assertEquals(dataGroupFactorySpy.usedNameInDatas.get(1), "recordInfo");
		assertEquals(dataGroupFactorySpy.usedNameInDatas.get(2), "type");
		assertEquals(dataGroupFactorySpy.usedNameInDatas.get(3), "dataDivider");
		assertEquals(dataGroupFactorySpy.usedNameInDatas.get(4), "createdBy");
		assertEquals(dataGroupFactorySpy.usedNameInDatas.get(5), "updated");
		assertEquals(dataGroupFactorySpy.usedNameInDatas.get(6), "updatedBy");

		DataGroup recordInfo = user.getFirstGroupWithNameInData("recordInfo");
		assertSame(recordInfo, dataGroupFactorySpy.factoredDataGroups.get(1));

		assertCorrectType(recordInfo);

		assertCorrectDataDivider(recordInfo);

		assertSame(recordInfo.getFirstDataAtomicWithNameInData("id"),
				dataAtomicFactorySpy.factoredDataAtomics.get(0));
		assertEquals(dataAtomicFactorySpy.factoredDataAtomics.get(0).value,
				String.valueOf(rowFromDb.get("db_id")));

		assertCorrectCreatedInfo(recordInfo);
		assertCorrectUpdatedInfo(recordInfo);
	}

	private void assertCorrectType(DataGroup recordInfo) {
		DataGroupSpy typeGroup = (DataGroupSpy) recordInfo.getFirstGroupWithNameInData("type");
		assertSame(typeGroup, dataGroupFactorySpy.factoredDataGroups.get(2));
		assertEquals(typeGroup.recordType, "recordType");
		assertEquals(typeGroup.recordId, "coraUser");
	}

	private void assertCorrectDataDivider(DataGroup recordInfo) {
		DataGroupSpy dataDividerGroup = (DataGroupSpy) recordInfo
				.getFirstGroupWithNameInData("dataDivider");
		assertSame(dataDividerGroup, dataGroupFactorySpy.factoredDataGroups.get(3));
		assertEquals(dataDividerGroup.recordType, "system");
		assertEquals(dataDividerGroup.recordId, "diva");
	}

	private void assertCorrectCreatedInfo(DataGroup recordInfo) {
		DataGroupSpy createdBy = (DataGroupSpy) recordInfo.getFirstGroupWithNameInData("createdBy");
		assertSame(createdBy, dataGroupFactorySpy.factoredDataGroups.get(4));
		assertEquals(createdBy.recordType, "coraUser");
		assertEquals(createdBy.recordId, "coraUser:4412982402853626");

		assertEquals(recordInfo.getFirstAtomicValueWithNameInData("tsCreated"),
				"2017-01-01T00:00:00.000000Z");
	}

	private void assertCorrectUpdatedInfo(DataGroup recordInfo) {
		DataGroupSpy updatedGroup = (DataGroupSpy) recordInfo
				.getFirstGroupWithNameInData("updated");
		assertSame(updatedGroup, dataGroupFactorySpy.factoredDataGroups.get(5));
		assertEquals(updatedGroup.getRepeatId(), "0");
		assertEquals(updatedGroup.getFirstAtomicValueWithNameInData("tsUpdated"),
				"2017-01-01T00:00:00.000000Z");

		DataGroupSpy updatedBy = (DataGroupSpy) updatedGroup
				.getFirstGroupWithNameInData("updatedBy");
		assertSame(updatedBy, dataGroupFactorySpy.factoredDataGroups.get(6));
		assertEquals(updatedBy.recordType, "coraUser");
		assertEquals(updatedBy.recordId, "coraUser:4412982402853626");
	}

	private DataAtomicSpy getFactoredDataAtomicByNumber(int noFactored) {
		return dataAtomicFactorySpy.factoredDataAtomics.get(noFactored);
	}

	@Test
	public void testReturnsDataGroupWithActiveUser() {
		DataGroup user = converter.fromMap(rowFromDb);
		assertEquals(user.getNameInData(), "user");
		assertEquals(user.getAttribute("type").getValue(), "coraUser");
		assertEquals(user.getFirstAtomicValueWithNameInData("activeStatus"), "active");
	}

	@Test
	public void testMinimalContentDoesNotContainNonMandatoryValues() {
		DataGroup user = converter.fromMap(rowFromDb);
		assertEquals(user.getNameInData(), "user");
		assertEquals(user.getAttribute("type").getValue(), "coraUser");
		assertFalse(user.containsChildWithNameInData("userFirstname"));
		assertFalse(user.containsChildWithNameInData("userLastname"));

	}

	// TODO: domain är inte med i coraUser, hur hantera detta? divaUser?
	// @Test
	// public void testUserDomain() throws Exception {
	// rowFromDb.put("domain", "uu");
	//
	// DataGroup user = converter.fromMap(rowFromDb);
	//
	// DataAtomicSpy factoredDataAtomicForId = getFactoredDataAtomicByNumber(11);
	// assertEquals(factoredDataAtomicForId.nameInData, "domain");
	// assertEquals(factoredDataAtomicForId.value, "uu");
	//
	// assertEquals(user.getFirstAtomicValueWithNameInData("domain"), "uu");
	// }

	@Test
	public void testUserName() {
		rowFromDb.put("first_name", "Kalle");
		rowFromDb.put("last_name", "Kula");
		DataGroup user = converter.fromMap(rowFromDb);
		assertEquals(user.getNameInData(), "user");

		// assertCorrectValuesForNameWasFactored();
		DataAtomicSpy factoredDataAtomicForName = getFactoredDataAtomicByNumber(4);
		assertEquals(factoredDataAtomicForName.nameInData, "userFirstname");
		DataAtomicSpy factoredDataAtomicForLastName = getFactoredDataAtomicByNumber(5);
		assertEquals(factoredDataAtomicForLastName.nameInData, "userLastname");

		assertSame(user.getFirstDataAtomicWithNameInData("userFirstname"),
				getFactoredDataAtomicByNumber(4));
		assertEquals(user.getFirstDataAtomicWithNameInData("userLastname"),
				getFactoredDataAtomicByNumber(5));
	}

	// private void assertCorrectValuesForNameWasFactored() {
	// DataAtomicSpy factoredDataAtomicForName = getFactoredDataAtomicByNumber(12);
	// assertEquals(factoredDataAtomicForName.nameInData, "userName");
	// assertEquals(factoredDataAtomicForName.value, "Java-fakulteten");
	// DataAtomicSpy factoredDataAtomicForLanguage = getFactoredDataAtomicByNumber(13);
	// assertEquals(factoredDataAtomicForLanguage.nameInData, "language");
	// assertEquals(factoredDataAtomicForLanguage.value, "sv");
	// }
	//
	// @Test
	// public void testTypeCode() {
	// String typeCode = "university";
	// rowFromDb.put("type_code", typeCode);
	//
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertEquals(user.getFirstAtomicValueWithNameInData("userType"), typeCode);
	// }
	//
	// @Test
	// public void testAlternativeName() {
	// rowFromDb.put("alternative_name", "Java Faculty");
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertTrue(user.containsChildWithNameInData("alternativeName"));
	// DataGroup alternativeName = user.getFirstGroupWithNameInData("alternativeName");
	// assertEquals(alternativeName.getFirstAtomicValueWithNameInData("language"), "en");
	// assertEquals(alternativeName.getFirstAtomicValueWithNameInData("userName"),
	// "Java Faculty");
	// }
	//
	// @Test
	// public void testOrganisationNotEligible() {
	// rowFromDb.put("not_eligible", true);
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertEquals(user.getFirstAtomicValueWithNameInData("eligible"), "no");
	// }
	//
	// @Test
	// public void testOrganisationEligible() {
	// rowFromDb.put("not_eligible", false);
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertEquals(user.getFirstAtomicValueWithNameInData("eligible"), "yes");
	// }
	//
	private void assertCorrectRecordInfoWithId(DataGroup user, String id) {
		DataGroup recordInfo = user.getFirstGroupWithNameInData("recordInfo");
		assertEquals(recordInfo.getFirstAtomicValueWithNameInData("id"), id);

		// DataGroup type = recordInfo.getFirstGroupWithNameInData("type");
		// assertEquals(type.getFirstAtomicValueWithNameInData("linkedRecordType"), "recordType");
		// assertEquals(type.getFirstAtomicValueWithNameInData("linkedRecordId"),
		// "divaOrganisation");
		//
		// DataGroup dataDivider = recordInfo.getFirstGroupWithNameInData("dataDivider");
		// assertEquals(dataDivider.getFirstAtomicValueWithNameInData("linkedRecordType"),
		// "system");
		// assertEquals(dataDivider.getFirstAtomicValueWithNameInData("linkedRecordId"), "diva");

		// assertCorrectCreatedAndUpdatedInfo(recordInfo);
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

	// @Test
	// public void testAdressMissing() {
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertFalse(user.containsChildWithNameInData("city"));
	// assertFalse(user.containsChildWithNameInData("street"));
	// assertFalse(user.containsChildWithNameInData("box"));
	// assertFalse(user.containsChildWithNameInData("postcode"));
	// assertFalse(user.containsChildWithNameInData("country"));
	//
	// }
	//
	// @Test
	// public void testAdress() {
	// rowFromDb.put("city", "uppsala");
	// rowFromDb.put("street", "Övre slottsgatan 1");
	// rowFromDb.put("postbox", "Box5435");
	// rowFromDb.put("postnumber", "345 34");
	// rowFromDb.put("country_code", "fi");
	//
	// DataGroup user = converter.fromMap(rowFromDb);
	// DataAtomicSpy factoredDataAtomicForCity = getFactoredDataAtomicByNumber(18);
	// assertEquals(factoredDataAtomicForCity.nameInData, "city");
	// assertEquals(factoredDataAtomicForCity.value, "uppsala");
	//
	// DataAtomicSpy factoredDataAtomicForStreet = getFactoredDataAtomicByNumber(19);
	// assertEquals(factoredDataAtomicForStreet.nameInData, "street");
	// assertEquals(factoredDataAtomicForStreet.value, "Övre slottsgatan 1");
	//
	// DataAtomicSpy factoredDataAtomicForBox = getFactoredDataAtomicByNumber(20);
	// assertEquals(factoredDataAtomicForBox.nameInData, "box");
	// assertEquals(factoredDataAtomicForBox.value, "Box5435");
	//
	// DataAtomicSpy factoredDataAtomicForPostcode = getFactoredDataAtomicByNumber(21);
	// assertEquals(factoredDataAtomicForPostcode.nameInData, "postcode");
	// assertEquals(factoredDataAtomicForPostcode.value, "345 34");
	//
	// DataAtomicSpy factoredDataAtomicForCountry = getFactoredDataAtomicByNumber(22);
	// assertEquals(factoredDataAtomicForCountry.nameInData, "country");
	// assertEquals(factoredDataAtomicForCountry.value, "FI");
	//
	// assertEquals(user.getFirstAtomicValueWithNameInData("city"), "uppsala");
	// assertEquals(user.getFirstAtomicValueWithNameInData("street"),
	// "Övre slottsgatan 1");
	// assertEquals(user.getFirstAtomicValueWithNameInData("box"), "Box5435");
	// assertEquals(user.getFirstAtomicValueWithNameInData("postcode"), "345 34");
	// assertEquals(user.getFirstAtomicValueWithNameInData("country"), "FI");
	//
	// }
	//
	// @Test
	// public void testOrganisationNumberMissing() {
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertFalse(user.containsChildWithNameInData("userNumber"));
	// }
	//
	// @Test
	// public void testOrganisationNumberIsnull() {
	// rowFromDb.put("orgnumber", null);
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertFalse(user.containsChildWithNameInData("userNumber"));
	// }
	//
	// @Test
	// public void testOrganisationNumberIsEmpty() {
	// rowFromDb.put("orgnumber", "");
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertFalse(user.containsChildWithNameInData("userNumber"));
	// }
	//
	// @Test
	// public void testOrganisationNumber() {
	// rowFromDb.put("orgnumber", "540002");
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertEquals(user.getFirstAtomicValueWithNameInData("userNumber"),
	// "540002");
	// }
	//
	// @Test
	// public void testOrganisationCodeMissing() {
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertFalse(user.containsChildWithNameInData("userCode"));
	// }
	//
	// @Test
	// public void testOrganisationCodeIsNull() {
	// rowFromDb.put("user_code", null);
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertFalse(user.containsChildWithNameInData("userCode"));
	// }
	//
	// @Test
	// public void testOrganisationCodeIsEmpty() {
	// rowFromDb.put("user_code", "");
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertFalse(user.containsChildWithNameInData("userCode"));
	// }
	//
	// @Test
	// public void testOrganisationCode() {
	// rowFromDb.put("user_code", "56783545");
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertEquals(user.getFirstAtomicValueWithNameInData("userCode"),
	// "56783545");
	// }
	//
	// @Test
	// public void testOrganisationUrlMissing() {
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertFalse(user.containsChildWithNameInData("URL"));
	// }
	//
	// @Test
	// public void testOrganisationUrlIsNull() {
	// rowFromDb.put("user_homepage", null);
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertFalse(user.containsChildWithNameInData("URL"));
	// }
	//
	// @Test
	// public void testOrganisationUrlIsEmpty() {
	// rowFromDb.put("user_homepage", "");
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertFalse(user.containsChildWithNameInData("URL"));
	// }
	//
	// @Test
	// public void testOrganisationURL() {
	// rowFromDb.put("user_homepage", "www.something.org");
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertEquals(user.getFirstAtomicValueWithNameInData("URL"), "www.something.org");
	// }
	//
	// @Test
	// public void testOrganisationClosedDateMissing() {
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertFalse(user.containsChildWithNameInData("closedDate"));
	// }
	//
	// @Test
	// public void testOrganisationClosedDateIsnull() {
	// rowFromDb.put("closed_date", null);
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertFalse(user.containsChildWithNameInData("closedDate"));
	// }
	//
	// @Test
	// public void testOrganisationClosedDateIsEmpty() {
	// rowFromDb.put("closed_date", "");
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertFalse(user.containsChildWithNameInData("closedDate"));
	// }
	//
	// @Test
	// public void testOrganisationClosedDate() {
	// Date date = Date.valueOf("2018-12-31");
	// rowFromDb.put("closed_date", date);
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertEquals(user.getFirstAtomicValueWithNameInData("closedDate"), "2018-12-31");
	// }
	//
	// @Test
	// public void testOrganisationLibrisIdMissing() {
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertFalse(user.containsChildWithNameInData("librisId"));
	// }
	//
	// @Test
	// public void testOrganisationLibrisIdIsNull() {
	// rowFromDb.put("libris_code", null);
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertFalse(user.containsChildWithNameInData("librisId"));
	// }
	//
	// @Test
	// public void testOrganisationLibrisIdIsEmpty() {
	// rowFromDb.put("libris_code", "");
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertFalse(user.containsChildWithNameInData("librisId"));
	// }
	//
	// @Test
	// public void testOrganisationLibrisId() {
	// rowFromDb.put("libris_code", "uuLibrisCode");
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertEquals(user.getFirstAtomicValueWithNameInData("librisId"), "uuLibrisCode");
	// }
	//
	// @Test
	// public void testOrganisationShowInDefenceFalse() {
	// rowFromDb.put("show_in_defence", false);
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertEquals(user.getFirstAtomicValueWithNameInData("showInDefence"), "no");
	// }
	//
	// @Test
	// public void testOrganisationShowInDefenceTrue() {
	// rowFromDb.put("show_in_defence", true);
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertEquals(user.getFirstAtomicValueWithNameInData("showInDefence"), "yes");
	// }
	//
	// @Test
	// public void testOrganisationTopLevelFalse() {
	// rowFromDb.put("top_level", false);
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertEquals(user.getFirstAtomicValueWithNameInData("topLevel"), "no");
	// }
	//
	// @Test
	// public void testOrganisationTopLevelTrue() {
	// rowFromDb.put("top_level", true);
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertEquals(user.getFirstAtomicValueWithNameInData("topLevel"), "yes");
	// }
	//
	// @Test
	// public void testOrganisationShowInPortalFalse() {
	// rowFromDb.put("show_in_portal", false);
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertEquals(user.getFirstAtomicValueWithNameInData("showInPortal"), "no");
	// }
	//
	// @Test
	// public void testOrganisationShowInPortalTrue() {
	// rowFromDb.put("show_in_portal", true);
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertEquals(user.getFirstAtomicValueWithNameInData("showInPortal"), "yes");
	// }
	//
	// @Test
	// public void testOrganisationNotRoot() {
	// rowFromDb.put("type_code", "unit");
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertEquals(user.getFirstAtomicValueWithNameInData("rootOrganisation"), "no");
	// }
	//
	// @Test
	// public void testOrganisationRoot() {
	// rowFromDb.put("type_code", "root");
	// DataGroup user = converter.fromMap(rowFromDb);
	// assertFalse(user.containsChildWithNameInData("userType"));
	// assertEquals(user.getFirstAtomicValueWithNameInData("rootOrganisation"), "yes");
	// }
}
