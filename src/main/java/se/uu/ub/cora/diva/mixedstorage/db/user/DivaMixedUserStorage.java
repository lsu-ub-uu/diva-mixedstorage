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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DbException;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverter;
import se.uu.ub.cora.gatekeeper.user.UserStorage;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.sqldatabase.RecordReader;

public class DivaMixedUserStorage implements UserStorage {

	private static final String DB_ID = "db_id";

	public static DivaMixedUserStorage usingGuestUserStorageRecordReaderAndUserConverterAndRoleReferenceCreator(
			UserStorage guestUserStorage, RecordReader recordReader,
			DivaDbToCoraConverter userConverter,
			DataGroupRoleReferenceCreator dataGroupRoleReferenceCreator) {
		return new DivaMixedUserStorage(guestUserStorage, recordReader, userConverter,
				dataGroupRoleReferenceCreator);
	}

	private UserStorage guestUserStorage;
	private RecordReader recordReader;
	private Logger log = LoggerProvider.getLoggerForClass(DivaMixedUserStorage.class);
	private DivaDbToCoraConverter userConverter;
	private DataGroupRoleReferenceCreator dataGroupRoleReferenceCreator;

	private DivaMixedUserStorage(UserStorage guestUserStorage, RecordReader recordReader,
			DivaDbToCoraConverter userConverter,
			DataGroupRoleReferenceCreator dataGroupRoleReferenceCreator) {
		this.guestUserStorage = guestUserStorage;
		this.recordReader = recordReader;
		this.userConverter = userConverter;
		this.dataGroupRoleReferenceCreator = dataGroupRoleReferenceCreator;
	}

	@Override
	public DataGroup getUserById(String id) {
		return guestUserStorage.getUserById(id);
	}

	@Override
	public DataGroup getUserByIdFromLogin(String idFromLogin) {
		logAndThrowExceptionIfUnexpectedFormatOf(idFromLogin);
		Map<String, Object> conditions = createConditions(idFromLogin);
		return readAndConvertUser(conditions);
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
		addUserId(idFromLogin, conditions);
		addDomain(idFromLogin, conditions);
		return conditions;
	}

	private void addUserId(String idFromLogin, Map<String, Object> conditions) {
		String userId = getUserIdFromIdFromLogin(idFromLogin);
		conditions.put("user_id", userId);
	}

	private void addDomain(String idFromLogin, Map<String, Object> conditions) {
		String userDomain = getDomainFromLogin(idFromLogin);
		conditions.put("domain", userDomain);
	}

	private DataGroup readAndConvertUser(Map<String, Object> conditions) {
		Map<String, Object> userRowFromDb = recordReader
				.readOneRowFromDbUsingTableAndConditions("public.user", conditions);
		DataGroup user = userConverter.fromMap(userRowFromDb);
		readAndPossiblyAddRoles(userRowFromDb, user);

		return user;
	}

	private void readAndPossiblyAddRoles(Map<String, Object> userRowFromDb, DataGroup user) {
		List<DataGroup> rolesList = readAndConvertClassicGroupsToCoraRoles(userRowFromDb);
		possiblyAddRoles(rolesList, user);
	}

	private List<DataGroup> readAndConvertClassicGroupsToCoraRoles(
			Map<String, Object> userRowFromDb) {
		List<Map<String, Object>> groupRowsFromDb = readGroupsFromDb(userRowFromDb);
		return convertClassicGroupsToCoraRoles(groupRowsFromDb);
	}

	private List<Map<String, Object>> readGroupsFromDb(Map<String, Object> userDataFromDb) {
		Map<String, Object> conditionsForGroupsForUser = calculateUserForGroupsConditions(
				userDataFromDb);
		return recordReader.readFromTableUsingConditions("public.groupsforuser",
				conditionsForGroupsForUser);
	}

	private List<DataGroup> convertClassicGroupsToCoraRoles(
			List<Map<String, Object>> groupRowsFromDb) {

		List<String> domains = new ArrayList<>();
		for (Map<String, Object> group : groupRowsFromDb) {
			if (groupTypeIsSystemAdmin(group)) {
				return createSystemAdminRole();
			} else if (groupTypeIsDomainAdminRole(group)) {
				domains.add((String) group.get("domain"));
			}
		}
		return getDomainAdminRolesOrEmptyList(domains);
	}

	private List<DataGroup> getDomainAdminRolesOrEmptyList(List<String> domains) {
		if (domainAdminRoleFound(domains)) {
			DataGroup domainAdmin = dataGroupRoleReferenceCreator
					.createRoleReferenceForDomainAdminUsingDomain(domains);
			return Arrays.asList(domainAdmin);
		}
		return Collections.emptyList();
	}

	private boolean groupTypeIsSystemAdmin(Map<String, Object> group) {
		return "systemAdmin".equals(group.get("group_type"));
	}

	private List<DataGroup> createSystemAdminRole() {
		DataGroup systemAdmin = dataGroupRoleReferenceCreator.createRoleReferenceForSystemAdmin();
		return Arrays.asList(systemAdmin);
	}

	private boolean groupTypeIsDomainAdminRole(Map<String, Object> group) {
		return "domainAdmin".equals(group.get("group_type"));
	}

	private boolean domainAdminRoleFound(List<String> domains) {
		return !domains.isEmpty();
	}

	private void possiblyAddRoles(List<DataGroup> roleList, DataGroup user) {
		if (matchingCoraRolesFound(roleList)) {

			// TODO: datagrupperna är ju redan skapade, ska de inte bara addas till user?
			// vad ska createUserRoleChild göra?createRoleReferenceForDomainAdminUsingDomain
			// borde det inte vara:
			// int repeatId = 0;
			// for(DataGroup role : roleList){
			// role.setRepeatId(String.valueOf(repeatId));
			// user.addChild(role);
			// repeatId++;

			// }
			DataGroup createUserRoleChild = dataGroupRoleReferenceCreator
					.createUserRoleChild(roleList);
			user.addChild(createUserRoleChild);
		}
	}

	private boolean matchingCoraRolesFound(List<DataGroup> rolesList) {
		return !rolesList.isEmpty();
	}

	private Map<String, Object> calculateUserForGroupsConditions(Map<String, Object> readRow) {
		Map<String, Object> groupConditions = new HashMap<>();
		groupConditions.put(DB_ID, readRow.get(DB_ID));
		return groupConditions;
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

	UserStorage getUserStorageForGuest() {
		// needed for test
		return guestUserStorage;
	}

	RecordReader getRecordReader() {
		// needed for test
		return recordReader;
	}

	DivaDbToCoraConverter getDbToCoraUserConverter() {
		// needed for test
		return userConverter;
	}

	DataGroupRoleReferenceCreator getDataGroupRoleReferenceCreator() {
		// needed for test
		return dataGroupRoleReferenceCreator;
	}

}
