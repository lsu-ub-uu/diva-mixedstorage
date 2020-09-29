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
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.StorageReadResult;

public class DivaMixedRecordStorageTest {
	private RecordStorageSpy basicStorage;
	private RecordStorageSpy divaFedoraToCoraStorage;
	private RecordStorage divaMixedRecordStorage;
	private RecordStorageSpy divaDbToCoraStorage;
	private DivaStorageFactorySpy storageFactory;

	@BeforeMethod
	public void beforeMethod() {
		basicStorage = new RecordStorageSpy();
		divaFedoraToCoraStorage = new RecordStorageSpy();
		divaDbToCoraStorage = new RecordStorageSpy();
		storageFactory = new DivaStorageFactorySpy();
		divaMixedRecordStorage = DivaMixedRecordStorage.usingBasicAndFedoraAndDbStorage(
				basicStorage, divaFedoraToCoraStorage, divaDbToCoraStorage, storageFactory);
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
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		expectedData.answer = divaMixedRecordStorage.read(expectedData.type, expectedData.id);

		expectedData.calledMethod = "read";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
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
	public void readPersonGoesToDivaFedoraToCoraStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "person";
		expectedData.id = "someId";
		expectedData.answer = divaMixedRecordStorage.read(expectedData.type, expectedData.id);

		expectedData.calledMethod = "read";
		assertNoInteractionWithStorage(basicStorage);
		assertExpectedDataSameAsInStorageSpy(divaFedoraToCoraStorage, expectedData);
	}

