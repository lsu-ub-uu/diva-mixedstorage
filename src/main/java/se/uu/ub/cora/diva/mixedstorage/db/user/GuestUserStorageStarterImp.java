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

import java.util.Map;

import se.uu.ub.cora.diva.mixedstorage.db.DbException;
import se.uu.ub.cora.gatekeeper.user.GuestUserStorageProvider;
import se.uu.ub.cora.gatekeeper.user.SelectOrder;
import se.uu.ub.cora.gatekeeper.user.UserStorage;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;

public class GuestUserStorageStarterImp implements GuestUserStorageStarter {

	private Logger logger = LoggerProvider.getLoggerForClass(GuestUserStorageStarterImp.class);
	private UserStorage guestUserStorage;

	@Override
	public void startUsingInitInfoAndGuestUserStorageProviders(Map<String, String> initInfo,
			Iterable<GuestUserStorageProvider> guestUserStorageProviders) {

		GuestUserStorageProvider guestUserStorageProvider = getImplementationBasedOnPreferenceLevelThrowErrorIfNone(
				guestUserStorageProviders, "GuestUserStorageProvider");
		guestUserStorageProvider.startUsingInitInfo(initInfo);
		guestUserStorage = guestUserStorageProvider.getGuestUserStorage();

	}

	private <T extends SelectOrder> T getImplementationBasedOnPreferenceLevelThrowErrorIfNone(
			Iterable<T> implementations, String interfaceClassName) {
		T implementation = findAndLogPreferedImplementation(implementations, interfaceClassName);
		throwErrorIfNoImplementationFound(interfaceClassName, implementation);
		logger.logInfoUsingMessage("Using " + implementation.getClass().getName() + " as "
				+ interfaceClassName + " implementation.");
		return implementation;
	}

	private <T extends SelectOrder> T findAndLogPreferedImplementation(Iterable<T> implementations,
			String interfaceClassName) {
		T implementation = null;
		int preferenceLevel = -99999;
		for (T currentImplementation : implementations) {
			if (preferenceLevel < currentImplementation.getOrderToSelectImplementionsBy()) {
				preferenceLevel = currentImplementation.getOrderToSelectImplementionsBy();
				implementation = currentImplementation;
			}
			logger.logInfoUsingMessage("Found " + currentImplementation.getClass().getName()
					+ " as " + interfaceClassName + " implementation with select order "
					+ currentImplementation.getOrderToSelectImplementionsBy() + ".");
		}
		return implementation;
	}

	private <T extends SelectOrder> void throwErrorIfNoImplementationFound(
			String interfaceClassName, T implementation) {
		if (null == implementation) {
			String errorMessage = "No implementations found for " + interfaceClassName;
			logger.logFatalUsingMessage(errorMessage);
			throw DbException.withMessage(errorMessage);
		}
	}

	@Override
	public UserStorage getGuestUserStorage() {
		return guestUserStorage;
	}

}
