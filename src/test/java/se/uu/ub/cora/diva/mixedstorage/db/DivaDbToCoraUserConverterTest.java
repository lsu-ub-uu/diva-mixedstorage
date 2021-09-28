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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.RowSpy;

public class DivaDbToCoraUserConverterTest {

	private DivaDbToCoraUserConverter converter;
	private RowSpy rowFromDb;
	private DataGroupFactorySpy dataGroupFactorySpy;
	private DataAtomicFactorySpy dataAtomicFactorySpy;

	@BeforeMethod
	public void beforeMethod() {
		dataGroupFactorySpy = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactorySpy);
		dataAtomicFactorySpy = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactorySpy);
		// rowFromDb = new HashMap<>();
		rowFromDb = new RowSpy();
		rowFromDb.addColumnWithValue("db_id", 678);
		converter = new DivaDbToCoraUserConverter();
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting user to Cora user: Map does not contain value for id")
	public void testEmptyMap() {
		rowFromDb = new RowSpy();
		DataGroup user = converter.fromMap(rowFromDb);
		assertNull(user);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting user to Cora user: Map does not contain value for id")
	public void testMapWithEmptyValueThrowsError() {
		rowFromDb = new RowSpy();
		rowFromDb.addColumnWithValue("db_id", "");
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting user to Cora user: Map does not contain value for id")
	public void testMapWithNonEmptyValueANDEmptyValueThrowsError() {
		rowFromDb = new RowSpy();
		rowFromDb.addColumnWithValue("first_name", "someName");
		rowFromDb.addColumnWithValue("db_id", "");
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting user to Cora user: Map does not contain value for id")
	public void mapDoesNotContainUserIdValue() {
		rowFromDb = new RowSpy();
		rowFromDb.addColumnWithValue("first_name", "someName");
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
				String.valueOf(rowFromDb.getValueByColumn("db_id")));

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

	@Test
	public void testUserName() {
		rowFromDb.addColumnWithValue("first_name", "Kalle");
		rowFromDb.addColumnWithValue("last_name", "Kula");
		DataGroup user = converter.fromMap(rowFromDb);
		assertEquals(user.getNameInData(), "user");

		DataAtomicSpy factoredDataAtomicForName = getFactoredDataAtomicByNumber(4);
		assertEquals(factoredDataAtomicForName.nameInData, "userFirstname");
		DataAtomicSpy factoredDataAtomicForLastName = getFactoredDataAtomicByNumber(5);
		assertEquals(factoredDataAtomicForLastName.nameInData, "userLastname");

		assertSame(user.getFirstDataAtomicWithNameInData("userFirstname"),
				getFactoredDataAtomicByNumber(4));
		assertEquals(user.getFirstDataAtomicWithNameInData("userLastname"),
				getFactoredDataAtomicByNumber(5));
	}
}
