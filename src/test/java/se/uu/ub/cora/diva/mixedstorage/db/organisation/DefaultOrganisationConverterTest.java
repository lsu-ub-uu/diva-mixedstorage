/*
 * Copyright 2018, 2019, 2020, 2021 Uppsala University Library
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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.data.DataRecordLink;
import se.uu.ub.cora.data.DataRecordLinkProvider;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.db.ConversionException;

public class DefaultOrganisationConverterTest {

	private DefaultConverter converter;
	private RowSpy rowFromDb;
	private DataGroupFactorySpy dataGroupFactorySpy;
	private DataAtomicFactorySpy dataAtomicFactorySpy;
	private DataRecordLinkFactorySpy dataRecordLinkFactorySpy;

	@BeforeMethod
	public void beforeMethod() {
		dataGroupFactorySpy = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactorySpy);
		dataAtomicFactorySpy = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactorySpy);
		dataRecordLinkFactorySpy = new DataRecordLinkFactorySpy();
		DataRecordLinkProvider.setDataRecordLinkFactory(dataRecordLinkFactorySpy);
		rowFromDb = new RowSpy();
		rowFromDb.addColumnWithValue("id", 57);
		rowFromDb.addColumnWithValue("type_code", "root");
		rowFromDb.addColumnWithValue("not_eligible", true);
		rowFromDb.addColumnWithValue("domain", "someDomain");
		converter = new DefaultOrganisationConverter();
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation to Cora organisation: Map does not contain value for id")
	public void testEmptyMap() {
		rowFromDb = new RowSpy();
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertNull(organisation);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation to Cora organisation: Map does not contain value for id")
	public void testMapWithEmptyValueThrowsError() {
		rowFromDb = new RowSpy();
		rowFromDb.addColumnWithValue("id", "");
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation to Cora organisation: Map does not contain value for id")
	public void testMapWithNonEmptyValueANDEmptyValueThrowsError() {
		rowFromDb = new RowSpy();
		rowFromDb.addColumnWithValue("defaultname", "someName");
		rowFromDb.addColumnWithValue("id", "");
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation to Cora organisation: Map does not contain value for id")
	public void mapDoesNotContainOrganisationIdValue() {
		rowFromDb = new RowSpy();
		rowFromDb.addColumnWithValue("defaultname", "someName");
		converter.fromMap(rowFromDb);
	}

	@Test
	public void testMinimalValuesReturnsDataGroupWithCorrectRecordInfo() {
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getNameInData(), "organisation");
		assertCorrectRecordInfoWithIdAndRecordType(organisation, "57", "rootOrganisation");

		DataGroupSpy factoredOrganisation = dataGroupFactorySpy.factoredDataGroups.get(0);
		assertEquals(factoredOrganisation.nameInData, "organisation");

		DataGroupSpy factoredRecordInfo = dataGroupFactorySpy.factoredDataGroups.get(1);
		assertEquals(factoredRecordInfo.nameInData, "recordInfo");
		assertSame(factoredRecordInfo, organisation.getFirstChildWithNameInData("recordInfo"));

		DataAtomicSpy factoredDataAtomicForId = getFactoredDataAtomicByNumber(0);
		assertEquals(factoredDataAtomicForId.nameInData, "id");
		assertEquals(factoredDataAtomicForId.value, "57");

		DataAtomicSpy selectable = getFactoredDataAtomicByNumber(4);
		assertEquals(selectable.nameInData, "selectable");
		assertEquals(selectable.value, "no");

	}

	private void assertCorrectRecordInfoWithIdAndRecordType(DataGroup organisation, String id,
			String recordType) {
		DataGroup recordInfo = organisation.getFirstGroupWithNameInData("recordInfo");
		assertEquals(recordInfo.getFirstAtomicValueWithNameInData("id"), id);

		DataRecordLink type = (DataRecordLink) recordInfo.getFirstChildWithNameInData("type");
		assertEquals(type.getLinkedRecordType(), "recordType");
		assertEquals(type.getLinkedRecordId(), recordType);

		DataRecordLink dataDivider = (DataRecordLink) recordInfo
				.getFirstChildWithNameInData("dataDivider");
		assertEquals(dataDivider.getLinkedRecordType(), "system");
		assertEquals(dataDivider.getLinkedRecordId(), "diva");

		assertTrue(recordInfo.containsChildWithNameInData("selectable"));
		assertEquals(recordInfo.getFirstAtomicValueWithNameInData("domain"), "someDomain");

		assertCorrectCreatedAndUpdatedInfo(recordInfo);
	}

	private DataAtomicSpy getFactoredDataAtomicByNumber(int noFactored) {
		return dataAtomicFactorySpy.factoredDataAtomics.get(noFactored);
	}

	@Test
	public void testMinimalValuesReturnsDataGroupWithCorrectRecordInfoWithSelectableTrue() {
		rowFromDb.addColumnWithValue("not_eligible", false);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getNameInData(), "organisation");
		assertCorrectRecordInfoWithIdAndRecordType(organisation, "57", "rootOrganisation");

		DataAtomicSpy selectable = getFactoredDataAtomicByNumber(4);
		assertEquals(selectable.nameInData, "selectable");
		assertEquals(selectable.value, "yes");

	}

	@Test
	public void testCorrectRecordTypeWhenOrganisationTypeIsRoot() {
		rowFromDb.addColumnWithValue("type_code", "root");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getNameInData(), "organisation");
		assertCorrectRecordInfoWithIdAndRecordType(organisation, "57", "rootOrganisation");

	}

	@Test
	public void testCorrectRecordTypeWhenOrganisationTypeIsTopLevel() {
		rowFromDb.addColumnWithValue("type_code", "university");
		rowFromDb.addColumnWithValue("top_level", true);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getNameInData(), "organisation");
		assertCorrectRecordInfoWithIdAndRecordType(organisation, "57", "topOrganisation");

	}

	@Test
	public void testCorrectRecordTypeWhenOrganisationTypeIsSubLevel() {
		rowFromDb.addColumnWithValue("type_code", "unit");
		rowFromDb.addColumnWithValue("top_level", false);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getNameInData(), "organisation");
		assertCorrectRecordInfoWithIdAndRecordType(organisation, "57", "subOrganisation");

	}

	@Test
	public void testOrganisationName() {
		rowFromDb.addColumnWithValue("defaultname", "Java-fakulteten");
		rowFromDb.addColumnWithValue("organisation_name_locale", "sv");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getNameInData(), "organisation");

		assertCorrectValuesForNameWasFactored();

		DataGroup nameGroup = organisation.getFirstGroupWithNameInData("organisationName");
		assertEquals(nameGroup.getFirstAtomicValueWithNameInData("name"), "Java-fakulteten");
		assertEquals(nameGroup.getFirstAtomicValueWithNameInData("language"), "sv");
	}

	private void assertCorrectValuesForNameWasFactored() {
		DataAtomicSpy factoredDataAtomicForName = getFactoredDataAtomicByNumber(5);
		assertEquals(factoredDataAtomicForName.nameInData, "name");
		assertEquals(factoredDataAtomicForName.value, "Java-fakulteten");
		DataAtomicSpy factoredDataAtomicForLanguage = getFactoredDataAtomicByNumber(6);
		assertEquals(factoredDataAtomicForLanguage.nameInData, "language");
		assertEquals(factoredDataAtomicForLanguage.value, "sv");
	}

	@Test
	public void testAlternativeName() {
		rowFromDb.addColumnWithValue("alternative_name", "Java Faculty");
		DataGroup organisation = converter.fromMap(rowFromDb);
		DataGroup alternativeName = organisation
				.getFirstGroupWithNameInData("organisationAlternativeName");
		assertEquals(alternativeName.getFirstAtomicValueWithNameInData("language"), "en");
		assertEquals(alternativeName.getFirstAtomicValueWithNameInData("name"), "Java Faculty");
	}

	private void assertCorrectCreatedAndUpdatedInfo(DataGroup recordInfo) {
		assertEquals(recordInfo.getFirstAtomicValueWithNameInData("tsCreated"),
				"2017-01-01T00:00:00.000000Z");
		DataRecordLink createdBy = (DataRecordLink) recordInfo
				.getFirstGroupWithNameInData("createdBy");
		assertEquals(createdBy.getLinkedRecordType(), "coraUser");
		assertEquals(createdBy.getLinkedRecordId(), "coraUser:4412982402853626");

		assertEquals(recordInfo.getAllGroupsWithNameInData("updated").size(), 1);
		DataGroup updated = recordInfo.getFirstGroupWithNameInData("updated");
		assertEquals(updated.getFirstAtomicValueWithNameInData("tsUpdated"),
				"2017-01-01T00:00:00.000000Z");
		assertEquals(updated.getRepeatId(), "0");

		DataRecordLink updatedBy = (DataRecordLink) updated
				.getFirstGroupWithNameInData("updatedBy");
		assertEquals(updatedBy.getLinkedRecordType(), "coraUser");
		assertEquals(updatedBy.getLinkedRecordId(), "coraUser:4412982402853626");

	}

	@Test
	public void testOrganisationClosedDateMissing() {
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("closedDate"));
	}

	@Test
	public void testOrganisationClosedDateIsnull() {
		rowFromDb.addColumnWithValue("closed_date", null);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("closedDate"));
	}

	@Test
	public void testOrganisationClosedDateIsEmpty() {
		rowFromDb.addColumnWithValue("closed_date", "");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("closedDate"));
	}

	@Test
	public void testOrganisationClosedDate() {
		Date date = Date.valueOf("2018-12-31");
		rowFromDb.addColumnWithValue("closed_date", date);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getFirstAtomicValueWithNameInData("closedDate"), "2018-12-31");
	}

}
