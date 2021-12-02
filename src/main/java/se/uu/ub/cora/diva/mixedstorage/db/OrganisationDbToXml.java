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

import java.util.List;

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

		Converter converter = xmlConverterFactory.factorConverter();
		StringBuilder combinedXML = new StringBuilder();
		String coraXml = converter.convert(dataGroup);
		combinedXML.append(coraXml);
		possiblyConvertParents(converter, combinedXML, dataGroup);

		return combinedXML.toString();
	}

	private void possiblyConvertParents(Converter converter, StringBuilder combinedXML,
			DataGroup parentDataGroup) {
		if (parentDataGroup.containsChildWithNameInData("parentOrganisation")) {
			convertParents(converter, parentDataGroup, combinedXML);
		}
	}

	private void convertParents(Converter converter, DataGroup dataGroup,
			StringBuilder combinedXML) {
		List<DataGroup> parents = dataGroup.getAllGroupsWithNameInData("parentOrganisation");
		for (DataGroup parent : parents) {
			convertParent(converter, combinedXML, parent);
		}
	}

	private void convertParent(Converter converter, StringBuilder combinedXML, DataGroup parent) {
		DataGroup parentDataGroup = readParentFromStorage(parent);
		String parentXML = converter.convert(parentDataGroup);
		String strippedXml = removeStartingXMLTag(parentXML);
		combinedXML.append(strippedXml);
		possiblyConvertParents(converter, combinedXML, parentDataGroup);
	}

	private String removeStartingXMLTag(String parentXML) {
		return parentXML.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
	}

	private DataGroup readParentFromStorage(DataGroup parent) {
		String parentId = extractParentId(parent);
		return dbStorage.read("organisation", parentId);
	}

	private String extractParentId(DataGroup parent) {
		DataGroup orgLink = parent.getFirstGroupWithNameInData("organisationLink");
		return orgLink.getFirstAtomicValueWithNameInData("linkedRecordId");
	}

}
