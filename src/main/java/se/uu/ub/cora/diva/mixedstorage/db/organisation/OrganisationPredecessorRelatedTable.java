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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DbStatement;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTable;
import se.uu.ub.cora.sqldatabase.Row;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;
import se.uu.ub.cora.sqldatabase.table.TableFacade;

public class OrganisationPredecessorRelatedTable extends OrganisationRelatedTable
		implements RelatedTable {

	private static final String DESCRIPTION = "description";
	private static final String INSERT = "insert";
	private static final String DELETE = "delete";
	private static final String INTERNAL_NOTE = "internalNote";
	private static final String ORGANISATION_PREDECESSOR_DESCRIPTION = "organisation_predecessor_description";
	private static final String PREDECESSOR_ID = "predecessor_id";
	private static final String ORGANISATION_PREDECESSOR_ID = "organisation_predecessor_id";
	private static final String ORGANISATION_ID = "organisation_id";
	private static final String ORGANISATION_PREDECESSOR = "organisation_predecessor";
	private Map<String, DataGroup> predecessorsInDataGroup;
	private Map<Integer, Row> mapWithPredecessorAsKey;

	public OrganisationPredecessorRelatedTable(SqlDatabaseFactory sqlDatabaseFactory) {
		this.sqlDatabaseFactory = sqlDatabaseFactory;
	}

	@Override
	public List<DbStatement> handleDbForDataGroup(DataGroup organisation,
			List<Row> existingPredecessors) {
		setIdAsInt(organisation);

		mapWithPredecessorAsKey = new HashMap<>(existingPredecessors.size());
		for (Row dbRow : existingPredecessors) {
			int predecessorId = (int) dbRow.getValueByColumn(ORGANISATION_PREDECESSOR_ID);
			mapWithPredecessorAsKey.put(predecessorId, dbRow);
		}
		List<DbStatement> dbStatements = new ArrayList<>();
		populateCollectionWithPredecessorsFromDataGroup(organisation);
		Set<String> predecessorIdsInDataGroup = createSetWithPredecessorsInDataGroupIds();

		if (predecessorIdsInDataGroup.isEmpty()) {
			deletePredecessors(dbStatements, existingPredecessors);
		} else {
			handleDeleteAndCreate(dbStatements, existingPredecessors, predecessorIdsInDataGroup);

		}
		return dbStatements;
	}

	private void populateCollectionWithPredecessorsFromDataGroup(DataGroup organisation) {
		List<DataGroup> predecessors = organisation
				.getAllGroupsWithNameInData("earlierOrganisation");
		predecessorsInDataGroup = new HashMap<>(predecessors.size());
		for (DataGroup predecessor : predecessors) {
			String predecessorId = extractPredecessorId(predecessor);
			predecessorsInDataGroup.put(predecessorId, predecessor);
		}
	}

	private Set<String> createSetWithPredecessorsInDataGroupIds() {
		Set<String> predecessorIds = new HashSet<>();
		predecessorIds.addAll(predecessorsInDataGroup.keySet());
		return predecessorIds;
	}

	@Override
	protected void handleDeleteAndCreate(List<DbStatement> dbStatements,
			List<Row> existingPredecessors, Set<String> idsFromDataGroup) {

		Set<String> idsInDatabase = getIdsForCurrentRowsInDatabase(existingPredecessors);
		Set<String> existsInDbAndDataGroup = createSetWithPredecessorsInDataGroupIds();
		existsInDbAndDataGroup.retainAll(idsInDatabase);

		if (idsInDatabase.isEmpty()) {
			addToDb(dbStatements, idsFromDataGroup);
		} else {
			Set<String> originalIdsFromDataGroup = Set.copyOf(idsFromDataGroup);
			addDataFromDataGroupNotAlreadyInDb(dbStatements, idsFromDataGroup, idsInDatabase);
			removeRowsNoLongerPresentInDataGroup(dbStatements, idsInDatabase,
					originalIdsFromDataGroup);
			handlePredecessorDescriptions(dbStatements, existsInDbAndDataGroup);
		}
	}

	private String extractPredecessorId(DataGroup predecessor) {
		DataGroup organisationLink = predecessor.getFirstGroupWithNameInData("organisationLink");
		return organisationLink.getFirstAtomicValueWithNameInData("linkedRecordId");
	}

	private void deletePredecessors(List<DbStatement> dbStatements, List<Row> predecessors) {
		for (Row readRow : predecessors) {
			int predecessorId = (int) readRow.getValueByColumn(ORGANISATION_PREDECESSOR_ID);
			createDeleteForPredecessorAndDescription(dbStatements, predecessorId);
		}
	}

	private void createDeleteForPredecessorAndDescription(List<DbStatement> dbStatements,
			int predecessorId) {
		dbStatements.add(createDeleteForPredecessorDescription(predecessorId));
		dbStatements.add(createDeleteForPredecessor(predecessorId));
	}

	private DbStatement createDeleteForPredecessorDescription(int predecessorId) {
		Map<String, Object> deleteConditions = createConditionWithOrganisationId();
		deleteConditions.put(PREDECESSOR_ID, predecessorId);
		return new DbStatement(DELETE, ORGANISATION_PREDECESSOR_DESCRIPTION, Collections.emptyMap(),
				deleteConditions);

	}

	private Map<String, Object> createConditionWithOrganisationId() {
		Map<String, Object> deleteConditions = new HashMap<>();
		deleteConditions.put(ORGANISATION_ID, organisationId);
		return deleteConditions;
	}

	private DbStatement createDeleteForPredecessor(int predecessorId) {
		Map<String, Object> deleteConditions = createConditionWithOrganisationId();
		deleteConditions.put(ORGANISATION_PREDECESSOR_ID, predecessorId);
		return new DbStatement(DELETE, ORGANISATION_PREDECESSOR, Collections.emptyMap(),
				deleteConditions);
	}

	@Override
	protected Set<String> getIdsForCurrentRowsInDatabase(List<Row> allCurrentPredecessorsInDb) {
		Set<String> predecessorIdsInDatabase = new HashSet<>(allCurrentPredecessorsInDb.size());
		for (Row readRow : allCurrentPredecessorsInDb) {
			predecessorIdsInDatabase
					.add(String.valueOf(readRow.getValueByColumn(ORGANISATION_PREDECESSOR_ID)));
		}
		return predecessorIdsInDatabase;
	}

	@Override
	protected void addToDb(List<DbStatement> dbStatments, Set<String> predecessorIdsInDataGroup) {
		for (String predecessorId : predecessorIdsInDataGroup) {
			Map<String, Object> values = createValuesForPredecessorInsert(predecessorId);
			dbStatments.add(new DbStatement(INSERT, ORGANISATION_PREDECESSOR, values,
					Collections.emptyMap()));
			possiblyAddPredecessorDescription(dbStatments, predecessorId);

		}
	}

	private void possiblyAddPredecessorDescription(List<DbStatement> dbStatments,
			String predecessorId) {
		DataGroup dataGroup = predecessorsInDataGroup.get(predecessorId);
		if (dataGroup.containsChildWithNameInData(INTERNAL_NOTE)) {
			String comment = dataGroup.getFirstAtomicValueWithNameInData(INTERNAL_NOTE);
			Map<String, Object> values = createValuesForDescriptionCreate(predecessorId, comment);
			dbStatments.add(new DbStatement(INSERT, ORGANISATION_PREDECESSOR_DESCRIPTION, values,
					Collections.emptyMap()));

		}
	}

	private Map<String, Object> createValuesForDescriptionCreate(String predecessorId,
			String comment) {
		Map<String, Object> descriptionValues = createConditionsForPredecessorDescription(
				predecessorId);
		try (TableFacade factorTableFacade = sqlDatabaseFactory.factorTableFacade()) {
			long nextVal = factorTableFacade
					.nextValueFromSequence("organisation_predecessor_description_sequence");
			descriptionValues.put(ORGANISATION_PREDECESSOR_ID, nextVal);
		}
		descriptionValues.put("last_updated", getCurrentTimestamp());
		descriptionValues.put(DESCRIPTION, comment);
		return descriptionValues;
	}

	@Override
	protected void addDataFromDataGroupNotAlreadyInDb(List<DbStatement> dbStatements,
			Set<String> predecessorIdsInDataGroup, Set<String> predecessorIdsInDatabase) {
		predecessorIdsInDataGroup.removeAll(predecessorIdsInDatabase);
		addToDb(dbStatements, predecessorIdsInDataGroup);
	}

	@Override
	protected void removeRowsNoLongerPresentInDataGroup(List<DbStatement> dbStatements,
			Set<String> predecessorsIdsInDatabase, Set<String> originalPredecessorsInDataGroup) {
		predecessorsIdsInDatabase.removeAll(originalPredecessorsInDataGroup);
		for (String predecessorId : predecessorsIdsInDatabase) {
			createDeleteForPredecessorAndDescription(dbStatements, Integer.parseInt(predecessorId));
		}
	}

	private Map<String, Object> createValuesForPredecessorInsert(String predecessorId) {
		Map<String, Object> values = createConditionWithOrganisationId();
		values.put(ORGANISATION_PREDECESSOR_ID, Integer.valueOf(predecessorId));
		return values;
	}

	private void handlePredecessorDescriptions(List<DbStatement> dbStatements,
			Set<String> existsInDbAndDataGroup) {

		for (String id : existsInDbAndDataGroup) {
			handlePredecessorDescriptionForPredecessorId(dbStatements, id);
		}
	}

	private void handlePredecessorDescriptionForPredecessorId(List<DbStatement> dbStatements,
			String predecessorId) {
		DataGroup dataGroup = predecessorsInDataGroup.get(predecessorId);
		Map<String, Object> conditions = createConditionsForPredecessorDescription(predecessorId);
		Row readDescription = mapWithPredecessorAsKey.get(Integer.valueOf(predecessorId));
		if (dataGroup.containsChildWithNameInData(INTERNAL_NOTE)) {
			handleDeleteAndCreatedForDescription(dbStatements, predecessorId, dataGroup, conditions,
					readDescription);
		} else {
			deleteDescriptionInDbIfExists(dbStatements, conditions, readDescription);
		}
	}

	private void handleDeleteAndCreatedForDescription(List<DbStatement> dbStatements, String id,
			DataGroup dataGroup, Map<String, Object> conditions, Row readDescription) {
		String descriptionInDataGroup = dataGroup.getFirstAtomicValueWithNameInData(INTERNAL_NOTE);
		if (!predecessorHasDescription(readDescription)) {
			createNewDescriptionInDb(dbStatements, id, descriptionInDataGroup);
		} else {
			String description = (String) readDescription.getValueByColumn(DESCRIPTION);
			if (!description.equals(descriptionInDataGroup)) {
				deleteDescriptionFromDbUsingConditions(dbStatements, conditions);
				createNewDescriptionInDb(dbStatements, id, descriptionInDataGroup);
			}
		}
	}

	private void deleteDescriptionInDbIfExists(List<DbStatement> dbStatements,
			Map<String, Object> conditions, Row readDescription) {
		if (predecessorHasDescription(readDescription)) {
			deleteDescriptionFromDbUsingConditions(dbStatements, conditions);
		}
	}

	private boolean predecessorHasDescription(Row readDescription) {
		return readDescription.hasColumnWithNonEmptyValue(DESCRIPTION);
	}

	private void deleteDescriptionFromDbUsingConditions(List<DbStatement> dbStatements,
			Map<String, Object> conditions) {
		dbStatements.add(new DbStatement(DELETE, ORGANISATION_PREDECESSOR_DESCRIPTION,
				Collections.emptyMap(), conditions));
	}

	private void createNewDescriptionInDb(List<DbStatement> dbStatements, String id,
			String descriptionInDataGroup) {
		Map<String, Object> values = createValuesForDescriptionCreate(id, descriptionInDataGroup);
		dbStatements.add(new DbStatement(INSERT, ORGANISATION_PREDECESSOR_DESCRIPTION, values,
				Collections.emptyMap()));
	}

	private Map<String, Object> createConditionsForPredecessorDescription(String id) {
		Map<String, Object> conditions = new HashMap<>();
		conditions.put(ORGANISATION_ID, organisationId);
		conditions.put(PREDECESSOR_ID, Integer.valueOf(id));
		return conditions;
	}

	public SqlDatabaseFactory getSqlDatabaseFactory() {
		return sqlDatabaseFactory;
	}

}
