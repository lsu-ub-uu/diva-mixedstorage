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
package se.uu.ub.cora.diva.mixedstorage.db;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.StorageReadResult;

public class DivaDbRecordStorageTest {
	private static final String ORGANISATION_TYPE = "organisation";
	private DivaDbRecordStorage divaRecordStorage;
	private DivaDbToCoraConverterFactorySpy converterFactorySpy;
	private RecordReaderFactorySpy recordReaderFactorySpy;
	private DivaDbFactorySpy divaDbFactorySpy;
	private DivaDbUpdaterFactorySpy divaDbUpdaterFactorySpy;

	@BeforeMethod
	public void BeforeMethod() {
		converterFactorySpy = new DivaDbToCoraConverterFactorySpy();
		recordReaderFactorySpy = new RecordReaderFactorySpy();
		divaDbFactorySpy = new DivaDbFactorySpy();
		divaDbUpdaterFactorySpy = new DivaDbUpdaterFactorySpy();
		divaRecordStorage = DivaDbRecordStorage
				.usingRecordReaderFactoryDivaFactoryAndDivaDbUpdaterFactory(recordReaderFactorySpy,
						divaDbFactorySpy, divaDbUpdaterFactorySpy, converterFactorySpy);
	}

	@Test
	public void testInit() throws Exception {
		assertNotNull(divaRecordStorage);
	}

	@Test
	public void divaToCoraRecordStorageImplementsRecordStorage() throws Exception {
		assertTrue(divaRecordStorage instanceof RecordStorage);
	}

	@Test
	public void testCallToDivaDbToCoraFactory() throws Exception {
		divaRecordStorage.read(ORGANISATION_TYPE, "someId");
		assertTrue(divaDbFactorySpy.factorWasCalled);
		assertEquals(divaDbFactorySpy.type, "organisation");
	}

	@Test
	public void testReadOrganisationMakeCorrectCalls() throws Exception {
		divaRecordStorage.read(ORGANISATION_TYPE, "someId");
		DivaDbSpy factored = divaDbFactorySpy.factored;
		assertEquals(factored.type, ORGANISATION_TYPE);
		assertEquals(factored.id, "someId");
	}

	@Test
	public void testReturnedDataGroupIsSameAsReturnedFromFactoredForRead() throws Exception {
		DataGroup readOrganisation = divaRecordStorage.read(ORGANISATION_TYPE, "someId");
		DivaDbSpy factored = divaDbFactorySpy.factored;
		assertEquals(readOrganisation, factored.dataGroup);
	}

	@Test
	public void testReadUserMakeCorrectCalls() throws Exception {
		divaRecordStorage.read("user", "53");
		DivaDbSpy factored = divaDbFactorySpy.factored;
		assertEquals(factored.type, "user");
		assertEquals(factored.id, "53");
	}

