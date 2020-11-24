/*
 * Copyright 2020 Uppsala University Library
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

import se.uu.ub.cora.connection.SqlConnectionProvider;
import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.DivaDbOrganisationUpdater;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.RelatedTableFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.SqlConnectionProviderSpy;
import se.uu.ub.cora.diva.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.sqldatabase.DataReaderImp;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;

public class DivaDbUpdaterFactoryTest {

	private LoggerFactorySpy loggerFactorySpy = new LoggerFactorySpy();
	private DivaDbUpdaterFactory factory;
	private DataToDbTranslaterFactorySpy translaterFactory;
	private RelatedTableFactorySpy relatedTableFactory;
	private RecordReaderFactory recordReaderFactory;
	private SqlConnectionProvider sqlConnectionProvider;

	@BeforeMethod
	public void setUp() {
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		translaterFactory = new DataToDbTranslaterFactorySpy();
		relatedTableFactory = new RelatedTableFactorySpy();
		recordReaderFactory = new RecordReaderFactorySpy();
		sqlConnectionProvider = new SqlConnectionProviderSpy();
		factory = new DivaDbUpdaterFactoryImp(translaterFactory, recordReaderFactory,
				relatedTableFactory, sqlConnectionProvider);
	}

	@Test
	public void testFactorOrganisation() {
		assertCorrectFactoredUpdatedForOrganisationType("organisation");
	}

	private void assertCorrectFactoredUpdatedForOrganisationType(String type) {
		var divaDbOrganisationUpdater = (DivaDbOrganisationUpdater) factory.factor(type);
		assertSame(divaDbOrganisationUpdater.getDataToDbTranslater(),
				translaterFactory.factoredTranslater);
		assertSame(divaDbOrganisationUpdater.getRelatedTableFactory(), relatedTableFactory);
		assertSame(divaDbOrganisationUpdater.getRecordReaderFactory(), recordReaderFactory);
		assertSame(divaDbOrganisationUpdater.getSqlConnectionProvider(), sqlConnectionProvider);
		assertTrue(divaDbOrganisationUpdater
				.getPreparedStatementCreator() instanceof PreparedStatementExecutorImp);

		DataReaderImp dataReader = (DataReaderImp) divaDbOrganisationUpdater.getDataReader();
		assertSame(dataReader.getSqlConnectionProvider(), sqlConnectionProvider);
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
