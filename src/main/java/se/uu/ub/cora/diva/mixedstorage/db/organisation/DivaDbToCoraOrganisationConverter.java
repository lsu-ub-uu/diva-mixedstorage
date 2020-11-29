/*
 * Copyright 2019, 2020 Uppsala University Library
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
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.mixedstorage.db.ConversionException;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverter;

public class DivaDbToCoraOrganisationConverter implements DivaDbToCoraConverter {

	private static final String ORGANISATION_ID = "id";
	private Map<String, Object> dbRow;
	private DataGroup organisation;
	private DefaultConverterFactory defaultConverterFactory;

	public DivaDbToCoraOrganisationConverter(DefaultConverterFactory converterFactory) {
		this.defaultConverterFactory = converterFactory;
	}

	@Override
	public DataGroup fromMap(Map<String, Object> dbRow) {
		this.dbRow = dbRow;
		if (organisationIsEmpty()) {
			throw ConversionException.withMessageAndException(
					"Error converting organisation to Cora organisation: Map does not contain value for "
							+ ORGANISATION_ID,
					null);
		}
		return createDataGroup();
	}

	private boolean organisationIsEmpty() {
		Object organisationId = dbRow.get(ORGANISATION_ID);
		return organisationId == null || "".equals(organisationId);
	}

	private DataGroup createDataGroup() {
		DefaultConverter defaultConverter = defaultConverterFactory.factor();
		organisation = defaultConverter.fromMap(dbRow);

		possiblyAddMoreData();
		return organisation;
	}

	private void possiblyAddMoreData() {
		String typeCode = (String) dbRow.get("type_code");
		if (notRootOrganisation(typeCode)) {
			addCommonDataForSubAndTopOrganisation();
			possiblyAddDataForTopOrganisation();
		}
	}

	private boolean notRootOrganisation(String typeCode) {
		return !"root".equals(typeCode);
	}

	private void possiblyAddDataForTopOrganisation() {
		if (isTopLevel()) {
			possiblyCreateAndAddDoctoralDegreeGrantor();
			possiblyCreateAndAddOrganisationNumber();
		}
	}

	private void addCommonDataForSubAndTopOrganisation() {
		createAndAddOrganisationType();
		possiblyCreateAndAddAddress();
		possiblyCreateAndAddOrganisationCode();
		possiblyCreateAndAddURL();
	}

	private boolean isTopLevel() {
		return (boolean) dbRow.get("top_level");
	}

	private void createAndAddOrganisationType() {
		String typeCode = (String) dbRow.get("type_code");
		organisation.addChild(DataAtomicProvider
				.getDataAtomicUsingNameInDataAndValue("organisationType", typeCode));
	}

	private void possiblyCreateAndAddAddress() {
		DataGroup address = createAddressGroup();
		if (atLeastOnePartOfAddressExist(address)) {
			organisation.addChild(address);
		}
	}

	private boolean atLeastOnePartOfAddressExist(DataGroup address) {
		return address.hasChildren();
	}

	private DataGroup createAddressGroup() {
		DataGroup address = DataGroupProvider.getDataGroupUsingNameInData("address");
		possiblyCreateAtomicValueUsingKeyAndNameInData(address, "city", "city");
		possiblyCreateAtomicValueUsingKeyAndNameInData(address, "street", "street");
		possiblyCreateAtomicValueUsingKeyAndNameInData(address, "postbox", "box");
		possiblyCreateAtomicValueUsingKeyAndNameInData(address, "postnumber", "postcode");
		addCountryConvertedToUpperCaseOrSetDefault(address);
		return address;
	}

	private void possiblyCreateAtomicValueUsingKeyAndNameInData(DataGroup dataGroup, String key,
			String nameInData) {
		if (valueExistsForKey(key)) {
			String value = (String) dbRow.get(key);
			dataGroup.addChild(
					DataAtomicProvider.getDataAtomicUsingNameInDataAndValue(nameInData, value));
		}
	}

	private void possiblyAddAtomicValueUsingKeyAndNameInDataToDefault(String key,
			String nameInData) {
		if (valueExistsForKey(key)) {
			String value = (String) dbRow.get(key);
			organisation.addChild(
					DataAtomicProvider.getDataAtomicUsingNameInDataAndValue(nameInData, value));
		}
	}

	private boolean valueExistsForKey(String key) {
		Object value = dbRow.get(key);
		return value != null && !"".equals(value);
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

	private void possiblyCreateAndAddOrganisationNumber() {
		possiblyAddAtomicValueUsingKeyAndNameInDataToDefault("orgnumber", "organisationNumber");
	}

	private void possiblyCreateAndAddOrganisationCode() {
		possiblyAddAtomicValueUsingKeyAndNameInDataToDefault("organisation_code",
				"organisationCode");
	}

	private void possiblyCreateAndAddURL() {
		possiblyAddAtomicValueUsingKeyAndNameInDataToDefault("organisation_homepage", "URL");
	}

	private void possiblyCreateAndAddDoctoralDegreeGrantor() {
		Object booleanValue = dbRow.get("show_in_defence");
		if (booleanValue != null) {
			createAndAddBooleanValue((boolean) booleanValue, "doctoralDegreeGrantor");
		}
	}

	private void createAndAddBooleanValue(boolean showInDefence, String nameInData) {
		String stringBooleanValue = showInDefence ? "yes" : "no";
		organisation.addChild(DataAtomicProvider.getDataAtomicUsingNameInDataAndValue(nameInData,
				stringBooleanValue));
	}

	public DefaultConverterFactory getDefaultConverterFactory() {
		return defaultConverterFactory;

	}
}