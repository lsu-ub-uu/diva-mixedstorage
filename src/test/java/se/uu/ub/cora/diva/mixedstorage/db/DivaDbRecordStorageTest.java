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
package se.uu.ub.cora.diva.mixedstorage.db;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.SqlDatabaseFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.TableFacadeSpy;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.TableQuerySpy;
import se.uu.ub.cora.sqldatabase.Row;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.StorageReadResult;

public class DivaDbRecordStorageTest {
	private static final String ORGANISATION_TYPE = "organisation";
	private DivaDbRecordStorage divaRecordStorage;
	private DivaDbToCoraConverterFactorySpy converterFactorySpy;
	private DivaDbFactorySpy divaDbFactorySpy;
	private DivaDbUpdaterFactorySpy divaDbUpdaterFactorySpy;
	private SqlDatabaseFactorySpy sqlDatabaseFactory;

	@BeforeMethod
	public void BeforeMethod() {
		converterFactorySpy = new DivaDbToCoraConverterFactorySpy();
		sqlDatabaseFactory = new SqlDatabaseFactorySpy();
		sqlDatabaseFactory.createAndAddRowToReturn("id", 123);
		sqlDatabaseFactory.createAndAddRowToReturn("id", 456);

		divaDbFactorySpy = new DivaDbFactorySpy();
		divaDbUpdaterFactorySpy = new DivaDbUpdaterFactorySpy();
		divaRecordStorage = DivaDbRecordStorage
				.usingRecordReaderFactoryDivaFactoryAndDivaDbUpdaterFactory(sqlDatabaseFactory,
						divaDbFactorySpy, divaDbUpdaterFactorySpy, converterFactorySpy);
	}

	@Test
	public void testInit() throws Exception {
		divaRecordStorage = DivaDbRecordStorage
				.usingRecordReaderFactoryDivaFactoryAndDivaDbUpdaterFactory(sqlDatabaseFactory,
						divaDbFactorySpy, divaDbUpdaterFactorySpy, converterFactorySpy);

		assertNotNull(divaRecordStorage);
		assertSame(divaRecordStorage.getSqlDatabaseFactory(), sqlDatabaseFactory);
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
		assertSame(factored.tableFacade, sqlDatabaseFactory.factoredTableFacade);
	}

	@Test
	public void testReadOrganisationClosesTableFacade() throws Exception {
		divaRecordStorage.read(ORGANISATION_TYPE, "someId");
		DivaDbSpy factored = divaDbFactorySpy.factored;
		TableFacadeSpy tableFacade = (TableFacadeSpy) factored.tableFacade;
		assertTrue(tableFacade.closeWasCalled);
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
		assertSame(recordStorageForOneTypeSpy.tableFacade, sqlDatabaseFactory.factoredTableFacade);
		assertSame(recordStorageForOneTypeSpy.databaseFacade,
				sqlDatabaseFactory.factoredDatabaseFacade);
	}

	@Test
	public void testUpdateClosesTableFacade() throws Exception {
		DataGroup organisation = new DataGroupSpy("someType");
		organisation.addChild(new DataAtomicSpy("organisationName", "someChangedName"));

		divaRecordStorage.update("someType", "56", organisation, null, null, "");

		assertTrue(sqlDatabaseFactory.factoredTableFacade.closeWasCalled);
		assertTrue(sqlDatabaseFactory.factoredDatabaseFacade.closeWasCalled);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "readList is not implemented for type: null")
	public void readListThrowsNotImplementedException() throws Exception {
		divaRecordStorage.readList(null, null);
	}

	@Test
	public void testReadOrganisationListFactorDbReader() throws Exception {
		divaRecordStorage.readList(ORGANISATION_TYPE, new DataGroupSpy("filter"));
		TableFacadeSpy tableFacade = sqlDatabaseFactory.factoredTableFacade;
		assertNotNull(tableFacade);
		assertTrue(tableFacade.closeWasCalled);
	}

	@Test
	public void testReadOrganisationListTableRequestedFromReader() throws Exception {
		divaRecordStorage.readList(ORGANISATION_TYPE, new DataGroupSpy("filter"));
		TableQuerySpy tableQuery = sqlDatabaseFactory.factoredTableQueries.get(0);
		assertEquals(tableQuery.tableName, "organisationview");
		TableFacadeSpy tableFacade = sqlDatabaseFactory.factoredTableFacade;
		assertSame(tableFacade.tableQueries.get(0), tableQuery);
	}

