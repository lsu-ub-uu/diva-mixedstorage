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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbTranslater;
import se.uu.ub.cora.diva.mixedstorage.db.DbStatement;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbUpdater;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTable;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTableFactory;
import se.uu.ub.cora.diva.mixedstorage.db.StatementExecutor;
import se.uu.ub.cora.sqldatabase.DatabaseFacade;
import se.uu.ub.cora.sqldatabase.Row;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;
import se.uu.ub.cora.sqldatabase.table.TableFacade;
import se.uu.ub.cora.sqldatabase.table.TableQuery;

public class DivaDbOrganisationUpdater implements DivaDbUpdater {

	private static final String ORGANISATION_ID = "organisation_id";
	private DataToDbTranslater organisationToDbTranslater;
	private RelatedTableFactory relatedTableFactory;
	// private RecordReaderFactory recordReaderFactory;
	// private RecordReader recordReader;
	// private SqlConnectionProvider connectionProvider;
	private StatementExecutor statementExecutor;
	private Map<String, Object> organisationConditions;
	private Map<String, Object> organisationValues;
	private SqlDatabaseFactory sqlDatabaseFactory;
	private TableFacade tableFacade;
	private DatabaseFacade databaseFacade;

	public DivaDbOrganisationUpdater(DataToDbTranslater dataTranslater,
			SqlDatabaseFactory sqlDatabaseFactory, RelatedTableFactory relatedTableFactory,
			StatementExecutor preparedStatementCreator) {
		this.organisationToDbTranslater = dataTranslater;
		this.sqlDatabaseFactory = sqlDatabaseFactory;
		this.relatedTableFactory = relatedTableFactory;
		this.statementExecutor = preparedStatementCreator;
		this.tableFacade = sqlDatabaseFactory.factorTableFacade();
		this.databaseFacade = sqlDatabaseFactory.factorDatabaseFacade();
	}

	@Override
	public void update(DataGroup dataGroup) {
		organisationToDbTranslater.translate(dataGroup);
		organisationConditions = organisationToDbTranslater.getConditions();
		organisationValues = organisationToDbTranslater.getValues();
		// recordReader = recordReaderFactory.factor();
		updateOrganisation(dataGroup);

	}

	private void updateOrganisation(DataGroup dataGroup) {
		List<Row> existingDbOrganisation = readExistingOrganisationRow();
		Map<String, Object> readConditionsRelatedTables = generateReadConditionsForRelatedTables();
		List<DbStatement> dbStatements = generateDbStatements(dataGroup,
				readConditionsRelatedTables, existingDbOrganisation);
		tryUpdateDatabaseWithGivenDbStatements(dbStatements);
	}

	private List<Row> readExistingOrganisationRow() {
		// Map<String, Object> readConditionsForOrganisation = generateReadConditions();
		TableQuery tableQuery = createTableQueryForReadOrganisation();

		return tableFacade.readRowsForQuery(tableQuery);
		// return recordReader.readFromTableUsingConditions("organisationview",
		// readConditionsForOrganisation);
	}

	private TableQuery createTableQueryForReadOrganisation() {
		TableQuery tableQuery = sqlDatabaseFactory.factorTableQuery("organisationview");
		int organisationsId = (int) organisationConditions.get(ORGANISATION_ID);
		tableQuery.addCondition(ORGANISATION_ID, organisationsId);
		return tableQuery;
	}
	//
	// private Map<String, Object> generateReadConditions() {
	// Map<String, Object> readConditions = new HashMap<>();
	// int organisationsId = (int) organisationConditions.get(ORGANISATION_ID);
	// readConditions.put("id", organisationsId);
	// return readConditions;
	// }

	private Map<String, Object> generateReadConditionsForRelatedTables() {
		Map<String, Object> readConditions = new HashMap<>();
		int organisationsId = (int) organisationConditions.get(ORGANISATION_ID);
		readConditions.put(ORGANISATION_ID, organisationsId);
		return readConditions;
	}

