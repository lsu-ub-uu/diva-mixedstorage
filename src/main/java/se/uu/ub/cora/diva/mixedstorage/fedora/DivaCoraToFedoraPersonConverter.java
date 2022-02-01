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
package se.uu.ub.cora.diva.mixedstorage.fedora;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import se.uu.ub.cora.converter.ConverterProvider;
import se.uu.ub.cora.converter.ExternallyConvertibleToStringConverter;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.classic.RepeatableRelatedLinkCollector;
import se.uu.ub.cora.xmlutils.transformer.CoraTransformation;
import se.uu.ub.cora.xmlutils.transformer.CoraTransformationFactory;

public class DivaCoraToFedoraPersonConverter implements DivaCoraToFedoraConverter {

	private CoraTransformationFactory transformationFactory;
	private static final String CORA_TO_FEDORA_PERSON_XSLT_PATH = "person/coraToFedoraPerson.xsl";
	private RepeatableRelatedLinkCollector repeatbleRelatedLinkCollector;

	public DivaCoraToFedoraPersonConverter(CoraTransformationFactory transformationFactory,
			RepeatableRelatedLinkCollector repeatbleRelatedLinkCollector) {
		this.transformationFactory = transformationFactory;
		this.repeatbleRelatedLinkCollector = repeatbleRelatedLinkCollector;

	}

	@Override
	public String toXML(DataGroup dataGroup) {
		ExternallyConvertibleToStringConverter converter = ConverterProvider
				.getExternallyConvertibleToStringConverter("xml");
		StringBuilder combinedXml = new StringBuilder(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?><personAccumulated>");

		convertTopDataGroupToXml(dataGroup, converter, combinedXml);

		convertDomainPartsDataGroupsToXml(dataGroup, converter, combinedXml);
		combinedXml.append("</personAccumulated>");
		return transformCoraXmlToFedoraXml(combinedXml);
	}

	private void convertTopDataGroupToXml(DataGroup dataRecord,
			ExternallyConvertibleToStringConverter converter, StringBuilder combinedXml) {
		String xml = converter.convert(dataRecord);
		String strippedXml = removeStartingXMLTag(xml);
		combinedXml.append(strippedXml);
	}

	private void convertDomainPartsDataGroupsToXml(DataGroup dataGroup,
			ExternallyConvertibleToStringConverter converter, StringBuilder combinedXml) {
		Map<String, List<DataGroup>> collectedLinks = collectLinksForPersonDomainParts(dataGroup);
		for (Entry<String, List<DataGroup>> entry : collectedLinks.entrySet()) {
			appendStartTag(combinedXml, entry);
			convertRelatedLinksForOneRecordType(converter, combinedXml, entry.getValue());
			appendEndTag(combinedXml, entry);
		}
	}

	private Map<String, List<DataGroup>> collectLinksForPersonDomainParts(DataGroup dataGroup) {
		List<DataGroup> personDomainParts = dataGroup
				.getAllGroupsWithNameInData("personDomainPart");
		return repeatbleRelatedLinkCollector.collectLinks(personDomainParts);
	}

	private void appendStartTag(StringBuilder combinedXml, Entry<String, List<DataGroup>> entry) {
		combinedXml.append("<").append(entry.getKey()).append(">");
	}

	private void convertRelatedLinksForOneRecordType(
			ExternallyConvertibleToStringConverter converter, StringBuilder combinedXml,
			List<DataGroup> dataGroupsForRecordType) {
		for (DataGroup collectedLinkDataGroup : dataGroupsForRecordType) {
			String relatedXml = converter.convert(collectedLinkDataGroup);
			String strippedXml = removeStartingXMLTag(relatedXml);
			combinedXml.append(strippedXml);
		}
	}

	private String removeStartingXMLTag(String xml) {
		return xml.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
	}

	private void appendEndTag(StringBuilder combinedXml, Entry<String, List<DataGroup>> entry) {
		combinedXml.append("</").append(entry.getKey()).append(">");
	}

	private String transformCoraXmlToFedoraXml(StringBuilder combinedXml) {
		CoraTransformation transformation = getTransformationFactory()
				.factor(CORA_TO_FEDORA_PERSON_XSLT_PATH);
		return transformation.transform(combinedXml.toString());
	}

	public CoraTransformationFactory getTransformationFactory() {
		// needed for test
		return transformationFactory;
	}

	public RepeatableRelatedLinkCollector getRepeatbleRelatedLinkCollector() {
		return repeatbleRelatedLinkCollector;
	}

}
