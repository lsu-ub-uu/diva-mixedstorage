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

public class OrganisationParentRelatedTable extends OrganisationRelatedTable
		implements RelatedTable {

	private static final String ORGANISATION_PARENT = "organisation_parent";
	private static final String ORGANISATION_PARENT_ID = "organisation_parent_id";
	private static final String ORGANISATION_ID = "organisation_id";

	public OrganisationParentRelatedTable(SqlDatabaseFactory sqlDatabaseFactory) {
		this.sqlDatabaseFactory = sqlDatabaseFactory;
	}

	@Override
	public List<DbStatement> handleDbForDataGroup(DataGroup organisation,
			List<Row> existingParents) {

		setIdAsInt(organisation);
		List<DbStatement> dbStatements = new ArrayList<>();
		Set<String> parentsInDataGroup = getParentIdsInDataGroup(organisation);

		if (noParentsToHandle(existingParents, parentsInDataGroup)) {
			return dbStatements;
		}

		if (noParentsInDataGroup(parentsInDataGroup)) {
			createDeleteStatements(dbStatements, existingParents);
		} else {
			handleDeleteAndCreate(dbStatements, existingParents, parentsInDataGroup);
		}

		return dbStatements;
	}

	private boolean noParentsToHandle(List<Row> existingParents, Set<String> parentsInDataGroup) {
		return existingParents.isEmpty() && noParentsInDataGroup(parentsInDataGroup);
	}

	private boolean noParentsInDataGroup(Set<String> parentsInDataGroup) {
		return parentsInDataGroup.isEmpty();
	}

	private void createDeleteStatements(List<DbStatement> dbStatements, List<Row> parents) {
		for (Row readRow : parents) {
			int parentId = (int) readRow.getValueByColumn(ORGANISATION_PARENT_ID);
			dbStatements.add(createDeleteStatement(parentId));
		}
	}

	private DbStatement createDeleteStatement(int parentId) {
		Map<String, Object> deleteConditions = new HashMap<>();
		deleteConditions.put(ORGANISATION_ID, organisationId);
		deleteConditions.put(ORGANISATION_PARENT_ID, parentId);
		return new DbStatement("delete", ORGANISATION_PARENT, Collections.emptyMap(),
				deleteConditions);
	}

	private Set<String> getParentIdsInDataGroup(DataGroup organisation) {
		List<DataGroup> parents = organisation.getAllGroupsWithNameInData("parentOrganisation");
		Set<String> parentIds = new HashSet<>(parents.size());
		for (DataGroup parent : parents) {
			String parentId = extractParentId(parent);
			parentIds.add(parentId);
		}
		return parentIds;
	}

	private String extractParentId(DataGroup parent) {
		DataGroup organisationLink = parent.getFirstGroupWithNameInData("organisationLink");
		return organisationLink.getFirstAtomicValueWithNameInData("linkedRecordId");
	}

	@Override
	protected Set<String> getIdsForCurrentRowsInDatabase(List<Row> allCurrentParentsInDb) {
		Set<String> parentIdsInDatabase = new HashSet<>(allCurrentParentsInDb.size());
		for (Row readRow : allCurrentParentsInDb) {
			parentIdsInDatabase
					.add(String.valueOf(readRow.getValueByColumn(ORGANISATION_PARENT_ID)));
		}
		return parentIdsInDatabase;
	}

	@Override
	protected void addToDb(List<DbStatement> dbStatements, Set<String> parentIdsInDataGroup) {
		for (String parentId : parentIdsInDataGroup) {
			DbStatement dbStatement = createInsertStatement(parentId);
			dbStatements.add(dbStatement);
		}
	}

	private DbStatement createInsertStatement(String parentId) {
		Map<String, Object> values = createValuesForParentInsert(parentId);
		return new DbStatement("insert", ORGANISATION_PARENT, values, Collections.emptyMap());
	}

	private Map<String, Object> createValuesForParentInsert(String parentId) {
		Map<String, Object> values = new HashMap<>();
		values.put(ORGANISATION_ID, organisationId);
		values.put(ORGANISATION_PARENT_ID, Integer.valueOf(parentId));
		return values;
	}

	@Override
	protected void addDataFromDataGroupNotAlreadyInDb(List<DbStatement> dbStatements,
			Set<String> parentIdsInDataGroup, Set<String> parentIdsInDatabase) {
		parentIdsInDataGroup.removeAll(parentIdsInDatabase);
		addToDb(dbStatements, parentIdsInDataGroup);
	}

	@Override
	protected void removeRowsNoLongerPresentInDataGroup(List<DbStatement> dbStatements,
			Set<String> parentIdsInDatabase, Set<String> originalParentsInDataGroup) {
		parentIdsInDatabase.removeAll(originalParentsInDataGroup);
		for (String parentId : parentIdsInDatabase) {
			dbStatements.add(createDeleteStatement(Integer.parseInt(parentId)));
		}
	}

	public SqlDatabaseFactory getSqlDatabaseFactory() {
		return sqlDatabaseFactory;
	}

}
