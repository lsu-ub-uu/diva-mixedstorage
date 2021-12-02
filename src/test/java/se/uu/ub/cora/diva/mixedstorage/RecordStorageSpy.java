/*
 * Copyright 2020 Uppsala University Library
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.searchstorage.SearchStorage;
import se.uu.ub.cora.storage.RecordNotFoundException;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.StorageReadResult;

public class RecordStorageSpy implements RecordStorage, SearchStorage {
	public RecordStorageSpyData data = new RecordStorageSpyData();
	public String searchTermId;
	public DataGroup returnedSearchTerm = new DataGroupSpy("searchTerm");
	public String indexTermId;
	public DataGroup returnedIndexTerm = new DataGroupSpy("indexTerm");
	private String storageType;
	public StorageReadResult storageReadResult;
	public boolean linkExistsInStorage = false;
	public boolean existsInStorage = true;
	public List<String> implementingTypes;
	public Map<String, Object> answerToReturn = new HashMap<>();

	public RecordStorageSpy() {
		this.storageType = "basicStorage";
	}

	public RecordStorageSpy(String storageType) {
		this.storageType = storageType;
	}

	@Override
	public DataGroup read(String type, String id) {
		data.type = type;
		data.id = id;
		data.calledMethod = "read";

		if (!existsInStorage) {
			throw new RecordNotFoundException("error from spy");
		}
		if (answerToReturn.containsKey(type + "_" + id)) {
			data.answer = answerToReturn.get(type + "_" + id);
		} else {
			DataGroup dummyDataGroup = new DataGroupSpy("DummyGroupFromRecordStorageSpy");
			data.answer = dummyDataGroup;
		}

		return (DataGroup) data.answer;
	}

	@Override
	public void create(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		data.type = type;
		data.id = id;
		data.record = record;
		data.collectedTerms = collectedTerms;
		data.linkList = linkList;
		data.dataDivider = dataDivider;
		data.calledMethod = "create";

	}

	@Override
	public void deleteByTypeAndId(String type, String id) {
		data.type = type;
		data.id = id;
		data.calledMethod = "deleteByTypeAndId";
	}

	@Override
	public boolean linksExistForRecord(String type, String id) {
		data.type = type;
		data.id = id;
		data.calledMethod = "linksExistForRecord";
		data.answer = false;
		return false;
	}

	@Override
	public void update(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		data.type = type;
		data.id = id;
		data.record = record;
		data.collectedTerms = collectedTerms;
		data.linkList = linkList;
		data.dataDivider = dataDivider;
		data.calledMethod = "update";
	}

	@Override
	public StorageReadResult readList(String type, DataGroup filter) {
		data.type = type;
		data.filter = filter;
		data.calledMethod = "readList";
		List<DataGroup> readList = new ArrayList<>();
		if ("db".equals(storageType) || "userStorage".equals(storageType)) {
			DataGroup dummyDataGroup = new DataGroupSpy("DummyGroupFromDbRecordStorageSpy");
			readList.add(dummyDataGroup);
		}
		DataGroup dummyDataGroup = new DataGroupSpy("DummyGroupFromRecordStorageSpy");
		readList.add(dummyDataGroup);
		data.answer = readList;
		storageReadResult = new StorageReadResult();
		storageReadResult.totalNumberOfMatches = readList.size();
		storageReadResult.listOfDataGroups = readList;
		return storageReadResult;
	}

	@Override
	public StorageReadResult readAbstractList(String type, DataGroup filter) {
		data.type = type;
		data.filter = filter;
		data.calledMethod = "readAbstractList";
		List<DataGroup> readList = new ArrayList<>();
		DataGroup dummyDataGroup = new DataGroupSpy("DummyGroupFromRecordStorageSpy");
		readList.add(dummyDataGroup);
		data.answer = readList;
		StorageReadResult storageReadResult = new StorageReadResult();
		storageReadResult.listOfDataGroups = readList;
		storageReadResult.totalNumberOfMatches = readList.size();
		return storageReadResult;
	}

	@Override
	public DataGroup readLinkList(String type, String id) {
		data.type = type;
		data.id = id;
		data.calledMethod = "readLinkList";

		DataGroup dummyDataGroup = new DataGroupSpy("DummyGroupFromRecordStorageSpy");
		data.answer = dummyDataGroup;
		return dummyDataGroup;
	}

	@Override
	public Collection<DataGroup> generateLinkCollectionPointingToRecord(String type, String id) {
		data.type = type;
		data.id = id;
		data.calledMethod = "generateLinkCollectionPointingToRecord";
		Collection<DataGroup> generatedList = new ArrayList<>();
		DataGroup dummyDataGroup = new DataGroupSpy("DummyGroupFromRecordStorageSpy");
		generatedList.add(dummyDataGroup);
		data.answer = generatedList;
		return generatedList;
	}
	//
	// @Override
	// public boolean recordsExistForRecordType(String type) {
	// data.type = type;
	// data.calledMethod = "recordsExistForRecordType";
	// data.answer = false;
	// return false;
	// }

	@Override
	public boolean recordExistsForAbstractOrImplementingRecordTypeAndRecordId(String type,
			String id) {
		data.type = type;
		data.id = id;
		data.calledMethod = "recordExistsForAbstractOrImplementingRecordTypeAndRecordId";
		data.answer = linkExistsInStorage;
		return linkExistsInStorage;
	}

	@Override
	public DataGroup getSearchTerm(String searchTermId) {
		this.searchTermId = searchTermId;
		return returnedSearchTerm;
	}

	@Override
	public DataGroup getCollectIndexTerm(String collectIndexTermId) {
		indexTermId = collectIndexTermId;
		return returnedIndexTerm;
	}

	@Override
	public long getTotalNumberOfRecordsForType(String type, DataGroup filter) {
		data.type = type;
		data.filter = filter;
		data.calledMethod = "getTotalNumberOfRecordsForType";

		data.answer = 234L;
		return (long) data.answer;
	}

	@Override
	public long getTotalNumberOfRecordsForAbstractType(String abstractType,
			List<String> implementingTypes, DataGroup filter) {
		data.type = abstractType;
		this.implementingTypes = implementingTypes;
		data.filter = filter;
		data.calledMethod = "getTotalNumberOfRecordsForAbstractType";
		data.answer = 567L;
		return (long) data.answer;
	}

}
