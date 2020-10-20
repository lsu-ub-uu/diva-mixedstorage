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
package se.uu.ub.cora.diva.mixedstorage.db.organisation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.db.ConnectionSpy;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbTranslaterSpy;
import se.uu.ub.cora.diva.mixedstorage.db.DbStatement;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbUpdater;
import se.uu.ub.cora.diva.mixedstorage.db.RecordReaderFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.RecordReaderSpy;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTableSpy;
import se.uu.ub.cora.sqldatabase.SqlStorageException;

public class DivaDbOrganisationUpdaterTest {

	private DivaDbUpdater organisationUpdater;
	private DataToDbTranslaterSpy dataTranslater;
	private RelatedTableFactorySpy relatedTableFactory;
	private RecordReaderFactorySpy recordReaderFactory;
	private DataGroup dataGroup;
	private SqlConnectionProviderSpy connectionProvider;
	private PreparedStatementExecutorSpy preparedStatementCreator;

	@BeforeMethod
	public void setUp() {
		createDefultDataGroup();
		dataTranslater = new DataToDbTranslaterSpy();
		recordReaderFactory = new RecordReaderFactorySpy();
		relatedTableFactory = new RelatedTableFactorySpy();
		connectionProvider = new SqlConnectionProviderSpy();
		preparedStatementCreator = new PreparedStatementExecutorSpy();
		organisationUpdater = new DivaDbOrganisationUpdater(dataTranslater, recordReaderFactory,
				relatedTableFactory, connectionProvider, preparedStatementCreator);
	}

	private void createDefultDataGroup() {
		dataGroup = new DataGroupSpy("organisation");
		DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", "4567"));
		dataGroup.addChild(recordInfo);
	}

	@Test
	public void testTranslaterAndDbStatmentForOrganisation() {
		organisationUpdater.update(dataGroup);
		assertEquals(dataTranslater.dataGroup, dataGroup);

		DbStatement organisationDbStatement = preparedStatementCreator.dbStatements.get(0);
		assertEquals(organisationDbStatement.getOperation(), "update");
		assertEquals(organisationDbStatement.getTableName(), "organisation");
		assertSame(organisationDbStatement.getValues(), dataTranslater.getValues());
		assertSame(organisationDbStatement.getConditions(), dataTranslater.getConditions());
	}

	@Test
	public void testAlternativeName() {
		organisationUpdater.update(dataGroup);

		RecordReaderSpy factoredReader = recordReaderFactory.factoredReaders.get(0);
		assertEquals(factoredReader.usedTableNames.get(0), "organisationview");
		assertEquals(factoredReader.usedConditionsList.get(0).get("id"), "4567");

		assertEquals(relatedTableFactory.relatedTableNames.get(0), "organisationAlternativeName");
		RelatedTableSpy firstRelatedTable = (RelatedTableSpy) relatedTableFactory.factoredRelatedTables
				.get(0);

		assertSame(firstRelatedTable.dataGroup, dataGroup);
		assertEquals(firstRelatedTable.dbRows, factoredReader.returnedListCollection.get(0));

	}

	@Test
	public void testAddress() {
		organisationUpdater.update(dataGroup);

		RecordReaderSpy factoredReader = recordReaderFactory.factoredReaders.get(0);
		assertEquals(relatedTableFactory.relatedTableNames.get(1), "organisationAddress");
		RelatedTableSpy addressTable = (RelatedTableSpy) relatedTableFactory.factoredRelatedTables
				.get(1);
		assertSame(addressTable.dataGroup, dataGroup);
		assertEquals(addressTable.dbRows, factoredReader.returnedListCollection.get(0));
	}

