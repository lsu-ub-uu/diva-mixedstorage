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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import se.uu.ub.cora.connection.SqlConnectionProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbTranslater;
import se.uu.ub.cora.diva.mixedstorage.db.DbStatement;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbUpdater;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTable;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTableFactory;
import se.uu.ub.cora.diva.mixedstorage.db.StatementExecutor;
import se.uu.ub.cora.sqldatabase.DataReader;
import se.uu.ub.cora.sqldatabase.RecordReader;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;
import se.uu.ub.cora.sqldatabase.SqlStorageException;

public class DivaDbOrganisationUpdater implements DivaDbUpdater {

	private static final String ORGANISATION_ID = "organisation_id";
	private DataToDbTranslater organisationToDbTranslater;
	private RelatedTableFactory relatedTableFactory;
	private RecordReaderFactory recordReaderFactory;
	private RecordReader recordReader;
	private SqlConnectionProvider connectionProvider;
	private StatementExecutor statementExecutor;
	private Map<String, Object> organisationConditions;
	private Map<String, Object> organisationValues;
	private DataReader dataReader;

	public DivaDbOrganisationUpdater(DataToDbTranslater dataTranslater,
			RecordReaderFactory recordReaderFactory, RelatedTableFactory relatedTableFactory,
			SqlConnectionProvider connectionProvider, StatementExecutor preparedStatementCreator,
			DataReader dataReader) {
		this.organisationToDbTranslater = dataTranslater;
		this.recordReaderFactory = recordReaderFactory;
		this.relatedTableFactory = relatedTableFactory;
		this.connectionProvider = connectionProvider;
		this.statementExecutor = preparedStatementCreator;
		this.dataReader = dataReader;

	}

	@Override
	public void update(DataGroup dataGroup) {
		organisationToDbTranslater.translate(dataGroup);
		organisationConditions = organisationToDbTranslater.getConditions();
		organisationValues = organisationToDbTranslater.getValues();
		recordReader = recordReaderFactory.factor();
		updateOrganisation(dataGroup);

	}

	private void updateOrganisation(DataGroup dataGroup) {
		possiblyThrowErrorIfDisallowedDependencyDetected(dataGroup);

		List<Map<String, Object>> existingDbOrganisation = readExistingOrganisationRow();
		Map<String, Object> readConditionsRelatedTables = generateReadConditionsForRelatedTables();
		List<DbStatement> dbStatements = generateDbStatements(dataGroup,
				readConditionsRelatedTables, existingDbOrganisation);
		tryUpdateDatabaseWithGivenDbStatements(dbStatements);
	}

	private void possiblyThrowErrorIfDisallowedDependencyDetected(DataGroup dataGroup) {
		List<Integer> parentIds = getIdsFromOrganisationLinkUsingNameInData(dataGroup,
				"parentOrganisation");
		List<Integer> predecessorIds = getIdsFromOrganisationLinkUsingNameInData(dataGroup,
				"formerName");

		throwErrorIfSameParentAndPredecessor(parentIds, predecessorIds);
		possiblyThrowErrorIfCircularDependencyDetected(parentIds, predecessorIds);
	}

	private List<Integer> getIdsFromOrganisationLinkUsingNameInData(DataGroup dataGroup,
			String nameInData) {
		List<DataGroup> parents = dataGroup.getAllGroupsWithNameInData(nameInData);
		return getIdListOfParentsAndPredecessors(parents);
	}

	private void throwErrorIfSameParentAndPredecessor(List<Integer> parentIds,
			List<Integer> predecessorIds) {
		boolean sameIdInBothList = parentIds.stream().anyMatch(predecessorIds::contains);
		if (sameIdInBothList) {
			throw SqlStorageException
					.withMessage("Organisation not updated due to same parent and predecessor");
		}
	}

	private void possiblyThrowErrorIfCircularDependencyDetected(List<Integer> parentIds,
			List<Integer> predecessorIds) {
		List<Integer> parentsAndPredecessors = combineParentsAndPredecessors(parentIds,
				predecessorIds);

		if (!parentsAndPredecessors.isEmpty()) {
			throwErrorIfLinkToSelf(parentsAndPredecessors);
			throwErrorIfCircularDependencyDetected(parentsAndPredecessors);
		}
	}

