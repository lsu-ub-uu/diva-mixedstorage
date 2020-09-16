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

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DbException;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterSpy;
import se.uu.ub.cora.diva.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.spy.MethodCallRecorder;
import se.uu.ub.cora.logger.LoggerProvider;

public class DivaMixedUserStorageTest {

	private UserStorageSpy guestUserStorage;
	private DivaMixedUserStorage userStorage;
	private RecordReaderUserSpy recordReader;
	private LoggerFactorySpy loggerFactorySpy;
	private String testedClassName = "DivaMixedUserStorage";
	private DivaDbToCoraConverterSpy userConverter;
	private DataGroupLinkCreatorSpy dataGroupLinkCreator;

	@BeforeMethod
	public void setUp() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		recordReader = new RecordReaderUserSpy();
		guestUserStorage = new UserStorageSpy();
		userConverter = new DivaDbToCoraConverterSpy();
		dataGroupLinkCreator = new DataGroupLinkCreatorSpy();
		userStorage = DivaMixedUserStorage
				.usingGuestUserStorageRecordReaderAndUserConverterAndLinkCreator(guestUserStorage,
						recordReader, userConverter, dataGroupLinkCreator);
	}

	@Test
	public void testInit() {
		assertSame(userStorage.getUserStorageForGuest(), guestUserStorage);
		assertSame(userStorage.getRecordReader(), recordReader);
		assertSame(userStorage.getDbToCoraUserConverter(), userConverter);
	}

	@Test
	public void testGetUserById() {
		String userId = "someUserId";
		DataGroup userById = userStorage.getUserById(userId);
		assertTrue(guestUserStorage.getUserByIdWasCalled);
		assertEquals(guestUserStorage.userId, userId);
		assertSame(userById, guestUserStorage.returnedUser);
	}

	@Test
	public void testGetUserByIdFromLoginTestTableNameAndConditions() {
		String userId = "userId@user.uu.se";
		userStorage.getUserByIdFromLogin(userId);

		recordReader.MCR.assertMethodWasCalled("readOneRowFromDbUsingTableAndConditions");
		recordReader.MCR.assertParameter("readOneRowFromDbUsingTableAndConditions", 0, "tableName",
				"public.user");
		// assertEquals(recordReader.usedTableName, "public.user");
		// Map<String, Object> usedConditions = recordReader.usedConditions;
		Map<String, Object> usedConditions = (Map<String, Object>) recordReader.MCR
				.getValueForMethodNameAndCallNumberAndParameterName(
						"readOneRowFromDbUsingTableAndConditions", 0, "conditions");
		assertEquals(usedConditions.get("user_id"), "userId");
		assertEquals(usedConditions.get("domain"), "uu");
	}

	@Test
	public void testGetUserByIdFromLoginTestReturnedDataGroup() {
		String userId = "userId@user.uu.se";
		DataGroup user = userStorage.getUserByIdFromLogin(userId);

		Object responseFromDB = recordReader.MCR
				.getReturnValue("readOneRowFromDbUsingTableAndConditions", 0);

		assertEquals(userConverter.mapToConvert, responseFromDB);
		assertSame(user, userConverter.convertedDbDataGroup);
	}

	@Test(expectedExceptions = DbException.class, expectedExceptionsMessageRegExp = ""
			+ "Unrecognized format of userIdFromLogin: userId@somedomainorg")
	public void testUnexpectedFormatOfUserIdFromLogin() {
		userStorage.getUserByIdFromLogin("userId@somedomainorg");
	}

	@Test
	public void testUnexpectedFormatOfUserIdFromLoginIsLogged() {
		try {
			userStorage.getUserByIdFromLogin("userId@somedomainorg");
		} catch (Exception e) {
		}
		assertEquals(loggerFactorySpy.getErrorLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Unrecognized format of userIdFromLogin: userId@somedomainorg");
	}

	@Test
	public void testReadGroupUsersView() throws Exception {
		setResponseForReadOneRowInRecordReaderSpy("917");

		String userId = "userId@user.uu.se";
		userStorage.getUserByIdFromLogin(userId);

		recordReader.MCR.assertMethodWasCalled("readFromTableUsingConditions");
		recordReader.MCR.assertParameter("readFromTableUsingConditions", 0, "tableName",
				"public.groupsForUser");

		Map<String, Object> returnedUserDbData = (Map<String, Object>) recordReader.MCR
				.getReturnValue("readOneRowFromDbUsingTableAndConditions", 0);

		Map<String, Object> conditionsForGroupsForUser = (Map<String, Object>) recordReader.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("readFromTableUsingConditions",
						0, "conditions");

		assertEquals(conditionsForGroupsForUser.get("db_id"), returnedUserDbData.get("db_id"));
	}

	private void setResponseForReadOneRowInRecordReaderSpy(String value) {
		Map<String, Object> response = new HashMap<>();
		response.put("db_id", Integer.parseInt(value));
		recordReader.responseToReadOneRowFromDbUsingTableAndConditions = response;
	}

	@Test
	public void testReadGroupUsersViewUsesDbIdFromUserDbCall() throws Exception {
		setResponseForReadOneRowInRecordReaderSpy("342");

		String userId = "userId@user.uu.se";
		userStorage.getUserByIdFromLogin(userId);

		Map<String, Object> returnedUserDbData = (Map<String, Object>) recordReader.MCR
				.getReturnValue("readOneRowFromDbUsingTableAndConditions", 0);

		Map<String, Object> conditionsForGroupsForUser = (Map<String, Object>) recordReader.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("readFromTableUsingConditions",
						0, "conditions");

		assertEquals(conditionsForGroupsForUser.get("db_id"), returnedUserDbData.get("db_id"));
	}

	@Test
	public void testDataGroupLinkCreatorNOTCalledForNoReturnedGroupsForUser() throws Exception {
		setResponseForReadOneRowInRecordReaderSpy("342");

		String userId = "userId@user.uu.se";
		userStorage.getUserByIdFromLogin(userId);

		dataGroupLinkCreator.MCR.assertMethodNotCalled("createRoleLinkForDomainAdminUsingDomain");
	}

	@Test
	public void testDataGroupLinkCreatorNOTCalledForUnimplementedReturnedGroupsForUser()
			throws Exception {
		setResponseForReadOneRowInRecordReaderSpy("342");
		addResponseForReadFromTableUsingConditonsReaderSpy("someGroupNotToAdd", "uu");

		String userId = "userId@user.uu.se";
		userStorage.getUserByIdFromLogin(userId);

		dataGroupLinkCreator.MCR.assertMethodNotCalled("createRoleLinkForDomainAdminUsingDomain");
	}

	@Test
	public void testDataGroupLinkCreatorCalledForReturnedGroupsForUserContainingDomainAdminUU()
			throws Exception {
		setResponseForReadOneRowInRecordReaderSpy("342");
		addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "uu");

		String userId = "userId@user.uu.se";
		userStorage.getUserByIdFromLogin(userId);

		MethodCallRecorder linkCreatorMCR = dataGroupLinkCreator.MCR;
		linkCreatorMCR.assertParameter("createRoleLinkForDomainAdminUsingDomain", 0, "domain",
				"uu");
	}

	@Test
	public void testDataGroupLinkCreatorCalledForReturnedGroupsForUserContainingDomainAdminKTH()
			throws Exception {
		setResponseForReadOneRowInRecordReaderSpy("342");
		addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "kth");

		String userId = "userId@user.uu.se";
		userStorage.getUserByIdFromLogin(userId);

		MethodCallRecorder linkCreatorMCR = dataGroupLinkCreator.MCR;
		linkCreatorMCR.assertParameter("createRoleLinkForDomainAdminUsingDomain", 0, "domain",
				"kth");
	}

	private void addResponseForReadFromTableUsingConditonsReaderSpy(String groupType,
			String domain) {
		Map<String, Object> row1 = new HashMap<>();
		row1.put("group_type", groupType);
		row1.put("domain", domain);
		recordReader.responseToReadFromTableUsingConditions.add(row1);
	}
}
