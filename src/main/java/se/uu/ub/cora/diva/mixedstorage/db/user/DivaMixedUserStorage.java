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

import java.util.HashMap;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DbException;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverter;
import se.uu.ub.cora.gatekeeper.user.UserStorage;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.sqldatabase.RecordReader;

public class DivaMixedUserStorage implements UserStorage {

	public static DivaMixedUserStorage usingGuestUserStorageRecordReaderAndUserConverter(
			UserStorage guestUserStorage, RecordReader recordReader,
			DivaDbToCoraConverter userConverter) {
		return new DivaMixedUserStorage(guestUserStorage, recordReader, userConverter);
	}

	private UserStorage guestUserStorage;
	private RecordReader recordReader;
	private Logger log = LoggerProvider.getLoggerForClass(DivaMixedUserStorage.class);
	private DivaDbToCoraConverter userConverter;

	private DivaMixedUserStorage(UserStorage guestUserStorage, RecordReader recordReader,
			DivaDbToCoraConverter userConverter) {
		this.guestUserStorage = guestUserStorage;
		this.recordReader = recordReader;
		this.userConverter = userConverter;
	}

	@Override
	public DataGroup getUserById(String id) {
		return guestUserStorage.getUserById(id);
	}

	@Override
	public DataGroup getUserByIdFromLogin(String idFromLogin) {
		logAndThrowExceptionIfUnexpectedFormatOf(idFromLogin);

		Map<String, Object> conditions = createConditions(idFromLogin);

		Map<String, Object> readRow = recordReader
				.readOneRowFromDbUsingTableAndConditions("public.user", conditions);
		return userConverter.fromMap(readRow);
	}

	private void logAndThrowExceptionIfUnexpectedFormatOf(String idFromLogin) {
		if (wrongFormatForIdFromLogin(idFromLogin)) {
			String errorMessage = "Unrecognized format of userIdFromLogin: " + idFromLogin;
			log.logErrorUsingMessage(errorMessage);
			throw DbException.withMessage(errorMessage);
		}
	}

	private boolean wrongFormatForIdFromLogin(String idFromLogin) {
		return !idFromLogin.matches("^\\w+@(\\w+\\.){1,}\\w+$");
	}

	private Map<String, Object> createConditions(String idFromLogin) {
		Map<String, Object> conditions = new HashMap<>();
		String userId = getUserIdFromIdFromLogin(idFromLogin);
		conditions.put("user_id", userId);
		String userDomain = getDomainFromLogin(idFromLogin);
		conditions.put("domain", userDomain);
		return conditions;
	}

	private String getUserIdFromIdFromLogin(String idFromLogin) {
		int indexOfAt = idFromLogin.indexOf('@');
		return idFromLogin.substring(0, indexOfAt);
	}

	private String getDomainFromLogin(String idFromLogin) {
		String[] splitAtAt = idFromLogin.split("@");
		String domainPart = splitAtAt[1];

		String[] loginDomainNameParts = domainPart.split("\\.");
		int secondLevelDomainPosition = loginDomainNameParts.length - 2;
		return loginDomainNameParts[secondLevelDomainPosition];
	}

	public UserStorage getUserStorageForGuest() {
		// needed for test
		return guestUserStorage;
	}

	public RecordReader getRecordReader() {
		// needed for test
		return recordReader;
	}

	public DivaDbToCoraConverter getDbToCoraUserConverter() {
		return userConverter;
	}

}
