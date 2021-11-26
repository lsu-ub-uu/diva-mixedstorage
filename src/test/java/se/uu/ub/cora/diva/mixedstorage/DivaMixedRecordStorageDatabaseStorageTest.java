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
import se.uu.ub.cora.storage.StorageReadResult;

public class DivaMixedRecordStorageDatabaseStorageTest {
	private static final String INDEX_BATCH_JOB = "indexBatchJob";
	private RecordStorageSpy basicStorage;
	private DivaMixedRecordStorage divaMixedRecordStorage;
	private RecordStorageSpy divaDbToCoraStorage;
	private RecordStorageSpy userStorage;
	private RecordStorageSpy databaseRecordStorage;

	private String id = "someId";
	private DataGroupSpy dataGroup = new DataGroupSpy("dummyRecord");
	private DataGroupSpy collectedTerms = new DataGroupSpy("collectedTerms");
	private DataGroupSpy linkList = new DataGroupSpy("linkList");
	private DataGroupSpy filter = new DataGroupSpy("filter");
	private String dataDivider = "someDataDivider";

	@BeforeMethod
	public void beforeMethod() {
		basicStorage = new RecordStorageSpy();
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
		DataGroup readIndexBatchJob = divaMixedRecordStorage.read(INDEX_BATCH_JOB, id);

		assertNull(basicStorage.data.calledMethod);
		assertCorrectTypeIdAndMethod(INDEX_BATCH_JOB, "read");
		assertSame(readIndexBatchJob, databaseRecordStorage.data.answer);

	}

	@Test
	public void readListGoesToDbStorageForIndexBatchJob() throws Exception {
		assertNoCallsMadeToStorage();
		StorageReadResult readList = divaMixedRecordStorage.readList(INDEX_BATCH_JOB, filter);

		assertNull(basicStorage.data.calledMethod);
		assertEquals(databaseRecordStorage.data.calledMethod, "readList");
		assertEquals(databaseRecordStorage.data.type, INDEX_BATCH_JOB);
		assertEquals(databaseRecordStorage.data.filter, filter);
		assertSame(readList, databaseRecordStorage.storageReadResult);

	}

	@Test
	public void createGoesToDbStorageForIndexBatchJob() throws Exception {
		assertNoCallsMadeToStorage();
		divaMixedRecordStorage.create(INDEX_BATCH_JOB, id, dataGroup, collectedTerms, linkList,
				dataDivider);

		assertNull(basicStorage.data.calledMethod);
		assertCorrectDataSentToDbStorageDefaultSettings("create", INDEX_BATCH_JOB);

	}

	private void assertNoCallsMadeToStorage() {
		assertNull(basicStorage.data.calledMethod);
		assertNull(databaseRecordStorage.data.calledMethod);
	}

	private void assertCorrectDataSentToDbStorageDefaultSettings(String calledMethod,
			String recordType) {
		assertCorrectTypeIdAndMethod(recordType, calledMethod);
		assertEquals(databaseRecordStorage.data.record, dataGroup);
		assertEquals(databaseRecordStorage.data.collectedTerms, collectedTerms);
		assertEquals(databaseRecordStorage.data.linkList, linkList);
		assertEquals(databaseRecordStorage.data.dataDivider, dataDivider);
	}

	@Test
	public void updateGoesToDbStorageForIndexBatchJob() throws Exception {
		assertNoCallsMadeToStorage();

		divaMixedRecordStorage.update(INDEX_BATCH_JOB, id, dataGroup, collectedTerms, linkList,
				dataDivider);

		assertNull(basicStorage.data.calledMethod);
		assertCorrectDataSentToDbStorageDefaultSettings("update", INDEX_BATCH_JOB);

	}

	@Test
	public void deletePersonDomainPartGoesToDbStorage() throws Exception {
		assertNoCallsMadeToStorage();

		divaMixedRecordStorage.deleteByTypeAndId(INDEX_BATCH_JOB, id);

		assertNull(basicStorage.data.calledMethod);
		assertCorrectTypeIdAndMethod(INDEX_BATCH_JOB, "deleteByTypeAndId");
	}

	private void assertCorrectTypeIdAndMethod(String recordType, String expectedMethod) {
		assertEquals(databaseRecordStorage.data.calledMethod, expectedMethod);
		assertEquals(databaseRecordStorage.data.type, recordType);
		assertEquals(databaseRecordStorage.data.id, id);
	}

	@Test
	public void testGetTotalNumberOfRecordsForPerson() {
		assertNoCallsMadeToStorage();

		long totalNumberOfRecords = divaMixedRecordStorage
				.getTotalNumberOfRecordsForType(INDEX_BATCH_JOB, filter);

		assertNull(basicStorage.data.calledMethod);
		assertEquals(databaseRecordStorage.data.type, INDEX_BATCH_JOB);
		assertSame(databaseRecordStorage.data.filter, filter);
		assertEquals(totalNumberOfRecords, databaseRecordStorage.data.answer);

	}

}
