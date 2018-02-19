/*
 * Copyright 2018 Uppsala University Library
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
package se.uu.ub.cora.diva.mixedstorage;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.spider.record.storage.RecordStorage;

public class DivaMixedRecordStorageTest {
	private RecordStorageSpy basicStorage;
	private RecordStorageSpy divaToCoraStorage;
	private RecordStorage divaMixedRecordStorage;

	@BeforeMethod
	public void beforeMethod() {
		basicStorage = new RecordStorageSpy();
		divaToCoraStorage = new RecordStorageSpy();
		divaMixedRecordStorage = DivaMixedRecordStorage.usingBasicAndDivaToCoraStorage(basicStorage,
				divaToCoraStorage);
	}

	@Test
	public void testInit() throws Exception {
		assertNotNull(divaMixedRecordStorage);
	}

	@Test
	public void divaMixedStorageImplementsRecordStorage() throws Exception {
		assertTrue(divaMixedRecordStorage instanceof RecordStorage);
	}

	@Test
	public void readGoesToBasicStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		expectedData.answer = divaMixedRecordStorage.read(expectedData.type, expectedData.id);

		expectedData.calledMethod = "read";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(divaToCoraStorage);
	}

	private void assertNoInteractionWithStorage(RecordStorageSpy recordStorageSpy) {
		assertNull(recordStorageSpy.data.type);
		assertNull(recordStorageSpy.data.id);
		assertNull(recordStorageSpy.data.calledMethod);
	}

	private void assertExpectedDataSameAsInStorageSpy(RecordStorageSpy recordStorageSpy,
			RecordStorageSpyData data) {
		RecordStorageSpyData spyData = recordStorageSpy.data;
		assertEquals(spyData.type, data.type);
		assertEquals(spyData.id, data.id);
		assertEquals(spyData.calledMethod, data.calledMethod);
		assertEquals(spyData.filter, data.filter);
		assertEquals(spyData.record, data.record);
		assertEquals(spyData.collectedTerms, data.collectedTerms);
		assertEquals(spyData.linkList, data.linkList);
		assertEquals(spyData.dataDivider, data.dataDivider);

		assertEquals(spyData.answer, data.answer);
	}

	@Test
	public void readPersonGoesToDivaToCoraStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "divaPerson";
		expectedData.id = "someId";
		expectedData.answer = divaMixedRecordStorage.read(expectedData.type, expectedData.id);

		expectedData.calledMethod = "read";
		assertNoInteractionWithStorage(basicStorage);
		assertExpectedDataSameAsInStorageSpy(divaToCoraStorage, expectedData);
	}

	@Test
	public void readListGoesToBasicStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.filter = DataGroup.withNameInData("filter");
		expectedData.answer = divaMixedRecordStorage.readList(expectedData.type,
				expectedData.filter);

		expectedData.calledMethod = "readList";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(divaToCoraStorage);
	}

	@Test
	public void readPersonListGoesToDivaToCoraStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "divaPerson";
		expectedData.filter = DataGroup.withNameInData("filter");
		expectedData.answer = divaMixedRecordStorage.readList(expectedData.type,
				expectedData.filter);

		expectedData.calledMethod = "readList";
		assertNoInteractionWithStorage(basicStorage);
		assertExpectedDataSameAsInStorageSpy(divaToCoraStorage, expectedData);
	}

	@Test
	public void createGoesToBasicStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		expectedData.record = DataGroup.withNameInData("dummyRecord");
		expectedData.collectedTerms = DataGroup.withNameInData("collectedTerms");
		expectedData.linkList = DataGroup.withNameInData("linkList");
		expectedData.dataDivider = "someDataDivider";
		divaMixedRecordStorage.create(expectedData.type, expectedData.id, expectedData.record,
				expectedData.collectedTerms, expectedData.linkList, expectedData.dataDivider);

		expectedData.calledMethod = "create";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(divaToCoraStorage);
	}

	@Test
	public void deleteByTypeAndIdGoesToBasicStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		divaMixedRecordStorage.deleteByTypeAndId(expectedData.type, expectedData.id);

		expectedData.calledMethod = "deleteByTypeAndId";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(divaToCoraStorage);
	}

	@Test
	public void linksExistForRecordGoesToBasicStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		expectedData.answer = divaMixedRecordStorage.linksExistForRecord(expectedData.type,
				expectedData.id);

		expectedData.calledMethod = "linksExistForRecord";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(divaToCoraStorage);
	}

	@Test
	public void updateGoesToBasicStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		expectedData.record = DataGroup.withNameInData("dummyRecord");
		expectedData.collectedTerms = DataGroup.withNameInData("collectedTerms");
		expectedData.linkList = DataGroup.withNameInData("linkList");
		expectedData.dataDivider = "someDataDivider";
		divaMixedRecordStorage.update(expectedData.type, expectedData.id, expectedData.record,
				expectedData.collectedTerms, expectedData.linkList, expectedData.dataDivider);

		expectedData.calledMethod = "update";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(divaToCoraStorage);
	}

	@Test
	public void readAbstractListGoesToBasicStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.filter = DataGroup.withNameInData("filter");
		expectedData.answer = divaMixedRecordStorage.readAbstractList(expectedData.type,
				expectedData.filter);

		expectedData.calledMethod = "readAbstractList";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(divaToCoraStorage);
	}

	@Test
	public void readLinkListGoesToBasicStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		expectedData.answer = divaMixedRecordStorage.readLinkList(expectedData.type,
				expectedData.id);

		expectedData.calledMethod = "readLinkList";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(divaToCoraStorage);
	}

	@Test
	public void generateLinkCollectionPointingToRecordGoesToBasicStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		expectedData.answer = divaMixedRecordStorage
				.generateLinkCollectionPointingToRecord(expectedData.type, expectedData.id);

		expectedData.calledMethod = "generateLinkCollectionPointingToRecord";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(divaToCoraStorage);
	}

	@Test
	public void recordsExistForRecordTypeGoesToBasicStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.answer = divaMixedRecordStorage.recordsExistForRecordType(expectedData.type);

		expectedData.calledMethod = "recordsExistForRecordType";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(divaToCoraStorage);
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdGoesToBasicStorage()
			throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		expectedData.answer = divaMixedRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(expectedData.type,
						expectedData.id);

		expectedData.calledMethod = "recordExistsForAbstractOrImplementingRecordTypeAndRecordId";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(divaToCoraStorage);
	}
}
