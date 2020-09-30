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
package se.uu.ub.cora.diva.mixedstorage;

import java.util.Collection;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.searchstorage.SearchStorage;
import se.uu.ub.cora.storage.RecordNotFoundException;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.StorageReadResult;

public final class DivaMixedRecordStorage implements RecordStorage, SearchStorage {

	private static final String USER = "user";
	private static final String CORA_USER = "coraUser";
	private static final String PERSON = "person";
	private static final String ORGANISATION = "divaOrganisation";
	private RecordStorage basicStorage;
	private RecordStorage divaFedoraStorage;
	private RecordStorage divaDbStorage;
	private DivaStorageFactory storageFactory;

	public static RecordStorage usingBasicFedoraAndDbStorageAndStorageFactory(
			RecordStorage basicStorage, RecordStorage divaFedoraStorage,
			RecordStorage divaDbStorage, DivaStorageFactory storageFactory) {
		return new DivaMixedRecordStorage(basicStorage, divaFedoraStorage, divaDbStorage,
				storageFactory);
	}

	private DivaMixedRecordStorage(RecordStorage basicStorage, RecordStorage divaFedoraStorage,
			RecordStorage divaDbStorage, DivaStorageFactory storageFactory) {
		this.basicStorage = basicStorage;
		this.divaFedoraStorage = divaFedoraStorage;
		this.divaDbStorage = divaDbStorage;
		this.storageFactory = storageFactory;
	}

	@Override
	public DataGroup read(String type, String id) {
		if (PERSON.equals(type)) {
			return divaFedoraStorage.read(type, id);
		}
		if (ORGANISATION.equals(type)) {
			return divaDbStorage.read(type, id);
		}
		if (USER.equals(type) || CORA_USER.equals(type)) {
			return handleUser(type, id);
		}
		return basicStorage.read(type, id);
	}

	private DataGroup handleUser(String type, String id) {
		try {
			RecordStorage userRecordStorage = storageFactory.factorForRecordType(type);
			return userRecordStorage.read(type, id);
		} catch (RecordNotFoundException e) {
			// do nothing, we keep looking in basicstorage
		}
		return basicStorage.read(type, id);
	}

	@Override
	public void create(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		basicStorage.create(type, id, record, collectedTerms, linkList, dataDivider);
	}

	@Override
	public void deleteByTypeAndId(String type, String id) {
		basicStorage.deleteByTypeAndId(type, id);
	}

	@Override
	public boolean linksExistForRecord(String type, String id) {
		return basicStorage.linksExistForRecord(type, id);
	}

	@Override
	public void update(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		if (PERSON.equals(type)) {
			divaFedoraStorage.update(type, id, record, collectedTerms, linkList, dataDivider);
		} else if (ORGANISATION.equals(type)) {
			divaDbStorage.update(type, id, record, collectedTerms, linkList, dataDivider);
		} else {
			basicStorage.update(type, id, record, collectedTerms, linkList, dataDivider);
		}
	}

	@Override
	public StorageReadResult readList(String type, DataGroup filter) {
		if (PERSON.equals(type)) {
			return divaFedoraStorage.readList(type, filter);
		}
		if (ORGANISATION.equals(type)) {
			return divaDbStorage.readList(type, filter);
		}
		if (CORA_USER.equals(type)) {
			return readListOfUsersFromDbAndBasicStorage(type, filter);
		}
		return basicStorage.readList(type, filter);
	}

	private StorageReadResult readListOfUsersFromDbAndBasicStorage(String type, DataGroup filter) {
		StorageReadResult resultFromDataBase = divaDbStorage.readList(type, filter);
		StorageReadResult resultFromBasicStorage = basicStorage.readList(type, filter);

		addResultFromDbToBasicStorageResult(resultFromDataBase, resultFromBasicStorage);
		return resultFromBasicStorage;
	}

	private void addResultFromDbToBasicStorageResult(StorageReadResult resultFromDataBase,
			StorageReadResult resultFromBasicStorage) {
		resultFromBasicStorage.listOfDataGroups.addAll(resultFromDataBase.listOfDataGroups);
		resultFromBasicStorage.totalNumberOfMatches += resultFromDataBase.totalNumberOfMatches;
	}

	@Override
	public StorageReadResult readAbstractList(String type, DataGroup filter) {
		if (USER.equals(type)) {
			return divaDbStorage.readAbstractList(type, filter);
		}
		return basicStorage.readAbstractList(type, filter);
	}

	@Override
	public DataGroup readLinkList(String type, String id) {
		return basicStorage.readLinkList(type, id);
	}

	@Override
	public Collection<DataGroup> generateLinkCollectionPointingToRecord(String type, String id) {
		return basicStorage.generateLinkCollectionPointingToRecord(type, id);
	}

	@Override
	public boolean recordsExistForRecordType(String type) {
		return basicStorage.recordsExistForRecordType(type);
	}

	@Override
	public boolean recordExistsForAbstractOrImplementingRecordTypeAndRecordId(String type,
			String id) {
		if (ORGANISATION.equals(type)) {
			return linkExistInDbStorage(type, id);
		}
		if (USER.equals(type) || CORA_USER.equals(type)) {
			return linkExistInUserStorage(type, id) || linkExistInBasicStorage(type, id);
		}
		return linkExistInBasicStorage(type, id);
	}

	private boolean linkExistInDbStorage(String type, String id) {
		return divaDbStorage.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(type, id);
	}

	private boolean linkExistInUserStorage(String type, String id) {
		RecordStorage userRecordStorage = storageFactory.factorForRecordType(type);
		return userRecordStorage.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(type,
				id);
	}

	private boolean linkExistInBasicStorage(String type, String id) {
		return basicStorage.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(type, id);
	}

	RecordStorage getBasicStorage() {
		// needed for test
		return basicStorage;
	}

	RecordStorage getFedoraStorage() {
		// needed for test
		return divaFedoraStorage;
	}

	RecordStorage getDbStorage() {
		// needed for test
		return divaDbStorage;
	}

	@Override
	public DataGroup getSearchTerm(String searchTermId) {
		return ((SearchStorage) basicStorage).getSearchTerm(searchTermId);
	}

	@Override
	public DataGroup getCollectIndexTerm(String collectIndexTermId) {
		return ((SearchStorage) basicStorage).getCollectIndexTerm(collectIndexTermId);
	}

	public DivaStorageFactory getStorageFactory() {
		return storageFactory;
	}
}
