/*
 * Copyright 2018, 2021 Uppsala University Library
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.StorageReadResult;

public class DivaMixedRecordStorageTest {
	private RecordStorageSpy basicStorage;
	private RecordStorageSpy divaFedoraToCoraStorage;
	private DivaMixedRecordStorage divaMixedRecordStorage;
	private RecordStorageSpy divaDbToCoraStorage;
	private RecordStorageSpy userStorage;
	private RecordStorageSpy databaseRecordStorage;
	private DatabaseStorageProviderSpy databaseStorageProvider;
	private ClassicFedoraUpdaterFactorySpy fedoraUpdaterFactory;
	private DivaMixedDependencies mixedDependencies;

	@BeforeMethod
	public void beforeMethod() {
		divaFedoraToCoraStorage = new RecordStorageSpy();
		createMixedDependencies();

		divaMixedRecordStorage = DivaMixedRecordStorage
				.usingDivaMixedDependencies(mixedDependencies);
	}

	private void createMixedDependencies() {
		mixedDependencies = new DivaMixedDependencies();
		basicStorage = new RecordStorageSpy();
		mixedDependencies.setBasicStorage(basicStorage);

		divaDbToCoraStorage = new RecordStorageSpy();
		mixedDependencies.setClassicDbStorage(divaDbToCoraStorage);

		userStorage = new RecordStorageSpy();
		mixedDependencies.setUserStorage(userStorage);

		databaseRecordStorage = new RecordStorageSpy();
		mixedDependencies.setDatabaseStorage(databaseRecordStorage);

	}

	@Test
	public void testInit() throws Exception {
		assertNotNull(divaMixedRecordStorage);
		assertSame(divaMixedRecordStorage.getBasicStorage(), basicStorage);
		assertSame(divaMixedRecordStorage.getClassicDbStorage(), divaDbToCoraStorage);
		assertSame(divaMixedRecordStorage.getDatabaseStorage(), databaseRecordStorage);
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
	public void readPersonGoesToCoraDatabaseStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(databaseRecordStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "person";
		expectedData.id = "someId";
		expectedData.answer = divaMixedRecordStorage.read(expectedData.type, expectedData.id);

		expectedData.calledMethod = "read";
		assertNoInteractionWithStorage(basicStorage);
		assertExpectedDataSameAsInStorageSpy(databaseRecordStorage, expectedData);
	}

	@Test
	public void readPersonDomainPartGoesToCoraDatabaseStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(databaseRecordStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "personDomainPart";
		expectedData.id = "authority-person:2:uu";
		expectedData.answer = divaMixedRecordStorage.read(expectedData.type, expectedData.id);

		expectedData.calledMethod = "read";
		assertNoInteractionWithStorage(basicStorage);
		assertExpectedDataSameAsInStorageSpy(databaseRecordStorage, expectedData);
	}

	@Test
	public void readOrganisationGoesToDivaDBToCoraStorage() throws Exception {
		assertReadGoesToDbStorageForOrganisationType("organisation");
	}

	private void assertReadGoesToDbStorageForOrganisationType(String recordType) {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);

		RecordStorageSpyData data = new RecordStorageSpyData();
		data.type = recordType;
		data.id = "someOrgId";
		data.answer = divaMixedRecordStorage.read(data.type, data.id);

		data.calledMethod = "read";
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		assertExpectedDataSameAsInStorageSpy(divaDbToCoraStorage, data);
	}

	@Test
	public void readRootOrganisationGoesToDivaDBToCoraStorage() throws Exception {
		assertReadGoesToDbStorageForOrganisationType("rootOrganisation");
	}

	@Test
	public void readTopOrganisationGoesToDivaDBToCoraStorage() throws Exception {
		assertReadGoesToDbStorageForOrganisationType("topOrganisation");
	}

	@Test
	public void readSubOrganisationGoesToDivaDBToCoraStorage() throws Exception {
		assertReadGoesToDbStorageForOrganisationType("subOrganisation");
	}

	@Test
	public void readUserGoesToUserStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);

		RecordStorageSpyData data = new RecordStorageSpyData();
		data.type = "user";
		data.id = "someUserId";
		data.answer = divaMixedRecordStorage.read(data.type, data.id);

		RecordStorageSpyData userStorageData = userStorage.data;
		assertEquals(userStorageData.calledMethod, "read");
		assertEquals(userStorageData.type, "user");
		assertEquals(userStorageData.id, "someUserId");
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		assertSame(data.answer, userStorageData.answer);
	}

	@Test
	public void readUserGoesToDivaDBToCoraStorageWithSomeDifferentUser() throws Exception {
		RecordStorageSpyData data = new RecordStorageSpyData();
		data.type = "user";
		data.id = "someDifferentUserId";
		data.answer = divaMixedRecordStorage.read(data.type, data.id);

		RecordStorageSpyData userStorageData = userStorage.data;
		assertEquals(userStorageData.id, "someDifferentUserId");
	}

	@Test
	public void readCoraUserGoesToUserStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);

		RecordStorageSpyData data = new RecordStorageSpyData();
		data.type = "coraUser";
		data.id = "someUserId";
		data.answer = divaMixedRecordStorage.read(data.type, data.id);

		RecordStorageSpyData userStorageData = userStorage.data;
		assertEquals(userStorageData.calledMethod, "read");
		assertEquals(userStorageData.type, "coraUser");
		assertEquals(userStorageData.id, "someUserId");
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		assertSame(data.answer, userStorageData.answer);
	}

	@Test
	public void testUserGoesToBasicStorageWhenNotFoundInUserStorage() throws Exception {
		DivaDbToCoraStorageNotFoundSpy divaDbToCoraStorageSpy = new DivaDbToCoraStorageNotFoundSpy();
		userStorage.existsInStorage = false;
		mixedDependencies.setClassicDbStorage(divaDbToCoraStorageSpy);
		divaMixedRecordStorage = DivaMixedRecordStorage
				.usingDivaMixedDependencies(mixedDependencies);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "user";
		expectedData.id = "coraUser:5368656924943436";
		expectedData.answer = divaMixedRecordStorage.read(expectedData.type, expectedData.id);

		expectedData.calledMethod = "read";
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		RecordStorageSpyData dataFromSpy = userStorage.data;

		assertEquals(dataFromSpy.calledMethod, "read");
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
	public void readPersonListGoesToCoraDatabaseStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(databaseRecordStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "person";
		expectedData.filter = new DataGroupSpy("filter");
		expectedData.answer = divaMixedRecordStorage.readList(expectedData.type,
				expectedData.filter).listOfDataGroups;

		expectedData.calledMethod = "readList";
		assertNoInteractionWithStorage(basicStorage);
		assertExpectedDataSameAsInStorageSpy(databaseRecordStorage, expectedData);
	}

	@Test
	public void readPersonDomainPartListGoesToCoraDatabaseStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(databaseRecordStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "personDomainPart";
		expectedData.filter = new DataGroupSpy("filter");
		expectedData.answer = divaMixedRecordStorage.readList(expectedData.type,
				expectedData.filter).listOfDataGroups;

		expectedData.calledMethod = "readList";
		assertNoInteractionWithStorage(basicStorage);
		assertExpectedDataSameAsInStorageSpy(databaseRecordStorage, expectedData);
	}

	@Test
	public void readOrganisationListGoesToDbToCoraStorage() throws Exception {
		assertReadListGoesToDbForOrganisationType("organisation");
	}

	private void assertReadListGoesToDbForOrganisationType(String recordType) {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);

		RecordStorageSpyData data = new RecordStorageSpyData();
		data.type = recordType;
		data.filter = new DataGroupSpy("filter");
		data.answer = divaMixedRecordStorage.readList(data.type, data.filter).listOfDataGroups;

		data.calledMethod = "readList";
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertExpectedDataSameAsInStorageSpy(divaDbToCoraStorage, data);
	}

	@Test
	public void readRootOrganisationListGoesToDbToCoraStorage() throws Exception {
		assertReadListGoesToDbForOrganisationType("rootOrganisation");
	}

	@Test
	public void readSubrganisationListGoesToDbToCoraStorage() throws Exception {
		assertReadListGoesToDbForOrganisationType("subOrganisation");
	}

	@Test
	public void readUserListGoesToDUserStorageANDToBasicStorage() throws Exception {
		userStorage = new RecordStorageSpy("userStorage");
		mixedDependencies.setUserStorage(userStorage);
		divaMixedRecordStorage = DivaMixedRecordStorage
				.usingDivaMixedDependencies(mixedDependencies);

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

		RecordStorageSpyData userStorageData = userStorage.data;
		assertEquals(userStorageData.calledMethod, "readList");
		assertEquals(userStorageData.type, type);
		assertSame(userStorageData.filter, filter);

		assertNoInteractionWithStorage(divaDbToCoraStorage);

		assertReturnedListContainsResultsFromBothStorage(answer, dataSentToBasicStorage,
				userStorageData);

		assertEquals(answer.totalNumberOfMatches, 3);

	}

	private void assertReturnedListContainsResultsFromBothStorage(StorageReadResult answer,
			RecordStorageSpyData dataSentToBasicStorage,
			RecordStorageSpyData dataSentToUserStorage) {
		Collection<?> listOfDataGroupsReturnedFromBasicstorage = (Collection<?>) dataSentToBasicStorage.answer;
		assertTrue(answer.listOfDataGroups.containsAll(listOfDataGroupsReturnedFromBasicstorage));
		Collection<?> listOfDataGroupsReturnedFromDatabase = (Collection<?>) dataSentToUserStorage.answer;
		assertTrue(answer.listOfDataGroups.containsAll(listOfDataGroupsReturnedFromDatabase));
	}

	@Test
	public void createGoesToBasicStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		RecordStorageSpyData expectedData = createExpectedDataWithDefaultSetting("someType",
				"someId");
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
	public void deletePersonDomainPartGoesToDbStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(databaseRecordStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "personDomainPart";
		expectedData.id = "someId";
		expectedData.calledMethod = "deleteByTypeAndId";
		divaMixedRecordStorage.deleteByTypeAndId(expectedData.type, expectedData.id);

		assertNoInteractionWithStorage(basicStorage);
		assertExpectedDataSameAsInStorageSpy(databaseRecordStorage, expectedData);
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

		String type = "someType";
		String id = "someId";
		RecordStorageSpyData expectedData = createExpectedDataWithDefaultSetting(type, id);
		divaMixedRecordStorage.update(expectedData.type, expectedData.id, expectedData.record,
				expectedData.collectedTerms, expectedData.linkList, expectedData.dataDivider);

		expectedData.calledMethod = "update";
		assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
	}

	@Test
	public void updatePersonGoesToCoraDatabaseStorage() throws Exception {
		assertNoInteractionWithStorageBefore();

		String type = "person";
		String id = "someId";
		RecordStorageSpyData expectedData = createExpectedDataWithDefaultSetting(type, id);
		divaMixedRecordStorage.update(expectedData.type, expectedData.id, expectedData.record,
				expectedData.collectedTerms, expectedData.linkList, expectedData.dataDivider);

		expectedData.calledMethod = "update";
		assertExpectedDataSameAsInStorageSpy(databaseRecordStorage, expectedData);
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);
	}

	@Test
	public void updatePersonDomainPartGoesToCoraDatabaseStorage() throws Exception {
		assertNoInteractionWithStorageBefore();

		String type = "personDomainPart";
		String id = "somePersonId:personDomainPartId";
		RecordStorageSpyData expectedData = createExpectedDataWithDefaultSetting(type, id);

		divaMixedRecordStorage.update(expectedData.type, expectedData.id, expectedData.record,
				expectedData.collectedTerms, expectedData.linkList, expectedData.dataDivider);

		expectedData.calledMethod = "update";
		assertExpectedDataSameAsInStorageSpy(databaseRecordStorage, expectedData);
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);
	}

	private void assertNoInteractionWithStorageBefore() {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(databaseRecordStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);
	}

	private RecordStorageSpyData createExpectedDataWithDefaultSetting(String type, String id) {
		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = type;
		expectedData.id = id;
		expectedData.record = new DataGroupSpy("dummyRecord");
		expectedData.collectedTerms = new DataGroupSpy("collectedTerms");
		expectedData.linkList = new DataGroupSpy("linkList");
		expectedData.dataDivider = "someDataDivider";
		return expectedData;
	}

	@Test
	public void createPersonDomainPartGoesToCoraDatabaseStorage() throws Exception {
		assertNoInteractionWithStorageBefore();

		String type = "personDomainPart";
		String id = "somePersonId:personDomainPartId";
		RecordStorageSpyData expectedData = createExpectedDataWithDefaultSetting(type, id);

		divaMixedRecordStorage.create(expectedData.type, expectedData.id, expectedData.record,
				expectedData.collectedTerms, expectedData.linkList, expectedData.dataDivider);

		expectedData.calledMethod = "create";
		assertExpectedDataSameAsInStorageSpy(databaseRecordStorage, expectedData);
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);
	}

	@Test
	public void updateOrganisationGoesToDbStorage() {
		assertUpdateGoesToDbForOrganisationType("organisation");
	}

	private void assertUpdateGoesToDbForOrganisationType(String type) {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);

		DivaDbToCoraStorageSpy divaDbToCoraStorageSpy = new DivaDbToCoraStorageSpy();
		mixedDependencies.setClassicDbStorage(divaDbToCoraStorageSpy);
		divaMixedRecordStorage = DivaMixedRecordStorage
				.usingDivaMixedDependencies(mixedDependencies);

		RecordStorageSpyData expectedData = createExpectedDataWithDefaultSetting(type, "someId");
		expectedData.calledMethod = "create";

		divaMixedRecordStorage.update(expectedData.type, expectedData.id, expectedData.record,
				expectedData.collectedTerms, expectedData.linkList, expectedData.dataDivider);

		RecordStorageSpyData spyData = divaDbToCoraStorageSpy.data;
		assertCorrectSpyData(expectedData, spyData);

		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertNoInteractionWithStorage(basicStorage);
	}

	@Test
	public void updateRootOrganisationGoesToDbStorage() {
		assertUpdateGoesToDbForOrganisationType("rootOrganisation");
	}

	@Test
	public void updateSubOrganisationGoesToDbStorage() {
		assertUpdateGoesToDbForOrganisationType("subOrganisation");
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
	public void readAbstractListForUserGoesToDivaDBToCoraStorageAndBasicStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "user";
		expectedData.filter = new DataGroupSpy("filter");
		StorageReadResult answer = divaMixedRecordStorage.readAbstractList(expectedData.type,
				expectedData.filter);

		expectedData.calledMethod = "readAbstractList";
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		RecordStorageSpyData divaDbToCoraStorageData = divaDbToCoraStorage.data;
		assertEquals(divaDbToCoraStorageData.calledMethod, expectedData.calledMethod);
		assertEquals(divaDbToCoraStorageData.type, expectedData.type);
		assertSame(divaDbToCoraStorageData.filter, expectedData.filter);

		RecordStorageSpyData dataSentToBasicStorage = basicStorage.data;
		assertEquals(dataSentToBasicStorage.type, expectedData.type);
		assertEquals(dataSentToBasicStorage.calledMethod, expectedData.calledMethod);
		assertSame(dataSentToBasicStorage.filter, expectedData.filter);

		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		assertReturnedListContainsResultsFromBothStorage(answer, dataSentToBasicStorage,
				divaDbToCoraStorageData);

		assertEquals(answer.totalNumberOfMatches, 2);
	}

	@Test
	public void readAbstractListForOrganisationGoesToDivaDBToCoraStorage() throws Exception {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = "organisation";
		expectedData.filter = new DataGroupSpy("filter");
		StorageReadResult answer = divaMixedRecordStorage.readAbstractList(expectedData.type,
				expectedData.filter);

		expectedData.calledMethod = "readAbstractList";
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		RecordStorageSpyData divaDbToCoraStorageData = divaDbToCoraStorage.data;
		assertEquals(divaDbToCoraStorageData.calledMethod, expectedData.calledMethod);
		assertEquals(divaDbToCoraStorageData.type, expectedData.type);
		assertSame(divaDbToCoraStorageData.filter, expectedData.filter);

		assertEquals(answer.listOfDataGroups, divaDbToCoraStorageData.answer);

		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertNoInteractionWithStorage(basicStorage);

		assertEquals(answer.totalNumberOfMatches, 1);
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
		assertRecordExistsGoesToDbForOrganisationType("organisation");
	}

	private void assertRecordExistsGoesToDbForOrganisationType(String recordType) {
		DivaDbToCoraStorageSpy divaDbToCoraStorageSpy = new DivaDbToCoraStorageSpy();

		mixedDependencies.setClassicDbStorage(divaDbToCoraStorageSpy);
		divaMixedRecordStorage = DivaMixedRecordStorage
				.usingDivaMixedDependencies(mixedDependencies);

		RecordStorageSpyData expectedData = new RecordStorageSpyData();
		expectedData.type = recordType;
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
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForRootOrgansiationGoesToDbStorage()
			throws Exception {
		assertRecordExistsGoesToDbForOrganisationType("rootOrganisation");
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForTopOrgansiationGoesToDbStorage()
			throws Exception {
		assertRecordExistsGoesToDbForOrganisationType("topOrganisation");
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForSubOrgansiationGoesToDbStorage()
			throws Exception {
		assertRecordExistsGoesToDbForOrganisationType("subOrganisation");
	}

	@Test
	public void testRecordExistsForAbstractOrImplementingRecordTypeAndRecordId() {
		databaseRecordStorage.linkExistsInStorage = true;

		String type = "personDomainPart";
		String id = "someId";
		String calledMethod = "recordExistsForAbstractOrImplementingRecordTypeAndRecordId";
		boolean recordExists = divaMixedRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(type, id);

		assertTrue(recordExists);
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);

		RecordStorageSpyData data = databaseRecordStorage.data;
		assertEquals(data.type, type);
		assertEquals(data.id, id);
		assertEquals(data.calledMethod, calledMethod);
	}

	// @Test
	// public void
	// recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForUserGoesFirstToUserStorage()
	// throws Exception {
	// userStorage.linkExistsInStorage = true;
	//
	// String type = "user";
	// String id = "userId";
	// boolean recordExists = divaMixedRecordStorage
	// .recordExistsForAbstractOrImplementingRecordTypeAndRecordId(type, id);
	// assertTrue(recordExists);
	//
	// RecordStorageSpyData data = userStorage.data;
	//
	// assertEquals(data.calledMethod,
	// "recordExistsForAbstractOrImplementingRecordTypeAndRecordId");
	// assertEquals(data.type, type);
	// assertEquals(data.id, id);
	//
	// assertSame(data.answer, recordExists);
	// }

	// @Test
	// public void
	// recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForCoraUserGoesFirstToUserStorage()
	// throws Exception {
	// userStorage.linkExistsInStorage = true;
	//
	// String type = "coraUser";
	// String id = "userId";
	// boolean recordExists = divaMixedRecordStorage
	// .recordExistsForAbstractOrImplementingRecordTypeAndRecordId(type, id);
	// assertTrue(recordExists);
	//
	// RecordStorageSpyData data = userStorage.data;
	//
	// assertEquals(data.calledMethod,
	// "recordExistsForAbstractOrImplementingRecordTypeAndRecordId");
	// assertEquals(data.type, type);
	// assertEquals(data.id, id);
	//
	// assertSame(data.answer, recordExists);
	// }

	// @Test
	// public void recordDoesNotExistForAbstractOrImplementingRecordTypeAndRecordIdForUser() {
	//
	// boolean recordExists = divaMixedRecordStorage
	// .recordExistsForAbstractOrImplementingRecordTypeAndRecordId("user", "id");
	// assertFalse(recordExists);
	// }

	// @Test
	// public void
	// recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForCoraUserAlsoGoesToBasicStorage()
	// throws Exception {
	// userStorage.existsInStorage = false;
	// basicStorage.linkExistsInStorage = true;
	//
	// RecordStorageSpyData expectedData = new RecordStorageSpyData();
	// expectedData.type = "coraUser";
	// expectedData.id = "someUserId";
	// expectedData.calledMethod = "recordExistsForAbstractOrImplementingRecordTypeAndRecordId";
	// expectedData.answer = true;
	// boolean recordExists = divaMixedRecordStorage
	// .recordExistsForAbstractOrImplementingRecordTypeAndRecordId(expectedData.type,
	// expectedData.id);
	//
	// assertTrue(recordExists);
	// assertExpectedDataSameAsInStorageSpy(basicStorage, expectedData);
	// assertNoInteractionWithStorage(divaFedoraToCoraStorage);
	//
	// assertEquals(userStorage.data.type, expectedData.type);
	// assertEquals(userStorage.data.id, expectedData.id);
	// assertEquals(userStorage.data.calledMethod, expectedData.calledMethod);
	//
	// }

	@Test
	public void testGetSearchTermGoesToBasicStorage() throws Exception {
		DataGroup searchTerm = divaMixedRecordStorage.getSearchTerm("someSearchTermId");
		assertEquals(basicStorage.searchTermId, "someSearchTermId");
		assertSame(searchTerm, basicStorage.returnedSearchTerm);
	}

	@Test
	public void testGetCollectIndexTermGoesToBasicStorage() throws Exception {
		DataGroup searchTerm = divaMixedRecordStorage.getCollectIndexTerm("someIndexTermId");
		assertEquals(basicStorage.indexTermId, "someIndexTermId");
		assertSame(searchTerm, basicStorage.returnedIndexTerm);
	}

	@Test
	public void testGetTotalNumberOfRecordsForTypeSubOrganisation() {
		String type = "subOrganisation";
		testGetTotalNumberOfRecordsForType(type);
	}

	private void testGetTotalNumberOfRecordsForType(String type) {
		DataGroup filter = new DataGroupSpy("filter");
		long totalNumberOfRecords = divaMixedRecordStorage.getTotalNumberOfRecordsForType(type,
				filter);
		RecordStorageSpyData data = divaDbToCoraStorage.data;
		assertEquals(data.type, type);
		assertSame(data.filter, filter);
		assertEquals(totalNumberOfRecords, data.answer);

		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertNoInteractionWithStorage(basicStorage);
	}

	@Test
	public void testGetTotalNumberOfRecordsForTypeTopOrganisation() {
		testGetTotalNumberOfRecordsForType("topOrganisation");
	}

	@Test
	public void testGetTotalNumberOfRecordsForTypeRootOrganisation() {
		testGetTotalNumberOfRecordsForType("rootOrganisation");
	}

	@Test
	public void testGetTotalNumberOfRecordsForPerson() {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(databaseRecordStorage);

		DataGroup filter = new DataGroupSpy("filter");
		long totalNumberOfRecords = divaMixedRecordStorage.getTotalNumberOfRecordsForType("person",
				filter);
		RecordStorageSpyData data = databaseRecordStorage.data;
		assertEquals(data.type, "person");
		assertSame(data.filter, filter);
		assertEquals(totalNumberOfRecords, data.answer);

		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertNoInteractionWithStorage(basicStorage);
	}

	@Test
	public void testGetTotalNumberOfRecordsForPersonDomainPart() {
		assertNoInteractionWithStorage(basicStorage);
		assertNoInteractionWithStorage(databaseRecordStorage);

		DataGroup filter = new DataGroupSpy("filter");
		long totalNumberOfRecords = divaMixedRecordStorage
				.getTotalNumberOfRecordsForType("personDomainPart", filter);
		RecordStorageSpyData data = databaseRecordStorage.data;
		assertEquals(data.type, "personDomainPart");
		assertSame(data.filter, filter);
		assertEquals(totalNumberOfRecords, data.answer);

		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertNoInteractionWithStorage(basicStorage);
	}

	@Test
	public void testGetTotalNumberOfRecordsForOtherType() {
		DataGroup filter = new DataGroupSpy("filter");
		long totalNumberOfRecords = divaMixedRecordStorage
				.getTotalNumberOfRecordsForType("otherType", filter);
		RecordStorageSpyData data = basicStorage.data;
		assertEquals(data.type, "otherType");
		assertSame(data.filter, filter);
		assertEquals(totalNumberOfRecords, data.answer);

		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);
	}

	@Test
	public void testGetTotalNumberOfRecordsForAbstractTypeOrganisation() {
		DataGroup filter = new DataGroupSpy("filter");
		List<String> implementingTypes = createImplementingTypes();

		long totalNumberOfRecords = divaMixedRecordStorage
				.getTotalNumberOfRecordsForAbstractType("organisation", implementingTypes, filter);
		assertDataSentToDbStorage(filter, implementingTypes, totalNumberOfRecords);
	}

	private void assertDataSentToDbStorage(DataGroup filter, List<String> implementingTypes,
			long totalNumberOfRecords) {
		RecordStorageSpyData data = divaDbToCoraStorage.data;
		assertEquals(data.type, "organisation");
		assertSame(data.filter, filter);
		assertSame(divaDbToCoraStorage.implementingTypes, implementingTypes);

		assertEquals(totalNumberOfRecords, data.answer);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertNoInteractionWithStorage(basicStorage);
	}

	@Test
	public void testGetTotalNumberOfRecordsForAbstractTypeOther() {
		DataGroup filter = new DataGroupSpy("filter");
		List<String> implementingTypes = createImplementingTypes();

		long totalNumberOfRecords = divaMixedRecordStorage
				.getTotalNumberOfRecordsForAbstractType("otherType", implementingTypes, filter);
		assertDataSentToBasicStorage(filter, implementingTypes, totalNumberOfRecords);
	}

	private void assertDataSentToBasicStorage(DataGroup filter, List<String> implementingTypes,
			long totalNumberOfRecords) {
		RecordStorageSpyData data = basicStorage.data;
		assertEquals(data.type, "otherType");
		assertSame(data.filter, filter);
		assertSame(basicStorage.implementingTypes, implementingTypes);

		assertEquals(totalNumberOfRecords, data.answer);
		assertNoInteractionWithStorage(divaFedoraToCoraStorage);
		assertNoInteractionWithStorage(divaDbToCoraStorage);
	}

	private List<String> createImplementingTypes() {
		List<String> implementingTypes = new ArrayList<>();
		implementingTypes.add("topOrganisation");
		implementingTypes.add("subOrganisation");
		return implementingTypes;
	}

}
