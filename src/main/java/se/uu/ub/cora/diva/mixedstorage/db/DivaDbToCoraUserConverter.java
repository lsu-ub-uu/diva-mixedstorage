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
package se.uu.ub.cora.diva.mixedstorage.db;

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.sqldatabase.Row;

public class DivaDbToCoraUserConverter implements DivaDbToCoraConverter {

	private static final String CORA_USER = "coraUser";
	private Row dbRow;

	@Override
	public DataGroup fromRow(Row dbRow) {
		this.dbRow = dbRow;
		throwErrorIfNoId();
		DataGroup user = createBasicActiveUserWithRecordInfo(dbRow);
		possiblyAddFirstName(dbRow, user);
		possiblyAddLastname(dbRow, user);
		return user;
	}

	private void throwErrorIfNoId() {
		if (valueIsEmpty("db_id")) {
			throw ConversionException.withMessageAndException(
					"Error converting user to Cora user: Map does not contain value for id", null);
		}
	}

	private DataGroup createBasicActiveUserWithRecordInfo(Row dbRow) {
		DataGroup user = DataGroupProvider.getDataGroupUsingNameInData("user");
		user.addAttributeByIdWithValue("type", CORA_USER);
		DataGroup recordInfo = createRecordInfo(dbRow);
		user.addChild(recordInfo);
		user.addChild(
				DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("activeStatus", "active"));
		return user;
	}

	private DataGroup createRecordInfo(Row map) {
		DataGroup recordInfo = createRecordInfoGroupWithId(map);
		createAndAddType(recordInfo);
		createAndAddDataDivider(recordInfo);
		createAndAddCreatedAndUpdatedInfo(recordInfo);
		return recordInfo;
	}

	private DataGroup createRecordInfoGroupWithId(Row map) {
		DataGroup recordInfo = DataGroupProvider.getDataGroupUsingNameInData("recordInfo");
		DataAtomic id = DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("id",
				String.valueOf(map.getValueByColumn("db_id")));
		recordInfo.addChild(id);
		return recordInfo;
	}

	private void createAndAddDataDivider(DataGroup recordInfo) {
		DataGroup dataDivider = DataGroupProvider
				.getDataGroupAsLinkUsingNameInDataTypeAndId("dataDivider", "system", "diva");
		recordInfo.addChild(dataDivider);
	}

	private void createAndAddType(DataGroup recordInfo) {
		DataGroup type = DataGroupProvider.getDataGroupAsLinkUsingNameInDataTypeAndId("type",
				"recordType", CORA_USER);
		recordInfo.addChild(type);
	}

	private void createAndAddCreatedAndUpdatedInfo(DataGroup recordInfo) {
		createAndAddCreatedInfo(recordInfo);
		createAndAddUpdatedInfo(recordInfo);
	}

	private void createAndAddCreatedInfo(DataGroup recordInfo) {
		DataGroup createdBy = createLinkUsingNameInDataRecordTypeAndRecordId("createdBy", CORA_USER,
				"coraUser:4412982402853626");
		recordInfo.addChild(createdBy);
		addPredefinedTimestampToDataGroupUsingNameInData(recordInfo, "tsCreated");
	}

	private void addPredefinedTimestampToDataGroupUsingNameInData(DataGroup recordInfo,
			String nameInData) {
		recordInfo.addChild(DataAtomicProvider.getDataAtomicUsingNameInDataAndValue(nameInData,
				"2017-01-01T00:00:00.000000Z"));
	}

	private void createAndAddUpdatedInfo(DataGroup recordInfo) {
		DataGroup updated = DataGroupProvider.getDataGroupUsingNameInData("updated");
		DataGroup updatedBy = createLinkUsingNameInDataRecordTypeAndRecordId("updatedBy", CORA_USER,
				"coraUser:4412982402853626");
		updated.addChild(updatedBy);
		addPredefinedTimestampToDataGroupUsingNameInData(updated, "tsUpdated");
		updated.setRepeatId("0");
		recordInfo.addChild(updated);
	}

	private DataGroup createLinkUsingNameInDataRecordTypeAndRecordId(String nameInData,
			String linkedRecordType, String linkedRecordId) {
		return DataGroupProvider.getDataGroupAsLinkUsingNameInDataTypeAndId(nameInData,
				linkedRecordType, linkedRecordId);
	}

	private void possiblyAddFirstName(Row dbRow, DataGroup user) {
		if (!valueIsEmpty("first_name")) {
			DataAtomic firstName = DataAtomicProvider.getDataAtomicUsingNameInDataAndValue(
					"userFirstname", (String) dbRow.getValueByColumn("first_name"));
			user.addChild(firstName);
		}
	}

	private boolean valueIsEmpty(String key) {
		Object valueForKey = dbRow.getValueByColumn(key);
		return valueForKey == null || "".equals(valueForKey);
	}

	private void possiblyAddLastname(Row dbRow, DataGroup user) {
		if (!valueIsEmpty("last_name")) {
			DataAtomic lastName = DataAtomicProvider.getDataAtomicUsingNameInDataAndValue(
					"userLastname", (String) dbRow.getValueByColumn("last_name"));
			user.addChild(lastName);
		}
	}

}
