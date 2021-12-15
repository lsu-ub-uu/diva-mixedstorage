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
import static org.testng.Assert.assertSame;

import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.db.DbStorageSpy;

public class DomainPartOrganisationCollectorTest {

	private DbStorageSpy coraDbStorage;
	private DbStorageSpy classicDbStorage;
	private DomainPartOrganisationCollector collector;

	@BeforeMethod
	public void setUp() {
		coraDbStorage = new DbStorageSpy();
		classicDbStorage = new DbStorageSpy();
		collector = new DomainPartOrganisationCollector(coraDbStorage, classicDbStorage);
	}

	@Test
	public void testInit() {
		assertSame(collector.getDbStorage(), coraDbStorage);
	}

	@Test
	public void testOneOrganisationInDomainPartNoParent() {
		DataGroupSpy personDomainPartLink = createPersonDomainPartLink("authority-person:111:test");
		DataGroupSpy personDomainPart = setUpDefaultDataGroup();
		coraDbStorage.dataGroupsToReturn.put("personDomainPart_authority-person:111:test",
				personDomainPart);

		Map<String, DataGroup> links = collector.collectLinks(personDomainPartLink);
		assertEquals(coraDbStorage.ids.get(0), "authority-person:111:test");
		assertEquals(coraDbStorage.types.get(0), "personDomainPart");
		assertCorrectReadOrganisation(0, "56");

		DataGroup returnedOrganisation = classicDbStorage.returnedDataGroups.get(0);
		assertSame(links.get("56"), returnedOrganisation);
		assertSame(links.get("authority-person:111:test"), coraDbStorage.returnedDataGroups.get(0));

	}

	private DataGroupSpy createPersonDomainPartLink(String linkedRecordId) {
		DataGroupSpy personDomainPartLink = new DataGroupSpy("personDomainPart");
		personDomainPartLink.addChild(new DataAtomicSpy("linkedRecordType", "personDomainPart"));
		personDomainPartLink.addChild(new DataAtomicSpy("linkedRecordId", linkedRecordId));
		return personDomainPartLink;
	}

	private DataGroupSpy setUpDefaultDataGroup() {
		DataGroupSpy personDomainPart = new DataGroupSpy("personDomainPart");
		DataGroupSpy affiliation = createAffiliation("56");
		personDomainPart.addChild(affiliation);
		return personDomainPart;
	}

	private DataGroupSpy createAffiliation(String orgId) {
		DataGroupSpy affiliation = new DataGroupSpy("affiliation");
		DataGroupSpy orgLink = new DataGroupSpy("organisationLink");
		orgLink.addChild(new DataAtomicSpy("linkedRecordId", orgId));
		affiliation.addChild(orgLink);
		return affiliation;
	}

	@Test
	public void testOneOrganisationInDomainPartWithParentAndGrandParent() {
		DataGroupSpy personDomainPartLink = createPersonDomainPartLink("authority-person:111:test");
		DataGroupSpy personDomainPart = setUpDefaultDataGroup();
		coraDbStorage.dataGroupsToReturn.put("personDomainPart_authority-person:111:test",
				personDomainPart);

		setUpDbStorgageToReturnParentAndGrandParent();

		Map<String, DataGroup> links = collector.collectLinks(personDomainPartLink);
		assertEquals(coraDbStorage.ids.get(0), "authority-person:111:test");
		assertEquals(coraDbStorage.types.get(0), "personDomainPart");

		assertCorrectReadOrganisation(0, "56");
		assertCorrectReadOrganisation(1, "156");
		assertCorrectReadOrganisation(2, "256");

		List<DataGroup> returnedDataGroups = classicDbStorage.returnedDataGroups;
		assertSame(links.get("56"), returnedDataGroups.get(0));
		assertSame(links.get("156"), classicDbStorage.returnedDataGroups.get(1));
		assertSame(links.get("256"), classicDbStorage.returnedDataGroups.get(2));
		assertSame(links.get("authority-person:111:test"), coraDbStorage.returnedDataGroups.get(0));
	}

	private void assertCorrectReadOrganisation(int index, String organisationId) {
		assertEquals(classicDbStorage.ids.get(index), organisationId);
		assertEquals(classicDbStorage.types.get(index), "organisation");
	}

	private void setUpDbStorgageToReturnParentAndGrandParent() {
		createOrganisationWithParentAndPutInSpy("organisation_56", "156");
		createOrganisationWithParentAndPutInSpy("organisation_156", "256");

	}

	private void createOrganisationWithParentAndPutInSpy(String key, String linkedRecordId) {
		DataGroupSpy orgToReturnFromStorage = new DataGroupSpy("organisation");
		DataGroupSpy parentLink = createParentOrganisationLink(linkedRecordId);
		orgToReturnFromStorage.addChild(parentLink);
		classicDbStorage.dataGroupsToReturn.put(key, orgToReturnFromStorage);
	}

	private DataGroupSpy createParentOrganisationLink(String linkedRecordId) {
		DataGroupSpy parentOrg = new DataGroupSpy("parentOrganisation");
		DataGroupSpy organisationLink = new DataGroupSpy("organisationLink");
		organisationLink.addChild(new DataAtomicSpy("linkedRecordId", linkedRecordId));
		parentOrg.addChild(organisationLink);
		return parentOrg;
	}

	@Test
	public void testSameParentLinkOnlyAddedOnce() {
		DataGroupSpy personDomainPartLink = createPersonDomainPartLink("authority-person:111:test");
		DataGroupSpy personDomainPart = setUpDefaultDataGroup();
		coraDbStorage.dataGroupsToReturn.put("personDomainPart_authority-person:111:test",
				personDomainPart);

		DataGroupSpy affiliation2 = createAffiliation("57");
		personDomainPart.addChild(affiliation2);

		createOrganisationWithParentAndPutInSpy("organisation_56", "156");
		createOrganisationWithParentAndPutInSpy("organisation_57", "156");

		Map<String, DataGroup> links = collector.collectLinks(personDomainPartLink);
		assertEquals(links.size(), 4);
		assertCorrectReadOrganisation(0, "56");
		assertCorrectReadOrganisation(1, "156");
		assertCorrectReadOrganisation(2, "57");
		assertCorrectReadOrganisation(3, "156");

		List<DataGroup> returnedDataGroups = classicDbStorage.returnedDataGroups;
		assertSame(links.get("56"), returnedDataGroups.get(0));
		assertSame(links.get("57"), classicDbStorage.returnedDataGroups.get(2));
		assertSame(links.get("156"), classicDbStorage.returnedDataGroups.get(3));
		assertSame(links.get("authority-person:111:test"), coraDbStorage.returnedDataGroups.get(0));
	}
}
