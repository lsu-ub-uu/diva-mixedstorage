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
package se.uu.ub.cora.diva.mixedstorage.classic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.classic.RepeatableRelatedLinkCollector;

public class RepeatableLinkCollectorSpy implements RepeatableRelatedLinkCollector {

	public List<DataGroup> groupsContainingLinks = new ArrayList<>();
	public int numberOfGroupsToReturn = 0;
	public Map<String, List<DataGroup>> mapToReturn = new HashMap<>();

	@Override
	public Map<String, List<DataGroup>> collectLinks(List<DataGroup> groupsContainingLinks) {
		this.groupsContainingLinks = groupsContainingLinks;

		return mapToReturn;
	}

}
