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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.RepeatableRelatedLinkCollector;
import se.uu.ub.cora.diva.mixedstorage.internal.RepeatableRelatedLinkCollectorImp;

public class RepeatableRelatedLinkCollectorTest {

	private RelatedLinkCollectorFactorySpy relatedlinkCollectorFactory;
	private RepeatableRelatedLinkCollector repeatableCollector;
	private Map<Integer, Map<String, Map<String, DataGroup>>> answerFromSpy;
	private Map<String, Map<String, DataGroup>> firstAnswer;
	private Map<String, Map<String, DataGroup>> secondAnswer;
	private Map<String, Map<String, DataGroup>> thirdAnswer;

	@BeforeMethod
	public void setUp() {
		relatedlinkCollectorFactory = new RelatedLinkCollectorFactorySpy();
		setUpAnswerFromSpy();
		repeatableCollector = new RepeatableRelatedLinkCollectorImp(relatedlinkCollectorFactory);
	}

	private void setUpAnswerFromSpy() {
		answerFromSpy = new HashMap<>();

		firstAnswer = new HashMap<>();
		Map<String, DataGroup> personDomainPartMap = new HashMap<>();
		personDomainPartMap.put("personDomainPart0", new DataGroupSpy("personDomainPart0"));
		firstAnswer.put("personDomainPart", personDomainPartMap);
		answerFromSpy.put(0, firstAnswer);

		secondAnswer = new HashMap<>();
		Map<String, DataGroup> personDomainPartMap2 = new HashMap<>();
		personDomainPartMap2.put("personDomainPart1", new DataGroupSpy("personDomainPart1"));
		secondAnswer.put("personDomainPart", personDomainPartMap2);
		setUpOrganisations(secondAnswer, "56", "156", "256");
		answerFromSpy.put(1, secondAnswer);

		thirdAnswer = new HashMap<>();
		Map<String, DataGroup> personDomainPartMap3 = new HashMap<>();
		personDomainPartMap3.put("personDomainPart2", new DataGroupSpy("personDomainPart2"));
		thirdAnswer.put("personDomainPart", personDomainPartMap3);
		setUpOrganisations(thirdAnswer, "256", "356", "456");
		answerFromSpy.put(2, thirdAnswer);

		relatedlinkCollectorFactory.mapsToReturnFromCollectorSpy = answerFromSpy;
	}

	private void setUpOrganisations(Map<String, Map<String, DataGroup>> answer, String... ids) {
		Map<String, DataGroup> organisations = new HashMap<>();
		for (String id : ids) {
			organisations.put(id, new DataGroupSpy("organisation"));
		}
		answer.put("organisation", organisations);
	}

	@Test
	public void testCollectLinksEmptyListIn() {
		Map<String, List<DataGroup>> linksAsDataGroups = repeatableCollector
				.collectLinks(Collections.emptyList());

		assertTrue(linksAsDataGroups.isEmpty());
	}

	@Test
	public void testCollectLinksOnlyPersonDomainPartReturnedFromCollector() {
		relatedlinkCollectorFactory.idsForDataGroupsToReturnForIndex = new HashMap<>();

		Map<String, List<DataGroup>> linksAsDataGroups = repeatableCollector
				.collectLinks(createListOfLinks(1));

		assertEquals(relatedlinkCollectorFactory.type, "personDomainPart");

		RelatedLinkCollectorSpy linkCollector = relatedlinkCollectorFactory.returnedLinkCollector;
		assertEquals(linkCollector.linksSentIn.size(), 1);

		assertEquals(linksAsDataGroups.get("personDomainParts").size(), 1);
		assertSame(linksAsDataGroups.get("personDomainParts").get(0),
				firstAnswer.get("personDomainPart").get("personDomainPart0"));
		assertNull(linksAsDataGroups.get("organisation"));

	}

	@Test
	public void testCollectLinksTwoRecordTypes() {
		List<DataGroup> groupsContainingLinks = createListOfLinks(2);

		Map<String, List<DataGroup>> linksAsDataGroups = repeatableCollector
				.collectLinks(groupsContainingLinks);

		RelatedLinkCollectorSpy linkCollector = relatedlinkCollectorFactory.returnedLinkCollector;
		assertEquals(linkCollector.linksSentIn.size(), 2);
		List<DataGroup> personDomainParts = linksAsDataGroups.get("personDomainParts");
		assertEquals(personDomainParts.size(), 2);

		assertSame(personDomainParts.get(0),
				firstAnswer.get("personDomainPart").get("personDomainPart0"));

		assertSame(personDomainParts.get(1),
				secondAnswer.get("personDomainPart").get("personDomainPart1"));

		assertCorrectOrganisationsFromSecondDataGroup(linksAsDataGroups);

	}

	private void assertCorrectOrganisationsFromSecondDataGroup(
			Map<String, List<DataGroup>> linksAsDataGroups) {
		List<DataGroup> organisationList = linksAsDataGroups.get("organisations");
		assertEquals(organisationList.size(), 3);
		assertSame(organisationList.get(0), secondAnswer.get("organisation").get("56"));
		assertSame(organisationList.get(1), secondAnswer.get("organisation").get("156"));
		assertSame(organisationList.get(2), secondAnswer.get("organisation").get("256"));
	}

	private List<DataGroup> createListOfLinks(int numOfLinks) {
		List<DataGroup> groupsContainingLinks = new ArrayList<>();
		for (int i = 0; i < numOfLinks; i++) {
			addDataGroup(groupsContainingLinks);
		}
		return groupsContainingLinks;
	}

	private void addDataGroup(List<DataGroup> groupsContainingLinks) {
		DataGroupSpy dataGroupSpy = new DataGroupSpy("personDomainPart");
		groupsContainingLinks.add(dataGroupSpy);
	}

	@Test
	public void testCollectLinksDuplicateOrganisations() {
		List<DataGroup> groupsContainingLinks = createListOfLinks(3);

		Map<String, List<DataGroup>> linksAsDataGroups = repeatableCollector
				.collectLinks(groupsContainingLinks);

		RelatedLinkCollectorSpy linkCollector = relatedlinkCollectorFactory.returnedLinkCollector;
		assertEquals(linkCollector.linksSentIn.size(), 3);
		List<DataGroup> personDomainPartList = linksAsDataGroups.get("personDomainParts");
		assertEquals(personDomainPartList.size(), 3);

		assertTrue(personDomainPartList
				.contains(firstAnswer.get("personDomainPart").get("personDomainPart0")));
		assertTrue(personDomainPartList
				.contains(secondAnswer.get("personDomainPart").get("personDomainPart1")));
		assertTrue(personDomainPartList
				.contains(thirdAnswer.get("personDomainPart").get("personDomainPart2")));

		assertOrganisationsDoesNotIncludeDuplicateOrganisation(linksAsDataGroups);

	}

	private void assertOrganisationsDoesNotIncludeDuplicateOrganisation(
			Map<String, List<DataGroup>> linksAsDataGroups) {
		List<DataGroup> organisationList = linksAsDataGroups.get("organisations");
		assertEquals(organisationList.size(), 5);

		assertSame(organisationList.get(0), secondAnswer.get("organisation").get("56"));
		assertSame(organisationList.get(1), secondAnswer.get("organisation").get("156"));

		assertSame(organisationList.get(2), thirdAnswer.get("organisation").get("256"));

		assertSame(organisationList.get(3), thirdAnswer.get("organisation").get("356"));
		assertSame(organisationList.get(4), thirdAnswer.get("organisation").get("456"));
	}

}
