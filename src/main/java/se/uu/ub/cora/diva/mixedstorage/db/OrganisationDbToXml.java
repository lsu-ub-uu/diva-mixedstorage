/*
 * Copyright 2021 Uppsala University Library
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import se.uu.ub.cora.converter.Converter;
import se.uu.ub.cora.converter.ConverterFactory;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.xmlutils.transformer.CoraTransformationFactory;

public class OrganisationDbToXml implements DbToXml {

	private RecordStorage dbStorage;
	private ConverterFactory xmlConverterFactory;
	private CoraTransformationFactory transformationFactory;

	public OrganisationDbToXml(RecordStorage dbStorage, ConverterFactory xmlConverterFactory,
			CoraTransformationFactory transformationFactory) {
		this.dbStorage = dbStorage;
		this.xmlConverterFactory = xmlConverterFactory;
		this.transformationFactory = transformationFactory;
	}

	@Override
	public String toXML(String recordType, String recordId) {
		DataGroup dataGroup = dbStorage.read(recordType, recordId);

		Map<String, DataGroup> linkedOrganisations = new HashMap<>();
		linkedOrganisations.put(recordId, dataGroup);

		Converter converter = xmlConverterFactory.factorConverter();
		StringBuilder combinedXML = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		possiblyConvertParents(dataGroup, linkedOrganisations);
		for (Entry<String, DataGroup> entry : linkedOrganisations.entrySet()) {
			String coraXml = converter.convert(entry.getValue());
			String strippedXml = removeStartingXMLTag(coraXml);
			combinedXML.append(strippedXml);

		}

		return combinedXML.toString();
	}

	private void possiblyConvertParents(DataGroup parentDataGroup,
			Map<String, DataGroup> linkedOrganisations) {
		if (parentDataGroup.containsChildWithNameInData("parentOrganisation")) {
			convertParents(parentDataGroup, linkedOrganisations);
		}
	}

	private void convertParents(DataGroup dataGroup, Map<String, DataGroup> linkedOrganisations) {
		List<DataGroup> parents = dataGroup.getAllGroupsWithNameInData("parentOrganisation");
		for (DataGroup parent : parents) {
			convertParent(parent, linkedOrganisations);
		}
	}

	private void convertParent(DataGroup parent, Map<String, DataGroup> linkedOrganisations) {
		DataGroup parentDataGroup = readParentFromStorage(parent, linkedOrganisations);
		possiblyConvertParents(parentDataGroup, linkedOrganisations);
	}

	private String removeStartingXMLTag(String parentXML) {
		return parentXML.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
	}

	private DataGroup readParentFromStorage(DataGroup parent,
			Map<String, DataGroup> linkedOrganisations) {
		String parentId = extractParentId(parent);
		DataGroup readDataGroup = dbStorage.read("organisation", parentId);
		linkedOrganisations.put(parentId, readDataGroup);
		return readDataGroup;
	}

	private String extractParentId(DataGroup parent) {
		DataGroup orgLink = parent.getFirstGroupWithNameInData("organisationLink");
		return orgLink.getFirstAtomicValueWithNameInData("linkedRecordId");
	}

}
