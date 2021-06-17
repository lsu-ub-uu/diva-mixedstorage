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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.db.DbException;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverter;
import se.uu.ub.cora.gatekeeper.user.UserStorage;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.sqldatabase.RecordReader;
import se.uu.ub.cora.sqldatabase.SqlStorageException;
import se.uu.ub.cora.storage.RecordNotFoundException;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.StorageReadResult;

public class DivaMixedUserStorage implements UserStorage, RecordStorage {

	private static final String PUBLIC_USER = "public.user";
	private static final String DOMAIN = "domain";
	private static final String USER_ID = "user_id";
	private static final String DOMAIN_ADMIN = "domainAdmin";
	private static final String SYSTEM_ADMIN = "systemAdmin";
	private static final String DB_ID = "db_id";
	private UserStorage guestUserStorage;
	private RecordReader recordReader;
	private Logger log = LoggerProvider.getLoggerForClass(DivaMixedUserStorage.class);
	private DivaDbToCoraConverter userConverter;
	private DataGroupRoleReferenceCreator dataGroupRoleReferenceCreator;

	public static DivaMixedUserStorage usingGuestUserStorageRecordReaderAndUserConverterAndRoleReferenceCreator(
			UserStorage guestUserStorage, RecordReader recordReader,
			DivaDbToCoraConverter userConverter,
			DataGroupRoleReferenceCreator dataGroupRoleReferenceCreator) {
		return new DivaMixedUserStorage(guestUserStorage, recordReader, userConverter,
				dataGroupRoleReferenceCreator);
	}

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
		return readUserByUserId(idFromLogin);
	}

	private DataGroup readUserByUserId(String idFromLogin) {
		logAndThrowExceptionIfUnexpectedFormatOf(idFromLogin);
		Map<String, Object> conditions = createConditions(idFromLogin);
		Map<String, Object> userRowFromDb = recordReader
				.readOneRowFromDbUsingTableAndConditions(PUBLIC_USER, conditions);
		DataGroup user = userConverter.fromMap(userRowFromDb);
		readAndPossiblyAddRoles(userRowFromDb, user);

		return user;
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
		conditions.put(USER_ID, userId);
	}

	private void addDomain(String idFromLogin, Map<String, Object> conditions) {
		String userDomain = getDomainFromLogin(idFromLogin);
		conditions.put(DOMAIN, userDomain);
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
		String groupType = getUsersGroupTypeWithMostPermissions(groupRowsFromDb);
		if (SYSTEM_ADMIN.equals(groupType)) {
			return createSystemAdminRole();
		}
		if (DOMAIN_ADMIN.equals(groupType)) {
			return createDomainAdminRole(groupRowsFromDb);
		}
		return createNoRolesForAllOtherGroupTypes();
	}

	private String getUsersGroupTypeWithMostPermissions(List<Map<String, Object>> groupRowsFromDb) {
		String groupType = "other";
		for (Map<String, Object> group : groupRowsFromDb) {
			if (groupTypeIsSystemAdmin(group)) {
				return SYSTEM_ADMIN;
			} else if (groupTypeIsDomainAdminRole(group)) {
				groupType = DOMAIN_ADMIN;
			}
		}
		return groupType;
	}

	private boolean groupTypeIsSystemAdmin(Map<String, Object> group) {
		return SYSTEM_ADMIN.equals(group.get("group_type"));
	}

	private boolean groupTypeIsDomainAdminRole(Map<String, Object> group) {
		return DOMAIN_ADMIN.equals(group.get("group_type"));
	}

	private List<DataGroup> createSystemAdminRole() {
		DataGroup systemAdmin = dataGroupRoleReferenceCreator.createRoleReferenceForSystemAdmin();
		return Collections.singletonList(systemAdmin);
	}

	private List<DataGroup> createDomainAdminRole(List<Map<String, Object>> groupRowsFromDb) {
		List<String> domains = readDomainsForDomainAdminRoles(groupRowsFromDb);
		DataGroup domainAdmin = dataGroupRoleReferenceCreator
				.createRoleReferenceForDomainAdminUsingDomains(domains);
		return Collections.singletonList(domainAdmin);
	}

	private List<String> readDomainsForDomainAdminRoles(List<Map<String, Object>> groupRowsFromDb) {
		List<String> domains = new ArrayList<>();
		for (Map<String, Object> group : groupRowsFromDb) {
			if (groupTypeIsDomainAdminRole(group)) {
				domains.add((String) group.get(DOMAIN));
			}
		}
		return domains;
	}

	private List<DataGroup> createNoRolesForAllOtherGroupTypes() {
		return Collections.emptyList();
	}

	private void possiblyAddRoles(List<DataGroup> roleList, DataGroup user) {
		if (matchingCoraRolesFound(roleList)) {
			addRoles(roleList, user);
		}
	}

	private boolean matchingCoraRolesFound(List<DataGroup> rolesList) {
		return !rolesList.isEmpty();
	}

	private void addRoles(List<DataGroup> roleList, DataGroup user) {
		int repeatId = 0;
		for (DataGroup role : roleList) {
			role.setRepeatId(String.valueOf(repeatId));
			user.addChild(role);
			repeatId++;
		}
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

	@Override
	public DataGroup read(String type, String id) {
		Map<String, Object> userRowFromDb = createConditionsAndTryToRead(id);
		return tryToConvertUserAndAddRoles(id, userRowFromDb);
	}

	private Map<String, Object> createConditionsAndTryToRead(String id) {
		throwDbExceptionIfIdNotAnIntegerValue(id);
		Map<String, Object> conditions = createConditionsForId(id);
		return tryToReadUser(id, conditions);
	}

	private void throwDbExceptionIfIdNotAnIntegerValue(String id) {
		try {
			Integer.valueOf(id);
		} catch (NumberFormatException ne) {
			throw new RecordNotFoundException("Can not convert id to integer for id: " + id, ne);
		}
	}

	private DataGroup tryToConvertUserAndAddRoles(String id, Map<String, Object> userRowFromDb) {
		try {
			DataGroup user = userConverter.fromMap(userRowFromDb);
			readAndPossiblyAddRoles(userRowFromDb, user);
			return user;
		} catch (SqlStorageException sqle) {
			throw new RecordNotFoundException("Error when reading roles for user: " + id, sqle);
		}
	}

	private Map<String, Object> tryToReadUser(String id, Map<String, Object> conditions) {
		try {
			return recordReader.readOneRowFromDbUsingTableAndConditions(PUBLIC_USER, conditions);
		} catch (SqlStorageException s) {
			throw new RecordNotFoundException("Record not found for type: user and id: " + id, s);
		}
	}

	private Map<String, Object> createConditionsForId(String id) {
		Map<String, Object> conditions = new HashMap<>();
		conditions.put(DB_ID, Integer.valueOf(id));
		return conditions;
	}

	@Override
	public void create(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		throw NotImplementedException.withMessage("create is not implemented for user");
	}

	@Override
	public void deleteByTypeAndId(String type, String id) {
		throw NotImplementedException.withMessage("deleteByTypeAndId is not implemented for user");
	}

	@Override
	public boolean linksExistForRecord(String type, String id) {
		throw NotImplementedException
				.withMessage("linksExistForRecord is not implemented for user");
	}

	@Override
	public void update(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		throw NotImplementedException.withMessage("update is not implemented for user");
	}

	@Override
	public StorageReadResult readList(String type, DataGroup filter) {
		List<Map<String, Object>> rowsFromDb = recordReader.readAllFromTable(PUBLIC_USER);
		List<DataGroup> dataGroups = convertUserGroupsAndAddToList(rowsFromDb);
		return createStorageResult(dataGroups);
	}

	private List<DataGroup> convertUserGroupsAndAddToList(List<Map<String, Object>> rowsFromDb) {
		List<DataGroup> dataGroups = new ArrayList<>(rowsFromDb.size());
		for (Map<String, Object> row : rowsFromDb) {
			DataGroup convertedUser = userConverter.fromMap(row);
			dataGroups.add(convertedUser);
		}
		return dataGroups;
	}

	private StorageReadResult createStorageResult(List<DataGroup> dataGroups) {
		StorageReadResult storageReadResult = new StorageReadResult();
		storageReadResult.listOfDataGroups = dataGroups;
		storageReadResult.start = 0;
		storageReadResult.totalNumberOfMatches = dataGroups.size();
		return storageReadResult;
	}

	@Override
	public StorageReadResult readAbstractList(String type, DataGroup filter) {
		throw NotImplementedException.withMessage("readAbstractList is not implemented for user");
	}

	@Override
	public DataGroup readLinkList(String type, String id) {
		throw NotImplementedException.withMessage("readLinkList is not implemented for user");
	}

	@Override
	public Collection<DataGroup> generateLinkCollectionPointingToRecord(String type, String id) {
		throw NotImplementedException
				.withMessage("generateLinkCollectionPointingToRecord is not implemented for user");
	}

	@Override
	public boolean recordExistsForAbstractOrImplementingRecordTypeAndRecordId(String type,
			String id) {
		if ("user".equals(type) || "coraUser".equals(type)) {
			return checkIfUserExist(id);
		}
		throw NotImplementedException.withMessage(
				"recordExistsForAbstractOrImplementingRecordTypeAndRecordId is not implemented for user");
	}

	private boolean checkIfUserExist(String id) {
		try {
			createConditionsAndTryToRead(id);
		} catch (RecordNotFoundException exception) {
			return false;
		}
		return true;
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
		// needed for test
		return userConverter;
	}

	public DataGroupRoleReferenceCreator getDataGroupRoleReferenceCreator() {
		// needed for test
		return dataGroupRoleReferenceCreator;
	}

	@Override
	public long getTotalNumberOfRecordsForType(String type, DataGroup filter) {
		throw NotImplementedException
				.withMessage("getTotalNumberOfRecordsForType is not implemented for user");
	}

	@Override
	public long getTotalNumberOfRecordsForAbstractType(String abstractType,
			List<String> implementingTypes, DataGroup filter) {
		throw NotImplementedException
				.withMessage("getTotalNumberOfRecordsForAbstractType is not implemented for user");
	}
}