	@Test
	public void testReadRootOrganisationListTableRequestedFromReader() throws Exception {
		divaRecordStorage.readList("rootOrganisation", new DataGroupSpy("filter"));
		TableQuerySpy tableQuery = sqlDatabaseFactory.factoredTableQueries.get(0);
		assertEquals(tableQuery.tableName, "rootorganisationview");
	}

	@Test
	public void testReadTopOrganisationListTableRequestedFromReader() throws Exception {
		divaRecordStorage.readList("topOrganisation", new DataGroupSpy("filter"));
		TableQuerySpy tableQuery = sqlDatabaseFactory.factoredTableQueries.get(0);
		assertEquals(tableQuery.tableName, "toporganisationview");
	}

	@Test
	public void testReadSubOrganisationListTableRequestedFromReader() throws Exception {
		divaRecordStorage.readList("subOrganisation", new DataGroupSpy("filter"));
		TableQuerySpy tableQuery = sqlDatabaseFactory.factoredTableQueries.get(0);
		assertEquals(tableQuery.tableName, "suborganisationview");
	}

	@Test
	public void testReadOrganisationListWithFilterWithFromAndTo() throws Exception {
		DataGroupSpy filter = new DataGroupSpy("filter");
		filter.addChild(new DataAtomicSpy("fromNo", "10"));
		filter.addChild(new DataAtomicSpy("toNo", "19"));

		divaRecordStorage.readList(ORGANISATION_TYPE, filter);
		TableQuerySpy tableQuery = sqlDatabaseFactory.factoredTableQueries.get(0);
		assertEquals(tableQuery.tableName, "organisationview");

		assertEquals(tableQuery.ascOrdersBy.get(0), "id");
		assertEquals(tableQuery.fromNumbers.get(0), Long.valueOf(10));
		assertEquals(tableQuery.toNumbers.get(0), Long.valueOf(19));
	}

	@Test
	public void testReadOrganisationListWithFilterWithNoFromAndNoTo() throws Exception {
		DataGroupSpy filter = new DataGroupSpy("filter");

		divaRecordStorage.readList(ORGANISATION_TYPE, filter);

		TableQuerySpy tableQuery = sqlDatabaseFactory.factoredTableQueries.get(0);
		assertEquals(tableQuery.tableName, "organisationview");

		assertEquals(tableQuery.ascOrdersBy.get(0), "id");
		assertEquals(tableQuery.fromNumbers.get(0), null);
		assertEquals(tableQuery.toNumbers.get(0), null);
	}

	@Test
	public void testReadOrganisationListFactorMultipleParentReader() throws Exception {
		divaRecordStorage.readList(ORGANISATION_TYPE, new DataGroupSpy("filter"));
		assertEquals(divaDbFactorySpy.usedTypes.get(0), "divaOrganisationParent");
	}

	@Test
	public void testReadOrganisationListMultipleFactoryIsCalledCorrectly() {
		sqlDatabaseFactory.createAndAddRowToReturn("id", 543);

		divaRecordStorage.readList(ORGANISATION_TYPE, new DataGroupSpy("filter"));

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
		sqlDatabaseFactory.createAndAddRowToReturn("id", 543);
		divaRecordStorage.readList(ORGANISATION_TYPE, new DataGroupSpy("filter"));

		TableFacadeSpy tableFacade = sqlDatabaseFactory.factoredTableFacade;

		List<Row> readRows = tableFacade.rowsToReturn;

		List<MultipleRowDbToDataReaderSpy> multipleReaders = divaDbFactorySpy.listOfFactoredMultiples;
		assertEquals(multipleReaders.get(0).usedId,
				String.valueOf(readRows.get(0).getValueByColumn("id")));
		assertSame(multipleReaders.get(0).tableFacade, sqlDatabaseFactory.factoredTableFacade);

		assertEquals(multipleReaders.get(1).usedId,
				String.valueOf(readRows.get(0).getValueByColumn("id")));
		assertSame(multipleReaders.get(1).tableFacade, sqlDatabaseFactory.factoredTableFacade);

		assertEquals(multipleReaders.get(2).usedId,
				String.valueOf(readRows.get(1).getValueByColumn("id")));
		assertEquals(multipleReaders.get(3).usedId,
				String.valueOf(readRows.get(1).getValueByColumn("id")));

		assertEquals(multipleReaders.get(4).usedId,
				String.valueOf(readRows.get(2).getValueByColumn("id")));
		assertEquals(multipleReaders.get(5).usedId,
				String.valueOf(readRows.get(2).getValueByColumn("id")));

	}

