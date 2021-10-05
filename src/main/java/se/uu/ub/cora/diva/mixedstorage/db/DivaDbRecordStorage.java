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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.MultipleRowDbToDataReader;
import se.uu.ub.cora.sqldatabase.Row;
import se.uu.ub.cora.sqldatabase.SqlDatabaseException;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;
import se.uu.ub.cora.sqldatabase.table.TableFacade;
import se.uu.ub.cora.sqldatabase.table.TableQuery;
import se.uu.ub.cora.storage.RecordNotFoundException;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.StorageReadResult;

public class DivaDbRecordStorage implements RecordStorage {

	private static final String ORGANISATION = "organisation";
	private DivaDbFactory divaDbFactory;
	private DivaDbUpdaterFactory divaDbUpdaterFactory;
	private DivaDbToCoraConverterFactory converterFactory;
	private SqlDatabaseFactory sqlDatabaseFactory;

	private DivaDbRecordStorage(SqlDatabaseFactory sqlDatabaseFactory,
			DivaDbFactory divaDbReaderFactory, DivaDbUpdaterFactory divaDbUpdaterFactory,
			DivaDbToCoraConverterFactory converterFactory) {
		this.sqlDatabaseFactory = sqlDatabaseFactory;
		this.divaDbFactory = divaDbReaderFactory;
		this.divaDbUpdaterFactory = divaDbUpdaterFactory;
		this.converterFactory = converterFactory;
	}

	public static DivaDbRecordStorage usingRecordReaderFactoryDivaFactoryAndDivaDbUpdaterFactory(
			SqlDatabaseFactory sqlDatabaseFactory, DivaDbFactory divaDbFactory,
			DivaDbUpdaterFactory divaDbUpdaterFactory,
			DivaDbToCoraConverterFactory converterFactory) {
		return new DivaDbRecordStorage(sqlDatabaseFactory, divaDbFactory, divaDbUpdaterFactory,
				converterFactory);
	}

	@Override
	public DataGroup read(String type, String id) {
		DivaDbReader divaDbReader = divaDbFactory.factor(type);
		return divaDbReader.read(type, id);
	}

	@Override
	public void create(String type, String id, DataGroup dataRecord, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		throw NotImplementedException.withMessage("create is not implemented");
	}

	@Override
	public void deleteByTypeAndId(String type, String id) {
		throw NotImplementedException.withMessage("deleteByTypeAndId is not implemented");
	}

	@Override
	public boolean linksExistForRecord(String type, String id) {
		throw NotImplementedException.withMessage("linksExistForRecord is not implemented");
	}

	@Override
	public void update(String type, String id, DataGroup dataRecord, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		DivaDbUpdater divaDbUpdater = divaDbUpdaterFactory.factor(type);
		divaDbUpdater.update(dataRecord);
	}

	private DataGroup convertOneMapFromDbToDataGroup(String type, Row readRow) {
		DivaDbToCoraConverter dbToCoraConverter = converterFactory.factor(type);
		return dbToCoraConverter.fromRow(readRow);
	}

	@Override
	public StorageReadResult readList(String type, DataGroup filter) {
		if (isOrganisation(type)) {
			String tableName = getTableName(type);
			return readOrganisationList(type, tableName, filter);
		}
		throw NotImplementedException.withMessage("readList is not implemented for type: " + type);
	}

	private String getTableName(String type) {
		String tableName = "organisationview";
		if ("rootOrganisation".equals(type)) {
			tableName = "rootorganisationview";
		} else if ("topOrganisation".equals(type)) {
			tableName = "toporganisationview";
		} else if ("subOrganisation".equals(type)) {
			tableName = "suborganisationview";
		}
		return tableName;
	}

	private boolean isOrganisation(String type) {
		return ORGANISATION.equals(type) || "rootOrganisation".equals(type)
				|| "subOrganisation".equals(type) || "topOrganisation".equals(type);
	}

	private StorageReadResult readOrganisationList(String type, String tableName,
			DataGroup filter) {
		TableQuery tableQuery = sqlDatabaseFactory.factorTableQuery(tableName);
		tableQuery.addOrderByAsc("id");
		tableQuery.setFromNo(getAtomicValueAsLongIfExists(filter, "fromNo"));
		tableQuery.setToNo(getAtomicValueAsLongIfExists(filter, "toNo"));
		TableFacade tableFacade = sqlDatabaseFactory.factorTableFacade();
		List<Row> rowsFromDb = tableFacade.readRowsForQuery(tableQuery);

		List<DataGroup> convertedGroups = new ArrayList<>();
		for (Row map : rowsFromDb) {
			convertOrganisation(type, convertedGroups, map);
		}
		return createStorageReadResult(convertedGroups);
	}

	private Long getAtomicValueAsLongIfExists(DataGroup filter, String nameInData) {
		if (filter.containsChildWithNameInData(nameInData)) {
			return extractAtomicValueAsInteger(filter, nameInData);
		}
		return null;
	}

	private void convertOrganisation(String type, List<DataGroup> convertedGroups, Row map) {
		DataGroup convertedOrganisation = convertOneMapFromDbToDataGroup(type, map);
		String id = String.valueOf(map.getValueByColumn("id"));
		addParentsToOrganisation(convertedOrganisation, id);
		addPredecessorsToOrganisation(convertedOrganisation, id);
		convertedGroups.add(convertedOrganisation);
	}

	private void addParentsToOrganisation(DataGroup convertedOrganisation, String id) {
		MultipleRowDbToDataReader parentMultipleReader = divaDbFactory
				.factorMultipleReader("divaOrganisationParent");
		List<DataGroup> readParents = parentMultipleReader.read("divaOrganisationParent", id);
		for (DataGroup parent : readParents) {
			convertedOrganisation.addChild(parent);
		}
	}

