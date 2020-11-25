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

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.mixedstorage.db.ConversionException;

public class DefaultOrganisationConverter implements DefaultConverter {

	private static final String ORGANISATION_ID = "id";
	private static final String ALTERNATIVE_NAME = "alternative_name";
	protected Map<String, Object> dbRow;
	protected DataGroup organisation;
	private String recordType;

	@Override
	public DataGroup fromMap(Map<String, Object> dbRow) {
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
		String typeCode = (String) dbRow.get("type_code");
		if ("root".equals(typeCode)) {
			recordType = "rootOrganisation";
		} else {
			possiblySetRecordTypeToTopLevel();
		}
	}

	private void possiblySetRecordTypeToTopLevel() {
		boolean topLevel = (boolean) dbRow.get("top_level");
		if (topLevel) {
			recordType = "topOrganisation";
		}
	}

	private boolean organisationIsEmpty() {
		Object organisationId = dbRow.get(ORGANISATION_ID);
		return organisationId == null || "".equals(organisationId);
	}

	private DataGroup createDataGroup() {
		createAndAddOrganisationWithRecordInfo();
		createAndAddDomain();
		createAndAddName();
		createAndAddAlternativeName();
		// createAndAddOrganisationType();
		possiblyCreateAndAddClosedDate();
		return organisation;
	}

	private void createAndAddOrganisationWithRecordInfo() {
		organisation = DataGroupProvider.getDataGroupUsingNameInData("organisation");
		String id = String.valueOf(dbRow.get(ORGANISATION_ID));
		DataGroup recordInfo = createRecordInfo(recordType, id);
		organisation.addChild(recordInfo);
	}

	private DataGroup createRecordInfo(String recordType, String id) {
		DataGroup recordInfo = DataGroupProvider.getDataGroupUsingNameInData("recordInfo");
		recordInfo.addChild(DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("id", id));
		createAndAddType(recordInfo, recordType);
		createAndAddDataDivider(recordInfo);
		createAndAddCreatedAndUpdatedInfo(recordInfo);
		return recordInfo;
	}

	private void createAndAddType(DataGroup recordInfo, String recordType) {
		DataGroup type = createLinkUsingNameInDataRecordTypeAndRecordId("type", "recordType",
				recordType);
		recordInfo.addChild(type);
	}

	private DataGroup createLinkUsingNameInDataRecordTypeAndRecordId(String nameInData,
			String linkedRecordType, String linkedRecordId) {
		DataGroup linkGroup = DataGroupProvider.getDataGroupUsingNameInData(nameInData);
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

	private void createAndAddDomain() {
		String domain = (String) dbRow.get("domain");
		organisation.addChild(
				DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("domain", domain));
	}

	private void createAndAddName() {
		DataGroup nameGroup = DataGroupProvider.getDataGroupUsingNameInData("name");
		DataAtomic name = createAtomicDataUsingColumnNameAndNameInData("defaultname",
				"organisationName");
		nameGroup.addChild(name);
		String nameLanguage = (String) dbRow.get("organisation_name_locale");
		nameGroup.addChild(
				DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("language", nameLanguage));
		organisation.addChild(nameGroup);
	}

	private DataAtomic createAtomicDataUsingColumnNameAndNameInData(String columnName,
			String nameInData) {
		String divaOrganisationName = (String) dbRow.get(columnName);
		return DataAtomicProvider.getDataAtomicUsingNameInDataAndValue(nameInData,
				divaOrganisationName);
	}

	private void createAndAddAlternativeName() {
		DataGroup alternativeNameDataGroup = DataGroupProvider
				.getDataGroupUsingNameInData("alternativeName");
		alternativeNameDataGroup.addChild(
				DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("language", "en"));
		String alternativeName = (String) dbRow.get(ALTERNATIVE_NAME);
		alternativeNameDataGroup.addChild(DataAtomicProvider
				.getDataAtomicUsingNameInDataAndValue("organisationName", alternativeName));
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
		Date dbClosedDate = (Date) dbRow.get("closed_date");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.format(dbClosedDate);
	}

	protected boolean valueExistsForKey(String key) {
		Object value = dbRow.get(key);
		return value != null && !"".equals(value);
	}

}
