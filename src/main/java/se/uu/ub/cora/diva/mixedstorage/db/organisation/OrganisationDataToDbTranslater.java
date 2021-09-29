/*
 * Copyright 2019, 2021 Uppsala University Library
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

import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbHelper;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbTranslater;
import se.uu.ub.cora.sqldatabase.Row;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;
import se.uu.ub.cora.sqldatabase.table.TableFacade;
import se.uu.ub.cora.sqldatabase.table.TableQuery;

public class OrganisationDataToDbTranslater implements DataToDbTranslater {

	private static final int ROOT_ORGANISATION_TYPE_ID = 49;
	private Map<String, Object> values = new HashMap<>();
	private Map<String, Object> conditions = new HashMap<>(1);
	private DataGroup dataGroup;
	private SqlDatabaseFactory sqlDatabaseFactory;
	private TableFacade tableFacade;

	public OrganisationDataToDbTranslater(SqlDatabaseFactory sqlDatabaseFactory) {
		this.sqlDatabaseFactory = sqlDatabaseFactory;
		this.tableFacade = sqlDatabaseFactory.factorTableFacade();
	}

	@Override
	public void translate(DataGroup dataGroup) {
		this.dataGroup = dataGroup;
		values = new HashMap<>();
		conditions = new HashMap<>(1);

		createConditionsAddingOrganisationId();
		createColumnsWithValuesForUpdateQuery();
	}

	private Map<String, Object> createConditionsAddingOrganisationId() {
		String id = DataToDbHelper.extractIdFromDataGroup(dataGroup);
		DataToDbHelper.throwDbExceptionIfIdNotAnIntegerValue(id);
		conditions.put("organisation_id", Integer.valueOf(id));
		return conditions;
	}

	private Map<String, Object> createColumnsWithValuesForUpdateQuery() {
		addOrganisationNameToColumns();
		addAtomicValuesToColumns();
		addSelectable();
		addShowInPortal();
		addDoctoralDegreeGrantor();
		addTopLevel();
		values.put("last_updated", getCurrentTimestamp());
		addOrganisationType();
		return values;
	}

	private void addOrganisationNameToColumns() {
		DataGroup nameGroup = dataGroup.getFirstGroupWithNameInData("organisationName");
		String name = nameGroup.getFirstAtomicValueWithNameInData("name");
		values.put("organisation_name", name);
		String language = nameGroup.getFirstAtomicValueWithNameInData("language");
		values.put("organisation_name_locale", language);
	}

	private void addAtomicValuesToColumns() {
		for (OrganisationAtomicColumns column : OrganisationAtomicColumns.values()) {
			addAtomicValueOrNullToColumn(column);
		}
	}

	private void addAtomicValueOrNullToColumn(OrganisationAtomicColumns column) {
		String coraName = column.coraName;
		String dbColumnName = column.dbName;
		if (!dataGroupHasValue(coraName)) {
			values.put(dbColumnName, null);
		} else {
			handleAndAddValue(coraName, dbColumnName, column);
		}

	}

	private boolean dataGroupHasValue(String coraName) {
		return dataGroup.containsChildWithNameInData(coraName);
	}

	private void handleAndAddValue(String coraName, String dbColumnName,
			OrganisationAtomicColumns column) {
		String type = column.type;
		String value = dataGroup.getFirstAtomicValueWithNameInData(coraName);
		if ("date".equals(type)) {
			addDataValue(dbColumnName, value);
		} else {
			values.put(dbColumnName, value);
		}
	}

	private void addDataValue(String dbColumnName, String value) {
		Date valueAsDate = Date.valueOf(value);
		values.put(dbColumnName, valueAsDate);
	}

	private void addSelectable() {
		boolean notEligible = false;
		if (selectableIsNoOrMissing()) {
			notEligible = true;
		}
		values.put("not_eligible", notEligible);
	}

	private boolean selectableIsNoOrMissing() {
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData("recordInfo");
		return !recordInfo.containsChildWithNameInData("selectable")
				|| "no".equals(recordInfo.getFirstAtomicValueWithNameInData("selectable"));
	}

	private void addShowInPortal() {
		boolean showInPortal = isTopLevel();
		values.put("show_in_portal", showInPortal);
	}

	private boolean booleanValueExistsAndIsTrue(String nameInData) {
		return dataGroup.containsChildWithNameInData(nameInData)
				&& "yes".equals(dataGroup.getFirstAtomicValueWithNameInData(nameInData));
	}

	private void addDoctoralDegreeGrantor() {
		translateStringToBooleanAndAddToValues("doctoralDegreeGrantor", "show_in_defence");
	}

	private void translateStringToBooleanAndAddToValues(String nameInData, String columnName) {
		boolean booleanValue = booleanValueExistsAndIsTrue(nameInData);
		values.put(columnName, booleanValue);
	}

	private void addTopLevel() {
		boolean isTopLevel = isTopLevel();
		values.put("top_level", isTopLevel);
	}

	private boolean isTopLevel() {
		String linkedRecordId = extractRecordType();
		return "topOrganisation".equals(linkedRecordId);
	}

	private String extractRecordType() {
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData("recordInfo");
		DataGroup type = recordInfo.getFirstGroupWithNameInData("type");
		return type.getFirstAtomicValueWithNameInData("linkedRecordId");
	}

	private Timestamp getCurrentTimestamp() {
		java.util.Date today = new java.util.Date();
		long time = today.getTime();
		return new Timestamp(time);
	}

	private void addOrganisationType() {
		Object typeId = isRootOrganisation() ? ROOT_ORGANISATION_TYPE_ID
				: getTypeCodeForOrganisationType();
		values.put("organisation_type_id", typeId);
	}

	private boolean isRootOrganisation() {
		return "rootOrganisation".equals(extractRecordType());
	}

	private Object getTypeCodeForOrganisationType() {
		TableQuery tableQuery = getSqlDatabaseFactory().factorTableQuery("organisation_type");
		tableQuery.addCondition("organisation_type_code",
				dataGroup.getFirstAtomicValueWithNameInData("organisationType"));
		Row readRow = tableFacade.readOneRowForQuery(tableQuery);
		return readRow.getValueByColumn("organisation_type_id");
	}

	@Override
	public Map<String, Object> getConditions() {
		return conditions;
	}

	@Override
	public Map<String, Object> getValues() {
		return values;
	}

	public SqlDatabaseFactory getSqlDatabaseFactory() {
		return sqlDatabaseFactory;
	}
}