	private void addPredecessorsToOrganisation(DataGroup convertedOrganisation, String id) {
		MultipleRowDbToDataReader predecessorReader = divaDbFactory
				.factorMultipleReader("divaOrganisationPredecessor");
		List<DataGroup> readPredecessors = predecessorReader.read("divaOrganisationPredecessor",
				id);
		for (DataGroup predecessor : readPredecessors) {
			convertedOrganisation.addChild(predecessor);
		}
	}

	private List<Row> readAllFromDb(String type) {
		TableQuery tableQuery = sqlDatabaseFactory.factorTableQuery(type);

		TableFacade factorTableFacade = sqlDatabaseFactory.factorTableFacade();
		return factorTableFacade.readRowsForQuery(tableQuery);
	}

	private StorageReadResult createStorageReadResult(List<DataGroup> listToReturn) {
		StorageReadResult storageReadResult = new StorageReadResult();
		storageReadResult.listOfDataGroups = listToReturn;
		return storageReadResult;
	}

	@Override
	public StorageReadResult readAbstractList(String type, DataGroup filter) {
		if ("user".equals(type)) {
			return readAndConvertUsers(type);
		}
		if (ORGANISATION.equals(type)) {
			return readOrganisationList(type, "organisationview", filter);
		} else {
			throw NotImplementedException.withMessage("readAbstractList is not implemented");
		}
	}

	private StorageReadResult readAndConvertUsers(String type) {
		List<Row> readAllFromDb = readAllFromDb("public.user");
		List<DataGroup> userDataGroups = convertDbResultToDataGroups(type, readAllFromDb);
		return createStorageReadResult(userDataGroups);
	}

	private List<DataGroup> convertDbResultToDataGroups(String type, List<Row> readAllFromDb) {
		List<DataGroup> userDataGroups = new ArrayList<>(readAllFromDb.size());
		for (Row rowFromDb : readAllFromDb) {
			DataGroup userDataGroup = convertRowToDataGroup(type, rowFromDb);
			userDataGroups.add(userDataGroup);
		}
		return userDataGroups;
	}

	private DataGroup convertRowToDataGroup(String type, Row rowFromDb) {
		DivaDbToCoraConverter converter = converterFactory.factor(type);
		return converter.fromRow(rowFromDb);
	}

	@Override
	public DataGroup readLinkList(String type, String id) {
		throw NotImplementedException.withMessage("readLinkList is not implemented");
	}

	@Override
	public Collection<DataGroup> generateLinkCollectionPointingToRecord(String type, String id) {
		throw NotImplementedException
				.withMessage("generateLinkCollectionPointingToRecord is not implemented");
	}

	@Override
	public boolean recordExistsForAbstractOrImplementingRecordTypeAndRecordId(String type,
			String id) {
		if (isOrganisation(type)) {
			return organisationExistsInDb(id, type);
		}
		throw NotImplementedException.withMessage(
				"recordExistsForAbstractOrImplementingRecordTypeAndRecordId is not implemented");
	}

	private boolean organisationExistsInDb(String id, String type) {
		try {
			String tableName = getTableName(type);
			tryToReadOrganisationFromDb(id, tableName);
			return true;
		} catch (RecordNotFoundException e) {
			return false;
		}
	}

	private void tryToReadOrganisationFromDb(String id, String tableName) {
		try {
			TableQuery tableQuery = sqlDatabaseFactory.factorTableQuery(tableName);
			tableQuery.addCondition("id", Integer.valueOf(id));
			TableFacade tableFacade = sqlDatabaseFactory.factorTableFacade();
			tableFacade.readOneRowForQuery(tableQuery);

		} catch (SqlDatabaseException | NumberFormatException e) {
			throw new RecordNotFoundException("Organisation not found: " + id);
		}
	}

	public DivaDbFactory getDivaDbToCoraFactory() {
		// needed for test
		return divaDbFactory;
	}

	public DivaDbUpdaterFactory getRecordStorageForOneTypeFactory() {
		// needed for test
		return divaDbUpdaterFactory;
	}

	public DivaDbToCoraConverterFactory getConverterFactory() {
		// needed for test
		return converterFactory;
	}

	@Override
	public long getTotalNumberOfRecordsForType(String type, DataGroup filter) {
		throwNotImplementedErrorIfNotOrganisation(type);

		String tableName = getTableName(type);
		TableQuery tableQuery = sqlDatabaseFactory.factorTableQuery(tableName);
		Long fromNo = getAtomicValueAsLongIfExists(filter, "fromNo");
		Long toNo = getAtomicValueAsLongIfExists(filter, "toNo");
		tableQuery.setFromNo(fromNo);
		tableQuery.setToNo(toNo);

		tableQuery.addOrderByAsc("id");

		TableFacade tableFacade = sqlDatabaseFactory.factorTableFacade();
		return tableFacade.readNumberOfRows(tableQuery);
	}

	private Long extractAtomicValueAsInteger(DataGroup filter, String nameInData) {
		String atomicValue = filter.getFirstAtomicValueWithNameInData(nameInData);
		return Long.valueOf(atomicValue);
	}

	private void throwNotImplementedErrorIfNotOrganisation(String type) {
		if (!isOrganisation(type)) {
			throw NotImplementedException.withMessage(
					"getTotalNumberOfRecordsForType is not implemented for type: " + type);
		}
	}

	@Override
	public long getTotalNumberOfRecordsForAbstractType(String abstractType,
			List<String> implementingTypes, DataGroup filter) {
		throwNotImplementedErrorIfNotOrganisation(abstractType);
		return getTotalNumberOfRecordsForType(abstractType, filter);
	}

	public SqlDatabaseFactory getSqlDatabaseFactory() {
		return sqlDatabaseFactory;
	}

}