	@Test
	public void readOrganisationGoesToDivaDBToCoraStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);

		RecordStorageSpyData data = new RecordStorageSpyData();
		data.type = "divaOrganisation";
		data.id = "someOrgId";
		data.answer = divaMixedRecordStorage.read(data.type, data.id);

		data.calledMethod = "read";
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		assertExpectedDataSameAsInStorageSpy(divaDbToCoraStorage, data);
	}

	@Test
	public void readUserGoesToDivaDBToCoraStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);

		RecordStorageSpyData data = new RecordStorageSpyData();
		data.type = "user";
		data.id = "someUserId";
		data.answer = divaMixedRecordStorage.read(data.type, data.id);

		assertEquals(storageFactory.type, "user");
		RecordStorageSpy factored = (RecordStorageSpy) storageFactory.factored;
		RecordStorageSpyData dataFromFactored = factored.data;
		assertEquals(dataFromFactored.calledMethod, "read");
		assertEquals(dataFromFactored.type, "user");
		assertEquals(dataFromFactored.id, "someUserId");
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		assertSame(data.answer, dataFromFactored.answer);
	}

	@Test
	public void readUserGoesToDivaDBToCoraStorageWithSomeDifferentUser() throws Exception {
		RecordStorageSpyData data = new RecordStorageSpyData();
		data.type = "user";
		data.id = "someDifferentUserId";
		data.answer = divaMixedRecordStorage.read(data.type, data.id);

		RecordStorageSpy factored = (RecordStorageSpy) storageFactory.factored;
		RecordStorageSpyData dataFromFactored = factored.data;
		assertEquals(dataFromFactored.id, "someDifferentUserId");
	}

	@Test
	public void readCoraUserGoesToDivaDBToCoraStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);

		RecordStorageSpyData data = new RecordStorageSpyData();
		data.type = "coraUser";
		data.id = "someUserId";
		data.answer = divaMixedRecordStorage.read(data.type, data.id);

		assertEquals(storageFactory.type, "coraUser");
		RecordStorageSpy factored = (RecordStorageSpy) storageFactory.factored;
		RecordStorageSpyData dataFromFactored = factored.data;
		assertEquals(dataFromFactored.calledMethod, "read");
		assertEquals(dataFromFactored.type, "coraUser");
		assertEquals(dataFromFactored.id, "someUserId");
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		assertSame(data.answer, dataFromFactored.answer);
	}

	@Test
	public void readUserGUESTGoesToBasicStorage() throws Exception {
		storageFactory.factorNotFound = true;
		DivaDbToCoraStorageNotFoundSpy divaDbToCoraStorageSpy = new DivaDbToCoraStorageNotFoundSpy();
		divaMixedRecordStorage = DivaMixedRecordStorage.usingBasicAndFedoraAndDbStorage(
				basicStorage, divaFedoraToCoraStorage, divaDbToCoraStorageSpy, storageFactory);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "user";
		expectedData.id = "coraUser:5368656924943436";
		expectedData.answer = divaMixedRecordStorage.read(expectedData.type, expectedData.id);

		expectedData.calledMethod = "read";
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertTrue(storageFactory.readWasCalled);
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
	}

	@Test
	public void readListGoesToBasicStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.filter = new DataGroupSpy("filter");
		expectedData.answer = divaMixedRecordStorage.readList(expectedData.type,
				expectedData.filter).listOfDataGroups;

		expectedData.calledMethod = "readList";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
	}

	@Test
	public void readPersonListGoesToFedoraToCoraStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "person";
		expectedData.filter = new DataGroupSpy("filter");
		expectedData.answer = divaMixedRecordStorage.readList(expectedData.type,
				expectedData.filter).listOfDataGroups;

		expectedData.calledMethod = "readList";
		assertNoInteractionWithStorage(basicStorage);
		assertExpectedDataSameAsInStorageSpy(divaFedoraToCoraStorage, expectedData);
	}

	@Test
	public void readOrganisationListGoesToDbToCoraStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);

		RecordStorageSpyData data = new RecordStorageSpyData();
		data.type = "divaOrganisation";
		data.filter = new DataGroupSpy("filter");
		data.answer = divaMixedRecordStorage.readList(data.type, data.filter).listOfDataGroups;

		data.calledMethod = "readList";
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertExpectedDataSameAsInStorageSpy(divaDbToCoraStorage, data);
	}

	// TODO: fortsätt här
	@Test
	public void readUserListGoesToDbToCoraStorageANDToBasicStorage() throws Exception {
		divaDbToCoraStorage = new RecordStorageSpy("db");
		divaMixedRecordStorage = DivaMixedRecordStorage.usingBasicAndFedoraAndDbStorage(
				basicStorage, divaFedoraToCoraStorage, divaDbToCoraStorage, storageFactory);

		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);

		String type = "coraUser";
		DataGroupSpy filter = new DataGroupSpy("filter");
		StorageReadResult answer = divaMixedRecordStorage.readList(type, filter);

		RecordStorageSpyData dataSentToBasicStorage = basicStorage.data;
		assertEquals(dataSentToBasicStorage.type, type);
		assertEquals(dataSentToBasicStorage.calledMethod, "readList");
		assertSame(dataSentToBasicStorage.filter, filter);

		RecordStorageSpyData dataSentToDbStorage = divaDbToCoraStorage.data;
		assertEquals(dataSentToDbStorage.type, type);
		assertEquals(dataSentToDbStorage.calledMethod, "readList");
		assertSame(dataSentToDbStorage.filter, filter);

		assertReturnedListContainsResultsFromBothStorage(answer, dataSentToBasicStorage,
				dataSentToDbStorage);

		assertEquals(answer.totalNumberOfMatches, 3);

	}

	private void assertReturnedListContainsResultsFromBothStorage(StorageReadResult answer,
			RecordStorageSpyData dataSentToBasicStorage, RecordStorageSpyData dataSentToDbStorage) {
		Collection<?> listOfDataGroupsReturnedFromBasicstorage = (Collection<?>) dataSentToBasicStorage.answer;
		assertTrue(answer.listOfDataGroups.containsAll(listOfDataGroupsReturnedFromBasicstorage));
		Collection<?> listOfDataGroupsReturnedFromDatabase = (Collection<?>) dataSentToDbStorage.answer;
		assertTrue(answer.listOfDataGroups.containsAll(listOfDataGroupsReturnedFromDatabase));
	}

	@Test
	public void createGoesToBasicStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		expectedData.record = new DataGroupSpy("dummyRecord");
		expectedData.collectedTerms = new DataGroupSpy("collectedTerms");
		expectedData.linkList = new DataGroupSpy("linkList");
		expectedData.dataDivider = "someDataDivider";
		divaMixedRecordStorage.create(expectedData.type, expectedData.id, expectedData.record,
				expectedData.collectedTerms, expectedData.linkList, expectedData.dataDivider);

		expectedData.calledMethod = "create";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);
	}

	@Test
	public void deleteByTypeAndIdGoesToBasicStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		divaMixedRecordStorage.deleteByTypeAndId(expectedData.type, expectedData.id);

		expectedData.calledMethod = "deleteByTypeAndId";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
	}

	@Test
	public void linksExistForRecordGoesToBasicStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		expectedData.answer = divaMixedRecordStorage.linksExistForRecord(expectedData.type,
				expectedData.id);

		expectedData.calledMethod = "linksExistForRecord";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
	}

	@Test
	public void updateGoesToBasicStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		expectedData.record = new DataGroupSpy("dummyRecord");
		expectedData.collectedTerms = new DataGroupSpy("collectedTerms");
		expectedData.linkList = new DataGroupSpy("linkList");
		expectedData.dataDivider = "someDataDivider";
		divaMixedRecordStorage.update(expectedData.type, expectedData.id, expectedData.record,
				expectedData.collectedTerms, expectedData.linkList, expectedData.dataDivider);

		expectedData.calledMethod = "update";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
	}

	@Test
	public void updatePersonGoesToFedoraStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "person";
		expectedData.id = "someId";
		expectedData.record = new DataGroupSpy("dummyRecord");
		expectedData.collectedTerms = new DataGroupSpy("collectedTerms");
		expectedData.linkList = new DataGroupSpy("linkList");
		expectedData.dataDivider = "someDataDivider";
		divaMixedRecordStorage.update(expectedData.type, expectedData.id, expectedData.record,
				expectedData.collectedTerms, expectedData.linkList, expectedData.dataDivider);

		expectedData.calledMethod = "update";
		assertExpectedDataSameAsInStorageSpy(divaFedoraToCoraStorage, expectedData);
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);
	}

	@Test
	public void updateOrganisationGoesToDbStorage() {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);

		DivaDbToCoraStorageSpy divaDbToCoraStorageSpy = new DivaDbToCoraStorageSpy();
		divaMixedRecordStorage = DivaMixedRecordStorage.usingBasicAndFedoraAndDbStorage(
				basicStorage, divaFedoraToCoraStorage, divaDbToCoraStorageSpy, null);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "divaOrganisation";
		expectedData.id = "someId";
		expectedData.record = new DataGroupSpy("dummyRecord");
		expectedData.collectedTerms = new DataGroupSpy("collectedTerms");
		expectedData.linkList = new DataGroupSpy("linkList");
		expectedData.dataDivider = "someDataDivider";
		expectedData.calledMethod = "create";

		divaMixedRecordStorage.update(expectedData.type, expectedData.id, expectedData.record,
				expectedData.collectedTerms, expectedData.linkList, expectedData.dataDivider);

		RecordStorageSpyData spyData = divaDbToCoraStorageSpy.data;
		assertCorrectSpyData(expectedData, spyData);

		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertNoInteractionWithStorage(basicStorage);
	}

	@Test
	public void readAbstractListGoesToBasicStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.filter = new DataGroupSpy("filter");
		expectedData.answer = divaMixedRecordStorage.readAbstractList(expectedData.type,
				expectedData.filter).listOfDataGroups;

		expectedData.calledMethod = "readAbstractList";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
	}

	@Test
	public void readAbstractListForUserGoesToDivaDBToCoraStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "user";
		expectedData.filter = new DataGroupSpy("filter");
		expectedData.answer = divaMixedRecordStorage.readAbstractList(expectedData.type,
				expectedData.filter).listOfDataGroups;

		expectedData.calledMethod = "readAbstractList";
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertExpectedDataSameAsInStorageSpy(divaDbToCoraStorage, expectedData);
	}

	@Test
	public void readLinkListGoesToBasicStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		expectedData.answer = divaMixedRecordStorage.readLinkList(expectedData.type,
				expectedData.id);

		expectedData.calledMethod = "readLinkList";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
	}

	@Test
	public void generateLinkCollectionPointingToRecordGoesToBasicStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		expectedData.answer = divaMixedRecordStorage
				.generateLinkCollectionPointingToRecord(expectedData.type, expectedData.id);

		expectedData.calledMethod = "generateLinkCollectionPointingToRecord";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
	}

	@Test
	public void recordsExistForRecordTypeGoesToBasicStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.answer = divaMixedRecordStorage.recordsExistForRecordType(expectedData.type);

		expectedData.calledMethod = "recordsExistForRecordType";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdGoesToBasicStorage()
			throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "someType";
		expectedData.id = "someId";
		expectedData.answer = divaMixedRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(expectedData.type,
						expectedData.id);

		expectedData.calledMethod = "recordExistsForAbstractOrImplementingRecordTypeAndRecordId";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForOrgansiationGoesToDbStorage()
			throws Exception {
		DivaDbToCoraStorageSpy divaDbToCoraStorageSpy = new DivaDbToCoraStorageSpy();
		divaMixedRecordStorage = DivaMixedRecordStorage.usingBasicAndFedoraAndDbStorage(
				basicStorage, divaFedoraToCoraStorage, divaDbToCoraStorageSpy, null);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "divaOrganisation";
		expectedData.id = "someId";
		expectedData.calledMethod = "recordExistsForAbstractOrImplementingRecordTypeAndRecordId";
		boolean recordExists = divaMixedRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(expectedData.type,
						expectedData.id);

		assertTrue(recordExists);
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		RecordStorageSpyData spyData = divaDbToCoraStorageSpy.data;
		assertCorrectSpyData(expectedData, spyData);
	}

	private void assertCorrectSpyData(RecordStorageSpyData expectedData,
			RecordStorageSpyData spyData) {
		assertEquals(spyData.type, expectedData.type);
		assertEquals(spyData.id, expectedData.id);
		assertEquals(spyData.calledMethod, expectedData.calledMethod);
	}

	@Test
	public void testGetSearchTermGoesToBasicStorage() throws Exception {
		DataGroup searchTerm = ((DivaMixedRecordStorage) divaMixedRecordStorage)
				.getSearchTerm("someSearchTermId");
		assertEquals(basicStorage.searchTermId, "someSearchTermId");
		assertSame(searchTerm, basicStorage.returnedSearchTerm);
	}

	@Test
	public void testGetCollectIndexTermGoesToBasicStorage() throws Exception {
		DataGroup searchTerm = ((DivaMixedRecordStorage) divaMixedRecordStorage)
				.getCollectIndexTerm("someIndexTermId");
		assertEquals(basicStorage.indexTermId, "someIndexTermId");
		assertSame(searchTerm, basicStorage.returnedIndexTerm);
	}

}