	@Test
	public void testParent() {
		organisationUpdater.update(dataGroup);

		RecordReaderSpy factoredReader = recordReaderFactory.factoredReaders.get(0);
		assertEquals(factoredReader.usedTableNames.get(1), "organisation_parent");
		assertEquals(factoredReader.usedConditionsList.get(1).get("organisation_id"), 4567);

		assertEquals(relatedTableFactory.relatedTableNames.get(2), "organisationParent");
		RelatedTableSpy secondRelatedTable = (RelatedTableSpy) relatedTableFactory.factoredRelatedTables
				.get(2);
		assertSame(secondRelatedTable.dataGroup, dataGroup);
		assertEquals(secondRelatedTable.dbRows, factoredReader.returnedListCollection.get(1));

	}

	@Test
	public void testPredecessor() {
		organisationUpdater.update(dataGroup);
		RecordReaderSpy factoredReader = recordReaderFactory.factoredReaders.get(0);
		assertEquals(factoredReader.usedTableNames.get(2), "divaorganisationpredecessor");
		assertEquals(factoredReader.usedConditionsList.get(2).get("organisation_id"), 4567);

		assertEquals(relatedTableFactory.relatedTableNames.get(3), "organisationPredecessor");
		RelatedTableSpy thirdRelatedTable = (RelatedTableSpy) relatedTableFactory.factoredRelatedTables
				.get(3);
		assertSame(thirdRelatedTable.dataGroup, dataGroup);
		assertEquals(thirdRelatedTable.dbRows, factoredReader.returnedListCollection.get(2));

	}

	@Test
	public void testConnectionAutoCommitIsFirstSetToFalseAndThenTrueOnException() {
		preparedStatementCreator.throwExceptionOnGenerateStatement = true;
		try {
			organisationUpdater.update(dataGroup);
		} catch (Exception sqlException) {
		}
		ConnectionSpy factoredConnection = connectionProvider.factoredConnection;
		assertFalse(factoredConnection.autoCommitChanges.get(0));
		assertTrue(factoredConnection.autoCommitChanges.get(1));
	}

	@Test
	public void testSQLConnectionConfiguration() {
		organisationUpdater.update(dataGroup);
		assertTrue(connectionProvider.getConnectionHasBeenCalled);
		ConnectionSpy factoredConnection = connectionProvider.factoredConnection;
		assertFalse(factoredConnection.autoCommitChanges.get(0));
		assertTrue(factoredConnection.autoCommitChanges.get(1));
		assertTrue(factoredConnection.commitWasCalled);
		assertTrue(factoredConnection.closeWasCalled);
	}

	@Test
	public void testConnectionClosedOnSQLException() throws Exception {
		preparedStatementCreator.throwExceptionOnGenerateStatement = true;
		try {
			organisationUpdater.update(dataGroup);
		} catch (Exception sqlException) {
		}
		ConnectionSpy factoredConnection = connectionProvider.factoredConnection;
		assertTrue(factoredConnection.closeWasCalled);
	}

	@Test
	public void testConnectionRollbackOnSQLException() throws Exception {
		preparedStatementCreator.throwExceptionOnGenerateStatement = true;
		try {
			organisationUpdater.update(dataGroup);
		} catch (Exception sqlException) {
		}
		ConnectionSpy factoredConnection = connectionProvider.factoredConnection;
		assertTrue(factoredConnection.rollbackWasCalled);
	}

	@Test
	public void testPreparedStatements() {
		organisationUpdater.update(dataGroup);
		assertTrue(preparedStatementCreator.createWasCalled);
		assertSame(preparedStatementCreator.connection, connectionProvider.factoredConnection);
		int orgStatementAndStatmentsFromSpy = 5;
		assertEquals(preparedStatementCreator.dbStatements.size(), orgStatementAndStatmentsFromSpy);

	}

	@Test(expectedExceptions = SqlStorageException.class, expectedExceptionsMessageRegExp = ""
			+ "Error executing prepared statement: Error executing statement: error from spy")
	public void testPreparedStatementThrowsException() {
		preparedStatementCreator.throwExceptionOnGenerateStatement = true;
		organisationUpdater.update(dataGroup);
	}
}
