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
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.logger.LoggerProvider;

public class ClassicIndexerFactoryTest {

	private LoggerFactorySpy loggerFactorySpy;
	private ClassicIndexerFactoryImp indexerFactory;
	private String baseUrl = "someBaseUrl";

	@BeforeMethod
	public void setUp() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		indexerFactory = new ClassicIndexerFactoryImp(baseUrl);
	}

	@Test
	public void testInit() {
		assertEquals(indexerFactory.getBaseUrl(), baseUrl);
	}

	@Test
	public void testFactorPersonIndexer() {
		PersonClassicIndexer classicIndexer = (PersonClassicIndexer) indexerFactory
				.factor("person");
		assertEquals(classicIndexer.getBaseUrl(), baseUrl);
		assertTrue(classicIndexer.getHttpHandlerFactory() instanceof HttpHandlerFactoryImp);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "No ClassicIndexer implementation for otherType")
	public void testNotImplemented() {
		indexerFactory.factor("otherType");
	}
}
