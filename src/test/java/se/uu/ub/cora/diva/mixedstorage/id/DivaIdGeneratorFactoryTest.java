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

import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.testng.annotations.Test;

import se.uu.ub.cora.fedoralegacy.FedoraConnectionInfo;
import se.uu.ub.cora.fedoralegacy.parser.XMLXPathParserFactoryImp;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;

public class DivaIdGeneratorFactoryTest {

	@Test
	public void testPrivateConstructor() throws Exception {
		Constructor<DivaIdGeneratorFactory> constructor = DivaIdGeneratorFactory.class
				.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
	}

	@Test(expectedExceptions = InvocationTargetException.class)
	public void testPrivateConstructorInvoke() throws Exception {
		Constructor<DivaIdGeneratorFactory> constructor = DivaIdGeneratorFactory.class
				.getDeclaredConstructor();
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void testFactorDivaIdGenerator() {
		FedoraConnectionInfo fedoraConnectionInfo = new FedoraConnectionInfo("someFedoraUrl",
				"someUsername", "somePassword");

		DivaIdGenerator recordIdGenerator = (DivaIdGenerator) DivaIdGeneratorFactory
				.factor(fedoraConnectionInfo);
		assertSame(recordIdGenerator.getFedoraConnectionInfo(), fedoraConnectionInfo);
		assertTrue(recordIdGenerator.getHttpHandlerFactory() instanceof HttpHandlerFactoryImp);
		assertTrue(
				recordIdGenerator.getXMLXPathParserFactory() instanceof XMLXPathParserFactoryImp);
	}
}
