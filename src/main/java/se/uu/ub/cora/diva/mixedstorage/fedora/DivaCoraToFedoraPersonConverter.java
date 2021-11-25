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

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.converter.Converter;
import se.uu.ub.cora.converter.ConverterProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.xmlutils.transformer.CoraTransformation;
import se.uu.ub.cora.xmlutils.transformer.CoraTransformationFactory;

public class DivaCoraToFedoraPersonConverter implements DivaCoraToFedoraConverter {

	private CoraTransformationFactory transformationFactory;
	private static final String PERSON_XSLT_PATH = "person/toCoraPerson.xsl";
	private static final String PERSON_WITH_DOMAIN_PART_XSLT_PATH = "person/toCoraPersonWithDomainPart.xsl";

	public DivaCoraToFedoraPersonConverter(CoraTransformationFactory transformationFactory) {
		this.transformationFactory = transformationFactory;
	}

	@Override
	public String toXML(DataGroup dataRecord) {
		String xml = convertToCoraXml(dataRecord);
		CoraTransformation transformation = transformationFactory.factor(PERSON_XSLT_PATH);
		return transformation.transform(xml);
	}

	private String convertToCoraXml(DataGroup dataRecord) {
		Converter converter = ConverterProvider.getConverter("xml");
		return converter.convert(dataRecord);
	}

	@Override
	public String toXML(DataGroup dataRecord, List<DataGroup> relatedRecords) {
		Converter converter = ConverterProvider.getConverter("xml");
		String mainXML = converter.convert(dataRecord);
		List<String> relatedXmlStrings = convertRelatedRecords(converter, relatedRecords);
		return transformToFedoraXml(mainXML, relatedXmlStrings);
	}

	private List<String> convertRelatedRecords(Converter converter,
			List<DataGroup> relatedRecords) {
		List<String> relatedXmlStrings = new ArrayList<>();
		for (DataGroup relatedRecord : relatedRecords) {
			convertAndAddRelatedRecord(converter, relatedXmlStrings, relatedRecord);
		}
		return relatedXmlStrings;
	}

	private void convertAndAddRelatedRecord(Converter converter, List<String> relatedXmlStrings,
			DataGroup relatedRecord) {
		String relatedXml = converter.convert(relatedRecord);
		relatedXmlStrings.add(relatedXml);
	}

	private String transformToFedoraXml(String mainXML, List<String> relatedXmlStrings) {
		CoraTransformation transformation = transformationFactory
				.factor(PERSON_WITH_DOMAIN_PART_XSLT_PATH);
		return transformation.transform(mainXML, relatedXmlStrings);
	}

}