	private List<DbStatement> generateDbStatements(DataGroup dataGroup,
			Map<String, Object> readConditions, List<Row> organisationRowsFromDb) {
		List<DbStatement> dbStatements = new ArrayList<>();
		dbStatements.add(createDbStatementForOrganisationUpdate());
		dbStatements
				.addAll(generateDbStatementsForAlternativeName(dataGroup, organisationRowsFromDb));
		dbStatements.addAll(generateDbStatementsForAddress(dataGroup, organisationRowsFromDb));
		dbStatements.addAll(generateDbStatementsForParents(dataGroup, readConditions));
		dbStatements.addAll(generateDbStatementsForPredecessors(dataGroup, readConditions));
		return dbStatements;
	}

	private DbStatement createDbStatementForOrganisationUpdate() {
		return new DbStatement("update", "organisation", organisationValues,
				organisationConditions);
	}

	private List<DbStatement> generateDbStatementsForAlternativeName(DataGroup dataGroup,
			List<Row> organisationRowsFromDb) {
		RelatedTable alternativeName = relatedTableFactory.factor("organisationAlternativeName");
		return alternativeName.handleDbForDataGroup(dataGroup, organisationRowsFromDb);
	}

	private List<DbStatement> generateDbStatementsForAddress(DataGroup dataGroup,
			List<Row> organisationRowsFromDb) {
		RelatedTable addressTable = relatedTableFactory.factor("organisationAddress");
		return addressTable.handleDbForDataGroup(dataGroup, organisationRowsFromDb);
	}

	private List<DbStatement> generateDbStatementsForParents(DataGroup dataGroup,
			Map<String, Object> readConditions) {
		List<Row> dbParents = null;
		// recordReader
		// .readFromTableUsingConditions("organisation_parent", readConditions);
		RelatedTable parent = relatedTableFactory.factor("organisationParent");
		return parent.handleDbForDataGroup(dataGroup, dbParents);
	}

	private List<DbStatement> generateDbStatementsForPredecessors(DataGroup dataGroup,
			Map<String, Object> readConditions) {
		List<Row> dbPredecessors = null;
		// recordReader
		// .readFromTableUsingConditions("divaorganisationpredecessor", readConditions);
		RelatedTable predecessor = relatedTableFactory.factor("organisationPredecessor");
		return predecessor.handleDbForDataGroup(dataGroup, dbPredecessors);
	}

	private void tryUpdateDatabaseWithGivenDbStatements(List<DbStatement> dbStatements) {
		// try (Connection connection = connectionProvider.getConnection();) {
		// tryUpdateDatabaseWithGivenDbStatementsUsingConnection(dbStatements, connection);
		// } catch (Exception e) {
		// throw SqlStorageException.withMessageAndException(
		// "Error executing prepared statement: " + e.getMessage(), e);
		// }
	}

	private void tryUpdateDatabaseWithGivenDbStatementsUsingConnection(
			List<DbStatement> dbStatements, Connection connection) throws SQLException {
		try {
			updateDatabaseWithGivenDbStatementsUsingConnection(dbStatements, connection);
		} catch (Exception innerException) {
			connection.rollback();
			throw innerException;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	private void updateDatabaseWithGivenDbStatementsUsingConnection(List<DbStatement> dbStatements,
			Connection connection) throws SQLException {
		// tableFacade.startTransaction();
		// databaseFacade.

		// connection.setACutoCommit(false);
		// statementExecutor.executeDbStatmentUsingDatabaseFacade(dbStatements, connection);
		// connection.commit();
	}

	public DataToDbTranslater getDataToDbTranslater() {
		// needed for test
		return organisationToDbTranslater;
	}

	public RelatedTableFactory getRelatedTableFactory() {
		// needed for test
		return relatedTableFactory;
	}

	// public RecordReaderFactory getRecordReaderFactory() {
	// // needed for test
	// return recordReaderFactory;
	// }
	//
	// public SqlConnectionProvider getSqlConnectionProvider() {
	// // needed for test
	// return connectionProvider;
	// }

	public StatementExecutor getPreparedStatementCreator() {
		// needed for test
		return statementExecutor;
	}

	public SqlDatabaseFactory getSqlDatabaseFactory() {
		return sqlDatabaseFactory;
	}

	public TableFacade getTableFacade() {
		return tableFacade;
	}

	public DatabaseFacade getDatabaseFacade() {
		return databaseFacade;
	}

	// public DataReader getDataReader() {
	// // needed for test
	// return dataReader;
	// }

}
