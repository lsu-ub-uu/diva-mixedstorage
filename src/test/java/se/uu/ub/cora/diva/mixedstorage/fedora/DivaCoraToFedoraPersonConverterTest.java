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

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.converter.ConverterProvider;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.logger.LoggerFactory;
import se.uu.ub.cora.logger.LoggerProvider;

public class DivaCoraToFedoraPersonConverterTest {
	private ConverterFactorySpy dataGroupToXmlConverterFactory;
	private TransformationFactorySpy transformationFactory;
	private String toCoraPersonXsl = "person/toCoraPerson.xsl";
	private DataGroupSpy defaultDataGroup = new DataGroupSpy("someNameInData");
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
		converter = new DivaCoraToFedoraPersonConverter(transformationFactory, repeatbleCollector);
	}

	@Test
	public void testToXmlNoDomainParts() {
		String fedoraXml = converter.toXML(defaultDataGroup);

		ConverterSpy groupToXmlConverter = dataGroupToXmlConverterFactory.factoredConverter;
		assertMainDataGroupWasConvertedToCoraXml(groupToXmlConverter);

		assertEquals(repeatbleCollector.groupsContainingLinks.size(), 0);

		List<CoraTransformationSpy> factoredTransformations = transformationFactory.factoredTransformations;
		assertMainXmlWasTransformedToFedoraXml(factoredTransformations);

		CoraTransformationSpy factoredTransformation = factoredTransformations.get(0);
		assertEquals(factoredTransformation.inputXml, groupToXmlConverter.commonStringToReturn + 0);

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
	public void testToXmlWithDomainParts() {
		repeatbleCollector.numberOfGroupsToReturn = 4;
		addPersonDomainPartChildren(defaultDataGroup);
		String fedoraXml = converter.toXML(defaultDataGroup);

		ConverterSpy groupToXmlConverter = dataGroupToXmlConverterFactory.factoredConverter;
		assertEquals(groupToXmlConverter.dataElements.size(), 5);
		assertSame(groupToXmlConverter.dataElements.get(0), defaultDataGroup);

		assertEquals(repeatbleCollector.groupsContainingLinks.size(), 3);
		assertSame(groupToXmlConverter.dataElements.get(1), repeatbleCollector.listToReturn.get(0));
		assertSame(groupToXmlConverter.dataElements.get(2), repeatbleCollector.listToReturn.get(1));
		assertSame(groupToXmlConverter.dataElements.get(3), repeatbleCollector.listToReturn.get(2));
		assertSame(groupToXmlConverter.dataElements.get(4), repeatbleCollector.listToReturn.get(3));

		List<CoraTransformationSpy> factoredTransformations = transformationFactory.factoredTransformations;
		assertMainXmlWasTransformedToFedoraXml(factoredTransformations);

		CoraTransformationSpy factoredTransformation = factoredTransformations.get(0);
		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>some returned string from converter spy0some returned string from converter spy1some returned string from converter spy2some returned string from converter spy3some returned string from converter spy4";
		assertEquals(factoredTransformation.inputXml, expectedXml);

		assertEquals(fedoraXml, factoredTransformation.xmlToReturn);
	}

	private void addPersonDomainPartChildren(DataGroupSpy dataGroup) {
		dataGroup.addChild(new DataGroupSpy("personDomainPart"));
		dataGroup.addChild(new DataGroupSpy("personDomainPart"));
		dataGroup.addChild(new DataGroupSpy("personDomainPart"));
	}

}
