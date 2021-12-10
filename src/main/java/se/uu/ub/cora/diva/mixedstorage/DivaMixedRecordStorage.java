/*
 * Copyright 2018, 2019, 2021 Uppsala University Library
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
import java.util.List;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.fedora.ClassicFedoraUpdater;
import se.uu.ub.cora.diva.mixedstorage.fedora.ClassicFedoraUpdaterFactory;
import se.uu.ub.cora.searchstorage.SearchStorage;
import se.uu.ub.cora.storage.RecordNotFoundException;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.StorageReadResult;

public final class DivaMixedRecordStorage implements RecordStorage, SearchStorage {

	private static final String PERSON_DOMAIN_PART = "personDomainPart";
	private static final String USER = "user";
	private static final String CORA_USER = "coraUser";
	private static final String PERSON = "person";
	private static final String ORGANISATION = "organisation";
	private RecordStorage basicStorage;
	private RecordStorage divaClassicDbStorage;
	private RecordStorage userStorage;
	private RecordStorage databaseStorage;
	private ClassicFedoraUpdaterFactory fedoraUpdaterFactory;

	public static RecordStorage usingBasicStorageClassicDbStorageUserStorageAndDatabaseStorage(
			RecordStorage basicStorage, RecordStorage divaDbStorage, RecordStorage userStorage,
			RecordStorage databaseStorage, ClassicFedoraUpdaterFactory fedoraUpdaterFactory) {
		return new DivaMixedRecordStorage(basicStorage, divaDbStorage, userStorage, databaseStorage,
				fedoraUpdaterFactory);
	}

	private DivaMixedRecordStorage(RecordStorage basicStorage, RecordStorage divaDbStorage,
			RecordStorage userStorage, RecordStorage databaseStorage,
			ClassicFedoraUpdaterFactory fedoraUpdatedFactory) {
		this.basicStorage = basicStorage;
		this.divaClassicDbStorage = divaDbStorage;
		this.userStorage = userStorage;
		this.databaseStorage = databaseStorage;
		this.fedoraUpdaterFactory = fedoraUpdatedFactory;
	}

	@Override
	public DataGroup read(String type, String id) {
		if (PERSON.equals(type)) {
			return databaseStorage.read(type, id);
		}
		if (PERSON_DOMAIN_PART.equals(type)) {
			return databaseStorage.read(type, id);
		}
		if (isOrganisation(type)) {
			return divaClassicDbStorage.read(type, id);
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
	public void create(String type, String id, DataGroup dataRecord, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		basicStorage.create(type, id, dataRecord, collectedTerms, linkList, dataDivider);
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
	public void update(String type, String id, DataGroup dataRecord, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		if (PERSON.equals(type)) {
			databaseStorage.update(type, id, dataRecord, collectedTerms, linkList, dataDivider);
			ClassicFedoraUpdater fedoraUpdater = fedoraUpdaterFactory.factor(PERSON);
			fedoraUpdater.updateInFedora(type, id, dataRecord);

		} else if (isOrganisation(type)) {
			divaClassicDbStorage.update(type, id, dataRecord, collectedTerms, linkList,
					dataDivider);
		} else {
			basicStorage.update(type, id, dataRecord, collectedTerms, linkList, dataDivider);
		}
	}

	@Override
	public StorageReadResult readList(String type, DataGroup filter) {
		if (PERSON.equals(type)) {
			return databaseStorage.readList(type, filter);
		}
		if (PERSON_DOMAIN_PART.equals(type)) {
			return databaseStorage.readList(type, filter);
		}
		if (isOrganisation(type)) {
			return divaClassicDbStorage.readList(type, filter);
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
			return divaClassicDbStorage.readAbstractList(type, filter);
		}
		return basicStorage.readAbstractList(type, filter);
	}

	private StorageReadResult readAbstractListOfUsersFromUserStorageAndBasicStorage(String type,
			DataGroup filter) {
		StorageReadResult dbUsers = divaClassicDbStorage.readAbstractList(type, filter);
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
		return divaClassicDbStorage.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(type,
				id);
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

	RecordStorage getClassicDbStorage() {
		// needed for test
		return divaClassicDbStorage;
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

	@Override
	public long getTotalNumberOfRecordsForType(String type, DataGroup filter) {
		if (PERSON.equals(type)) {
			return databaseStorage.getTotalNumberOfRecordsForType(type, filter);
		}
		if (PERSON_DOMAIN_PART.equals(type)) {
			return databaseStorage.getTotalNumberOfRecordsForType(type, filter);
		}
		if (isOrganisation(type)) {
			return divaClassicDbStorage.getTotalNumberOfRecordsForType(type, filter);
		}
		return basicStorage.getTotalNumberOfRecordsForType(type, filter);
	}

	@Override
	public long getTotalNumberOfRecordsForAbstractType(String abstractType,
			List<String> implementingTypes, DataGroup filter) {
		if (isOrganisation(abstractType)) {
			return divaClassicDbStorage.getTotalNumberOfRecordsForAbstractType(abstractType,
					implementingTypes, filter);
		}
		return basicStorage.getTotalNumberOfRecordsForAbstractType(abstractType, implementingTypes,
				filter);
	}

	public RecordStorage getDatabaseStorage() {
		return databaseStorage;
	}

	ClassicFedoraUpdaterFactory getFedoraUpdaterFactory() {
		return fedoraUpdaterFactory;
	}
}
