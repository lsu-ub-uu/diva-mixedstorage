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

	public DivaDbOrganisationUpdater(DataToDbTranslater dataTranslater,
			SqlDatabaseFactory sqlDatabaseFactory, RelatedTableFactory relatedTableFactory,
			StatementExecutor preparedStatementCreator) {
		this.organisationToDbTranslater = dataTranslater;
		this.sqlDatabaseFactory = sqlDatabaseFactory;
		this.relatedTableFactory = relatedTableFactory;
		this.statementExecutor = preparedStatementCreator;
	}

	@Override
	public void update(TableFacade tableFacade, DatabaseFacade databaseFacade,
			DataGroup dataGroup) {
		organisationToDbTranslater.translate(dataGroup);
		organisationConditions = organisationToDbTranslater.getConditions();
		organisationValues = organisationToDbTranslater.getValues();
		updateOrganisation(tableFacade, databaseFacade, dataGroup);

	}

	private void updateOrganisation(TableFacade tableFacade, DatabaseFacade databaseFacade,
			DataGroup dataGroup) {
		List<Row> existingDbOrganisation = readExistingOrganisationRow(tableFacade);
		List<DbStatement> dbStatements = generateDbStatements(tableFacade, dataGroup,
				existingDbOrganisation);
		tryUpdateDatabaseWithGivenDbStatements(databaseFacade, dbStatements);
	}

	private List<Row> readExistingOrganisationRow(TableFacade tableFacade) {
		TableQuery tableQuery = createTableQueryForReadOrganisation();
		return tableFacade.readRowsForQuery(tableQuery);
	}

	private TableQuery createTableQueryForReadOrganisation() {
		TableQuery tableQuery = sqlDatabaseFactory.factorTableQuery("organisationview");
		addConditionForReadFromView(tableQuery);
		return tableQuery;
	}

	private void addConditionForReadFromView(TableQuery tableQuery) {
		int organisationsId = (int) organisationConditions.get(ORGANISATION_ID);
		tableQuery.addCondition("id", organisationsId);
	}

	private void addConditionForRead(TableQuery tableQuery) {
		int organisationsId = (int) organisationConditions.get(ORGANISATION_ID);
		tableQuery.addCondition(ORGANISATION_ID, organisationsId);
	}

	private List<DbStatement> generateDbStatements(TableFacade tableFacade, DataGroup dataGroup,
			List<Row> organisationRowsFromDb) {
		List<DbStatement> dbStatements = new ArrayList<>();
		dbStatements.add(createDbStatementForOrganisationUpdate());
		dbStatements
				.addAll(generateDbStatementsForAlternativeName(dataGroup, organisationRowsFromDb));
		dbStatements.addAll(generateDbStatementsForAddress(dataGroup, organisationRowsFromDb));
		dbStatements.addAll(generateDbStatementsForParents(tableFacade, dataGroup));
		dbStatements.addAll(generateDbStatementsForPredecessors(tableFacade, dataGroup));
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

	private List<DbStatement> generateDbStatementsForParents(TableFacade tableFacade,
			DataGroup dataGroup) {
		TableQuery tableQuery = sqlDatabaseFactory.factorTableQuery("organisation_parent");
		addConditionForRead(tableQuery);
		List<Row> dbParents = tableFacade.readRowsForQuery(tableQuery);
		RelatedTable parent = relatedTableFactory.factor("organisationParent");
		return parent.handleDbForDataGroup(dataGroup, dbParents);
	}

	private List<DbStatement> generateDbStatementsForPredecessors(TableFacade tableFacade,
			DataGroup dataGroup) {
		TableQuery tableQuery = sqlDatabaseFactory.factorTableQuery("divaorganisationpredecessor");
		addConditionForRead(tableQuery);
		List<Row> dbPredecessors = tableFacade.readRowsForQuery(tableQuery);
		RelatedTable predecessor = relatedTableFactory.factor("organisationPredecessor");
		return predecessor.handleDbForDataGroup(dataGroup, dbPredecessors);
	}

	private void tryUpdateDatabaseWithGivenDbStatements(DatabaseFacade databaseFacade,
			List<DbStatement> dbStatements) {
		try {
			tryUpdateDatabaseWithGivenDbStatementsUsingConnection(databaseFacade, dbStatements);
		} catch (Exception e) {
			throw SqlDatabaseException.withMessageAndException(
					"Error executing prepared statement: " + e.getMessage(), e);
		}
	}

	private void tryUpdateDatabaseWithGivenDbStatementsUsingConnection(
			DatabaseFacade databaseFacade, List<DbStatement> dbStatements) {
		try {
			updateDatabaseWithGivenDbStatementsUsingConnection(databaseFacade, dbStatements);
		} catch (Exception innerException) {
			databaseFacade.rollback();
			throw innerException;
		} finally {
			databaseFacade.endTransaction();
		}
	}

	private void updateDatabaseWithGivenDbStatementsUsingConnection(DatabaseFacade databaseFacade,
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

}
