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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbHelper;
import se.uu.ub.cora.diva.mixedstorage.db.DbException;
import se.uu.ub.cora.diva.mixedstorage.db.DbStatement;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTable;
import se.uu.ub.cora.sqldatabase.Row;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;
import se.uu.ub.cora.sqldatabase.table.TableFacade;

public class OrganisationAlternativeNameRelatedTable implements RelatedTable {

	private static final String ORGANISATION_NAME_ID = "organisation_name_id";
	private static final String ORGANISATION_NAME = "organisation_name";
	private static final String ALTERNATIVE_NAME = "organisationAlternativeName";
	private SqlDatabaseFactory sqlDatabaseFactory;
	private Row alternativeNameRow;

	public OrganisationAlternativeNameRelatedTable(SqlDatabaseFactory sqlDatabaseFactory) {
		this.sqlDatabaseFactory = sqlDatabaseFactory;
	}

	@Override
	public List<DbStatement> handleDbForDataGroup(DataGroup organisation,
			List<Row> alternativeNameRows) {
		throwExceptionIfAlternativeNameIsMissing(organisation);
		throwErrorIfMoreThanOneAlternativeNameInDb(alternativeNameRows);
		alternativeNameRow = getRowIfOnlyOneOrEmptyMap(alternativeNameRows);
		String organisationId = getOrganisationId(organisation);
		List<DbStatement> dbStatements = new ArrayList<>();

		handleAlternativeName(organisation, organisationId, dbStatements);

		return dbStatements;
	}

	private void throwExceptionIfAlternativeNameIsMissing(DataGroup organisation) {
		if (!organisationHaveAlternativeName(organisation)) {
			throw DbException.withMessage("Organisation must have alternative name");
		}
	}

	private boolean organisationHaveAlternativeName(DataGroup organisation) {
		return organisation.containsChildWithNameInData(ALTERNATIVE_NAME)
				&& alternativeNameContainsValueForName(organisation);

	}

	private boolean alternativeNameContainsValueForName(DataGroup organisation) {
		DataGroup alternativeNameGroup = organisation.getFirstGroupWithNameInData(ALTERNATIVE_NAME);
		return alternativeNameGroup.containsChildWithNameInData("name");
	}

	private void throwErrorIfMoreThanOneAlternativeNameInDb(List<Row> alternativeNameRows) {
		if (alternativeNameRows.size() > 1) {
			throw DbException
					.withMessage("Organisation can not have more than one alternative name");
		}
	}

	private Row getRowIfOnlyOneOrEmptyMap(List<Row> readRows) {
		return readRows.size() == 1 ? readRows.get(0) : null;
	}

	private String getOrganisationId(DataGroup organisation) {
		String organisationId = DataToDbHelper.extractIdFromDataGroup(organisation);
		DataToDbHelper.throwDbExceptionIfIdNotAnIntegerValue(organisationId);
		return organisationId;
	}

	private void handleAlternativeName(DataGroup organisation, String organisationId,
			List<DbStatement> dbStatements) {

		if (alternativeNameExistsInDatabase(alternativeNameRow)) {
			handleUpdate(organisation, organisationId, dbStatements);
		} else {
			handleInsert(dbStatements, organisation, organisationId);
		}
	}

	private boolean alternativeNameExistsInDatabase(Row readRow) {
		return readRow != null;
	}

	private void handleUpdate(DataGroup organisation, String organisationId,
			List<DbStatement> dbStatements) {
		boolean nameInDataGroupDiffersFromNameInDb = nameInDbNotSameAsNameInDataGroup(organisation);
		if (nameInDataGroupDiffersFromNameInDb) {
			int nameId = (int) alternativeNameRow.getValueByColumn(ORGANISATION_NAME_ID);
			updateAlternativeName(dbStatements, organisation, organisationId, nameId);
		}
	}

	private boolean nameInDbNotSameAsNameInDataGroup(DataGroup organisation) {
		String nameOfOrganisation = getAlternativeNameFromOrganisation(organisation);
		String organisationNameInDb = (String) alternativeNameRow
				.getValueByColumn(ORGANISATION_NAME);
		return !nameOfOrganisation.equals(organisationNameInDb);
	}

	private String getAlternativeNameFromOrganisation(DataGroup organisation) {
		DataGroup alternativeNameGroup = organisation.getFirstGroupWithNameInData(ALTERNATIVE_NAME);
		return alternativeNameGroup.getFirstAtomicValueWithNameInData("name");
	}

	private void updateAlternativeName(List<DbStatement> dbStatements, DataGroup organisation,
			String organisationId, int nameId) {
		Map<String, Object> values = generateValues(organisation, organisationId);
		Map<String, Object> conditions = new HashMap<>();
		conditions.put(ORGANISATION_NAME_ID, nameId);
		dbStatements.add(new DbStatement("update", ORGANISATION_NAME, values, conditions));
	}

	private Map<String, Object> generateValues(DataGroup organisation, String organisationId) {
		Map<String, Object> values = new HashMap<>();
		values.put("locale", "en");
		values.put("organisation_id", Integer.valueOf(organisationId));
		values.put("last_updated", getCurrentTimestamp());
		values.put(ORGANISATION_NAME, getAlternativeNameFromOrganisation(organisation));
		return values;
	}

	private Timestamp getCurrentTimestamp() {
		Date today = new Date();
		long time = today.getTime();
		return new Timestamp(time);
	}

	private void handleInsert(List<DbStatement> dbStatements, DataGroup organisation,
			String organisationId) {
		try (TableFacade tableFacade = sqlDatabaseFactory.factorTableFacade()) {
			Map<String, Object> values = generateValues(organisation, organisationId);
			addOrganisationNameIdNextValue(tableFacade, values);
			dbStatements.add(
					new DbStatement("insert", ORGANISATION_NAME, values, Collections.emptyMap()));
		}
	}

	private void addOrganisationNameIdNextValue(TableFacade tableFacade,
			Map<String, Object> values) {
		long nextValue = tableFacade.nextValueFromSequence("name_sequence");
		values.put(ORGANISATION_NAME_ID, nextValue);
	}

	public SqlDatabaseFactory getSqlDatabaseFactory() {
		return sqlDatabaseFactory;
	}
}
