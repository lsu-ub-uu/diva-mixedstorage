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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.MultipleRowDbToDataReader;
import se.uu.ub.cora.sqldatabase.RecordReader;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;
import se.uu.ub.cora.sqldatabase.ResultDelimiter;
import se.uu.ub.cora.sqldatabase.SqlStorageException;
import se.uu.ub.cora.storage.RecordNotFoundException;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.StorageReadResult;

public class DivaDbRecordStorage implements RecordStorage {

	private static final String ORGANISATION = "organisation";
	private RecordReaderFactory recordReaderFactory;
	private DivaDbFactory divaDbFactory;
	private DivaDbUpdaterFactory divaDbUpdaterFactory;
	private DivaDbToCoraConverterFactory converterFactory;

	private DivaDbRecordStorage(RecordReaderFactory recordReaderFactory,
			DivaDbFactory divaDbReaderFactory, DivaDbUpdaterFactory divaDbUpdaterFactory,
			DivaDbToCoraConverterFactory converterFactory) {
		this.recordReaderFactory = recordReaderFactory;
		this.divaDbFactory = divaDbReaderFactory;
		this.divaDbUpdaterFactory = divaDbUpdaterFactory;
		this.converterFactory = converterFactory;

	}

	public static DivaDbRecordStorage usingRecordReaderFactoryDivaFactoryAndDivaDbUpdaterFactory(
			RecordReaderFactory recordReaderFactory, DivaDbFactory divaDbFactory,
			DivaDbUpdaterFactory divaDbUpdaterFactory,
			DivaDbToCoraConverterFactory converterFactory) {
		return new DivaDbRecordStorage(recordReaderFactory, divaDbFactory, divaDbUpdaterFactory,
				converterFactory);
	}

	@Override
	public DataGroup read(String type, String id) {
		DivaDbReader divaDbReader = divaDbFactory.factor(type);
		return divaDbReader.read(type, id);
	}