	@Test
	public void testReadOrganisationListAllReadFromDbAreSentToConverterAndAddedToResult() {
		sqlDatabaseFactory.createAndAddRowToReturn("id", 543);
		StorageReadResult readList = divaRecordStorage.readList(ORGANISATION_TYPE,
				new DataGroupSpy("filter"));

		TableFacadeSpy tableFacade = sqlDatabaseFactory.factoredTableFacade;

		List<DataGroup> organisations = readList.listOfDataGroups;

		assertReadRecordIsSentToConverterUsingIndex(tableFacade, 0);
		assertConvertedRowIsAddedToResultUsingIndex(readList, 0);
		assertCorrectParentsAndPredecessorsWereAddedToOrganisation(organisations.get(0), 0, 1);

		assertReadRecordIsSentToConverterUsingIndex(tableFacade, 1);
		assertConvertedRowIsAddedToResultUsingIndex(readList, 1);
		assertCorrectParentsAndPredecessorsWereAddedToOrganisation(organisations.get(1), 2, 3);

		assertReadRecordIsSentToConverterUsingIndex(tableFacade, 2);
		assertConvertedRowIsAddedToResultUsingIndex(readList, 2);
		assertCorrectParentsAndPredecessorsWereAddedToOrganisation(organisations.get(2), 4, 5);

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

	private void assertReadRecordIsSentToConverterUsingIndex(TableFacadeSpy tableFacade,
			int index) {
		Row readRow = tableFacade.rowsToReturn.get(index);
		DivaDbToCoraConverterSpy factoredConverter = converterFactorySpy.factoredConverters
				.get(index);
		assertSame(readRow, factoredConverter.rowToConvert);
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
		TableQuerySpy tableQuery = sqlDatabaseFactory.factoredTableQueries.get(0);
		assertEquals(tableQuery.tableName, "public.user");
	}

	@Test
	public void testReadAbstractListForUserReturnsCorrectData() throws Exception {
		sqlDatabaseFactory.createAndAddRowToReturn("id", 543);

		StorageReadResult result = divaRecordStorage.readAbstractList("user",
				new DataGroupSpy("filter"));

		TableFacadeSpy tableFacade = sqlDatabaseFactory.factoredTableFacade;

		assertDataSentFromDbToConverterToResultUsingIndex(tableFacade, result, 0);
		assertDataSentFromDbToConverterToResultUsingIndex(tableFacade, result, 1);
		assertDataSentFromDbToConverterToResultUsingIndex(tableFacade, result, 2);

		assertTrue(tableFacade.closeWasCalled);

	}

	private void assertDataSentFromDbToConverterToResultUsingIndex(TableFacadeSpy tableFacade,
			StorageReadResult result, int index) {
		List<Row> returnedList = tableFacade.rowsToReturn;
		DivaDbToCoraConverterSpy converter = converterFactorySpy.factoredConverters.get(index);

		Row rowFromDb = returnedList.get(index);
		Row rowSentToConverter = converter.rowToConvert;
		assertEquals(rowFromDb, rowSentToConverter);

		DataGroup dataGroupInReturnedResult = result.listOfDataGroups.get(index);
		DataGroup dataGroupReturnedFromConverter = converter.convertedDbDataGroup;
		assertSame(dataGroupInReturnedResult, dataGroupReturnedFromConverter);
	}

	@Test
	public void testReadAbstractListForOrganisationFactorDbReader() throws Exception {
		divaRecordStorage.readAbstractList("organisation", new DataGroupSpy("filter"));
		assertEquals(sqlDatabaseFactory.tableNames.get(0), "organisationview");
	}

	@Test
	public void testReadAbstractListForOrganisationReturnsCorrectData() throws Exception {
		sqlDatabaseFactory.createAndAddRowToReturn("id", 543);
		StorageReadResult result = divaRecordStorage.readAbstractList("organisation",
				new DataGroupSpy("filter"));

		TableFacadeSpy tableFacade = sqlDatabaseFactory.factoredTableFacade;

		assertDataSentFromDbToConverterToResultUsingIndex(tableFacade, result, 0);
		assertDataSentFromDbToConverterToResultUsingIndex(tableFacade, result, 1);
		assertDataSentFromDbToConverterToResultUsingIndex(tableFacade, result, 2);

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
			+ "recordExistsForAbstractOrImplementingRecordTypeAndRecordId is not implemented")
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdThrowsNotImplementedException()
			throws Exception {
		divaRecordStorage.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(null, null);
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForOrganisationWhenExists() {
		assertCorrectRecordExistsWhenExistsForOrganisationType("organisation", "organisationview");
	}

	private void assertCorrectRecordExistsWhenExistsForOrganisationType(String type,
			String tableName) {
		boolean organisationExists = divaRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(type, "26");

		assertEquals(sqlDatabaseFactory.tableNames.get(0), tableName);
		TableQuerySpy tableQuery = sqlDatabaseFactory.factoredTableQueries.get(0);
		TableFacadeSpy tableFacade = sqlDatabaseFactory.factoredTableFacade;
		assertEquals(tableQuery.conditions.get("id"), 26);
		assertSame(tableQuery, tableFacade.tableQueries.get(0));
		assertTrue(organisationExists);

		assertTrue(tableFacade.closeWasCalled);
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForRootOrganisationWhenExists() {
		assertCorrectRecordExistsWhenExistsForOrganisationType("rootOrganisation",
				"rootorganisationview");
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForTopOrganisationWhenExists() {
		assertCorrectRecordExistsWhenExistsForOrganisationType("topOrganisation",
				"toporganisationview");
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForSubOrganisationWhenExists() {
		assertCorrectRecordExistsWhenExistsForOrganisationType("subOrganisation",
				"suborganisationview");
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForOrganisationWhenNotExist() {
		sqlDatabaseFactory.tablesToThrowExceptionFor.add("organisationview");

		boolean organisationExists = divaRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId("organisation", "600");

		assertCorrectRecordExistsWhenNotExistsForOrganisationType("organisation",
				"organisationview");
		assertFalse(organisationExists);
	}

	private void assertCorrectRecordExistsWhenNotExistsForOrganisationType(String type,
			String tableName) {
		assertEquals(sqlDatabaseFactory.tableNames.get(0), tableName);
		TableQuerySpy tableQuery = sqlDatabaseFactory.factoredTableQueries.get(0);
		TableFacadeSpy tableFacade = sqlDatabaseFactory.factoredTableFacade;
		assertEquals(tableQuery.conditions.get("id"), 600);
		assertSame(tableQuery, tableFacade.tableQueries.get(0));
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForRootOrganisationWhenNotExist() {
		sqlDatabaseFactory.tablesToThrowExceptionFor.add("rootorganisationview");

		boolean organisationExists = divaRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId("rootOrganisation",
						"600");

		assertCorrectRecordExistsWhenNotExistsForOrganisationType("rootOrganisation",
				"rootorganisationview");
		assertFalse(organisationExists);
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForTopOrganisationWhenNotExist() {
		sqlDatabaseFactory.tablesToThrowExceptionFor.add("toporganisationview");

		boolean organisationExists = divaRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId("topOrganisation",
						"600");

		assertCorrectRecordExistsWhenNotExistsForOrganisationType("topOrganisation",
				"toporganisationview");
		assertFalse(organisationExists);
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForSubOrganisationWhenNotExist() {
		sqlDatabaseFactory.tablesToThrowExceptionFor.add("suborganisationview");

		boolean organisationExists = divaRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId("subOrganisation",
						"600");
		assertCorrectRecordExistsWhenNotExistsForOrganisationType("subOrganisation",
				"suborganisationview");
		assertFalse(organisationExists);
	}

	@Test
	public void testRecordExistsDivaOrganisationCallsDataReaderWithStringIdReturnsFalse()
			throws Exception {
		boolean organisationExists = divaRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId("organisation",
						"notAnInt");
		assertFalse(organisationExists);
	}

	@Test
	public void testGetTotalNumberOfRecordsForTypeSubOrganisation() {
		testGetTotalNumberOfRecordsForTypeEmptyFilter("subOrganisation", "suborganisationview");

	}

	private void testGetTotalNumberOfRecordsForTypeEmptyFilter(String type, String tableName) {
		DataGroupSpy filterDataGroup = new DataGroupSpy("filter");
		long totalNumberOfRecordsForType = divaRecordStorage.getTotalNumberOfRecordsForType(type,
				filterDataGroup);

		TableFacadeSpy tableFacade = sqlDatabaseFactory.factoredTableFacade;
		TableQuerySpy tableQuery = sqlDatabaseFactory.factoredTableQueries.get(0);
		assertEquals(sqlDatabaseFactory.tableNames.get(0), tableName);
		assertSame(tableFacade.tableQueries.get(0), tableQuery);
		assertTrue(tableQuery.conditions.isEmpty());
		assertEquals(totalNumberOfRecordsForType, tableFacade.numOfReadRows);

		assertTrue(tableFacade.closeWasCalled);
	}

	@Test
	public void testGetTotalNumberOfRecordsForTypeTopOrganisation() {
		testGetTotalNumberOfRecordsForTypeEmptyFilter("topOrganisation", "toporganisationview");

	}

	@Test
	public void testGetTotalNumberOfRecordsForTypeRootOrganisation() {
		testGetTotalNumberOfRecordsForTypeEmptyFilter("rootOrganisation", "rootorganisationview");

	}

	@Test(expectedExceptions = NotImplementedException.class)
	public void testGetTotalNumberOfRecordsForTypeNotImplemented() {
		divaRecordStorage.getTotalNumberOfRecordsForType("typeNotImplemented",
				new DataGroupSpy("filter"));

	}

	@Test
	public void testGetTotalNumberOfRecordsWithFromNoTo() {
		DataGroupSpy filterDataGroup = new DataGroupSpy("filter");
		filterDataGroup.addChild(new DataAtomicSpy("fromNo", "4"));
		long totalNumberOfRecordsForType = divaRecordStorage
				.getTotalNumberOfRecordsForType("topOrganisation", filterDataGroup);

		assertEquals(sqlDatabaseFactory.tableNames.get(0), "toporganisationview");
		TableQuerySpy tableQuery = sqlDatabaseFactory.factoredTableQueries.get(0);

		assertEquals(tableQuery.ascOrdersBy.get(0), "id");
		assertEquals(tableQuery.fromNumbers.get(0), Long.valueOf(4));
		assertEquals(tableQuery.toNumbers.get(0), null);

		assertEquals(totalNumberOfRecordsForType,
				sqlDatabaseFactory.factoredTableFacade.numToReturn);
	}

	@Test
	public void testGetTotalNumberOfRecordsWithNoFromButTo() {
		DataGroupSpy filterDataGroup = new DataGroupSpy("filter");
		filterDataGroup.addChild(new DataAtomicSpy("toNo", "4"));

		divaRecordStorage.getTotalNumberOfRecordsForType("topOrganisation", filterDataGroup);

		assertEquals(sqlDatabaseFactory.tableNames.get(0), "toporganisationview");
		TableQuerySpy tableQuery = sqlDatabaseFactory.factoredTableQueries.get(0);

		assertEquals(tableQuery.fromNumbers.get(0), null);
		assertEquals(tableQuery.toNumbers.get(0), Long.valueOf(4));
	}

	@Test
	public void testGetTotalNumberOfRecordsWithFromAndTo() {
		DataGroupSpy filterDataGroup = new DataGroupSpy("filter");
		filterDataGroup.addChild(new DataAtomicSpy("fromNo", "4"));
		filterDataGroup.addChild(new DataAtomicSpy("toNo", "9"));

		divaRecordStorage.getTotalNumberOfRecordsForType("topOrganisation", filterDataGroup);

		divaRecordStorage.getTotalNumberOfRecordsForType("topOrganisation", filterDataGroup);

		assertEquals(sqlDatabaseFactory.tableNames.get(0), "toporganisationview");
		TableQuerySpy tableQuery = sqlDatabaseFactory.factoredTableQueries.get(0);

		assertEquals(tableQuery.fromNumbers.get(0), Long.valueOf(4));
		assertEquals(tableQuery.toNumbers.get(0), Long.valueOf(9));
	}

	@Test
	public void testGetTotalNumberOfRecordsForAbstractTypeOrganisation() {
		DataGroupSpy filterDataGroup = new DataGroupSpy("filter");
		List<String> implementingTypes = new ArrayList<>();
		implementingTypes.add("subOrganisation");

		long totalNumberOfRecordsForType = divaRecordStorage.getTotalNumberOfRecordsForAbstractType(
				"organisation", implementingTypes, filterDataGroup);

		assertEquals(sqlDatabaseFactory.tableNames.get(0), "organisationview");
		assertTrue(sqlDatabaseFactory.factoredTableQueries.get(0).conditions.isEmpty());

		TableQuerySpy tableQuery = sqlDatabaseFactory.factoredTableQueries.get(0);

		assertEquals(tableQuery.fromNumbers.get(0), null);
		assertEquals(tableQuery.toNumbers.get(0), null);
		assertEquals(totalNumberOfRecordsForType,
				sqlDatabaseFactory.factoredTableFacade.numToReturn);
	}

	@Test(expectedExceptions = NotImplementedException.class)
	public void testGetTotalNumberOfAbstractRecordsForTypeNotImplemented() {
		divaRecordStorage.getTotalNumberOfRecordsForAbstractType("typeNotImplemented",
				new ArrayList<>(), new DataGroupSpy("filter"));

	}
}
