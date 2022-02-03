/*
 * Copyright 2022 Uppsala University Library
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

import se.uu.ub.cora.storage.RecordStorage;

/**
 * DivaMixedDependencies holds all dependencies needed to store records in DiVA on Cora, and to
 * synchronize changes to DiVA classic
 */
public class DivaMixedDependencies {

	private RecordStorage basicStorage;
	private RecordStorage classicDbStorage;
	private RecordStorage userStorage;
	private RecordStorage databaseStorage;
	// private ClassicFedoraUpdaterFactory fedoraUpdaterFactory;
	// private ClassicIndexerFactory indexerFactory;
	// private RelatedLinkCollectorFactory relatedLinkCollectorFactory;

	public void setBasicStorage(RecordStorage basicStorage) {
		this.basicStorage = basicStorage;
	}

	public RecordStorage getBasicStorage() {
		return basicStorage;
	}

	public void setClassicDbStorage(RecordStorage classicDbStorage) {
		this.classicDbStorage = classicDbStorage;

	}

	public RecordStorage getClassicDbStorage() {
		return classicDbStorage;
	}

	public void setUserStorage(RecordStorage userStorage) {
		this.userStorage = userStorage;

	}

	public RecordStorage getUserStorage() {
		return userStorage;
	}

	public void setDatabaseStorage(RecordStorage databaseStorage) {
		this.databaseStorage = databaseStorage;

	}

	public RecordStorage getDatabaseStorage() {
		return databaseStorage;
	}

	// public void setClassicFedoraUpdaterFactory(ClassicFedoraUpdaterFactory fedoraUpdaterFactory)
	// {
	// this.fedoraUpdaterFactory = fedoraUpdaterFactory;
	// }
	//
	// public ClassicFedoraUpdaterFactory getClassicFedoraUpdaterFactory() {
	// return fedoraUpdaterFactory;
	// }
	//
	// public void setClassicIndexerFactory(ClassicIndexerFactory indexerFactory) {
	// this.indexerFactory = indexerFactory;
	//
	// }
	//
	// public ClassicIndexerFactory getClassicIndexerFactory() {
	// return indexerFactory;
	// }
	//
	// public void setRelatedLinkCollectorFactory(
	// RelatedLinkCollectorFactory relatedLinkCollectorFactory) {
	// this.relatedLinkCollectorFactory = relatedLinkCollectorFactory;
	//
	// }
	//
	// public RelatedLinkCollectorFactory getRelatedLinkCollectorFactory() {
	// return relatedLinkCollectorFactory;
	// }

}
