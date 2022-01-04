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
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.RepeatableLinkCollectorSpy;
import se.uu.ub.cora.diva.mixedstorage.RepeatableRelatedLinkCollector;
import se.uu.ub.cora.xmlutils.transformer.XsltTransformationFactory;

public class ClassicFedoraUpdaterFactoryTest {

	private HttpHandlerFactorySpy httpHandlerFactory;
	private ClassicFedoraUpdaterFactoryImp factory;
	private String baseUrl = "someBaseUrl";
	private String username = "someUserName";
	private String password = "somePassword";
	private RepeatableRelatedLinkCollector repeatableLinkCollector;
	private FedoraConnectionInfo fedoraConnectionInfo;

	@BeforeMethod
	public void setUp() {
		httpHandlerFactory = new HttpHandlerFactorySpy();
		repeatableLinkCollector = new RepeatableLinkCollectorSpy();
		fedoraConnectionInfo = new FedoraConnectionInfo(baseUrl, username, password);
		factory = new ClassicFedoraUpdaterFactoryImp(httpHandlerFactory, repeatableLinkCollector,
				fedoraConnectionInfo);
	}

	@Test
	public void testInit() {
		assertSame(factory.getHttpHandlerFactory(), httpHandlerFactory);
		assertSame(factory.getRepeatableRelatedLinkCollector(), repeatableLinkCollector);
	}

	@Test
	public void testFactorPerson() {
		String recordType = "person";
		ClassicFedoraPersonUpdater updater = (ClassicFedoraPersonUpdater) factory
				.factor(recordType);
		assertSame(updater.getHttpHandlerFactory(), httpHandlerFactory);
		FedoraConnectionInfo fedoraConnectionInfoUpdater = updater.getFedoraConnectionInfo();
		assertSame(fedoraConnectionInfoUpdater, fedoraConnectionInfo);

		DivaFedoraConverterFactoryImp divaCoraToFedoraConverterFactory = (DivaFedoraConverterFactoryImp) updater
				.getDivaCoraToFedoraConverterFactory();
		assertEquals(divaCoraToFedoraConverterFactory.getFedoraURL(), baseUrl);
		assertTrue(divaCoraToFedoraConverterFactory
				.getCoraTransformerFactory() instanceof XsltTransformationFactory);
		assertSame(divaCoraToFedoraConverterFactory.getRepeatableRelatedLinkCollector(),
				repeatableLinkCollector);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "Factor ClassicFedoraUpdater not implemented for otherType")
	public void testFactorOtherType() {
		factory.factor("otherType");
	}

}
