/*
 * Copyright 2018, 2021 Uppsala University Library
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
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;

public class DivaFedoraConverterFactoryTest {
	private DivaFedoraConverterFactoryImp divaToCoraConverterFactoryImp;
	private String fedoraURL = "someFedoraUrl";
	private TransformationFactorySpy transformationFactory;

	@BeforeMethod
	public void beforeMethod() {
		transformationFactory = new TransformationFactorySpy();
		divaToCoraConverterFactoryImp = DivaFedoraConverterFactoryImp
				.usingFedoraURLAndTransformerFactory(fedoraURL, transformationFactory);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "No converter implemented for: someType")
	public void factorUnknownTypeThrowsException() throws Exception {
		divaToCoraConverterFactoryImp.factorToCoraConverter("someType");
	}

	@Test
	public void testFactoryPerson() throws Exception {
		DivaFedoraToCoraConverter converter = divaToCoraConverterFactoryImp
				.factorToCoraConverter("person");
		DivaFedoraToCoraConverterImp personConverter = (DivaFedoraToCoraConverterImp) converter;

		assertNotNull(personConverter.getCoraTransformation());
		assertSame(personConverter.getCoraTransformation(),
				transformationFactory.transformationSpy);

		assertEquals(transformationFactory.xsltPath, "person/coraPerson.xsl");
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
	public void testFactorToFedoraForPersonHasCorrectDependencies() throws Exception {
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