	@Test
	public void testUserFromDivaDbToCoraIsReturnedFromRead() throws Exception {
		DataGroup readOrganisation = divaRecordStorage.read("user", "53");
		DivaDbSpy factored = divaDbFactorySpy.factored;
		assertEquals(readOrganisation, factored.dataGroup);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "create is not implemented")
	public void createThrowsNotImplementedException() throws Exception {
		divaRecordStorage.create(null, null, null, null, null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "deleteByTypeAndId is not implemented")
	public void deleteByTypeAndIdThrowsNotImplementedException() throws Exception {
		divaRecordStorage.deleteByTypeAndId(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "linksExistForRecord is not implemented")
	public void linksExistForRecordThrowsNotImplementedException() throws Exception {
		divaRecordStorage.linksExistForRecord(null, null);
	}

	@Test
	public void testUpdateOrganisationFactorOrganisationDbRecordStorageForOneType()
			throws Exception {
		DataGroup record = new DataGroupSpy("organisation");
		record.addChild(new DataAtomicSpy("organisationName", "someChangedName"));

		String dataDivider = "";
		divaRecordStorage.update("organisation", "56", record, null, null, dataDivider);
		assertTrue(divaDbUpdaterFactorySpy.factorWasCalled);
		assertEquals(divaDbUpdaterFactorySpy.types.get(0), "organisation");
	}

	@Test
	public void testUpdateUsesSentInType() throws Exception {
		DataGroup organisation = new DataGroupSpy("someType");
		organisation.addChild(new DataAtomicSpy("organisationName", "someChangedName"));

		divaRecordStorage.update("someType", "56", organisation, null, null, "");

		assertEquals(divaDbUpdaterFactorySpy.types.get(0), "someType");
	}

	@Test
	public void testUpdateOrganisationUsesRecordStorageForOneTypeFromFactory() throws Exception {
		DataGroup organisation = new DataGroupSpy("organisation");
		organisation.addChild(new DataAtomicSpy("organisationName", "someChangedName"));

		String dataDivider = "";
		divaRecordStorage.update("divaOrganisation", "56", organisation, null, null, dataDivider);

		DivaDbUpdaterSpy recordStorageForOneTypeSpy = (DivaDbUpdaterSpy) divaDbUpdaterFactorySpy.divaDbUpdaterList
				.get(0);
		assertEquals(recordStorageForOneTypeSpy.dataGroup, organisation);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "readList is not implemented for type: null")
	public void readListThrowsNotImplementedException() throws Exception {
		divaRecordStorage.readList(null, null);
	}

	@Test
	public void testReadOrganisationListFactorDbReader() throws Exception {
		divaRecordStorage.readList(ORGANISATION_TYPE, new DataGroupSpy("filter"));
		assertTrue(recordReaderFactorySpy.factorWasCalled);
	}

	@Test
	public void testReadOrganisationListTableRequestedFromReader() throws Exception {
		divaRecordStorage.readList(ORGANISATION_TYPE, new DataGroupSpy("filter"));
		RecordReaderSpy recordReader = recordReaderFactorySpy.factored;
		assertEquals(recordReader.usedTableName, ORGANISATION_TYPE);
	}

	@Test
	public void testReadRootOrganisationListTableRequestedFromReader() throws Exception {
		divaRecordStorage.readList("rootOrganisation", new DataGroupSpy("filter"));
		RecordReaderSpy recordReader = recordReaderFactorySpy.factored;
		assertEquals(recordReader.usedTableName, ORGANISATION_TYPE);
	}

	@Test
	public void testReadcommonOrganisationListTableRequestedFromReader() throws Exception {
		divaRecordStorage.readList("commonOrganisation", new DataGroupSpy("filter"));
		RecordReaderSpy recordReader = recordReaderFactorySpy.factored;
		assertEquals(recordReader.usedTableName, ORGANISATION_TYPE);
	}

	@Test
	public void testReadOrganisationListFactorMultipleParentReader() throws Exception {
		divaRecordStorage.readList(ORGANISATION_TYPE, new DataGroupSpy("filter"));
		assertEquals(divaDbFactorySpy.usedTypes.get(0), "divaOrganisationParent");
	}

	@Test
	public void testReadOrganisationListMultipleFactoryIsCalledCorrectly() {
		RecordReaderFactoryForListSpy recordReaderFactoryForList = new RecordReaderFactoryForListSpy();
		setUpRecordStorageAndReadList(recordReaderFactoryForList);

		assertEquals(divaDbFactorySpy.listOfFactoredMultiples.size(), 6);
		assertEquals(divaDbFactorySpy.usedTypes.size(), 6);
		assertEquals(divaDbFactorySpy.usedTypes.get(0), "divaOrganisationParent");
		assertEquals(divaDbFactorySpy.usedTypes.get(2), "divaOrganisationParent");
		assertEquals(divaDbFactorySpy.usedTypes.get(4), "divaOrganisationParent");
		assertEquals(divaDbFactorySpy.usedTypes.get(1), "divaOrganisationPredecessor");
		assertEquals(divaDbFactorySpy.usedTypes.get(3), "divaOrganisationPredecessor");
		assertEquals(divaDbFactorySpy.usedTypes.get(5), "divaOrganisationPredecessor");
	}

	@Test
	public void testReadOrganisationListMultipleReadersAreCalledCorrectly() {
		RecordReaderFactoryForListSpy recordReaderFactoryForList = new RecordReaderFactoryForListSpy();
		setUpRecordStorageAndReadList(recordReaderFactoryForList);

		RecordReaderForListSpy recordReader = recordReaderFactoryForList.factored;

		List<Map<String, Object>> readRows = recordReader.returnedList;

		List<MultipleRowDbToDataReaderSpy> multipleReaders = divaDbFactorySpy.listOfFactoredMultiples;
		assertSame(multipleReaders.get(0).usedId, readRows.get(0).get("id"));
		assertSame(multipleReaders.get(1).usedId, readRows.get(0).get("id"));

		assertSame(multipleReaders.get(2).usedId, readRows.get(1).get("id"));
		assertSame(multipleReaders.get(3).usedId, readRows.get(1).get("id"));

		assertSame(multipleReaders.get(4).usedId, readRows.get(2).get("id"));
		assertSame(multipleReaders.get(5).usedId, readRows.get(2).get("id"));

	}

	@Test
	public void testReadOrganisationListAllReadFromDbAreSentToConverterAndAddedToResult() {
		RecordReaderFactoryForListSpy recordReaderFactoryForList = new RecordReaderFactoryForListSpy();
		StorageReadResult readList = setUpRecordStorageAndReadList(recordReaderFactoryForList);

		RecordReaderForListSpy recordReader = recordReaderFactoryForList.factored;
		List<DataGroup> organisations = readList.listOfDataGroups;

		assertReadRecordIsSentToConverterUsingIndex(recordReader, 0);
		assertConvertedRowIsAddedToResultUsingIndex(readList, 0);
		assertCorrectParentsAndPredecessorsWereAddedToOrganisation(organisations.get(0), 0, 1);

		assertReadRecordIsSentToConverterUsingIndex(recordReader, 1);
		assertConvertedRowIsAddedToResultUsingIndex(readList, 1);
		assertCorrectParentsAndPredecessorsWereAddedToOrganisation(organisations.get(1), 2, 3);

		assertReadRecordIsSentToConverterUsingIndex(recordReader, 2);
		assertConvertedRowIsAddedToResultUsingIndex(readList, 2);
		assertCorrectParentsAndPredecessorsWereAddedToOrganisation(organisations.get(2), 4, 5);

	}

	private StorageReadResult setUpRecordStorageAndReadList(
			RecordReaderFactoryForListSpy recordReaderFactoryForList) {
		divaRecordStorage = DivaDbRecordStorage
				.usingRecordReaderFactoryDivaFactoryAndDivaDbUpdaterFactory(
						recordReaderFactoryForList, divaDbFactorySpy, divaDbUpdaterFactorySpy,
						converterFactorySpy);

		StorageReadResult readList = divaRecordStorage.readList(ORGANISATION_TYPE,
				new DataGroupSpy("filter"));
		return readList;
	}

	private void assertCorrectParentsAndPredecessorsWereAddedToOrganisation(DataGroup organisation,
			int parentReaderIndex, int predecessorReaderIndex) {
		List<DataGroup> parentsInOrganisation = organisation
				.getAllGroupsWithNameInData("divaOrganisationParentChildFromSpy");
		MultipleRowDbToDataReaderSpy parentMultipleReader = divaDbFactorySpy.listOfFactoredMultiples
				.get(parentReaderIndex);
		List<DataGroup> returnedList = parentMultipleReader.returnedList;
		assertSame(parentsInOrganisation.get(0), returnedList.get(0));
		assertSame(parentsInOrganisation.get(1), returnedList.get(1));

		List<DataGroup> predececssorsInOrganisation = organisation
				.getAllGroupsWithNameInData("divaOrganisationPredecessorChildFromSpy");
		MultipleRowDbToDataReaderSpy predecessorMultipleReader = divaDbFactorySpy.listOfFactoredMultiples
				.get(predecessorReaderIndex);
		List<DataGroup> returnedListPredecessors = predecessorMultipleReader.returnedList;
		assertSame(predececssorsInOrganisation.get(0), returnedListPredecessors.get(0));
		assertSame(predececssorsInOrganisation.get(1), returnedListPredecessors.get(1));
	}

	private void assertReadRecordIsSentToConverterUsingIndex(RecordReaderForListSpy recordReader,
			int index) {
		Map<String, Object> readRow = recordReader.returnedList.get(index);
		DivaDbToCoraConverterSpy factoredConverter = converterFactorySpy.factoredConverters
				.get(index);
		assertSame(readRow, factoredConverter.mapToConvert);
	}

	private void assertConvertedRowIsAddedToResultUsingIndex(StorageReadResult readList,
			int index) {
		DivaDbToCoraConverterSpy converterSpy = converterFactorySpy.factoredConverters.get(index);
		assertSame(converterSpy.convertedDbDataGroup, readList.listOfDataGroups.get(index));
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "readAbstractList is not implemented")
	public void readAbstractListThrowsNotImplementedException() throws Exception {
		divaRecordStorage.readAbstractList(null, null);
	}

	@Test
	public void testReadAbstractListForUserFactorDbReader() throws Exception {
		divaRecordStorage.readAbstractList("user", new DataGroupSpy("filter"));
		assertTrue(recordReaderFactorySpy.factorWasCalled);
		RecordReaderSpy factored = recordReaderFactorySpy.factored;
		assertEquals(factored.usedTableName, "public.user");
	}

	@Test
	public void testReadAbstractListForUserReturnsCorrectData() throws Exception {
		recordReaderFactorySpy.noOfRecordsToReturn = 3;
		StorageReadResult result = divaRecordStorage.readAbstractList("user",
				new DataGroupSpy("filter"));

		RecordReaderSpy factoredReader = recordReaderFactorySpy.factored;
		assertDataSentFromDbToConverterToResultUsingIndex(factoredReader, result, 0);
		assertDataSentFromDbToConverterToResultUsingIndex(factoredReader, result, 1);
		assertDataSentFromDbToConverterToResultUsingIndex(factoredReader, result, 2);

	}

	private void assertDataSentFromDbToConverterToResultUsingIndex(RecordReaderSpy factoredReader,
			StorageReadResult result, int index) {
		List<Map<String, Object>> returnedList = factoredReader.returnedList;
		DivaDbToCoraConverterSpy converter = converterFactorySpy.factoredConverters.get(index);

		Map<String, Object> rowFromDb = returnedList.get(index);
		Map<String, Object> rowSentToConverter = converter.mapToConvert;
		assertEquals(rowFromDb, rowSentToConverter);

		DataGroup dataGroupInReturnedResult = result.listOfDataGroups.get(index);
		DataGroup dataGroupReturnedFromConverter = converter.convertedDbDataGroup;
		assertSame(dataGroupInReturnedResult, dataGroupReturnedFromConverter);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "readLinkList is not implemented")
	public void readLinkListThrowsNotImplementedException() throws Exception {
		divaRecordStorage.readLinkList(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "generateLinkCollectionPointingToRecord is not implemented")
	public void generateLinkCollectionPointingToRecordThrowsNotImplementedException()
			throws Exception {
		divaRecordStorage.generateLinkCollectionPointingToRecord(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "recordsExistForRecordType is not implemented")
	public void recordsExistForRecordTypeThrowsNotImplementedException() throws Exception {
		divaRecordStorage.recordsExistForRecordType(null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "recordExistsForAbstractOrImplementingRecordTypeAndRecordId is not implemented")
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdThrowsNotImplementedException()
			throws Exception {
		divaRecordStorage.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(null, null);
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForOrganisationWhenExists() {
		assertCorrectRecordExistsWhenExistsForOrganisationType("organisation");
	}

	private void assertCorrectRecordExistsWhenExistsForOrganisationType(String type) {
		boolean organisationExists = divaRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(type, "26");
		RecordReaderSpy readerSpy = recordReaderFactorySpy.factored;
		assertEquals(readerSpy.usedTableName, "organisation");
		Map<String, Object> usedConditions = readerSpy.usedConditions;
		assertEquals(usedConditions.get("organisation_id"), 26);
		assertTrue(organisationExists);
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForRootOrganisationWhenExists() {
		assertCorrectRecordExistsWhenExistsForOrganisationType("rootOrganisation");
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForcommonOrganisationWhenExists() {
		assertCorrectRecordExistsWhenExistsForOrganisationType("commonOrganisation");
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForOrganisationWhenNotExist() {
		assertCorrectRecordExistsWhenNotExistsForOrganisationType("organisation");
	}

	private void assertCorrectRecordExistsWhenNotExistsForOrganisationType(String type) {
		boolean organisationExists = divaRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(type, "600");
		RecordReaderSpy readerSpy = recordReaderFactorySpy.factored;
		assertEquals(readerSpy.usedTableName, "organisation");
		Map<String, Object> usedConditions = readerSpy.usedConditions;
		assertEquals(usedConditions.get("organisation_id"), 600);
		assertFalse(organisationExists);
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForRootOrganisationWhenNotExist() {
		assertCorrectRecordExistsWhenNotExistsForOrganisationType("rootOrganisation");
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForcommonOrganisationWhenNotExist() {
		assertCorrectRecordExistsWhenNotExistsForOrganisationType("commonOrganisation");
	}

	@Test
	public void testRecordExistsDivaOrganisationCallsDataReaderWithStringIdReturnsFalse()
			throws Exception {
		boolean organisationExists = divaRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId("organisation",
						"notAnInt");
		assertFalse(organisationExists);
	}
}
