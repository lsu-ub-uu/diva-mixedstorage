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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.converter.ConverterProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.logger.LoggerFactory;
import se.uu.ub.cora.logger.LoggerProvider;

public class DivaFedoraToCoraPersonConverter2Test {

	private ConverterFactorySpy converterFactory;

	@BeforeMethod
	public void setUp() {
		LoggerFactory loggerFactory = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactory);
		converterFactory = new ConverterFactorySpy();
		ConverterProvider.setConverterFactory("xml", converterFactory);

	}

	@Test
	public void testFromXml() {
		XsltTransformationSpy transformation = new XsltTransformationSpy();
		DivaFedoraToCoraConverter personConverter = new DivaFedoraToCoraPersonConverter(
				transformation);

		String xml = "someXmlString";
		DataGroup fromXML = personConverter.fromXML(xml);
		assertEquals(transformation.inputXml, xml);

		ConverterSpy factoredConverter = converterFactory.factoredConverter;
		assertEquals(factoredConverter.dataString, transformation.stringToReturn);
		assertEquals(fromXML, factoredConverter.dataGroupToReturn);
	}

}
