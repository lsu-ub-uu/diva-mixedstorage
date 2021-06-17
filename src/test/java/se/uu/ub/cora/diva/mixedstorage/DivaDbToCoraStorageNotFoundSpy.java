package se.uu.ub.cora.diva.mixedstorage;

import java.util.Collection;
import java.util.List;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.storage.RecordNotFoundException;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.StorageReadResult;

public class DivaDbToCoraStorageNotFoundSpy implements RecordStorage {

	public boolean readWasCalled = false;
	public String type;
	public String id;

	@Override
	public void create(String arg0, String arg1, DataGroup arg2, DataGroup arg3, DataGroup arg4,
			String arg5) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteByTypeAndId(String arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<DataGroup> generateLinkCollectionPointingToRecord(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean linksExistForRecord(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DataGroup read(String type, String id) {
		readWasCalled = true;
		throw new RecordNotFoundException("User not found: " + id);
	}

	@Override
	public StorageReadResult readAbstractList(String arg0, DataGroup arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataGroup readLinkList(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StorageReadResult readList(String arg0, DataGroup arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean recordExistsForAbstractOrImplementingRecordTypeAndRecordId(String type,
			String id) {
		this.type = type;
		this.id = id;
		// TODO Auto-generated method stub
		return false;
	}
	//
	// @Override
	// public boolean recordsExistForRecordType(String arg0) {
	// // TODO Auto-generated method stub
	// return false;
	// }

	@Override
	public void update(String arg0, String arg1, DataGroup arg2, DataGroup arg3, DataGroup arg4,
			String arg5) {
		// TODO Auto-generated method stub

	}

	@Override
	public long getTotalNumberOfRecordsForType(String type, DataGroup filter) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getTotalNumberOfRecordsForAbstractType(String abstractType,
			List<String> implementingTypes, DataGroup filter) {
		// TODO Auto-generated method stub
		return 0;
	}

}
