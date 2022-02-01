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
package se.uu.ub.cora.diva.mixedstorage;

import static org.testng.Assert.assertSame;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.storage.RecordStorage;

public class DivaMixedDependenciesTest {

	private DivaMixedDependencies mixedDependencies;

	@BeforeMethod
	public void setUp() {
		mixedDependencies = new DivaMixedDependencies();
	}

	@Test
	public void testSetGetBasicStorage() {
		RecordStorage basicStorage = new RecordStorageSpy();
		mixedDependencies.setBasicStorage(basicStorage);
		assertSame(mixedDependencies.getBasicStorage(), basicStorage);
	}

	@Test
	public void testSetGetClassicDbStorage() {
		RecordStorage classicDbStorage = new RecordStorageSpy();
		mixedDependencies.setClassicDbStorage(classicDbStorage);
		assertSame(mixedDependencies.getClassicDbStorage(), classicDbStorage);
	}

	@Test
	public void testSetGetUserStorage() {
		RecordStorage userStorage = new RecordStorageSpy();
		mixedDependencies.setUserStorage(userStorage);
		assertSame(mixedDependencies.getUserStorage(), userStorage);
	}

	@Test
	public void testSetGetDatabaseStorage() {
		RecordStorage databaseStorage = new RecordStorageSpy();
		mixedDependencies.setDatabaseStorage(databaseStorage);
		assertSame(mixedDependencies.getDatabaseStorage(), databaseStorage);
	}

	// @Test
	// public void testSetGetClassicFedoraUpdaterFactory() {
	// ClassicFedoraUpdaterFactory fedoraUpdaterFactory = new ClassicFedoraUpdaterFactorySpy();
	// mixedDependencies.setClassicFedoraUpdaterFactory(fedoraUpdaterFactory);
	// assertSame(mixedDependencies.getClassicFedoraUpdaterFactory(), fedoraUpdaterFactory);
	// }
	//
	// @Test
	// public void testSetGetClassicIndexerFactory() {
	// ClassicIndexerFactory indexerFactory = new ClassicIndexerFactorySpy();
	// mixedDependencies.setClassicIndexerFactory(indexerFactory);
	// assertSame(mixedDependencies.getClassicIndexerFactory(), indexerFactory);
	// }
	//
	// @Test
	// public void testSetGetRelatedLinkCollectorFactory() {
	// RelatedLinkCollectorFactory relatedLinkCollectorFactory = new
	// RelatedLinkCollectorFactorySpy();
	// mixedDependencies.setRelatedLinkCollectorFactory(relatedLinkCollectorFactory);
	// assertSame(mixedDependencies.getRelatedLinkCollectorFactory(), relatedLinkCollectorFactory);
	// }
}
