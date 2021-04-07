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
	private static final String ORGANISATION = "organisation";
	private RecordStorage basicStorage;
	private RecordStorage divaFedoraStorage;
	private RecordStorage divaDbStorage;
	private RecordStorage userStorage;

	public static RecordStorage usingBasicFedoraAndDbStorageAndStorageFactory(
			RecordStorage basicStorage, RecordStorage divaFedoraStorage,
			RecordStorage divaDbStorage, RecordStorage userStorage) {
		return new DivaMixedRecordStorage(basicStorage, divaFedoraStorage, divaDbStorage,
				userStorage);
	}

	private DivaMixedRecordStorage(RecordStorage basicStorage, RecordStorage divaFedoraStorage,
			RecordStorage divaDbStorage, RecordStorage userStorage) {
		this.basicStorage = basicStorage;
		this.divaFedoraStorage = divaFedoraStorage;
		this.divaDbStorage = divaDbStorage;
		this.userStorage = userStorage;
	}

	@Override
	public DataGroup read(String type, String id) {
		if (PERSON.equals(type)) {
			return divaFedoraStorage.read(type, id);
		}
		if ("personDomainPart".equals(type)) {
			return divaFedoraStorage.read(type, id);
		}
		if (isOrganisation(type)) {
			return divaDbStorage.read(type, id);
		}
		if (USER.equals(type) || CORA_USER.equals(type)) {
			return handleUserStorage(type, id);
		}
		return basicStorage.read(type, id);
	}

	private boolean isOrganisation(String type) {
		return ORGANISATION.equals(type) || "rootOrganisation".equals(type)
				|| "topOrganisation".equals(type) || "subOrganisation".equals(type);
	}

	private DataGroup handleUserStorage(String type, String id) {
		try {
			return userStorage.read(type, id);
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
		} else if (isOrganisation(type)) {
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
		if ("personDomainPart".equals(type)) {
			return divaFedoraStorage.readList(type, filter);
		}
		if (isOrganisation(type)) {
			return divaDbStorage.readList(type, filter);
		}
		if (CORA_USER.equals(type)) {
			return readListOfUsersFromUserStorageAndBasicStorage(type, filter);
		}
		return basicStorage.readList(type, filter);
	}

	private StorageReadResult readListOfUsersFromUserStorageAndBasicStorage(String type,
			DataGroup filter) {
		StorageReadResult resultFromUserStorage = userStorage.readList(type, filter);
		StorageReadResult resultFromBasicStorage = basicStorage.readList(type, filter);

		addResultFromSecondResultToFirst(resultFromUserStorage, resultFromBasicStorage);
		return resultFromBasicStorage;
	}

	private void addResultFromSecondResultToFirst(StorageReadResult firstResult,
			StorageReadResult secondResult) {
		secondResult.listOfDataGroups.addAll(firstResult.listOfDataGroups);
		secondResult.totalNumberOfMatches += firstResult.totalNumberOfMatches;
	}

	@Override
	public StorageReadResult readAbstractList(String type, DataGroup filter) {
		if (USER.equals(type)) {
			return readAbstractListOfUsersFromUserStorageAndBasicStorage(type, filter);
		}
		if (ORGANISATION.equals(type)) {
			return divaDbStorage.readAbstractList(type, filter);
		}
		return basicStorage.readAbstractList(type, filter);
	}

	private StorageReadResult readAbstractListOfUsersFromUserStorageAndBasicStorage(String type,
			DataGroup filter) {
		StorageReadResult dbUsers = divaDbStorage.readAbstractList(type, filter);
		StorageReadResult basicStorageUsers = basicStorage.readAbstractList(type, filter);
		addResultFromSecondResultToFirst(basicStorageUsers, dbUsers);
		return dbUsers;
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
		if (isOrganisation(type)) {
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
		return userStorage.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(type, id);
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

	public RecordStorage getUserStorage() {
		// Needed for tests
		return userStorage;

	}
}
