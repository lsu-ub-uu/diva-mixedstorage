/*
 * Copyright 2022 Uppsala University Library
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
package se.uu.ub.cora.diva.mixedstorage.id;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.ParseException;
import se.uu.ub.cora.diva.mixedstorage.fedora.FedoraConnectionInfo;
import se.uu.ub.cora.diva.mixedstorage.fedora.FedoraException;
import se.uu.ub.cora.diva.mixedstorage.fedora.HttpHandlerFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.fedora.HttpHandlerSpy;
import se.uu.ub.cora.storage.RecordIdGenerator;

public class DivaIdGeneratorTest {

	private RecordIdGenerator idGenerator;
	private HttpHandlerFactorySpy httpHandlerFactory;
	private String fedoraBaseURL = "http://diva-cora-fedora:8088/fedora/";
	private String fedoraUsername = "fedoraUser";
	private String fedoraPassword = "fedoraPassword";
	private XMLXPathParserFactorySpy parserFactory;

	@BeforeMethod
	public void setUp() {
		createHttpHandlerFactoryWithDefaultSetUp();
		parserFactory = new XMLXPathParserFactorySpy();
		FedoraConnectionInfo fedoraConnectionInfo = new FedoraConnectionInfo(fedoraBaseURL,
				fedoraUsername, fedoraPassword);
		idGenerator = new DivaIdGenerator(httpHandlerFactory, fedoraConnectionInfo, parserFactory);
	}

	private void createHttpHandlerFactoryWithDefaultSetUp() {
		httpHandlerFactory = new HttpHandlerFactorySpy();
		httpHandlerFactory.responseCodes.add(200);
		httpHandlerFactory.responseTexts.add("some response text");
	}

	@Test
	public void testCommonIdGenerator() {
		String generatedId = idGenerator.getIdForType("someType");
		assertTrue(generatedId.matches("^someType:[0-9]+$"));
	}

	@Test
	public void testToCommonIdNotSame() {
		String generatedId = idGenerator.getIdForType("someType");
		String generatedId2 = idGenerator.getIdForType("someType");
		assertNotEquals(generatedId, generatedId2);
	}

	@Test
	public void testIdGeneratorForPersonCorrectHttpHandler() {
		idGenerator.getIdForType("person");

		assertEquals(httpHandlerFactory.factoredHttpHandlers.size(), 1);
		assertEquals(httpHandlerFactory.urls.get(0),
				fedoraBaseURL + "objects/nextPID?namespace=authority-person&format=xml");

		HttpHandlerSpy factoredHttpHandler = httpHandlerFactory.factoredHttpHandlers.get(0);
		assertEquals(factoredHttpHandler.requestMethod, "POST");
		assertEquals(factoredHttpHandler.userName, "fedoraUser");
		assertEquals(factoredHttpHandler.password, "fedoraPassword");

	}

	@Test
	public void testIdGeneratorForPersonCorrectlyParsed() {
		String generatedId = idGenerator.getIdForType("person");

		XMLXPathParserSpy factoredPathParser = parserFactory.factoredPathParser;
		HttpHandlerSpy factoredHttpHandler = httpHandlerFactory.factoredHttpHandlers.get(0);
		assertEquals(factoredPathParser.responseXmlToHandle, factoredHttpHandler.responseText);

		assertEquals(factoredPathParser.xpathString, "/pidList/pid/text()");
		assertEquals(generatedId, factoredPathParser.returnedPidFromPathParser);

	}

	@Test(expectedExceptions = FedoraException.class, expectedExceptionsMessageRegExp = ""
			+ "getting next pid from fedora failed, with response code: 500")
	public void testErrorFetchingPersonPid() throws Exception {
		httpHandlerFactory.responseCodes = new ArrayList<>(List.of(500));
		idGenerator.getIdForType("person");
	}

	@Test(expectedExceptions = ParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error parsing response from fedora: some error from spy")
	public void testErrorParsingPid() throws Exception {
		parserFactory.throwException = true;
		idGenerator.getIdForType("person");
	}

}
