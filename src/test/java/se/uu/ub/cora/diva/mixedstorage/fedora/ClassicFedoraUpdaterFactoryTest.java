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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;

public class ClassicFedoraUpdaterFactoryTest {

	private HttpHandlerFactorySpy httpHandlerFactory;
	private ClassicFedoraUpdateFactory factory;
	private DivaFedoraConverterFactory toFedoraConverterFactory;
	private String baseUrl = "someBaseUrl";
	private String username = "someUserName";
	private String password = "somePassword";

	@BeforeMethod
	public void setUp() {
		httpHandlerFactory = new HttpHandlerFactorySpy();
		toFedoraConverterFactory = new DivaFedoraConverterFactorySpy();
		factory = new ClassicFedoraUpdaterFactoryImp(httpHandlerFactory, toFedoraConverterFactory,
				baseUrl, username, password);
	}

	@Test
	public void testFactorPerson() {
		String recordType = "person";
		ClassicFedoraPersonUpdater updater = (ClassicFedoraPersonUpdater) factory
				.factor(recordType);
		assertSame(updater.getHttpHandlerFactory(), httpHandlerFactory);
		assertSame(updater.getDivaCoraToFedoraConverterFactory(), toFedoraConverterFactory);
		assertEquals(updater.getBaseUrl(), baseUrl);
		assertEquals(updater.getUsername(), username);
		assertEquals(updater.getPassword(), password);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "Factor ClassicFedoraUpdater not implemented for otherType")
	public void testFactorOtherType() {
		factory.factor("otherType");
	}

}