	private List<Integer> combineParentsAndPredecessors(List<Integer> parentIds,
			List<Integer> predecessorIds) {
		List<Integer> combinedList = new ArrayList<>();
		combinedList.addAll(parentIds);
		combinedList.addAll(predecessorIds);
		return combinedList;
	}

	private void throwErrorIfLinkToSelf(List<Integer> parentsAndPredecessorIds) {
		int organisationsId = (int) organisationConditions.get(ORGANISATION_ID);
		if (parentsAndPredecessorIds.contains(organisationsId)) {
			throw SqlStorageException.withMessage("Organisation not updated due to link to self");
		}
	}

	private List<Integer> getIdListOfParentsAndPredecessors(
			List<DataGroup> parentsAndPredecessors) {
		List<Integer> organisationIds = new ArrayList<>();
		for (DataGroup parent : parentsAndPredecessors) {
			int organisationId = extractOrganisationIdFromLink(parent);
			organisationIds.add(organisationId);
		}
		return organisationIds;
	}

	private void throwErrorIfCircularDependencyDetected(List<Integer> parentsAndPredecessorIds) {
		StringJoiner questionMarkPart = getCorrectNumberOfConditionPlaceHolders(
				parentsAndPredecessorIds);

		String sql = getSqlForFindingRecursion(questionMarkPart);
		List<Object> values = createValuesForCircularDependencyQuery(parentsAndPredecessorIds);
		executeAndThrowErrorIfCircularDependencyExist(sql, values);
	}

	private StringJoiner getCorrectNumberOfConditionPlaceHolders(
			List<Integer> parentsAndPredecessors) {
		StringJoiner questionMarkPart = new StringJoiner(", ");
		for (int i = 0; i < parentsAndPredecessors.size(); i++) {
			questionMarkPart.add("?");
		}
		return questionMarkPart;
	}

	private String getSqlForFindingRecursion(StringJoiner questionMarkPart) {
		return "with recursive org_tree as (select distinct organisation_id, relation"
				+ " from organisation_relations where organisation_id in (" + questionMarkPart
				+ ") " + "union all"
				+ " select distinct relation.organisation_id, relation.relation from"
				+ " organisation_relations as relation"
				+ " join org_tree as child on child.relation = relation.organisation_id)"
				+ " select * from org_tree where relation = ?";
	}

	private void executeAndThrowErrorIfCircularDependencyExist(String sql, List<Object> values) {
		List<Map<String, Object>> result = dataReader
				.executePreparedStatementQueryUsingSqlAndValues(sql, values);
		if (!result.isEmpty()) {
			throw SqlStorageException.withMessage(
					"Organisation not updated due to circular dependency with parent or predecessor");
		}
	}

	private List<Object> createValuesForCircularDependencyQuery(
			List<Integer> parentsAndPredecessorIds) {
		List<Object> values = new ArrayList<>();
		addValuesForParentsAndPredecessors(parentsAndPredecessorIds, values);
		addValueForOrganisationId(values);
		return values;
	}

	private void addValueForOrganisationId(List<Object> values) {
		int organisationsId = (int) organisationConditions.get(ORGANISATION_ID);
		values.add(organisationsId);
	}

	private void addValuesForParentsAndPredecessors(List<Integer> parentsAndPredecessorIds,
			List<Object> values) {
		for (Integer parentId : parentsAndPredecessorIds) {
			values.add(parentId);
		}
	}

	private int extractOrganisationIdFromLink(DataGroup parent) {
		DataGroup parentLink = parent.getFirstGroupWithNameInData("organisationLink");
		String linkedRecordId = parentLink.getFirstAtomicValueWithNameInData("linkedRecordId");
		return Integer.parseInt(linkedRecordId);
	}

	private List<Map<String, Object>> readExistingOrganisationRow() {
		Map<String, Object> readConditionsForOrganisation = generateReadConditions();
		return recordReader.readFromTableUsingConditions("organisationview",
				readConditionsForOrganisation);
	}

