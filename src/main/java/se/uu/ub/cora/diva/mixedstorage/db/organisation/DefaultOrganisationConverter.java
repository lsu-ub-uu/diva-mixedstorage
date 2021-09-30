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

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.data.DataRecordLinkProvider;
import se.uu.ub.cora.diva.mixedstorage.db.ConversionException;
import se.uu.ub.cora.sqldatabase.DatabaseValues;
import se.uu.ub.cora.sqldatabase.Row;

public class DefaultOrganisationConverter implements DefaultConverter {

	private static final String ORGANISATION_ID = "id";
	private static final String ALTERNATIVE_NAME = "alternative_name";
	protected Row dbRow;
	protected DataGroup organisation;
	private String recordType;

	@Override
	public DataGroup fromMap(Row dbRow) {
		this.dbRow = dbRow;
		if (organisationIsEmpty()) {
			throw ConversionException.withMessageAndException(
					"Error converting organisation to Cora organisation: Map does not contain value for "
							+ ORGANISATION_ID,
					null);
		}
		setRecordType();
		return createDataGroup();
	}

	private void setRecordType() {
		recordType = "subOrganisation";
		possiblyChangeRecordType();
	}

	private void possiblyChangeRecordType() {
		String typeCode = (String) dbRow.getValueByColumn("type_code");
		if ("root".equals(typeCode)) {
			recordType = "rootOrganisation";
		} else {
			possiblySetRecordTypeToTopLevel();
		}
	}

	private void possiblySetRecordTypeToTopLevel() {
		boolean topLevel = (boolean) dbRow.getValueByColumn("top_level");
		if (topLevel) {
			recordType = "topOrganisation";
		}
	}

	private boolean organisationIsEmpty() {
		Object organisationId = dbRow.getValueByColumn(ORGANISATION_ID);
		return organisationId == null || "".equals(organisationId);
	}

	private DataGroup createDataGroup() {
		createAndAddOrganisationWithRecordInfo();
		createAndAddName();
		createAndAddAlternativeName();
		possiblyCreateAndAddClosedDate();
		return organisation;
	}

	private void createAndAddOrganisationWithRecordInfo() {
		organisation = DataGroupProvider.getDataGroupUsingNameInData("organisation");
		String id = String.valueOf(dbRow.getValueByColumn(ORGANISATION_ID));
		DataGroup recordInfo = createRecordInfo(recordType, id);
		organisation.addChild(recordInfo);
	}

	private DataGroup createRecordInfo(String recordType, String id) {
		DataGroup recordInfo = DataGroupProvider.getDataGroupUsingNameInData("recordInfo");
		recordInfo.addChild(DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("id", id));
		createAndAddType(recordInfo, recordType);
		createAndAddDataDivider(recordInfo);
		createAndAddCreatedAndUpdatedInfo(recordInfo);
		createAndAddDomain(recordInfo);
		createAndAddSelectable(recordInfo);
		return recordInfo;
	}

	private void createAndAddType(DataGroup recordInfo, String recordType) {
		DataGroup type = createLinkUsingNameInDataRecordTypeAndRecordId("type", "recordType",
				recordType);
		recordInfo.addChild(type);
	}

	private DataGroup createLinkUsingNameInDataRecordTypeAndRecordId(String nameInData,
			String linkedRecordType, String linkedRecordId) {
		DataGroup linkGroup = DataRecordLinkProvider.getDataRecordLinkUsingNameInData(nameInData);
		linkGroup.addChild(DataAtomicProvider
				.getDataAtomicUsingNameInDataAndValue("linkedRecordType", linkedRecordType));
		linkGroup.addChild(DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("linkedRecordId",
				linkedRecordId));
		return linkGroup;
	}

	private void createAndAddDataDivider(DataGroup recordInfo) {
		DataGroup dataDivider = createLinkUsingNameInDataRecordTypeAndRecordId("dataDivider",
				"system", "diva");
		recordInfo.addChild(dataDivider);
	}

