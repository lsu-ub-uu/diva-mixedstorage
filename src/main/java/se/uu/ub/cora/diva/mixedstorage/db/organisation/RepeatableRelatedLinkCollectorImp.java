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
package se.uu.ub.cora.diva.mixedstorage.db.organisation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import se.uu.ub.cora.data.DataGroup;

public class RepeatableRelatedLinkCollectorImp implements RepeatableRelatedLinkCollector {

	private RelatedLinkCollectorFactory relatedlinkCollectorFactory;

	public RepeatableRelatedLinkCollectorImp(
			RelatedLinkCollectorFactory relatedlinkCollectorFactory) {
		this.relatedlinkCollectorFactory = relatedlinkCollectorFactory;
	}

	@Override
	public List<DataGroup> collectLinks(List<DataGroup> groupsContainingLinks) {
		Map<String, DataGroup> combinedCollectedLinks = collectDistinctiveLinksAsDataGroups(
				groupsContainingLinks);
		return putDataGroupsInList(combinedCollectedLinks);
	}

	private Map<String, DataGroup> collectDistinctiveLinksAsDataGroups(
			List<DataGroup> groupsContainingLinks) {
		RelatedLinkCollector linkCollector = factorLinkCollector();
		Map<String, DataGroup> combinedCollectedLinks = new HashMap<>();
		for (DataGroup dataGroup : groupsContainingLinks) {
			Map<String, DataGroup> collectedLinks = linkCollector.collectLinks(dataGroup);
			combinedCollectedLinks.putAll(collectedLinks);
		}
		return combinedCollectedLinks;
	}

	private RelatedLinkCollector factorLinkCollector() {
		return relatedlinkCollectorFactory.factor("personDomainPart");
	}

	private List<DataGroup> putDataGroupsInList(Map<String, DataGroup> combinedCollectedLinks) {
		List<DataGroup> linksAsDataGroup = new ArrayList<>();
		for (Entry<String, DataGroup> entry : combinedCollectedLinks.entrySet()) {
			linksAsDataGroup.add(entry.getValue());
		}
		return linksAsDataGroup;
	}

}
