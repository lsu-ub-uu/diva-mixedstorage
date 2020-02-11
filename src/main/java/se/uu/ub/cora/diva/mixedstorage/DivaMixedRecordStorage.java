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
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.StorageReadResult;

public final class DivaMixedRecordStorage implements RecordStorage, SearchStorage {

	private static final String PERSON = "person";
	private static final String ORGANISATION = "divaOrganisation";
	private RecordStorage basicStorage;
	private RecordStorage divaFedoraStorage;
	private RecordStorage divaDbStorage;

	public static RecordStorage usingBasicAndFedoraAndDbStorage(RecordStorage basicStorage,
			RecordStorage divaFedoraStorage, RecordStorage divaDbStorage) {
		return new DivaMixedRecordStorage(basicStorage, divaFedoraStorage, divaDbStorage);
	}

	private DivaMixedRecordStorage(RecordStorage basicStorage,
			RecordStorage divaFedoraStorage, RecordStorage divaDbStorage) {
		this.basicStorage = basicStorage;
		this.divaFedoraStorage = divaFedoraStorage;
		this.divaDbStorage = divaDbStorage;
	}

	@Override
	public DataGroup read(String type, String id) {
		if (PERSON.equals(type)) {
			return divaFedoraStorage.read(type, id);
		}
		if (ORGANISATION.equals(type)) {
			return divaDbStorage.read(type, id);
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
		return basicStorage.readList(type, filter);
	}

	@Override
	public StorageReadResult readAbstractList(String type, DataGroup filter) {
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
			return divaDbStorage
					.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(type, id);
		}
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
}
