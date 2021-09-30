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

import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.db.organisation.DatabaseFacadeSpy;

public class PreparedStatementExecutorTest {

	private StatementExecutor statementExecutor;
	private Map<String, Object> conditions;
	private Map<String, Object> values;
	private DbStatement updateDbStatement;
	private DbStatement deleteDbStatement;
	private DbStatement insertDbStatement;
	private ConnectionSpy connectionSpy;
	private DatabaseFacadeSpy databaseFacade;

	@BeforeMethod
	public void setUp() {
		connectionSpy = new ConnectionSpy();
		statementExecutor = new PreparedStatementExecutorImp();
		databaseFacade = new DatabaseFacadeSpy();
		setUpDefaultValuesAndConditions();
		createStatements();
	}

	private void setUpDefaultValuesAndConditions() {
		conditions = new HashMap<>();
		values = new HashMap<>();
		values.put("name", "someName");
	}

	private void createStatements() {
		updateDbStatement = new DbStatement("update", "organisation", values, conditions);
		deleteDbStatement = new DbStatement("delete", "organisation", Collections.emptyMap(),
				conditions);
		insertDbStatement = new DbStatement("insert", "organisation", values,
				Collections.emptyMap());
	}

	@Test
	public void testUpdateNoConditions() {
		List<DbStatement> dbStatements = List.of(updateDbStatement);

		statementExecutor.executeDbStatmentUsingDatabaseFacade(dbStatements, databaseFacade);

		assertEquals(databaseFacade.sqls.get(0), "UPDATE organisation SET name = ?");

		List<Object> values = databaseFacade.valuesList.get(0);
		assertEquals(values.size(), 1);
		assertEquals(values.get(0), "someName");

	}

	// @Test
	// public void testPreparedStatementIsExecutedAndClosed() throws Exception {
	// List<DbStatement> dbStatements = List.of(updateDbStatement);
	// statementExecutor.executeDbStatmentUsingDatabaseFacade(dbStatements, databaseFacade);
	// var firstStatement = connectionSpy.createdPreparedStatements.get(0);
	// assertTrue(firstStatement.executeWasCalled);
	// assertTrue(firstStatement.closedWasCalled);
	// }

	// @Test
	// public void testPreparedStatementIsClosedOnSQLException() throws Exception {
	// List<DbStatement> dbStatements = List.of(updateDbStatement);
	// connectionSpy.throwExceptionOnPreparedStatementExecute = true;
	// try {
	// statementExecutor.executeDbStatmentUsingDatabaseFacade(dbStatements, databaseFacade);
	// } catch (Exception e) {
	// }
	// var firstStatement = connectionSpy.createdPreparedStatements.get(0);
	// assertTrue(firstStatement.closedWasCalled);
	// }

	// @Test
	// public void testValuesAreSetOnPreparedStatementBeforeSQLExecute() throws Exception {
	// List<DbStatement> dbStatements = List.of(updateDbStatement);
	// connectionSpy.throwExceptionOnPreparedStatementExecute = true;
	// try {
	// statementExecutor.executeDbStatmentUsingDatabaseFacade(dbStatements, connectionSpy);
	// } catch (Exception e) {
	// }
	// var firstStatement = connectionSpy.createdPreparedStatements.get(0);
	// assertEquals(firstStatement.usedSetObjects.get("1"), "someName");
	// }
	//
	@Test
	public void testUpdateWithConditions() {
		conditions.put("id", 35);
		List<DbStatement> dbStatements = List.of(updateDbStatement);
		statementExecutor.executeDbStatmentUsingDatabaseFacade(dbStatements, databaseFacade);

		assertEquals(databaseFacade.sqls.get(0), "UPDATE organisation SET name = ? WHERE id = ?");

		List<Object> values = databaseFacade.valuesList.get(0);
		assertEquals(values.size(), 2);
		assertEquals(values.get(0), "someName");
		assertEquals(values.get(1), 35);
	}

	@Test
	public void testUpdateWithMultipleValuesAndConditions() {
		values.put("address", "some address");
		conditions.put("id", 35);
		conditions.put("otherId", 3500);
		List<DbStatement> dbStatements = List.of(updateDbStatement);
		statementExecutor.executeDbStatmentUsingDatabaseFacade(dbStatements, databaseFacade);

		assertEquals(databaseFacade.sqls.get(0),
				"UPDATE organisation SET address = ?, name = ? WHERE otherId = ? AND id = ?");

		List<Object> values = databaseFacade.valuesList.get(0);
		assertEquals(values.size(), 4);
		assertEquals(values.get(0), "some address");
		assertEquals(values.get(1), "someName");
		assertEquals(values.get(2), 3500);
		assertEquals(values.get(3), 35);
	}

	// @Test
	// public void testSetTimestampPreparedStatement() throws Exception {
	//
	// Date today = new Date();
	// long time = today.getTime();
	// Timestamp timestamp = new Timestamp(time);
	// values.put("lastupdated", timestamp);
	// List<DbStatement> dbStatements = List.of(updateDbStatement);
	// statementExecutor.executeDbStatmentUsingDatabaseFacade(dbStatements, connectionSpy);
	// List<PreparedStatementSpy> preparedStatements = connectionSpy.createdPreparedStatements;
	// PreparedStatementSpy preparedStatement = preparedStatements.get(0);
	// assertSame(connectionSpy.preparedStatementSpy, preparedStatement);
	// assertEquals(preparedStatement.usedSetObjects.get("1"), "someName");
	// assertTrue(preparedStatement.usedSetTimestamps.get("2") instanceof Timestamp);
	// }

