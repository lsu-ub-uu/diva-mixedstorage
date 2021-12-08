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

	private DbStorageSpy dbStorage;
	private DomainPartOrganisationCollector collector;

	@BeforeMethod
	public void setUp() {
		dbStorage = new DbStorageSpy();
		collector = new DomainPartOrganisationCollector(dbStorage);
	}

	@Test
	public void testInit() {
		assertSame(collector.getDbStorage(), dbStorage);
	}

	@Test
	public void testOneOrganisationInDomainPartNoParent() {
		DataGroupSpy personDomainPart = setUpDefaultDataGroup();

		Map<String, DataGroup> links = collector.collectLinks(personDomainPart);
		assertCorrectReadOrganisation(0, "56");

		DataGroup returnedOrganisation = dbStorage.returnedDataGroups.get(0);
		assertSame(links.get("56"), returnedOrganisation);

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
		DataGroupSpy personDomainPart = setUpDefaultDataGroup();

		setUpDbStorgageToReturnParentAndGrandParent();

		Map<String, DataGroup> links = collector.collectLinks(personDomainPart);
		assertCorrectReadOrganisation(0, "56");
		assertCorrectReadOrganisation(1, "156");
		assertCorrectReadOrganisation(2, "256");

		List<DataGroup> returnedDataGroups = dbStorage.returnedDataGroups;
		assertSame(links.get("56"), returnedDataGroups.get(0));
		assertSame(links.get("156"), dbStorage.returnedDataGroups.get(1));
		assertSame(links.get("256"), dbStorage.returnedDataGroups.get(2));

	}

	private void assertCorrectReadOrganisation(int index, String organisationId) {
		assertEquals(dbStorage.ids.get(index), organisationId);
		assertEquals(dbStorage.types.get(index), "organisation");
	}

	private void setUpDbStorgageToReturnParentAndGrandParent() {
		createOrganisationWithParentAndPutInSpy("organisation_56", "156");
		createOrganisationWithParentAndPutInSpy("organisation_156", "256");

	}

	private void createOrganisationWithParentAndPutInSpy(String key, String linkedRecordId) {
		DataGroupSpy orgToReturnFromStorage = new DataGroupSpy("organisation");
		DataGroupSpy parentLink = createParentOrganisationLink(linkedRecordId);
		orgToReturnFromStorage.addChild(parentLink);
		dbStorage.dataGroupsToReturn.put(key, orgToReturnFromStorage);
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
		DataGroupSpy personDomainPart = setUpDefaultDataGroup();
		DataGroupSpy affiliation2 = createAffiliation("57");
		personDomainPart.addChild(affiliation2);

		createOrganisationWithParentAndPutInSpy("organisation_56", "156");
		createOrganisationWithParentAndPutInSpy("organisation_57", "156");

		Map<String, DataGroup> links = collector.collectLinks(personDomainPart);
		assertEquals(links.size(), 3);
		assertCorrectReadOrganisation(0, "56");
		assertCorrectReadOrganisation(1, "156");
		assertCorrectReadOrganisation(2, "57");
		assertCorrectReadOrganisation(3, "156");

		List<DataGroup> returnedDataGroups = dbStorage.returnedDataGroups;
		assertSame(links.get("56"), returnedDataGroups.get(0));
		assertSame(links.get("57"), dbStorage.returnedDataGroups.get(2));
		assertSame(links.get("156"), dbStorage.returnedDataGroups.get(3));

	}
}
