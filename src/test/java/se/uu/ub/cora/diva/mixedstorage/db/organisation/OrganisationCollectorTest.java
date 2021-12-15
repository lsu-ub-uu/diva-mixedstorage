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

import static org.testng.Assert.assertSame;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.db.DbStorageSpy;

public class OrganisationCollectorTest {

	private DbStorageSpy dbStorage;
	private OrganisationCollector collector;

	@BeforeMethod
	public void setUp() {
		dbStorage = new DbStorageSpy();
		collector = new OrganisationCollector(dbStorage);
	}

	@Test
	public void testInit() {
		assertSame(collector.getDbStorage(), dbStorage);
	}

	@Test
	public void testOneOrganisationNoParent() {
		DataGroupSpy organisationLink = new DataGroupSpy("organisationLink");
		organisationLink.addChild(new DataAtomicSpy("linkedRecordType", "organisation"));
		organisationLink.addChild(new DataAtomicSpy("linkedRecordId", "someOrganisation"));
		//
		// Map<String, DataGroup> links = collector.collectLinks(organisationLink);
		// assertEquals(dbStorage.ids.get(0), "someOrganisation");
		// assertEquals(dbStorage.types.get(0), "personDomainPart");
		// assertCorrectReadOrganisation(0, "56");
		//
		// DataGroup returnedOrganisation = classicDbStorage.returnedDataGroups.get(0);
		// assertSame(links.get("56"), returnedOrganisation);
		// assertSame(links.get("authority-person:111:test"), dbStorage.returnedDataGroups.get(0));

	}

}
