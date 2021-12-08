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

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;

public class RepeatableRelatedLinkCollectorTest {

	private RelatedLinkCollectorSpy linkCollector;

	@Test
	public void testCollectLinksEmptyListIn() {
		linkCollector = new RelatedLinkCollectorSpy();
		RepeatableRelatedLinkCollector repeatableCollector = new RepeatableRelatedLinkCollectorImp(
				linkCollector);

		List<DataGroup> linksAsDataGroups = repeatableCollector
				.collectLinks(Collections.emptyList());

		assertEquals(linkCollector.linksSentIn.size(), 0);
		assertEquals(linksAsDataGroups.size(), 0);

	}

	@Test
	public void testCollectLinksNoReturnedGroupsFromCollector() {
		linkCollector = new RelatedLinkCollectorSpy();
		linkCollector.idsForDataGroupsToReturnForIndex = new HashMap<>();
		RepeatableRelatedLinkCollector repeatableCollector = new RepeatableRelatedLinkCollectorImp(
				linkCollector);

		List<DataGroup> groupsContainingLinks = createListOfLinks();
		List<DataGroup> linksAsDataGroups = repeatableCollector.collectLinks(groupsContainingLinks);

		assertEquals(linkCollector.linksSentIn.size(), 3);
		assertEquals(linksAsDataGroups.size(), 0);

	}

	@Test
	public void testCollectLinks() {
		linkCollector = new RelatedLinkCollectorSpy();
		createResponseToReturnFromSpy();

		RepeatableRelatedLinkCollector repeatableCollector = new RepeatableRelatedLinkCollectorImp(
				linkCollector);

		List<DataGroup> groupsContainingLinks = createListOfLinks();
		List<DataGroup> linksAsDataGroups = repeatableCollector.collectLinks(groupsContainingLinks);

		assertEquals(linkCollector.linksSentIn.size(), 3);
		assertEquals(linksAsDataGroups.size(), 7);

	}

	private List<DataGroup> createListOfLinks() {
		List<DataGroup> groupsContainingLinks = new ArrayList<>();
		addDataGroup(groupsContainingLinks);
		addDataGroup(groupsContainingLinks);
		addDataGroup(groupsContainingLinks);
		return groupsContainingLinks;
	}

	private void addDataGroup(List<DataGroup> groupsContainingLinks) {
		DataGroupSpy dataGroupSpy = new DataGroupSpy("personDomainPart");
		groupsContainingLinks.add(dataGroupSpy);
	}

	private void createResponseToReturnFromSpy() {
		Map<Integer, List<String>> dataGroupsToReturnFromSpy = new HashMap<>();
		dataGroupsToReturnFromSpy.put(0, Arrays.asList("56", "45", "34"));
		dataGroupsToReturnFromSpy.put(1, Arrays.asList("156", "145", "134"));
		dataGroupsToReturnFromSpy.put(2, Arrays.asList("56", "45", "234"));
		linkCollector.idsForDataGroupsToReturnForIndex = dataGroupsToReturnFromSpy;
	}
}