	private Map<String, Object> generateReadConditions() {
		Map<String, Object> readConditions = new HashMap<>();
		int organisationsId = (int) organisationConditions.get(ORGANISATION_ID);
		readConditions.put("id", organisationsId);
		return readConditions;
	}

	private Map<String, Object> generateReadConditionsForRelatedTables() {
		Map<String, Object> readConditions = new HashMap<>();
		int organisationsId = (int) organisationConditions.get(ORGANISATION_ID);
		readConditions.put(ORGANISATION_ID, organisationsId);
		return readConditions;
	}

	private List<DbStatement> generateDbStatements(DataGroup dataGroup,
			Map<String, Object> readConditions, List<Map<String, Object>> dbOrganisation) {
		List<DbStatement> dbStatements = new ArrayList<>();
		dbStatements.add(createDbStatementForOrganisationUpdate());
		dbStatements.addAll(generateDbStatementsForAlternativeName(dataGroup, dbOrganisation));
		dbStatements.addAll(generateDbStatementsForAddress(dataGroup, dbOrganisation));
		dbStatements.addAll(generateDbStatementsForParents(dataGroup, readConditions));
		dbStatements.addAll(generateDbStatementsForPredecessors(dataGroup, readConditions));
		return dbStatements;
	}

	private DbStatement createDbStatementForOrganisationUpdate() {
		return new DbStatement("update", "organisation", organisationValues,
				organisationConditions);
	}

	private List<DbStatement> generateDbStatementsForAlternativeName(DataGroup dataGroup,
			List<Map<String, Object>> dbOrganisation) {
		RelatedTable alternativeName = relatedTableFactory.factor("organisationAlternativeName");
		return alternativeName.handleDbForDataGroup(dataGroup, dbOrganisation);
	}

	private List<DbStatement> generateDbStatementsForAddress(DataGroup dataGroup,
			List<Map<String, Object>> dbOrganisation) {
		RelatedTable addressTable = relatedTableFactory.factor("organisationAddress");
		return addressTable.handleDbForDataGroup(dataGroup, dbOrganisation);
	}

	private List<DbStatement> generateDbStatementsForParents(DataGroup dataGroup,
			Map<String, Object> readConditions) {
		List<Map<String, Object>> dbParents = recordReader
				.readFromTableUsingConditions("organisation_parent", readConditions);
		RelatedTable parent = relatedTableFactory.factor("organisationParent");
		return parent.handleDbForDataGroup(dataGroup, dbParents);
	}

	private List<DbStatement> generateDbStatementsForPredecessors(DataGroup dataGroup,
			Map<String, Object> readConditions) {
		List<Map<String, Object>> dbPredecessors = recordReader
				.readFromTableUsingConditions("divaorganisationpredecessor", readConditions);
		RelatedTable predecessor = relatedTableFactory.factor("organisationPredecessor");
		return predecessor.handleDbForDataGroup(dataGroup, dbPredecessors);
	}

	private void tryUpdateDatabaseWithGivenDbStatements(List<DbStatement> dbStatements) {
		try (Connection connection = connectionProvider.getConnection();) {
			tryUpdateDatabaseWithGivenDbStatementsUsingConnection(dbStatements, connection);
		} catch (Exception e) {
			throw SqlStorageException.withMessageAndException(
					"Error executing prepared statement: " + e.getMessage(), e);
		}
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
		connection.setAutoCommit(false);
		statementExecutor.executeDbStatmentUsingConnection(dbStatements, connection);
		connection.commit();
	}

	public DataToDbTranslater getDataToDbTranslater() {
		// needed for test
		return organisationToDbTranslater;
	}

	public RelatedTableFactory getRelatedTableFactory() {
		// needed for test
		return relatedTableFactory;
	}

	public RecordReaderFactory getRecordReaderFactory() {
		// needed for test
		return recordReaderFactory;
	}

	public SqlConnectionProvider getSqlConnectionProvider() {
		// needed for test
		return connectionProvider;
	}

	public StatementExecutor getPreparedStatementCreator() {
		// needed for test
		return statementExecutor;
	}

	public DataReader getDataReader() {
		// needed for test
		return dataReader;
	}

}
