/*
 * Copyright 2021 Uppsala University Library
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
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;

public class DivaMixedRecordStorage2Test {
	private RecordStorageSpy basicStorage;
	private RecordStorageSpy divaFedoraToCoraStorage;
	private DivaMixedRecordStorage divaMixedRecordStorage;
	private RecordStorageSpy divaDbToCoraStorage;
	private RecordStorageSpy userStorage;
	private RecordStorageSpy databaseRecordStorage;
	private DatabaseStorageProviderSpy databaseStorageProvider;

	private String id = "someId";
	private DataGroupSpy dataGroup = new DataGroupSpy("dummyRecord");
	private DataGroupSpy collectedTerms = new DataGroupSpy("collectedTerms");
	private DataGroupSpy linkList = new DataGroupSpy("linkList");
	private String dataDivider = "someDataDivider";

	@BeforeMethod
	public void beforeMethod() {
		basicStorage = new RecordStorageSpy();
		divaFedoraToCoraStorage = new RecordStorageSpy();
		divaDbToCoraStorage = new RecordStorageSpy();
		userStorage = new RecordStorageSpy();
		databaseRecordStorage = new RecordStorageSpy();
		divaMixedRecordStorage = (DivaMixedRecordStorage) DivaMixedRecordStorage
				.usingBasicStorageClassicDbStorageUserStorageAndDatabaseStorage(basicStorage,
						divaDbToCoraStorage, userStorage, databaseRecordStorage);
	}

	@Test
	public void readGoesToDbStorageForIndexBatchJob() throws Exception {
		assertNoCallsMadeToStorage();
		String recordType = "indexBatchJob";
		DataGroup readIndexBatchJob = divaMixedRecordStorage.read(recordType, id);

		assertNull(basicStorage.data.calledMethod);
		assertEquals(databaseRecordStorage.data.calledMethod, "read");
		assertEquals(databaseRecordStorage.data.type, recordType);
		assertEquals(databaseRecordStorage.data.id, id);
		assertSame(readIndexBatchJob, databaseRecordStorage.data.answer);

	}

	@Test
	public void createGoesToDbStorageForIndexBatchJob() throws Exception {
		assertNoCallsMadeToStorage();
		String recordType = "indexBatchJob";
		divaMixedRecordStorage.create(recordType, id, dataGroup, collectedTerms, linkList,
				dataDivider);

		assertNull(basicStorage.data.calledMethod);
		assertCorrectDataSentToDbStorageDefaultSettings("create", recordType);

	}

	private void assertNoCallsMadeToStorage() {
		assertNull(basicStorage.data.calledMethod);
		assertNull(databaseRecordStorage.data.calledMethod);
	}

	private void assertCorrectDataSentToDbStorageDefaultSettings(String calledMethod,
			String recordType) {
		assertEquals(databaseRecordStorage.data.calledMethod, calledMethod);
		assertEquals(databaseRecordStorage.data.type, recordType);
		assertEquals(databaseRecordStorage.data.id, id);
		assertEquals(databaseRecordStorage.data.record, dataGroup);
		assertEquals(databaseRecordStorage.data.collectedTerms, collectedTerms);
		assertEquals(databaseRecordStorage.data.linkList, linkList);
		assertEquals(databaseRecordStorage.data.dataDivider, dataDivider);
	}

	@Test
	public void updateGoesToDbStorageForIndexBatchJob() throws Exception {
		assertNoCallsMadeToStorage();

		String recordType = "indexBatchJob";
		divaMixedRecordStorage.update(recordType, id, dataGroup, collectedTerms, linkList,
				dataDivider);

		assertNull(basicStorage.data.calledMethod);
		assertCorrectDataSentToDbStorageDefaultSettings("update", recordType);

	}

	// @Test
	// public void deletePersonDomainPartGoesToDbStorage() throws Exception {
	// assertNoInteractionWithStorage(basicStorage);
	// assertNoInteractionWithStorage(databaseRecordStorage);
	//
	// RecordStorageSpyData expectedData = new RecordStorageSpyData();
	// expectedData.type = "personDomainPart";
	// expectedData.id = "someId";
	// expectedData.calledMethod = "deleteByTypeAndId";
	// divaMixedRecordStorage.deleteByTypeAndId(expectedData.type, expectedData.id);
	//
	// assertNoInteractionWithStorage(basicStorage);
	// assertExpectedDataSameAsInStorageSpy(databaseRecordStorage, expectedData);
	// }
}
