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

import java.util.Map;

import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;

public class SubOrganisationConverter extends DefaultOrganisationConverter {

	@Override
	public DataGroup fromMap(Map<String, Object> dbRow) {
		DataGroup basicDataGroup = super.fromMap(dbRow);
		possiblyCreateAndAddAddress(basicDataGroup);
		possiblyCreateAndAddOrganisationCode(basicDataGroup);
		return basicDataGroup;
	}

	@Override
	public String getRecordType() {
		return "subOrganisation";
	}

	private void possiblyCreateAndAddAddress(DataGroup dataGroup) {
		possiblyAddAtomicValueUsingKeyAndNameInData(dataGroup, "city", "city");
		possiblyAddAtomicValueUsingKeyAndNameInData(dataGroup, "street", "street");
		possiblyAddAtomicValueUsingKeyAndNameInData(dataGroup, "postbox", "box");
		possiblyAddAtomicValueUsingKeyAndNameInData(dataGroup, "postnumber", "postcode");
		addCountryConvertedToUpperCaseOrSetDefault(dataGroup);
		possiblyCreateAndAddURL(dataGroup);
	}

	private void possiblyAddAtomicValueUsingKeyAndNameInData(DataGroup dataGroup, String key,
			String nameInData) {
		if (valueExistsForKey(key)) {
			String value = (String) dbRow.get(key);
			dataGroup.addChild(
					DataAtomicProvider.getDataAtomicUsingNameInDataAndValue(nameInData, value));
		}
	}

	private void addCountryConvertedToUpperCaseOrSetDefault(DataGroup dataGroup) {
		if (valueExistsForKey("country_code")) {
			addCountryConvertedToUpperCase(dataGroup);
		}
	}

	private void addCountryConvertedToUpperCase(DataGroup dataGroup) {
		String uppercaseValue = ((String) dbRow.get("country_code")).toUpperCase();
		dataGroup.addChild(
				DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("country", uppercaseValue));
	}

	private void possiblyCreateAndAddOrganisationCode(DataGroup dataGroup) {
		possiblyAddAtomicValueUsingKeyAndNameInData(dataGroup, "organisation_code",
				"organisationCode");
	}

	private void possiblyCreateAndAddURL(DataGroup dataGroup) {
		possiblyAddAtomicValueUsingKeyAndNameInData(dataGroup, "organisation_homepage", "URL");
	}

}
