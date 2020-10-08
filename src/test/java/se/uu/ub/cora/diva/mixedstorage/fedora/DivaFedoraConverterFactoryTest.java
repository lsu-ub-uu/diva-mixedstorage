/*
 * Copyright 2018 Uppsala University Library
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
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;

public class DivaFedoraConverterFactoryTest {
	private DivaFedoraConverterFactoryImp divaToCoraConverterFactoryImp;
	private String fedoraURL = "someFedoraUrl";

	@BeforeMethod
	public void beforeMethod() {
		divaToCoraConverterFactoryImp = DivaFedoraConverterFactoryImp.usingFedoraURL(fedoraURL);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "No converter implemented for: someType")
	public void factorUnknownTypeThrowsException() throws Exception {
		divaToCoraConverterFactoryImp.factorToCoraConverter("someType");
	}

	@Test
	public void testFactoryPerson() throws Exception {
		DivaXmlParserFedoraToCoraConverter converter = divaToCoraConverterFactoryImp
				.factorToCoraConverter("person");
		assertTrue(converter instanceof DivaXmlParserFedoraToCoraPersonConverter);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "No converter implemented for: someType")
	public void factorToFedoraUnknownTypeThrowsException() throws Exception {
		divaToCoraConverterFactoryImp.factorToFedoraConverter("someType");
	}

	@Test
	public void testFactoryToFedoraPerson() throws Exception {
		DivaCoraToFedoraConverter converter = divaToCoraConverterFactoryImp
				.factorToFedoraConverter("person");
		assertTrue(converter instanceof DivaCoraToFedoraPersonConverter);
	}

	@Test
	public void testFactorToFedoraForPlaceHasCorrectDependencies() throws Exception {
		DivaCoraToFedoraPersonConverter converter = (DivaCoraToFedoraPersonConverter) divaToCoraConverterFactoryImp
				.factorToFedoraConverter("person");
		assertTrue(converter.getHttpHandlerFactory() instanceof HttpHandlerFactoryImp);
		assertEquals(converter.getFedorURL(), fedoraURL);
	}

	@Test
	public void testGetFedoraURLNeededForTests() throws Exception {
		assertEquals(divaToCoraConverterFactoryImp.getFedoraURL(), fedoraURL);
	}
}
