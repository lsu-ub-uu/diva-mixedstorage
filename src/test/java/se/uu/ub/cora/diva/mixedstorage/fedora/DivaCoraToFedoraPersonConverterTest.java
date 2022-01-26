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
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.converter.ConverterProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.RepeatableLinkCollectorSpy;
import se.uu.ub.cora.diva.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.logger.LoggerFactory;
import se.uu.ub.cora.logger.LoggerProvider;

public class DivaCoraToFedoraPersonConverterTest {
	private ConverterFactorySpy dataGroupToXmlConverterFactory;
	private TransformationFactorySpy transformationFactory;
	private String toCoraPersonXsl = "person/coraToFedoraPerson.xsl";
	private DataGroupSpy defaultDataGroup;
	private DivaCoraToFedoraConverter converter;
	private RepeatableLinkCollectorSpy repeatbleCollector;

	@BeforeMethod
	public void setUp() {
		LoggerFactory loggerFactory = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactory);
		dataGroupToXmlConverterFactory = new ConverterFactorySpy();
		ConverterProvider.setConverterFactory("xml", dataGroupToXmlConverterFactory);
		transformationFactory = new TransformationFactorySpy();
		repeatbleCollector = new RepeatableLinkCollectorSpy();
		defaultDataGroup = new DataGroupSpy("someNameInData");
		converter = new DivaCoraToFedoraPersonConverter(transformationFactory, repeatbleCollector);
	}

	@Test
	public void testToXmlNoDomainPartsNoUsers() {
		String fedoraXml = converter.toXML(defaultDataGroup);

		ConverterSpy groupToXmlConverter = dataGroupToXmlConverterFactory.factoredConverter;
		assertMainDataGroupWasConvertedToCoraXml(groupToXmlConverter);

		assertEquals(repeatbleCollector.groupsContainingLinks.size(), 0);

		List<CoraTransformationSpy> factoredTransformations = transformationFactory.factoredTransformations;
		assertMainXmlWasTransformedToFedoraXml(factoredTransformations);

		CoraTransformationSpy factoredTransformation = factoredTransformations.get(0);
		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><personAccumulated>some returned string from converter spy0</personAccumulated>";
		assertEquals(factoredTransformation.inputXml, expectedXml);

		assertEquals(fedoraXml, factoredTransformation.xmlToReturn);

	}

	private void assertMainDataGroupWasConvertedToCoraXml(ConverterSpy groupToXmlConverter) {
		assertEquals(groupToXmlConverter.dataElements.size(), 1);
		assertSame(groupToXmlConverter.dataElements.get(0), defaultDataGroup);
	}

	private void assertMainXmlWasTransformedToFedoraXml(
			List<CoraTransformationSpy> factoredTransformations) {
		assertEquals(factoredTransformations.size(), 1);
		assertEquals(transformationFactory.xsltPath, toCoraPersonXsl);
	}

	@Test
	public void testToXmlWithOnlyDomainPartsNoOrganisations() {
		addDataGroupsForRecordTypeToAnswerFromSpy("personDomainParts", 4);
		addPersonDomainPartChildren(defaultDataGroup);

		String fedoraXml = converter.toXML(defaultDataGroup);

		ConverterSpy groupToXmlConverter = dataGroupToXmlConverterFactory.factoredConverter;
		assertEquals(groupToXmlConverter.dataElements.size(), 5);
		assertSame(groupToXmlConverter.dataElements.get(0), defaultDataGroup);

		assertEquals(repeatbleCollector.groupsContainingLinks.size(), 3);
		Map<String, List<DataGroup>> retunedMapFromSpy = repeatbleCollector.mapToReturn;
		List<DataGroup> personDomainParts = retunedMapFromSpy.get("personDomainParts");
		assertSame(groupToXmlConverter.dataElements.get(1), personDomainParts.get(0));
		assertSame(groupToXmlConverter.dataElements.get(2), personDomainParts.get(1));
		assertSame(groupToXmlConverter.dataElements.get(3), personDomainParts.get(2));
		assertSame(groupToXmlConverter.dataElements.get(4), personDomainParts.get(3));

		List<CoraTransformationSpy> factoredTransformations = transformationFactory.factoredTransformations;
		assertMainXmlWasTransformedToFedoraXml(factoredTransformations);

		CoraTransformationSpy factoredTransformation = factoredTransformations.get(0);
		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><personAccumulated>some returned string from converter spy0<personDomainParts>some returned string from converter spy1some returned string from converter spy2some returned string from converter spy3some returned string from converter spy4</personDomainParts></personAccumulated>";
		assertEquals(factoredTransformation.inputXml, expectedXml);

		assertEquals(fedoraXml, factoredTransformation.xmlToReturn);
	}

	private void addDataGroupsForRecordTypeToAnswerFromSpy(String recordType,
			int numOfDomainParts) {
		List<DataGroup> dataGroupList = new ArrayList<>();
		for (int i = 0; i < numOfDomainParts; i++) {
			dataGroupList.add(new DataGroupSpy(recordType));
		}
		repeatbleCollector.mapToReturn.put(recordType, dataGroupList);
	}

	private void addPersonDomainPartChildren(DataGroupSpy dataGroup) {
		dataGroup.addChild(new DataGroupSpy("personDomainPart"));
		dataGroup.addChild(new DataGroupSpy("personDomainPart"));
		dataGroup.addChild(new DataGroupSpy("personDomainPart"));
	}

	@Test
	public void testToXmlWithDomainPartsAndOrganisations() {
		addDataGroupsForRecordTypeToAnswerFromSpy("personDomainParts", 2);
		addDataGroupsForRecordTypeToAnswerFromSpy("organisations", 4);
		addPersonDomainPartChildren(defaultDataGroup);
		String fedoraXml = converter.toXML(defaultDataGroup);

		ConverterSpy groupToXmlConverter = dataGroupToXmlConverterFactory.factoredConverter;
		assertEquals(groupToXmlConverter.dataElements.size(), 7);
		assertSame(groupToXmlConverter.dataElements.get(0), defaultDataGroup);

		assertEquals(repeatbleCollector.groupsContainingLinks.size(), 3);
		Map<String, List<DataGroup>> retunedMapFromSpy = repeatbleCollector.mapToReturn;
		List<DataGroup> personDomainParts = retunedMapFromSpy.get("personDomainParts");
		assertSame(groupToXmlConverter.dataElements.get(5), personDomainParts.get(0));
		assertSame(groupToXmlConverter.dataElements.get(6), personDomainParts.get(1));

		List<DataGroup> organisations = retunedMapFromSpy.get("organisations");
		assertSame(groupToXmlConverter.dataElements.get(1), organisations.get(0));
		assertSame(groupToXmlConverter.dataElements.get(2), organisations.get(1));
		assertSame(groupToXmlConverter.dataElements.get(3), organisations.get(2));
		assertSame(groupToXmlConverter.dataElements.get(4), organisations.get(3));
		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><personAccumulated>some returned string from converter spy0<organisations>some returned string from converter spy1some returned string from converter spy2some returned string from converter spy3some returned string from converter spy4</organisations><personDomainParts>some returned string from converter spy5some returned string from converter spy6</personDomainParts></personAccumulated>";
		CoraTransformationSpy factoredTransformation = transformationFactory.factoredTransformations
				.get(0);

		assertEquals(factoredTransformation.inputXml, expectedXml);

		assertEquals(fedoraXml, factoredTransformation.xmlToReturn);
	}
}
