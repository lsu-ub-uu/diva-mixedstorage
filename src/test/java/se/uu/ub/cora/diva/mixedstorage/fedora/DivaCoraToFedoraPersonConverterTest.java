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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.converter.ConverterProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.logger.LoggerFactory;
import se.uu.ub.cora.logger.LoggerProvider;

public class DivaCoraToFedoraPersonConverterTest {
	private ConverterFactorySpy dataGroupToXmlConverterFactory;
	private TransformationFactorySpy transformationFactory;
	private String toCoraPersonXsl = "person/toCoraPerson.xsl";
	private String toCoraPersonWithDomainPartXsl = "person/toCoraPersonWithDomainPart.xsl";
	private DataGroupSpy dataGroup = new DataGroupSpy("someNameInData");
	private DivaCoraToFedoraConverter converter;

	@BeforeMethod
	public void setUp() {
		LoggerFactory loggerFactory = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactory);
		dataGroupToXmlConverterFactory = new ConverterFactorySpy();
		ConverterProvider.setConverterFactory("xml", dataGroupToXmlConverterFactory);
		transformationFactory = new TransformationFactorySpy();
		converter = new DivaCoraToFedoraPersonConverter(transformationFactory);
	}

	@Test
	public void testToXml() {
		String fedoraXml = converter.toXML(dataGroup);
		ConverterSpy factoredConverter = dataGroupToXmlConverterFactory.factoredConverter;
		assertEquals(factoredConverter.dataElements.size(), 1);
		assertSame(factoredConverter.dataElements.get(0), dataGroup);

		List<CoraTransformationSpy> factoredTransformations = transformationFactory.factoredTransformations;
		assertEquals(factoredTransformations.size(), 1);
		assertEquals(transformationFactory.xsltPath, toCoraPersonXsl);

		CoraTransformationSpy factoredTransformation = factoredTransformations.get(0);
		assertEquals(factoredTransformation.inputXml, factoredConverter.commonStringToReturn + 0);

		assertEquals(fedoraXml, factoredTransformation.xmlToReturn);
	}

	@Test
	public void testToXmlWithRelatedRecords() {
		List<DataGroup> relatedDataGroups = createRelatedDataGroups();

		String fedoraXml = converter.toXML(dataGroup, relatedDataGroups);

		assertDataGroupsAreSentToXmlConverter(dataGroup, relatedDataGroups);
		assertTransformationFactoryWasCalledCorrectly();

		List<CoraTransformationSpy> factoredTransformations = transformationFactory.factoredTransformations;
		CoraTransformationSpy factoredTransformation = factoredTransformations.get(0);

		assertConvertedGroupsAreSentToTransformation(factoredTransformation);

		assertEquals(fedoraXml, factoredTransformation.xmlToReturn);
	}

	private void assertDataGroupsAreSentToXmlConverter(DataGroupSpy dataGroup,
			List<DataGroup> relatedDataGroups) {
		ConverterSpy factoredConverter = dataGroupToXmlConverterFactory.factoredConverter;
		assertEquals(factoredConverter.dataElements.size(), 3);
		assertSame(factoredConverter.dataElements.get(0), dataGroup);
		assertSame(factoredConverter.dataElements.get(1), relatedDataGroups.get(0));
		assertSame(factoredConverter.dataElements.get(2), relatedDataGroups.get(1));
	}

	private void assertTransformationFactoryWasCalledCorrectly() {
		List<CoraTransformationSpy> factoredTransformations = transformationFactory.factoredTransformations;
		assertEquals(factoredTransformations.size(), 1);
		assertEquals(transformationFactory.xsltPath, toCoraPersonWithDomainPartXsl);
	}

	private void assertConvertedGroupsAreSentToTransformation(
			CoraTransformationSpy factoredTransformation) {
		ConverterSpy factoredConverter = dataGroupToXmlConverterFactory.factoredConverter;

		List<String> returnedStringsFromConverter = factoredConverter.returnedStrings;
		assertEquals(factoredTransformation.mainXml, returnedStringsFromConverter.get(0));

		assertEquals(factoredTransformation.relatedXmls.get(0),
				returnedStringsFromConverter.get(1));
		assertEquals(factoredTransformation.relatedXmls.get(1),
				returnedStringsFromConverter.get(2));
	}

	private List<DataGroup> createRelatedDataGroups() {
		List<DataGroup> relatedDataGroups = new ArrayList<>();
		relatedDataGroups.add(new DataGroupSpy("personDomainPart"));
		relatedDataGroups.add(new DataGroupSpy("personDomainPart"));
		return relatedDataGroups;
	}

}
