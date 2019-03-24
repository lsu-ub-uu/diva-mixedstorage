/*
 * Copyright 2018, 2019 Uppsala University Library
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

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.spider.data.SpiderReadResult;
import se.uu.ub.cora.spider.record.storage.RecordStorage;
import se.uu.ub.cora.sqldatabase.RecordReader;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;
import se.uu.ub.cora.sqldatabase.RecordUpdater;
import se.uu.ub.cora.sqldatabase.RecordUpdaterFactory;

public class DivaDbRecordStorage implements RecordStorage {

	private static final String DIVA_ORGANISATION = "divaOrganisation";
	private RecordReaderFactory recordReaderFactory;
	private DivaDbToCoraConverterFactory converterFactory;
	private DivaDbToCoraFactory divaDbToCoraFactory;
	private RecordUpdaterFactory recordUpdaterFactory;
	private CoraToDbConverterFactory toDbConverterFactory;

	private DivaDbRecordStorage(RecordReaderFactory recordReaderFactory,
			RecordUpdaterFactory recordUpdaterFactory,
			DivaDbToCoraConverterFactory converterFactory, DivaDbToCoraFactory divaDbToCoraFactory,
			CoraToDbConverterFactory toDbConverterFactory) {
		this.recordReaderFactory = recordReaderFactory;
		this.recordUpdaterFactory = recordUpdaterFactory;
		this.converterFactory = converterFactory;
		this.divaDbToCoraFactory = divaDbToCoraFactory;
		this.toDbConverterFactory = toDbConverterFactory;
	}

	public static DivaDbRecordStorage usingFactories(RecordReaderFactory recordReaderFactory,
			RecordUpdaterFactory recordUpdaterFactory,
			DivaDbToCoraConverterFactory converterFactory, DivaDbToCoraFactory divaDbToCoraFactory,
			CoraToDbConverterFactory toDbConverterFactory) {
		return new DivaDbRecordStorage(recordReaderFactory, recordUpdaterFactory, converterFactory,
				divaDbToCoraFactory, toDbConverterFactory);
	}

	@Override
	public DataGroup read(String type, String id) {
		if (DIVA_ORGANISATION.equals(type)) {
			DivaDbToCora divaDbToCora = divaDbToCoraFactory.factor(type);
			return divaDbToCora.readAndConvertOneRow(type, id);
		}
		throw NotImplementedException.withMessage("read is not implemented for type: " + type);
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
		if (DIVA_ORGANISATION.equals(type)) {
			PreparedStatementInfo psInfo = convertRecord(type, record);
			updateRecord(psInfo);
		} else {
			throw NotImplementedException.withMessage("update is not implemented");
		}
	}

	private void updateRecord(PreparedStatementInfo psInfo) {
		RecordUpdater recordUpdater = recordUpdaterFactory.factor();
		recordUpdater.update(psInfo.tableName, psInfo.values, psInfo.conditions);
	}

	private PreparedStatementInfo convertRecord(String type, DataGroup record) {
		CoraToDbConverter coraToDbOrganisationConverter = toDbConverterFactory.factor(type);
		return coraToDbOrganisationConverter.convert(record);
	}

	@Override
	public SpiderReadResult readList(String type, DataGroup filter) {
		if (DIVA_ORGANISATION.equals(type)) {
			List<Map<String, String>> rowsFromDb = readAllFromDb(type);
			return createSpiderReadResultFromDbData(type, rowsFromDb);
		}
		throw NotImplementedException.withMessage("readList is not implemented for type: " + type);
	}

	private List<Map<String, String>> readAllFromDb(String type) {
		RecordReader recordReader = recordReaderFactory.factor();
		return recordReader.readAllFromTable(type);
	}

	private SpiderReadResult createSpiderReadResultFromDbData(String type,
			List<Map<String, String>> rowsFromDb) {
		SpiderReadResult spiderReadResult = new SpiderReadResult();
		spiderReadResult.listOfDataGroups = convertListOfMapsFromDbToDataGroups(type, rowsFromDb);
		return spiderReadResult;
	}

	private List<DataGroup> convertListOfMapsFromDbToDataGroups(String type,
			List<Map<String, String>> readAllFromTable) {
		List<DataGroup> convertedList = new ArrayList<>();
		for (Map<String, String> map : readAllFromTable) {
			DataGroup convertedGroup = convertOneMapFromDbToDataGroup(type, map);
			convertedList.add(convertedGroup);
		}
		return convertedList;
	}

	private DataGroup convertOneMapFromDbToDataGroup(String type, Map<String, String> readRow) {
		DivaDbToCoraConverter dbToCoraConverter = converterFactory.factor(type);
		return dbToCoraConverter.fromMap(readRow);
	}

	@Override
	public SpiderReadResult readAbstractList(String type, DataGroup filter) {
		throw NotImplementedException.withMessage("readAbstractList is not implemented");
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
	public boolean recordsExistForRecordType(String type) {
		throw NotImplementedException.withMessage("recordsExistForRecordType is not implemented");
	}

	@Override
	public boolean recordExistsForAbstractOrImplementingRecordTypeAndRecordId(String type,
			String id) {
		if (DIVA_ORGANISATION.equals(type)) {
			return organisationWithIdExistInDatabase(id);
		} else {
			throw NotImplementedException.withMessage(
					"recordExistsForAbstractOrImplementingRecordTypeAndRecordId is not implemented");
		}
	}

	private boolean organisationWithIdExistInDatabase(String id) {
		Map<String, Object> conditions = createConditions(id);
		RecordReader reader = recordReaderFactory.factor();
		Map<String, String> readRow = reader.readOneRowFromDbUsingTableAndConditions("organisation",
				conditions);
		return recordExist(readRow);
	}

	private boolean recordExist(Map<String, String> readRow) {
		return !readRow.isEmpty();
	}

	private Map<String, Object> createConditions(String id) {
		Map<String, Object> conditions = new HashMap<>();
		conditions.put("organisation_id", Integer.valueOf(id));
		return conditions;
	}

}
