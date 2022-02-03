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
import java.util.Map.Entry;

import se.uu.ub.cora.data.DataGroup;

public class RepeatableRelatedLinkCollectorImp implements RepeatableRelatedLinkCollector {

	private RelatedLinkCollectorFactory relatedlinkCollectorFactory;

	public RepeatableRelatedLinkCollectorImp(
			RelatedLinkCollectorFactory relatedlinkCollectorFactory) {
		this.relatedlinkCollectorFactory = relatedlinkCollectorFactory;
	}

	@Override
	public Map<String, List<DataGroup>> collectLinks(List<DataGroup> groupsContainingLinks) {
		Map<String, Map<String, DataGroup>> combinedCollectedLinks = collectDistinctiveLinksAsDataGroups(
				groupsContainingLinks);
		return putDataGroupsInList(combinedCollectedLinks);
	}

	private Map<String, Map<String, DataGroup>> collectDistinctiveLinksAsDataGroups(
			List<DataGroup> groupsContainingLinks) {
		Map<String, Map<String, DataGroup>> collectedLinks = new HashMap<>();
		RelatedLinkCollector linkCollector = factorLinkCollector();
		for (DataGroup dataGroup : groupsContainingLinks) {
			collectLinksFromDataGroup(collectedLinks, linkCollector, dataGroup);
		}
		return collectedLinks;
	}

	private void collectLinksFromDataGroup(
			Map<String, Map<String, DataGroup>> combinedCollectedLinks,
			RelatedLinkCollector linkCollector, DataGroup dataGroup) {
		Map<String, Map<String, DataGroup>> collectedLinks = linkCollector.collectLinks(dataGroup);
		for (Entry<String, Map<String, DataGroup>> entry : collectedLinks.entrySet()) {
			String recordTypeInPlural = getRecordTypeInPlural(entry);
			addMapForKeyIfMissing(combinedCollectedLinks, recordTypeInPlural);
			combinedCollectedLinks.get(recordTypeInPlural).putAll(entry.getValue());
		}
	}

	private String getRecordTypeInPlural(Entry<String, Map<String, DataGroup>> entry) {
		String recordType = entry.getKey();
		return recordType + "s";
	}

	private void addMapForKeyIfMissing(Map<String, Map<String, DataGroup>> combinedCollectedLinks,
			String recordType) {
		combinedCollectedLinks.computeIfAbsent(recordType, key -> new HashMap<>());
	}

	private RelatedLinkCollector factorLinkCollector() {
		return relatedlinkCollectorFactory.factor("personDomainPart");
	}

	private Map<String, List<DataGroup>> putDataGroupsInList(
			Map<String, Map<String, DataGroup>> combinedCollectedLinks) {
		Map<String, List<DataGroup>> result = new HashMap<>();

		for (Entry<String, Map<String, DataGroup>> entry : combinedCollectedLinks.entrySet()) {
			String recordType = entry.getKey();
			addListForKeyIfMissing(result, recordType);
			addAllDataGroupsForCurrentRecordType(result, entry.getValue(), recordType);
		}
		return result;
	}

	private void addAllDataGroupsForCurrentRecordType(Map<String, List<DataGroup>> result,
			Map<String, DataGroup> dataGroupMapWithIdAsKey, String recordType) {

		for (Entry<String, DataGroup> entry : dataGroupMapWithIdAsKey.entrySet()) {
			result.get(recordType).add(entry.getValue());
		}
	}

	private void addListForKeyIfMissing(Map<String, List<DataGroup>> result, String recordType) {
		result.computeIfAbsent(recordType, key -> new ArrayList<>());
	}

	public RelatedLinkCollectorFactory getRelatedLinkCollectorFactory() {
		return relatedlinkCollectorFactory;
	}

}
