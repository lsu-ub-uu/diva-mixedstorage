package se.uu.ub.cora.diva.mixedstorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.searchstorage.SearchStorage;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.StorageReadResult;

public class RecordStorageSpy implements RecordStorage, SearchStorage {
	RecordStorageSpyData data = new RecordStorageSpyData();
	public String searchTermId;
	public DataGroup returnedSearchTerm = new DataGroupSpy("searchTerm");
	public String indexTermId;
	public DataGroup returnedIndexTerm = new DataGroupSpy("indexTerm");
	private String storageType;
	public StorageReadResult storageReadResult;

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

		DataGroup dummyDataGroup = new DataGroupSpy("DummyGroupFromRecordStorageSpy");
		data.answer = dummyDataGroup;
		return dummyDataGroup;
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
		if ("db".equals(storageType)) {
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

	@Override
	public boolean recordsExistForRecordType(String type) {
		data.type = type;
		data.calledMethod = "recordsExistForRecordType";
		data.answer = false;
		return false;
	}

	@Override
	public boolean recordExistsForAbstractOrImplementingRecordTypeAndRecordId(String type,
			String id) {
		data.type = type;
		data.id = id;
		data.calledMethod = "recordExistsForAbstractOrImplementingRecordTypeAndRecordId";
		data.answer = false;
		return false;
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

}