	@Override
	public void create(String type, String id, DataGroup record, DataGroup collectedTerms,
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
	public void update(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		DivaDbUpdater divaDbUpdater = divaDbUpdaterFactory.factor(type);
		divaDbUpdater.update(record);
	}

	private Map<String, Object> createConditionsAddingOrganisationId(String id) {
		throwDbExceptionIfIdNotAnIntegerValue(id);
		Map<String, Object> conditions = new HashMap<>(1);
		conditions.put("id", Integer.valueOf(id));
		return conditions;
	}

	private void throwDbExceptionIfIdNotAnIntegerValue(String id) {
		try {
			Integer.valueOf(id);
		} catch (NumberFormatException ne) {
			throw DbException.withMessageAndException("Record not found: " + id, ne);
		}
	}

	private DataGroup convertOneMapFromDbToDataGroup(String type, Map<String, Object> readRow) {
		DivaDbToCoraConverter dbToCoraConverter = converterFactory.factor(type);
		return dbToCoraConverter.fromMap(readRow);
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

	//
	// {
	// "name": "filter",
	// "children": [
	// {
	// "name": "part",
	// "children": [
	// {
	// "name": "key",
	// "value": "domain"
	// },
	// {
	// "name": "value",
	// "value": "uu"
	// }
	// ],
	// "repeatId": "0"
	// },
	// {
	// "name": "part",
	// "children": [
	// {
	// "name": "key",
	// "value": "start"
	// },
	// {
	// "name": "value",
	// "value": "0"
	// }
	// ],
	// "repeatId": "1"
	// },{
	// "name": "part",
	// "children": [
	// {
	// "name": "key",
	// "value": "end"
	// },
	// {
	// "name": "value",
	// "value": "200"
	// }
	// ],
	// "repeatId": "2"
	// }
	// ]
	// }
	private StorageReadResult readOrganisationList(String type, String tableName,
			DataGroup filter) {
		ResultDelimiter resultDelimiter = createResultDelimiter(filter);

		RecordReader recordReader = recordReaderFactory.factor();
		List<Map<String, Object>> rowsFromDb = recordReader.readAllFromTable(tableName,
				resultDelimiter);

		List<DataGroup> convertedGroups = new ArrayList<>();
		for (Map<String, Object> map : rowsFromDb) {
			convertOrganisation(type, convertedGroups, map);
		}
		return createStorageReadResult(convertedGroups);
	}

	/**** Should all of this be calculated somewhere else?? *****/
	private ResultDelimiter createResultDelimiter(DataGroup filter) {
		Integer fromNum = calculateFromNum(filter);
		Integer toNum = calculateToNum(filter);

		Integer limit = calculateLimit(fromNum, toNum);
		Integer offset = fromNum != 0 ? fromNum - 1 : null;
		return new ResultDelimiter(limit, offset);
	}

	private Integer calculateFromNum(DataGroup filter) {
		if (filter.containsChildWithNameInData("fromNo")) {
			return getAtomicValueAsInteger(filter, "fromNo");
		}
		return 0;
	}

	private Integer getAtomicValueAsInteger(DataGroup filter, String nameInData) {
		String atomicValue = filter.getFirstAtomicValueWithNameInData(nameInData);
		return Integer.valueOf(atomicValue);
	}

	private Integer calculateToNum(DataGroup filter) {
		if (filter.containsChildWithNameInData("toNo")) {
			return getAtomicValueAsInteger(filter, "toNo");
		}
		return null;
	}

	private Integer calculateLimit(Integer fromNum, Integer toNum) {
		if (toNum != null) {
			return fromNum != 0 ? fromToDifferencePlusOne(fromNum, toNum) : toNum;
		}
		return null;
	}

	private int fromToDifferencePlusOne(Integer fromNum, Integer toNum) {
		return (toNum - fromNum) + 1;
	}

	/*******************************************************/

	private void convertOrganisation(String type, List<DataGroup> convertedGroups,
			Map<String, Object> map) {
		DataGroup convertedOrganisation = convertOneMapFromDbToDataGroup(type, map);
		String id = String.valueOf(map.get("id"));
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

	private List<Map<String, Object>> readAllFromDb(String type) {
		RecordReader recordReader = recordReaderFactory.factor();
		return recordReader.readAllFromTable(type);
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
		List<Map<String, Object>> readAllFromDb = readAllFromDb("public.user");
		List<DataGroup> userDataGroups = convertDbResultToDataGroups(type, readAllFromDb);
		return createStorageReadResult(userDataGroups);
	}

	private List<DataGroup> convertDbResultToDataGroups(String type,
			List<Map<String, Object>> readAllFromDb) {
		List<DataGroup> userDataGroups = new ArrayList<>(readAllFromDb.size());
		for (Map<String, Object> rowFromDb : readAllFromDb) {
			DataGroup userDataGroup = convertRowToDataGroup(type, rowFromDb);
			userDataGroups.add(userDataGroup);
		}
		return userDataGroups;
	}

	private DataGroup convertRowToDataGroup(String type, Map<String, Object> rowFromDb) {
		DivaDbToCoraConverter converter = converterFactory.factor(type);
		return converter.fromMap(rowFromDb);
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

	private Map<String, Object> tryToReadOrganisationFromDb(String id, String tableName) {
		try {
			RecordReader recordReader = recordReaderFactory.factor();
			Map<String, Object> conditions = createConditionsAddingOrganisationId(id);
			return recordReader.readOneRowFromDbUsingTableAndConditions(tableName, conditions);
		} catch (SqlStorageException | DbException e) {
			throw new RecordNotFoundException("Organisation not found: " + id);
		}
	}

	public RecordReaderFactory getRecordReaderFactory() {
		// needed for test
		return recordReaderFactory;
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
		RecordReader recordReader = recordReaderFactory.factor();
		// filter is not implemented yet -- need to decide how filter should be structured
		Map<String, Object> conditions = new HashMap<>();
		String tableName = getTableName(type);

		return recordReader.readNumberOfRows(tableName, conditions);
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

}
