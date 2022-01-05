/*
 * Copyright 2021 Uppsala University Library
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
package se.uu.ub.cora.diva.mixedstorage.internal;

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.RelatedLinkCollector;
import se.uu.ub.cora.diva.mixedstorage.RelatedLinkCollectorFactory;
import se.uu.ub.cora.storage.RecordStorage;

public class RelatedLinkCollectorFactoryImp implements RelatedLinkCollectorFactory {

	private RecordStorage recordStorage;
	private RecordStorage classicDbStorage;

	public RelatedLinkCollectorFactoryImp(RecordStorage recordStorage,
			RecordStorage classicDbStorage) {
		this.recordStorage = recordStorage;
		this.classicDbStorage = classicDbStorage;
	}

	@Override
	public RelatedLinkCollector factor(String type) {
		if ("personDomainPart".equals(type)) {
			return new DomainPartOrganisationCollector(recordStorage, classicDbStorage);
		}
		throw NotImplementedException.withMessage("Factor not implemented for type otherType");
	}

	public RecordStorage getRecordStorage() {
		return recordStorage;
	}

	public RecordStorage getClassicDbStorage() {
		return classicDbStorage;
	}

}