	// @Test(expectedExceptions = SqlStorageException.class, expectedExceptionsMessageRegExp = ""
	// + "Error executing statement: UPDATE organisation SET name = \\?")
	// public void testSQlException() {
	// List<DbStatement> dbStatements = List.of(updateDbStatement);
	// connectionSpy.throwException = true;
	// statementExecutor.executeDbStatmentUsingDatabaseFacade(dbStatements, connectionSpy);
	// }
	//
	// /***************************************** DELETE
	// ********************************************/
	//
	@Test
	public void testDeleteWithOneCondition() {
		conditions.put("id", 35);
		List<DbStatement> dbStatements = List.of(deleteDbStatement);
		statementExecutor.executeDbStatmentUsingDatabaseFacade(dbStatements, databaseFacade);

		assertEquals(databaseFacade.sqls.get(0), "DELETE FROM organisation WHERE id = ?");

		List<Object> values = databaseFacade.valuesList.get(0);
		assertEquals(values.size(), 1);
		assertEquals(values.get(0), 35);
	}

	@Test
	public void testDeleteWithSeveralConditions() throws Exception {
		conditions.put("id", 35);
		conditions.put("anotherId", 72);
		conditions.put("lastId", "47");
		List<DbStatement> dbStatements = List.of(deleteDbStatement);
		statementExecutor.executeDbStatmentUsingDatabaseFacade(dbStatements, databaseFacade);

		assertEquals(databaseFacade.sqls.get(0),
				"DELETE FROM organisation WHERE anotherId = ? AND lastId = ? AND id = ?");

		List<Object> values = databaseFacade.valuesList.get(0);
		assertEquals(values.size(), 3);
		assertEquals(values.get(0), 72);
		assertEquals(values.get(1), "47");
		assertEquals(values.get(2), 35);

	}

	/*****************************************
	 * INSERT
	 ********************************************/

	// @Test
	// public void testInsertWithOneValue() throws Exception {
	// PreparedStatementSpy preparedStatement = (PreparedStatementSpy) statementExecutor
	// .createFromDbStatment(insertDbStatement);
	// assertSame(connection.preparedStatementSpy, preparedStatement);
	// assertEquals(connection.sql, "INSERT INTO null(column) VALUES(values)");
	//
	// }

	@Test
	public void testInsertWithOneValue() throws Exception {
		List<DbStatement> dbStatements = List.of(insertDbStatement);
		statementExecutor.executeDbStatmentUsingDatabaseFacade(dbStatements, databaseFacade);

		assertEquals(databaseFacade.sqls.get(0), "INSERT INTO organisation(name) VALUES(?)");

		assertCorrectPreparedStatementDefaultInsert();
	}

	private void assertCorrectPreparedStatementDefaultInsert() {
		List<Object> values = databaseFacade.valuesList.get(0);
		assertEquals(values.size(), 1);
		assertEquals(values.get(0), "someName");
	}

	@Test
	public void testInsertWithMultipleValue() throws Exception {
		values.put("address", "some address");
		values.put("alternative_name", "some other name");
		values.put("org_id", 12345);
		List<DbStatement> dbStatements = List.of(insertDbStatement);
		statementExecutor.executeDbStatmentUsingDatabaseFacade(dbStatements, databaseFacade);

		assertEquals(databaseFacade.sqls.get(0),
				"INSERT INTO organisation(alternative_name, address, org_id, name) VALUES(?, ?, ?, ?)");

		List<Object> values = databaseFacade.valuesList.get(0);
		assertEquals(values.size(), 4);
		assertEquals(values.get(0), "some other name");
		assertEquals(values.get(1), "some address");
		assertEquals(values.get(2), 12345);
		assertEquals(values.get(3), "someName");
	}

	@Test
	public void testMultipleStatements() {
		conditions.put("id", 35);
		List<DbStatement> dbStatements = List.of(insertDbStatement, updateDbStatement,
				deleteDbStatement);
		statementExecutor.executeDbStatmentUsingDatabaseFacade(dbStatements, databaseFacade);

		assertEquals(databaseFacade.sqls.get(0), "INSERT INTO organisation(name) VALUES(?)");
		assertEquals(databaseFacade.sqls.get(1), "UPDATE organisation SET name = ? WHERE id = ?");
		assertEquals(databaseFacade.sqls.get(2), "DELETE FROM organisation WHERE id = ?");

		List<List<Object>> valueList = databaseFacade.valuesList;
		assertEquals(valueList.get(0).size(), 1);
		assertEquals(valueList.get(0).get(0), "someName");

		assertEquals(valueList.get(1).size(), 2);
		assertEquals(valueList.get(1).get(0), "someName");
		assertEquals(valueList.get(1).get(1), 35);

		assertEquals(valueList.get(2).size(), 1);
		assertEquals(valueList.get(2).get(0), 35);
	}

}
