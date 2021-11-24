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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.converter.ConverterProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.logger.LoggerFactory;
import se.uu.ub.cora.logger.LoggerProvider;

public class ClassicFedoraPersonUpdaterTest {

	private ClassicFedoraPersonUpdater fedoraUpdater;
	private HttpHandlerFactorySpy httpHandlerFactory;
	private DivaFedoraConverterFactorySpy toFedoraConverterFactory;
	private ConverterFactorySpy dataGroupToXmlConverterFactory;
	private String baseUrl = "someBaseUrl/";
	private String fedoraUsername = "someFedoraUserName";
	private String fedoraPassword = "someFedoraPassWord";
	private DataGroupSpy dataGroup = new DataGroupSpy("someNameInData");
	private List<DataGroup> relatedDataGroups = new ArrayList<>();

	@BeforeMethod
	public void setUp() {
		LoggerFactory loggerFactory = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactory);

		setUpHttpHandlerFactory();
		toFedoraConverterFactory = new DivaFedoraConverterFactorySpy();
		dataGroupToXmlConverterFactory = new ConverterFactorySpy();
		ConverterProvider.setConverterFactory("xml", dataGroupToXmlConverterFactory);

		relatedDataGroups.add(new DataGroupSpy("personDomainPart"));
		relatedDataGroups.add(new DataGroupSpy("personDomainPart"));

		fedoraUpdater = new ClassicFedoraPersonUpdater(httpHandlerFactory, toFedoraConverterFactory,
				baseUrl, fedoraUsername, fedoraPassword);

	}

	private void setUpHttpHandlerFactory() {
		httpHandlerFactory = new HttpHandlerFactorySpy();
		httpHandlerFactory.responseTexts.add("some default responseText");
		httpHandlerFactory.responseCodes.add(200);
	}

	@Test
	public void testInit() {
		assertSame(fedoraUpdater.getHttpHandlerFactory(), httpHandlerFactory);
		assertSame(fedoraUpdater.getDivaCoraToFedoraConverterFactory(), toFedoraConverterFactory);
	}

	@Test
	public void testHttpHandler() {
		fedoraUpdater.updateInFedora("someRecordType", "someRecordId", dataGroup,
				relatedDataGroups);

		HttpHandlerSpy factoredHttpHandler = httpHandlerFactory.factoredHttpHandlers.get(0);
		assertNotNull(factoredHttpHandler);

		assertEquals(httpHandlerFactory.urls.get(0),
				"someBaseUrl/objects/someRecordId/datastreams/METADATA?format=?xml&controlGroup=M&logMessage=coraWritten&checksumType=SHA-512");

		assertEquals(factoredHttpHandler.requestMethod, "PUT");

		assertCorrectCredentials(factoredHttpHandler);

		DivaCoraToFedoraConverterSpy factoredConverter = (DivaCoraToFedoraConverterSpy) toFedoraConverterFactory.factoredToFedoraConverters
				.get(0);
		assertEquals(factoredConverter.returnedXML, factoredHttpHandler.outputStrings.get(0));

	}

	private void assertCorrectCredentials(HttpHandlerSpy factoredHttpHandler) {
		String encoded = Base64.getEncoder().encodeToString(
				(fedoraUsername + ":" + fedoraPassword).getBytes(StandardCharsets.UTF_8));
		assertEquals(factoredHttpHandler.requestProperties.get("Authorization"),
				"Basic " + encoded);
	}

	@Test
	public void testConverters() {
		fedoraUpdater.updateInFedora("someRecordType", "someRecordId", dataGroup,
				relatedDataGroups);

		ConverterSpy factoredConverter = dataGroupToXmlConverterFactory.factoredConverter;
		assertEquals(factoredConverter.dataElements.size(), 3);
		assertSame(factoredConverter.dataElements.get(0), dataGroup);
		assertSame(factoredConverter.dataElements.get(1), relatedDataGroups.get(0));
		assertSame(factoredConverter.dataElements.get(2), relatedDataGroups.get(1));

		// DivaCoraToFedoraConverterSpy factoredConverter = (DivaCoraToFedoraConverterSpy)
		// toFedoraConverterFactory.factoredToFedoraConverters
		// .get(0);
		// assertEquals(toFedoraConverterFactory.factoredToFedoraTypes.get(0), "someRecordType");
		// assertEquals(factoredConverter.record, dataGroup);
	}

	@Test(expectedExceptions = FedoraException.class)
	public void testErrorFromFedora() {
		httpHandlerFactory.responseCodes = new ArrayList<>();
		httpHandlerFactory.responseCodes.add(505);
		fedoraUpdater.updateInFedora("someRecordType", "someRecordId", dataGroup,
				relatedDataGroups);
	}
}
