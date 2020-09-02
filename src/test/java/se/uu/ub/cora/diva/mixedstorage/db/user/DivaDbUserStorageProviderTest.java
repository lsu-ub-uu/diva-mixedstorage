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
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroupFactory;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.mixedstorage.DataGroupFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.gatekeeper.user.GuestUserStorageProvider;
import se.uu.ub.cora.gatekeeper.user.UserStorage;
import se.uu.ub.cora.logger.LoggerProvider;

public class DivaDbUserStorageProviderTest {
	private String basePath = "/tmp/recordStorageOnDiskTemp/";
	private LoggerFactorySpy loggerFactorySpy;
	private DataGroupFactory dataGroupFactory;
	private DivaDbUserStorageProvider divaUserStorageProvider;
	private List<GuestUserStorageProvider> guestUserStorageProviders;

	private Map<String, String> initInfo;
	private GuestUserStorageStarterSpy starter;

	@BeforeMethod
	public void setUp() throws IOException {
		setUpFactoriesAndProviders();
		setUpInitInfo();
		guestUserStorageProviders = new ArrayList<>();
		guestUserStorageProviders.add(new GuestUserStorageProviderSpy());
		divaUserStorageProvider = new DivaDbUserStorageProvider();
		starter = new GuestUserStorageStarterSpy();
		divaUserStorageProvider.setGuestUserStorageStarter(starter);
	}

	private void setUpFactoriesAndProviders() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		dataGroupFactory = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactory);
	}

	private void setUpInitInfo() {
		initInfo = new HashMap<>();
		initInfo.put("storageOnDiskBasePath", basePath);
		initInfo.put("databaseLookupName", "java:/comp/env/jdbc/postgres");
	}

	@Test
	public void testPreferenceLevel() {
		divaUserStorageProvider.startUsingInitInfo(null);
		assertEquals(divaUserStorageProvider.getOrderToSelectImplementionsBy(), 10);
	}

	@Test
	public void testDefaultStarter() {
		divaUserStorageProvider = new DivaDbUserStorageProvider();
		assertTrue(divaUserStorageProvider
				.getUserStorageStarter() instanceof GuestUserStorageStarterImp);
	}

	@Test
	public void testInit() throws Exception {
		divaUserStorageProvider.startUsingInitInfo(initInfo);
		DivaMixedUserStorage userStorage = (DivaMixedUserStorage) divaUserStorageProvider
				.getUserStorage();
		GuestUserStorageStarter userStorageStarter = divaUserStorageProvider
				.getUserStorageStarter();

		UserStorage guestUserStorageInUserStorage = userStorage.getUserStorageForGuest();
		UserStorage guestUserStorageFromStarter = userStorageStarter.getGuestUserStorage();

		assertSame(guestUserStorageInUserStorage, guestUserStorageFromStarter);

		// kolla RecordReader
		// kolla userConverter

	}

	@Test
	public void testGuestUserStorageStarter() throws Exception {
		divaUserStorageProvider.startUsingInitInfo(initInfo);

		assertTrue(starter.starterWasCalled);
		assertSame(starter.initInfo, initInfo);
		Iterable<GuestUserStorageProvider> iterable = starter.guestUserStorageProviderImplementations;
		assertTrue(iterable instanceof ServiceLoader);

	}

	// @Test(expectedExceptions = DbException.class, expectedExceptionsMessageRegExp = ""
	// + "No implementations found for GuestUserStorageProvider")
	// public void testStartGuestUserStorageThrowsErrorIfNoUserStorageImplementations()
	// throws Exception {
	// guestUserStorageProviders.clear();
	// divaUserStorageProvider.startUsingInitInfo(initInfo);
	//
	// }
}
