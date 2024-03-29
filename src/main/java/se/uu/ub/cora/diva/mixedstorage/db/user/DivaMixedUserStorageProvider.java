/*
 * Copyright 2020, 2021 Uppsala University Library
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

import java.util.Map;
import java.util.ServiceLoader;

import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverter;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraUserConverter;
import se.uu.ub.cora.gatekeeper.user.GuestUserStorageProvider;
import se.uu.ub.cora.gatekeeper.user.UserStorage;
import se.uu.ub.cora.gatekeeper.user.UserStorageProvider;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactoryImp;

public class DivaMixedUserStorageProvider implements UserStorageProvider {

	private DivaMixedUserStorage userStorage;
	private Map<String, String> initInfo;
	private GuestUserStorageStarter guestUserStorageStarter = new GuestUserStorageStarterImp();
	private Logger logger = LoggerProvider.getLoggerForClass(DivaMixedUserStorageProvider.class);

	@Override
	public int getOrderToSelectImplementionsBy() {
		return 10;
	}

	@Override
	public UserStorage getUserStorage() {
		return userStorage;
	}

	@Override
	public void startUsingInitInfo(Map<String, String> initInfo) {
		this.initInfo = initInfo;
		createUserStorage();
	}

	private void createUserStorage() {
		UserStorage guestUserStorage = createGuestUserStorage();
		SqlDatabaseFactoryImp sqlDatabaseFactory = createDatabaseFactory();
		DivaDbToCoraConverter converter = new DivaDbToCoraUserConverter();
		DataGroupRoleReferenceCreator dataGroupRoleReferenceCreator = new DataGroupRoleReferenceCreatorImp();

		userStorage = DivaMixedUserStorage
				.usingGuestUserStorageDatabaseFactoryAndUserConverterAndRoleReferenceCreator(
						guestUserStorage, sqlDatabaseFactory, converter,
						dataGroupRoleReferenceCreator);
	}

	private UserStorage createGuestUserStorage() {
		Iterable<GuestUserStorageProvider> guestUserStorageProviders = ServiceLoader
				.load(GuestUserStorageProvider.class);
		guestUserStorageStarter.startUsingInitInfoAndGuestUserStorageProviders(initInfo,
				guestUserStorageProviders);
		return guestUserStorageStarter.getGuestUserStorage();
	}

	private SqlDatabaseFactoryImp createDatabaseFactory() {
		String name = tryToGetInitParameter("databaseLookupName");
		return SqlDatabaseFactoryImp.usingLookupNameFromContext(name);
	}

	private String tryToGetInitParameter(String parameterName) {
		throwErrorIfKeyIsMissingFromInitInfo(parameterName);
		String parameter = initInfo.get(parameterName);
		logger.logInfoUsingMessage("Found " + parameter + " as " + parameterName);
		return parameter;
	}

	private void throwErrorIfKeyIsMissingFromInitInfo(String key) {
		if (!initInfo.containsKey(key)) {
			String errorMessage = "InitInfo must contain " + key;
			logger.logFatalUsingMessage(errorMessage);
			throw new RuntimeException(errorMessage);
		}
	}

	public GuestUserStorageStarter getUserStorageStarter() {
		return guestUserStorageStarter;
	}

	public void setGuestUserStorageStarter(GuestUserStorageStarter guestUserStorageStarter) {
		this.guestUserStorageStarter = guestUserStorageStarter;

	}

}
