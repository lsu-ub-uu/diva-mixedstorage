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
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.RelatedLinkCollector;
import se.uu.ub.cora.gatekeeper.user.UserStorage;

public class UserLinkInRecordInfoCollector implements RelatedLinkCollector {

	private UserStorage userStorage;

	public UserLinkInRecordInfoCollector(UserStorage divaMixedUserStorage) {
		this.userStorage = divaMixedUserStorage;
	}

	@Override
	public Map<String, Map<String, DataGroup>> collectLinks(DataGroup recordInfo) {
		HashMap<String, DataGroup> userMap = new HashMap<>();
		addCreatedBy(recordInfo, userMap);
		addUpdatedBy(recordInfo, userMap);
		return createMapIncludingUserMap(userMap);
	}

	private void addCreatedBy(DataGroup dataGroup, HashMap<String, DataGroup> userMap) {
		if (dataGroup.containsChildWithNameInData("createdBy")) {
			DataGroup createdBy = dataGroup.getFirstGroupWithNameInData("createdBy");
			readUserAndAddToMap(userMap, createdBy);
		}
	}

	private void readUserAndAddToMap(HashMap<String, DataGroup> userMap, DataGroup userLink) {
		String id = userLink.getFirstAtomicValueWithNameInData("linkedRecordId");
		DataGroup user = userStorage.getUserByIdFromLogin(id);
		userMap.put(id, user);
	}

	private void addUpdatedBy(DataGroup dataGroup, HashMap<String, DataGroup> userMap) {
		List<DataGroup> updatedGroups = dataGroup.getAllGroupsWithNameInData("updated");
		for (DataGroup updated : updatedGroups) {
			DataGroup updatedBy = updated.getFirstGroupWithNameInData("updatedBy");
			readUserAndAddToMap(userMap, updatedBy);
		}
	}

	private Map<String, Map<String, DataGroup>> createMapIncludingUserMap(
			HashMap<String, DataGroup> userMap) {
		Map<String, Map<String, DataGroup>> links = new HashMap<>();
		if (!userMap.isEmpty()) {
			links.put("user", userMap);
		}
		return links;
	}

	UserStorage getUserStorage() {
		return userStorage;
	}

}
