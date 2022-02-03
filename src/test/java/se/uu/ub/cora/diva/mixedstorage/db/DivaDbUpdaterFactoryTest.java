/*
 * Copyright 2020, 2021 Uppsala University Library
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
package se.uu.ub.cora.diva.mixedstorage.db;

import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.DivaDbOrganisationUpdater;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.SqlDatabaseFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.internal.RelatedTableFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.logger.LoggerProvider;

public class DivaDbUpdaterFactoryTest {

	private LoggerFactorySpy loggerFactorySpy = new LoggerFactorySpy();
	private DivaDbUpdaterFactory factory;
	private DataToDbTranslaterFactorySpy translaterFactory;
	private RelatedTableFactorySpy relatedTableFactory;
	private SqlDatabaseFactorySpy sqlDatabaseFactory;

	@BeforeMethod
	public void setUp() {
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		translaterFactory = new DataToDbTranslaterFactorySpy();
		relatedTableFactory = new RelatedTableFactorySpy();
		sqlDatabaseFactory = new SqlDatabaseFactorySpy();
		factory = new DivaDbUpdaterFactoryImp(translaterFactory, sqlDatabaseFactory,
				relatedTableFactory);
	}

	@Test
	public void testFactorOrganisation() {
		var factoredUpdater = (DivaDbOrganisationUpdater) factory.factor("organisation");
		assertSame(factoredUpdater.getDataToDbTranslater(), translaterFactory.factoredTranslater);
		assertSame(factoredUpdater.getRelatedTableFactory(), relatedTableFactory);
		assertSame(factoredUpdater.getSqlDatabaseFactory(), sqlDatabaseFactory);
		assertTrue(factoredUpdater
				.getPreparedStatementCreator() instanceof PreparedStatementExecutorImp);
	}

	private void assertCorrectFactoredUpdatedForOrganisationType(String type) {
		var divaDbOrganisationUpdater = (DivaDbOrganisationUpdater) factory.factor(type);
		assertSame(divaDbOrganisationUpdater.getDataToDbTranslater(),
				translaterFactory.factoredTranslater);
		assertSame(divaDbOrganisationUpdater.getRelatedTableFactory(), relatedTableFactory);
		assertTrue(divaDbOrganisationUpdater
				.getPreparedStatementCreator() instanceof PreparedStatementExecutorImp);
	}

	@Test
	public void testFactorRootOrganisation() {
		assertCorrectFactoredUpdatedForOrganisationType("rootOrganisation");
	}

	@Test
	public void testFactorTopOrganisation() {
		assertCorrectFactoredUpdatedForOrganisationType("topOrganisation");
	}

	@Test
	public void testFactorSubOrganisation() {
		assertCorrectFactoredUpdatedForOrganisationType("subOrganisation");
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "Updater not implemented for someNonExistingUpdaterType")
	public void testNotImplemented() {
		factory.factor("someNonExistingUpdaterType");
	}
}
