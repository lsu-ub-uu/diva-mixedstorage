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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.RelatedLinkCollector;

public class RelatedLinkCollectorSpy implements RelatedLinkCollector {

	public List<DataGroup> linksSentIn = new ArrayList<>();
	public Map<Integer, List<String>> idsForDataGroupsToReturnForIndex = new HashMap<>();
	public Map<Integer, Map<String, Map<String, DataGroup>>> mapsToReturn = new HashMap<>();
	private int callIndex = 0;

	@Override
	public Map<String, Map<String, DataGroup>> collectLinks(DataGroup personDomainPart) {
		linksSentIn.add(personDomainPart);
		Map<String, Map<String, DataGroup>> mapToReturn = mapsToReturn.get(callIndex);
		callIndex++;
		return mapToReturn;

		// Map<String, DataGroup> dataGroupsToReturn = new HashMap<>();
		// if (idsForDataGroupsToReturnForIndex.containsKey(callIndex)) {
		// List<String> idList = idsForDataGroupsToReturnForIndex.get(callIndex);
		// for (String id : idList) {
		// dataGroupsToReturn.put(id, new DataGroupSpy("organisation"));
		// }
		// }

		// return dataGroupsToReturn;
	}

}
