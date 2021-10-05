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
package se.uu.ub.cora.diva.mixedstorage.db.organisation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbTranslaterSpy;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTableSpy;
import se.uu.ub.cora.sqldatabase.SqlDatabaseException;

public class DivaDbOrganisationUpdaterTest {

	private DivaDbOrganisationUpdater organisationUpdater;
	private DataToDbTranslaterSpy dataTranslater;
	private RelatedTableFactorySpy relatedTableFactory;
	private DataGroup dataGroup;
	private PreparedStatementExecutorSpy preparedStatementCreator;
	private SqlDatabaseFactorySpy sqlDatabaseFactory;
	private TableFacadeSpy tableFacade;
	private DatabaseFacadeSpy databaseFacade;

	@BeforeMethod
	public void setUp() {
		createDefaultDataGroup();
		dataTranslater = new DataToDbTranslaterSpy();
		relatedTableFactory = new RelatedTableFactorySpy();
		preparedStatementCreator = new PreparedStatementExecutorSpy();
		sqlDatabaseFactory = new SqlDatabaseFactorySpy();
		tableFacade = new TableFacadeSpy();
		databaseFacade = new DatabaseFacadeSpy();
		organisationUpdater = new DivaDbOrganisationUpdater(dataTranslater, sqlDatabaseFactory,
				relatedTableFactory, preparedStatementCreator);
	}

	private void createDefaultDataGroup() {
		dataGroup = new DataGroupSpy("organisation");
		DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", "4567"));
		dataGroup.addChild(recordInfo);
	}

	@Test
	public void testInit() {
		assertSame(organisationUpdater.getSqlDatabaseFactory(), sqlDatabaseFactory);
	}

	@Test
	public void testTranslaterConditionsUsedWhenReadingOrganisation() {
		organisationUpdater.update(tableFacade, databaseFacade, dataGroup);
		assertEquals(dataTranslater.dataGroup, dataGroup);

		assertEquals(sqlDatabaseFactory.tableNames.get(0), "organisationview");

		TableQuerySpy tableQuery = (TableQuerySpy) tableFacade.tableQueries.get(0);
		assertNotNull(tableQuery);
		assertSame(tableFacade.tableQueries.get(0), sqlDatabaseFactory.factoredTableQueries.get(0));

		Map<String, Object> conditions = dataTranslater.getConditions();
		assertEquals(tableQuery.conditions.get("organisation_id"),
				conditions.get("organisation_id"));

	}

	@Test
	public void testAlternativeName() {
		organisationUpdater.update(tableFacade, databaseFacade, dataGroup);

		assertEquals(sqlDatabaseFactory.tableNames.get(0), "organisationview");

		TableQuerySpy tableQuery = (TableQuerySpy) tableFacade.tableQueries.get(0);
		assertSame(tableFacade.tableQueries.get(0), sqlDatabaseFactory.factoredTableQueries.get(0));

		Map<String, Object> conditions = dataTranslater.getConditions();
		assertEquals(tableQuery.conditions.get("organisation_id"),
				conditions.get("organisation_id"));

		assertEquals(relatedTableFactory.relatedTableNames.get(0), "organisationAlternativeName");
		RelatedTableSpy firstRelatedTable = (RelatedTableSpy) relatedTableFactory.factoredRelatedTables
				.get(0);

		assertSame(firstRelatedTable.tableFacade, tableFacade);
		assertSame(firstRelatedTable.dataGroup, dataGroup);
		assertEquals(firstRelatedTable.dbRows, tableFacade.rowsToReturn);

	}

	@Test
	public void testAddress() {
		organisationUpdater.update(tableFacade, databaseFacade, dataGroup);

		assertSame(tableFacade.tableQueries.get(0), sqlDatabaseFactory.factoredTableQueries.get(0));

		assertEquals(relatedTableFactory.relatedTableNames.get(1), "organisationAddress");
		RelatedTableSpy addressTable = (RelatedTableSpy) relatedTableFactory.factoredRelatedTables
				.get(1);

		assertSame(addressTable.tableFacade, tableFacade);
		assertSame(addressTable.dataGroup, dataGroup);
		assertEquals(addressTable.dbRows, tableFacade.rowsToReturn);
	}

