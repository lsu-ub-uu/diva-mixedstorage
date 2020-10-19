/*
 * Copyright 2019 Uppsala University Library
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
import se.uu.ub.cora.sqldatabase.RecordReader;

public class OrganisationDataToDbTranslater implements DataToDbTranslater {

	private Map<String, Object> values = new HashMap<>();
	private Map<String, Object> conditions = new HashMap<>(1);
	private DataGroup dataGroup;
	private RecordReader recordReader;

	public OrganisationDataToDbTranslater(RecordReader recordReader) {
		this.recordReader = recordReader;
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
		addEligible();
		addShowInPortal();
		addShowInDefence();
		addTopLevel();
		values.put("last_updated", getCurrentTimestamp());
		addOrgansiationType();
		return values;
	}

	private void addOrganisationNameToColumns() {
		DataGroup nameGroup = dataGroup.getFirstGroupWithNameInData("name");
		String name = nameGroup.getFirstAtomicValueWithNameInData("organisationName");
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

	private void addEligible() {
		boolean notEligible = false;
		if (eligibleIsNoOrMissing()) {
			notEligible = true;
		}
		values.put("not_eligible", notEligible);
	}

	private boolean eligibleIsNoOrMissing() {
		return !dataGroup.containsChildWithNameInData("eligible")
				|| "no".equals(dataGroup.getFirstAtomicValueWithNameInData("eligible"));
	}

	private void addShowInPortal() {
		translateStringToBooleanAndAddToValues("showInPortal", "show_in_portal");
	}

	private boolean booleanValueExistsAndIsTrue(String nameInData) {
		return dataGroup.containsChildWithNameInData(nameInData)
				&& "yes".equals(dataGroup.getFirstAtomicValueWithNameInData(nameInData));
	}

	private void addShowInDefence() {
		translateStringToBooleanAndAddToValues("showInDefence", "show_in_defence");
	}

	private void translateStringToBooleanAndAddToValues(String nameInData, String columnName) {
		boolean booleanValue = booleanValueExistsAndIsTrue(nameInData);
		values.put(columnName, booleanValue);
	}

	private void addTopLevel() {
		translateStringToBooleanAndAddToValues("topLevel", "top_level");
	}

	private Timestamp getCurrentTimestamp() {
		java.util.Date today = new java.util.Date();
		long time = today.getTime();
		return new Timestamp(time);

	}

	private void addOrgansiationType() {
		Object typeId = getTypeCodeForOrganisationType();
		values.put("organisation_type_id", typeId);
	}

	private Object getTypeCodeForOrganisationType() {
		Map<String, Object> conditionsForReadType = createConditionsForReadingType();

		Map<String, Object> organisationTypeRow = recordReader
				.readOneRowFromDbUsingTableAndConditions("organisation_type",
						conditionsForReadType);
		return organisationTypeRow.get("organisation_type_id");
	}

	private Map<String, Object> createConditionsForReadingType() {
		Map<String, Object> conditionsForReadType = new HashMap<>();
		conditionsForReadType.put("organisation_type_code",
				dataGroup.getFirstAtomicValueWithNameInData("organisationType"));
		return conditionsForReadType;
	}

	@Override
	public Map<String, Object> getConditions() {
		return conditions;
	}

	@Override
	public Map<String, Object> getValues() {
		return values;
	}

	public RecordReader getRecordReader() {
		// needed for test
		return recordReader;
	}

}