	private void createAndAddCreatedAndUpdatedInfo(DataGroup recordInfo) {
		createAndAddCreatedInfo(recordInfo);
		createAndAddUpdatedInfo(recordInfo);
	}

	private void createAndAddCreatedInfo(DataGroup recordInfo) {
		DataGroup createdBy = createLinkUsingNameInDataRecordTypeAndRecordId("createdBy",
				"coraUser", "coraUser:4412982402853626");
		recordInfo.addChild(createdBy);
		addPredefinedTimestampToDataGroupUsingNameInData(recordInfo, "tsCreated");
	}

	private void createAndAddUpdatedInfo(DataGroup recordInfo) {
		DataGroup updated = DataGroupProvider.getDataGroupUsingNameInData("updated");
		DataGroup updatedBy = createLinkUsingNameInDataRecordTypeAndRecordId("updatedBy",
				"coraUser", "coraUser:4412982402853626");
		updated.addChild(updatedBy);
		addPredefinedTimestampToDataGroupUsingNameInData(updated, "tsUpdated");
		updated.setRepeatId("0");
		recordInfo.addChild(updated);
	}

	private void addPredefinedTimestampToDataGroupUsingNameInData(DataGroup recordInfo,
			String nameInData) {
		recordInfo.addChild(DataAtomicProvider.getDataAtomicUsingNameInDataAndValue(nameInData,
				"2017-01-01T00:00:00.000000Z"));
	}

	private void createAndAddSelectable(DataGroup recordInfo) {
		String selectableValue = getSelectableValue();
		DataAtomic selectable = DataAtomicProvider
				.getDataAtomicUsingNameInDataAndValue("selectable", selectableValue);
		recordInfo.addChild(selectable);
	}

	private String getSelectableValue() {
		Object notEligable = dbRow.getValueByColumn("not_eligible");
		return isSelectable(notEligable) ? "yes" : "no";
	}

	private boolean isSelectable(Object notEligable) {
		return !(boolean) notEligable;
	}

	private void createAndAddDomain(DataGroup recordInfo) {
		String domain = (String) dbRow.getValueByColumn("domain");
		recordInfo.addChild(
				DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("domain", domain));
	}

	private void createAndAddName() {
		DataGroup nameGroup = DataGroupProvider.getDataGroupUsingNameInData("organisationName");
		DataAtomic name = createAtomicDataUsingColumnNameAndNameInData("defaultname", "name");
		nameGroup.addChild(name);
		String nameLanguage = (String) dbRow.getValueByColumn("organisation_name_locale");
		nameGroup.addChild(
				DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("language", nameLanguage));
		organisation.addChild(nameGroup);
	}

	private DataAtomic createAtomicDataUsingColumnNameAndNameInData(String columnName,
			String nameInData) {
		String divaOrganisationName = (String) dbRow.getValueByColumn(columnName);
		return DataAtomicProvider.getDataAtomicUsingNameInDataAndValue(nameInData,
				divaOrganisationName);
	}

	private void createAndAddAlternativeName() {
		DataGroup alternativeNameDataGroup = DataGroupProvider
				.getDataGroupUsingNameInData("organisationAlternativeName");
		alternativeNameDataGroup.addChild(
				DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("language", "en"));
		String alternativeName = (String) dbRow.getValueByColumn(ALTERNATIVE_NAME);
		alternativeNameDataGroup.addChild(
				DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("name", alternativeName));
		organisation.addChild(alternativeNameDataGroup);
	}

	private void possiblyCreateAndAddClosedDate() {
		if (valueExistsForKey("closed_date")) {
			createAndAddClosedDate();
		}
	}

	private void createAndAddClosedDate() {
		String closedDate = getDateAsString();
		organisation.addChild(
				DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("closedDate", closedDate));
	}

	private String getDateAsString() {
		Date dbClosedDate = (Date) dbRow.getValueByColumn("closed_date");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.format(dbClosedDate);
	}

	protected boolean valueExistsForKey(String key) {
		Object value = dbRow.getValueByColumn(key);
		return value != null && !(value.equals(DatabaseValues.NULL)) && !"".equals(value);
	}

}
