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
package se.uu.ub.cora.diva.mixedstorage.classic;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.HttpHandlerFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.fedora.HttpHandlerSpy;
import se.uu.ub.cora.diva.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.logger.LoggerProvider;

public class PersonClassicIndexerTest {

	private LoggerFactorySpy loggerFactorySpy;
	private HttpHandlerFactorySpy httpHandlerFactory;
	private String baseUrl;
	private PersonClassicIndexer indexer;
	private String testedClassName = "PersonClassicIndexer";

	@BeforeMethod
	public void setUp() {
		setUpLogger();
		httpHandlerFactory = new HttpHandlerFactorySpy();
		httpHandlerFactory.responseTexts.add("some responseText from spy");
		httpHandlerFactory.responseCodes.add(200);
		baseUrl = "someBase/index/url";
		indexer = new PersonClassicIndexer(httpHandlerFactory, baseUrl);
	}

	private void setUpLogger() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
	}

	@Test
	public void testInit() {
		assertSame(indexer.getHttpHandlerFactory(), httpHandlerFactory);
		assertEquals(indexer.getBaseUrl(), baseUrl);
	}

	@Test
	public void testIndex() {
		indexer.index("someRecordId");
		HttpHandlerSpy factoredHttpHandler = httpHandlerFactory.factoredHttpHandlers.get(0);
		assertEquals(httpHandlerFactory.urls.get(0),
				baseUrl + "authority/person/index/someRecordId");
		assertEquals(factoredHttpHandler.requestMethod, "GET");
		assertEquals(loggerFactorySpy.getNoOfErrorLogMessagesUsingClassName(testedClassName), 0);
	}

	@Test
	public void testNotOKResponse() {
		httpHandlerFactory.responseCodes.remove(0);
		httpHandlerFactory.responseCodes.add(500);
		indexer.index("someRecordId");

		String firstErrorMessage = loggerFactorySpy
				.getErrorLogMessageUsingClassNameAndNo(testedClassName, 0);
		assertEquals(firstErrorMessage,
				"Unable to index record in classic, recordId: someRecordId");

	}

}
