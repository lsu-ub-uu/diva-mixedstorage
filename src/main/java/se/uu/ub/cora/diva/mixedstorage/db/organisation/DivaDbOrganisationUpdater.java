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

import java.util.ArrayList;
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
import se.uu.ub.cora.sqldatabase.SqlDatabaseException;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;
import se.uu.ub.cora.sqldatabase.table.TableFacade;
import se.uu.ub.cora.sqldatabase.table.TableQuery;

public class DivaDbOrganisationUpdater implements DivaDbUpdater {

	private static final String ORGANISATION_ID = "organisation_id";
	private DataToDbTranslater organisationToDbTranslater;
	private RelatedTableFactory relatedTableFactory;
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
		updateOrganisation(dataGroup);

	}

	private void updateOrganisation(DataGroup dataGroup) {
		List<Row> existingDbOrganisation = readExistingOrganisationRow();
		List<DbStatement> dbStatements = generateDbStatements(dataGroup, existingDbOrganisation);
		tryUpdateDatabaseWithGivenDbStatements(dbStatements);
	}

	private List<Row> readExistingOrganisationRow() {
		TableQuery tableQuery = createTableQueryForReadOrganisation();
		return tableFacade.readRowsForQuery(tableQuery);
	}

	private TableQuery createTableQueryForReadOrganisation() {
		TableQuery tableQuery = sqlDatabaseFactory.factorTableQuery("organisationview");
		addConditionForRead(tableQuery);
		return tableQuery;
	}

	private void addConditionForRead(TableQuery tableQuery) {
		int organisationsId = (int) organisationConditions.get(ORGANISATION_ID);
		tableQuery.addCondition(ORGANISATION_ID, organisationsId);
	}

	private List<DbStatement> generateDbStatements(DataGroup dataGroup,
			List<Row> organisationRowsFromDb) {
		List<DbStatement> dbStatements = new ArrayList<>();
		dbStatements.add(createDbStatementForOrganisationUpdate());
		dbStatements
				.addAll(generateDbStatementsForAlternativeName(dataGroup, organisationRowsFromDb));
		dbStatements.addAll(generateDbStatementsForAddress(dataGroup, organisationRowsFromDb));
		dbStatements.addAll(generateDbStatementsForParents(dataGroup));
		dbStatements.addAll(generateDbStatementsForPredecessors(dataGroup));
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

	private List<DbStatement> generateDbStatementsForParents(DataGroup dataGroup) {
		TableQuery tableQuery = sqlDatabaseFactory.factorTableQuery("organisation_parent");
		addConditionForRead(tableQuery);
		List<Row> dbParents = tableFacade.readRowsForQuery(tableQuery);
		RelatedTable parent = relatedTableFactory.factor("organisationParent");
		return parent.handleDbForDataGroup(dataGroup, dbParents);
	}

	private List<DbStatement> generateDbStatementsForPredecessors(DataGroup dataGroup) {
		TableQuery tableQuery = sqlDatabaseFactory.factorTableQuery("divaorganisationpredecessor");
		addConditionForRead(tableQuery);
		List<Row> dbPredecessors = tableFacade.readRowsForQuery(tableQuery);
		RelatedTable predecessor = relatedTableFactory.factor("organisationPredecessor");
		return predecessor.handleDbForDataGroup(dataGroup, dbPredecessors);
	}

	private void tryUpdateDatabaseWithGivenDbStatements(List<DbStatement> dbStatements) {
		try {
			tryUpdateDatabaseWithGivenDbStatementsUsingConnection(dbStatements);
		} catch (Exception e) {
			throw SqlDatabaseException.withMessageAndException(
					"Error executing prepared statement: " + e.getMessage(), e);
		}
	}

	private void tryUpdateDatabaseWithGivenDbStatementsUsingConnection(
			List<DbStatement> dbStatements) {
		try {
			updateDatabaseWithGivenDbStatementsUsingConnection(dbStatements);
		} catch (Exception innerException) {
			databaseFacade.rollback();
			throw innerException;
		} finally {
			databaseFacade.endTransaction();
		}
	}

	private void updateDatabaseWithGivenDbStatementsUsingConnection(
			List<DbStatement> dbStatements) {
		databaseFacade.startTransaction();
		statementExecutor.executeDbStatmentUsingDatabaseFacade(dbStatements, databaseFacade);
		databaseFacade.endTransaction();
	}

	public DataToDbTranslater getDataToDbTranslater() {
		// needed for test
		return organisationToDbTranslater;
	}

	public RelatedTableFactory getRelatedTableFactory() {
		// needed for test
		return relatedTableFactory;
	}

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

}