	@Test
	public void testParent() {
		organisationUpdater.update(tableFacade, databaseFacade, dataGroup);

		assertEquals(sqlDatabaseFactory.tableNames.get(1), "organisation_parent");

		TableQuerySpy tableQuery = (TableQuerySpy) tableFacade.tableQueries.get(1);
		assertSame(tableQuery, sqlDatabaseFactory.factoredTableQueries.get(1));
		assertEquals(tableQuery.conditions.get("organisation_id"), 4567);

		RelatedTableSpy secondRelatedTable = (RelatedTableSpy) relatedTableFactory.factoredRelatedTables
				.get(2);

		assertSame(secondRelatedTable.tableFacade, tableFacade);
		assertSame(secondRelatedTable.dataGroup, dataGroup);
		assertEquals(secondRelatedTable.dbRows, tableFacade.rowsToReturn);

	}

	@Test
	public void testPredecessor() {
		organisationUpdater.update(tableFacade, databaseFacade, dataGroup);
		assertEquals(sqlDatabaseFactory.tableNames.get(2), "divaorganisationpredecessor");

		TableQuerySpy tableQuery = (TableQuerySpy) tableFacade.tableQueries.get(2);
		assertSame(tableQuery, sqlDatabaseFactory.factoredTableQueries.get(2));
		assertEquals(tableQuery.conditions.get("organisation_id"), 4567);

		assertEquals(relatedTableFactory.relatedTableNames.get(3), "organisationPredecessor");
		RelatedTableSpy thirdRelatedTable = (RelatedTableSpy) relatedTableFactory.factoredRelatedTables
				.get(3);

		assertSame(thirdRelatedTable.tableFacade, tableFacade);
		assertSame(thirdRelatedTable.dataGroup, dataGroup);
		assertEquals(thirdRelatedTable.dbRows, tableFacade.rowsToReturn);
	}

	@Test
	public void testSQLConnectionConfiguration() {
		organisationUpdater.update(tableFacade, databaseFacade, dataGroup);
		assertTrue(databaseFacade.startTransactionWasCalled);
		assertTrue(databaseFacade.endTransactionWasCalled);
	}

	@Test
	public void testConnectionClosedOnSQLException() throws Exception {
		preparedStatementCreator.throwExceptionOnGenerateStatement = true;
		try {
			organisationUpdater.update(tableFacade, databaseFacade, dataGroup);
		} catch (Exception sqlException) {
		}
		assertTrue(databaseFacade.endTransactionWasCalled);
	}

	@Test
	public void testRollbackOnSQLException() throws Exception {
		preparedStatementCreator.throwExceptionOnGenerateStatement = true;
		try {
			organisationUpdater.update(tableFacade, databaseFacade, dataGroup);
		} catch (Exception sqlException) {
		}
		assertTrue(databaseFacade.rollbackWasCalled);
	}

	@Test
	public void testPreparedStatements() {
		organisationUpdater.update(tableFacade, databaseFacade, dataGroup);
		assertTrue(preparedStatementCreator.createWasCalled);
		assertSame(preparedStatementCreator.databaseFacade, databaseFacade);
		int orgStatementAndStatmentsFromSpy = 5;
		assertEquals(preparedStatementCreator.dbStatements.size(), orgStatementAndStatmentsFromSpy);

	}

	@Test(expectedExceptions = SqlDatabaseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error executing prepared statement: Error executing statement: error from spy")
	public void testPreparedStatementThrowsException() {
		preparedStatementCreator.throwExceptionOnGenerateStatement = true;
		organisationUpdater.update(tableFacade, databaseFacade, dataGroup);
	}
}
