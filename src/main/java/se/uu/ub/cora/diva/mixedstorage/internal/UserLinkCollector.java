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
package se.uu.ub.cora.diva.mixedstorage.internal;

import java.util.HashMap;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.RelatedLinkCollector;
import se.uu.ub.cora.storage.RecordStorage;

public class UserLinkCollector implements RelatedLinkCollector {

	private RecordStorage classicDbStorage;

	public UserLinkCollector(RecordStorage classicDbStorage) {
		this.classicDbStorage = classicDbStorage;
	}

	@Override
	public Map<String, Map<String, DataGroup>> collectLinks(DataGroup dataGroup) {
		DataGroup createdBy = dataGroup.getFirstGroupWithNameInData("createdBy");
		String id = createdBy.getFirstAtomicValueWithNameInData("linkedRecordId");
		DataGroup user = classicDbStorage.read("public.user", id);
		HashMap<String, DataGroup> userMap = new HashMap<>();
		userMap.put("", null);
		Map<String, Map<String, DataGroup>> links = new HashMap<>();
		links.put("user", userMap);
		return links;
	}

	RecordStorage getClassicDbStorage() {
		return classicDbStorage;
	}

}
