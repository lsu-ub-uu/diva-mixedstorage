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
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.fedoralegacy.FedoraConnectionInfo;
import se.uu.ub.cora.fedoralegacy.FedoraException;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.storage.RecordIdGenerator;
import se.uu.ub.cora.storage.RecordIdGeneratorProvider;

public class DivaIdGeneratorProviderTest {

	private Map<String, String> initInfo = new HashMap<>();
	private LoggerFactorySpy loggerFactorySpy;
	private String testedClassName = "DivaIdGeneratorProvider";
	private RecordIdGeneratorProvider idGeneratorProvider;

	@BeforeMethod
	public void setUp() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		initInfo = new HashMap<>();
		initInfo.put("fedoraURL", "someFedoraURL");
		initInfo.put("fedoraUsername", "someFedoraUsername");
		initInfo.put("fedoraPassword", "someFedoraPassword");
		idGeneratorProvider = new DivaIdGeneratorProvider();
	}

	@Test
	public void testGetSelectOrder() {
		int selectOrder = idGeneratorProvider.getOrderToSelectImplementionsBy();
		assertEquals(selectOrder, 10);

	}

	@Test
	public void testNormalStartupReturnsDivaIdGenerator() {
		idGeneratorProvider.startUsingInitInfo(initInfo);
		RecordIdGenerator recordIdGenerator = idGeneratorProvider.getRecordIdGenerator();
		assertTrue(recordIdGenerator instanceof DivaIdGenerator);
	}

	@Test
	public void testNormalStartupReturnsTheSameIdGeneratorForMultipleCalls() {
		idGeneratorProvider.startUsingInitInfo(initInfo);
		RecordIdGenerator recordIdGenerator = idGeneratorProvider.getRecordIdGenerator();
		RecordIdGenerator recordIdGenerator2 = idGeneratorProvider.getRecordIdGenerator();
		assertSame(recordIdGenerator, recordIdGenerator2);
	}

	@Test
	public void testInitInfoParametersAreUsedInGenerator() throws Exception {
		idGeneratorProvider.startUsingInitInfo(initInfo);

		DivaIdGenerator recordIdGenerator = (DivaIdGenerator) idGeneratorProvider
				.getRecordIdGenerator();
		FedoraConnectionInfo fedoraConnectionInfo = recordIdGenerator.getFedoraConnectionInfo();
		assertEquals(fedoraConnectionInfo.fedoraUrl, initInfo.get("fedoraURL"));
		assertEquals(fedoraConnectionInfo.fedoraUsername, initInfo.get("fedoraUsername"));
		assertEquals(fedoraConnectionInfo.fedoraPassword, initInfo.get("fedoraPassword"));
	}

	@Test
	public void testLoggingNormalStartup() {
		idGeneratorProvider.startUsingInitInfo(initInfo);
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"DivaIdGeneratorProvider starting DivaIdGenerator...");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 1),
				"Found someFedoraURL as fedoraURL");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 2),
				"DivaIdGeneratorProvider started DivaIdGenerator");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassName(testedClassName), 3);
		assertEquals(loggerFactorySpy.getNoOfFatalLogMessagesUsingClassName(testedClassName), 0);
	}

	@Test(expectedExceptions = FedoraException.class, expectedExceptionsMessageRegExp = ""
			+ "InitInfo must contain fedoraURL")
	public void testErrorIfMissingStartParameterFedoraURL() {
		initInfo.remove("fedoraURL");
		idGeneratorProvider.startUsingInitInfo(initInfo);
	}

	@Test(expectedExceptions = FedoraException.class, expectedExceptionsMessageRegExp = ""
			+ "InitInfo must contain fedoraUsername")
	public void testErrorIfMissingStartParameterFedoraUsername() {
		initInfo.remove("fedoraUsername");
		idGeneratorProvider.startUsingInitInfo(initInfo);
	}

	@Test(expectedExceptions = FedoraException.class, expectedExceptionsMessageRegExp = ""
			+ "InitInfo must contain fedoraPassword")
	public void testErrorIfMissingStartParameterFedoraPassword() {
		initInfo.remove("fedoraPassword");
		idGeneratorProvider.startUsingInitInfo(initInfo);
	}

	@Test
	public void testLoggingAndErrorIfMissingParameterFedoraURL() {
		assertFatalLogMessageForMissingParameter("fedoraURL");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassName(testedClassName), 1);
	}

	private void assertFatalLogMessageForMissingParameter(String parameterName) {
		initInfo.remove(parameterName);
		try {
			idGeneratorProvider.startUsingInitInfo(initInfo);
		} catch (Exception e) {

		}
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"DivaIdGeneratorProvider starting DivaIdGenerator...");
		assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
				"InitInfo must contain " + parameterName);
		assertEquals(loggerFactorySpy.getNoOfFatalLogMessagesUsingClassName(testedClassName), 1);
	}

	@Test
	public void testLoggingAndErrorIfMissingParameterFedoraUsername() {
		assertFatalLogMessageForMissingParameter("fedoraUsername");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassName(testedClassName), 2);
	}

	@Test
	public void testLoggingAndErrorIfMissingParameterFedoraPassword() {
		assertFatalLogMessageForMissingParameter("fedoraPassword");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassName(testedClassName), 2);
	}
}
