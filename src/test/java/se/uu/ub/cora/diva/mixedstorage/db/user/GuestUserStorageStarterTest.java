/*
 * Copyright 2020 Uppsala University Library
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
package se.uu.ub.cora.diva.mixedstorage.db.user;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.db.DbException;
import se.uu.ub.cora.diva.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.gatekeeper.user.GuestUserStorageProvider;
import se.uu.ub.cora.gatekeeper.user.UserStorage;
import se.uu.ub.cora.logger.LoggerProvider;

public class GuestUserStorageStarterTest {

	private List<GuestUserStorageProvider> guestUserStorageProviders;
	private Map<String, String> initInfo;
	private LoggerFactorySpy loggerFactorySpy;
	private String testedClassName = "GuestUserStorageStarterImp";
	private GuestUserStorageStarter starter;

	@BeforeMethod
	public void beforeMethod() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		initInfo = new HashMap<>();
		initInfo.put("guestUserId", "someGuestUserId");
		guestUserStorageProviders = new ArrayList<>();
		guestUserStorageProviders.add(new GuestUserStorageProviderSpy());

	}

	@Test(expectedExceptions = DbException.class, expectedExceptionsMessageRegExp = ""
			+ "No implementations found for GuestUserStorageProvider")
	public void testStartModuleThrowsErrorIfNGuestoUserStorageImplementations() throws Exception {
		guestUserStorageProviders.clear();
		startGuestUserStorageStarter();
	}

	private void startGuestUserStorageStarter() {
		starter = new GuestUserStorageStarterImp();
		starter.startUsingInitInfoAndGuestUserStorageProviders(initInfo, guestUserStorageProviders);
	}

	@Test
	public void testStartModuleLogsErrorIfNoUserStorageProviderImplementations() throws Exception {
		guestUserStorageProviders.clear();
		Exception caughtException = null;
		try {
			startGuestUserStorageStarter();
		} catch (Exception e) {
			caughtException = e;
		}
		assertNotNull(caughtException);
		assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
				"No implementations found for GuestUserStorageProvider");
	}

	@Test
	public void testStartLogsInfoIfMoreThanOneGuestUserStorageProviderImplementations()
			throws Exception {
		guestUserStorageProviders.add(new GuestUserStorageProviderSpy2());
		guestUserStorageProviders.add(new GuestUserStorageProviderSpy());
		startGuestUserStorageStarter();
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Found se.uu.ub.cora.diva.mixedstorage.db.user.GuestUserStorageProviderSpy as "
						+ "GuestUserStorageProvider implementation with select order 0.");

		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 1),
				"Found se.uu.ub.cora.diva.mixedstorage.db.user.GuestUserStorageProviderSpy2 as "
						+ "GuestUserStorageProvider implementation with select order 2.");

		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 2),
				"Found se.uu.ub.cora.diva.mixedstorage.db.user.GuestUserStorageProviderSpy as "
						+ "GuestUserStorageProvider implementation with select order 0.");

		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 3),
				"Using se.uu.ub.cora.diva.mixedstorage.db.user.GuestUserStorageProviderSpy2 as "
						+ "GuestUserStorageProvider implementation.");
	}

	@Test
	public void testStartInitInfoSentToUserStorageProviderImplementation() throws Exception {
		GuestUserStorageProviderSpy guestUserStorageProviderSpy = (GuestUserStorageProviderSpy) guestUserStorageProviders
				.get(0);
		startGuestUserStorageStarter();
		assertSame(guestUserStorageProviderSpy.initInfo, initInfo);
	}

	@Test
	public void testGetGuestUserStorage() {
		startGuestUserStorageStarter();
		UserStorage guestUserStorage = starter.getGuestUserStorage();
		GuestUserStorageProviderSpy guestUserStorageProviderSpy = (GuestUserStorageProviderSpy) guestUserStorageProviders
				.get(0);

		assertSame(guestUserStorageProviderSpy.returnedGuestUserStorage, guestUserStorage);
	}
}
