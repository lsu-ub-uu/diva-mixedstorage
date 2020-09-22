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
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataElement;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.db.DbException;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterSpy;
import se.uu.ub.cora.diva.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.spy.MethodCallRecorder;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.storage.RecordNotFoundException;
import se.uu.ub.cora.storage.RecordStorage;

public class DivaMixedUserStorageTest {

	private UserStorageSpy guestUserStorage;
	private DivaMixedUserStorage userStorage;
	private RecordReaderUserSpy recordReader;
	private LoggerFactorySpy loggerFactorySpy;
	private String testedClassName = "DivaMixedUserStorage";
	private DivaDbToCoraConverterSpy userConverter;
	private DataGroupRoleReferenceCreatorSpy dataGroupRoleReferenceCreator;
	private String userId;
	private RecordStorage recordStorage;

	@BeforeMethod
	public void setUp() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		recordReader = new RecordReaderUserSpy();
		guestUserStorage = new UserStorageSpy();
		userConverter = new DivaDbToCoraConverterSpy();
		dataGroupRoleReferenceCreator = new DataGroupRoleReferenceCreatorSpy();
		userStorage = DivaMixedUserStorage
				.usingGuestUserStorageRecordReaderAndUserConverterAndRoleReferenceCreator(
						guestUserStorage, recordReader, userConverter,
						dataGroupRoleReferenceCreator);
		userId = "userId@user.uu.se";
		setResponseForReadOneRowInRecordReaderSpy("342");
		recordStorage = userStorage;
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
		userStorage.getUserByIdFromLogin(userId);

		recordReader.MCR.assertMethodWasCalled("readOneRowFromDbUsingTableAndConditions");
		recordReader.MCR.assertParameter("readOneRowFromDbUsingTableAndConditions", 0, "tableName",
				"public.user");
		Map<?, ?> usedConditions = (Map<?, ?>) recordReader.MCR
				.getValueForMethodNameAndCallNumberAndParameterName(
						"readOneRowFromDbUsingTableAndConditions", 0, "conditions");
		assertEquals(usedConditions.get("user_id"), "userId");
		assertEquals(usedConditions.get("domain"), "uu");
	}

	@Test
	public void testReadTestTableNameAndConditions() {
		recordStorage.read("", "14");

		recordReader.MCR.assertMethodWasCalled("readOneRowFromDbUsingTableAndConditions");
		recordReader.MCR.assertParameter("readOneRowFromDbUsingTableAndConditions", 0, "tableName",
				"public.user");
		Map<?, ?> usedConditions = (Map<?, ?>) recordReader.MCR
				.getValueForMethodNameAndCallNumberAndParameterName(
						"readOneRowFromDbUsingTableAndConditions", 0, "conditions");
		assertEquals(usedConditions.get("db_id"), "14");
		assertEquals(usedConditions.size(), 1);
	}

	@Test
	public void testGetUserByIdFromLoginTestReturnedDataGroup() {
		DataGroup user = userStorage.getUserByIdFromLogin(userId);

		assertAnswerFromRecordReaderUsedInConverter(user);
	}

	private void assertAnswerFromRecordReaderUsedInConverter(DataGroup user) {
		Object responseFromDB = recordReader.MCR
				.getReturnValue("readOneRowFromDbUsingTableAndConditions", 0);

		assertEquals(userConverter.mapToConvert, responseFromDB);
		assertSame(user, userConverter.convertedDbDataGroup);
	}

	@Test
	public void testReadTestReturnedDataGroup() {
		DataGroup user = recordStorage.read("", "14");

		assertAnswerFromRecordReaderUsedInConverter(user);
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

		userStorage.getUserByIdFromLogin(userId);

		assertSecondCallToDbToReadViewGroupsForUserAndUsesDbIdFromFirstCall();
	}

	private void assertSecondCallToDbToReadViewGroupsForUserAndUsesDbIdFromFirstCall() {
		recordReader.MCR.assertMethodWasCalled("readFromTableUsingConditions");
		recordReader.MCR.assertParameter("readFromTableUsingConditions", 0, "tableName",
				"public.groupsforuser");

		Map<?, ?> returnedUserDbData = (Map<?, ?>) recordReader.MCR
				.getReturnValue("readOneRowFromDbUsingTableAndConditions", 0);

		Map<?, ?> conditionsForGroupsForUser = (Map<?, ?>) recordReader.MCR
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
	public void testReadRecordGroupUsersView() throws Exception {
		setResponseForReadOneRowInRecordReaderSpy("917");

		recordStorage.read("", "14");

		assertSecondCallToDbToReadViewGroupsForUserAndUsesDbIdFromFirstCall();
	}

	@Test
	public void testdataGroupRoleReferenceCreatorNOTCalledForNoReturnedGroupsForUser()
			throws Exception {

		userStorage.getUserByIdFromLogin(userId);

		dataGroupRoleReferenceCreator.methodCallRecorder
				.assertMethodNotCalled("createRoleLinkForDomainAdminUsingDomain");
	}

	@Test
	public void testdataGroupRoleReferenceCreatorNOTCalledForNoReturnedGroupsForUserReadingRecord()
			throws Exception {

		recordStorage.read("", "14");

		dataGroupRoleReferenceCreator.methodCallRecorder
				.assertMethodNotCalled("createRoleLinkForDomainAdminUsingDomain");
	}

	@Test
	public void testdataGroupRoleReferenceCreatorNOTCalledForUnimplementedReturnedGroupsForUser()
			throws Exception {
		addResponseForReadFromTableUsingConditonsReaderSpy("someGroupNotToAdd", "uu");

		userStorage.getUserByIdFromLogin(userId);

		assertRoleReferenceCreatorNOTCalled();
	}

	private void assertRoleReferenceCreatorNOTCalled() {
		dataGroupRoleReferenceCreator.methodCallRecorder
				.assertMethodNotCalled("createRoleLinkForDomainAdminUsingDomain");
		dataGroupRoleReferenceCreator.methodCallRecorder
				.assertMethodNotCalled("createRoleReferenceForSystemAdmin");
	}

	@Test
	public void testdataGroupRoleReferenceCreatorNOTCalledForUnimplementedReturnedGroupsForUserReadingRecord()
			throws Exception {
		addResponseForReadFromTableUsingConditonsReaderSpy("someGroupNotToAdd", "uu");

		recordStorage.read("", "14");

		assertRoleReferenceCreatorNOTCalled();
	}

	@Test
	public void testdataGroupRoleReferenceCreatorOnlyCalledForDomainAdminGroup() throws Exception {
		addResponseForReadFromTableUsingConditonsReaderSpy("someGroupNotToAdd", "kth");
		addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "uu");

		recordStorage.read("", "14");

		assertRoleReferenceCreatorCalledCorrectlyForDomainUU();
	}

	private void assertRoleReferenceCreatorCalledCorrectlyForDomainUU() {
		MethodCallRecorder roleReferenceMCR = dataGroupRoleReferenceCreator.methodCallRecorder;

		List<?> domainList = (List<?>) roleReferenceMCR
				.getValueForMethodNameAndCallNumberAndParameterName(
						"createRoleReferenceForDomainAdminUsingDomains", 0, "domains");

		assertEquals(domainList.size(), 1);
		assertEquals(domainList.get(0), "uu");
		roleReferenceMCR
				.assertNumberOfCallsToMethod("createRoleReferenceForDomainAdminUsingDomains", 1);
		roleReferenceMCR.assertMethodNotCalled("createRoleReferenceForSystemAdmin");
	}

	@Test
	public void testdataGroupRoleReferenceCreatorCalledForReturnedGroupsForUserContainingDomainAdminUU()
			throws Exception {
		addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "uu");

		userStorage.getUserByIdFromLogin(userId);

		assertRoleReferenceCreatorCalledCorrectlyForDomainUU();
	}

	@Test
	public void testdataGroupRoleReferenceCreatorCalledForReturnedGroupsForUserContainingDomainAdminUUReadingRecord()
			throws Exception {
		addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "uu");

		recordStorage.read("", "14");

		assertRoleReferenceCreatorCalledCorrectlyForDomainUU();
	}

	@Test
	public void testdataGroupRoleReferenceCreatorCalledForReturnedGroupsForUserContainingSeveralDomainAdmin()
			throws Exception {
		addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "uu");
		addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "kth");

		userStorage.getUserByIdFromLogin(userId);

		assertRoleReferenceCreatorCalledCorrectlyForTwoDomains();
	}

	private void assertRoleReferenceCreatorCalledCorrectlyForTwoDomains() {
		MethodCallRecorder roleReferenceMCR = dataGroupRoleReferenceCreator.methodCallRecorder;

		List<?> domainList = (List<?>) roleReferenceMCR
				.getValueForMethodNameAndCallNumberAndParameterName(
						"createRoleReferenceForDomainAdminUsingDomains", 0, "domains");

		assertEquals(domainList.size(), 2);
		assertEquals(domainList.get(0), "uu");
		assertEquals(domainList.get(1), "kth");
		roleReferenceMCR.assertMethodNotCalled("createRoleReferenceForSystemAdmin");
	}

	@Test
	public void testdataGroupRoleReferenceCreatorCalledForReturnedGroupsForUserContainingSeveralDomainAdminReadingRecord()
			throws Exception {
		addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "uu");
		addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "kth");

		recordStorage.read("", "14");

		assertRoleReferenceCreatorCalledCorrectlyForTwoDomains();
	}

	@Test
	public void testdataGroupRoleReferenceCreatorCalledForReturnedGroupsForUserContainingDomainAdminKTH()
			throws Exception {
		addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "kth");

		userStorage.getUserByIdFromLogin(userId);

		assertRoleReferenceCreatorCalledForKTHAndNotSystemAdmin();
	}

	private void assertRoleReferenceCreatorCalledForKTHAndNotSystemAdmin() {
		MethodCallRecorder roleReferenceMCR = dataGroupRoleReferenceCreator.methodCallRecorder;
		List<?> domainList = (List<?>) roleReferenceMCR
				.getValueForMethodNameAndCallNumberAndParameterName(
						"createRoleReferenceForDomainAdminUsingDomains", 0, "domains");

		assertEquals(domainList.get(0), "kth");
		roleReferenceMCR.assertMethodNotCalled("createRoleReferenceForSystemAdmin");
	}

	@Test
	public void testdataGroupRoleReferenceCreatorCalledForReturnedGroupsForUserContainingDomainAdminKTHReadingRecord()
			throws Exception {
		addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "kth");

		recordStorage.read("", "14");

		assertRoleReferenceCreatorCalledForKTHAndNotSystemAdmin();
	}

	private void addResponseForReadFromTableUsingConditonsReaderSpy(String groupType,
			String domain) {
		Map<String, Object> row1 = new HashMap<>();
		row1.put("group_type", groupType);
		row1.put("domain", domain);
		recordReader.responseToReadFromTableUsingConditions.add(row1);
	}

	@Test
	public void testDataGroupRoleReferenceCreatorCalledForReturnedGroupsForUserContainingSystemAdmin()
			throws Exception {
		addResponseForReadFromTableUsingConditonsReaderSpy("systemAdmin", "diva");

		userStorage.getUserByIdFromLogin(userId);

		assertRoleReferenceCreatorCalledForSystemAdminAndNOTDiVA();
	}

	private void assertRoleReferenceCreatorCalledForSystemAdminAndNOTDiVA() {
		MethodCallRecorder roleReferenceMCR = dataGroupRoleReferenceCreator.methodCallRecorder;
		roleReferenceMCR.assertMethodWasCalled("createRoleReferenceForSystemAdmin");
		roleReferenceMCR.assertMethodNotCalled("createRoleReferenceForDomainAdmin");
	}

	@Test
	public void testDataGroupRoleReferenceCreatorCalledForReturnedGroupsForUserContainingSystemAdminReadingRecord()
			throws Exception {
		addResponseForReadFromTableUsingConditonsReaderSpy("systemAdmin", "diva");

		recordStorage.read("", "14");

		assertRoleReferenceCreatorCalledForSystemAdminAndNOTDiVA();
	}

	@Test
	public void testDataGroupRoleReferenceCreatorCalledForReturnedGroupsForUserContainingSeveralSystemAdmin()
			throws Exception {
		addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "kth");
		addResponseForReadFromTableUsingConditonsReaderSpy("systemAdmin", "diva");
		addResponseForReadFromTableUsingConditonsReaderSpy("systemAdmin", "other");

		userStorage.getUserByIdFromLogin(userId);

		assertRoleReferenceCreatorCalledOnlyOnceForSystemAdmin();
	}

	private void assertRoleReferenceCreatorCalledOnlyOnceForSystemAdmin() {
		MethodCallRecorder roleReferenceMCR = dataGroupRoleReferenceCreator.methodCallRecorder;
		roleReferenceMCR.assertNumberOfCallsToMethod("createRoleReferenceForSystemAdmin", 1);
		roleReferenceMCR.assertMethodNotCalled("createRoleReferenceForDomainAdminUsingDomain");
	}

	@Test
	public void testDataGroupRoleReferenceCreatorCalledForReturnedGroupsForUserContainingSeveralSystemAdminReadingRecord()
			throws Exception {
		addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "kth");
		addResponseForReadFromTableUsingConditonsReaderSpy("systemAdmin", "diva");
		addResponseForReadFromTableUsingConditonsReaderSpy("systemAdmin", "other");

		recordStorage.read("", "14");

		assertRoleReferenceCreatorCalledOnlyOnceForSystemAdmin();
	}

	@Test
	public void testCreateUserRoleChildHasNotBeenCalledIfRolesNotExist() throws Exception {
		addResponseForReadFromTableUsingConditonsReaderSpy("someGroupNotToAdd", "uu");

		userStorage.getUserByIdFromLogin(userId);

		assertCreateUserRoleChildNOTCalled();
	}

	private void assertCreateUserRoleChildNOTCalled() {
		MethodCallRecorder roleReferenceMCR = dataGroupRoleReferenceCreator.methodCallRecorder;
		roleReferenceMCR.assertMethodNotCalled("createUserRoleChild");
	}

	@Test
	public void testCreateUserRoleChildHasNotBeenCalledIfRolesNotExistReadingRecord()
			throws Exception {
		addResponseForReadFromTableUsingConditonsReaderSpy("someGroupNotToAdd", "uu");

		recordStorage.read("", "14");

		assertCreateUserRoleChildNOTCalled();
	}

	@Test
	public void testCreateUserIsCalledWithSeveralDataGroupsAndSystemAdminExists() throws Exception {
		addResponseForReadFromTableUsingConditonsReaderSpy("systemAdmin", "uu");
		addResponseForReadFromTableUsingConditonsReaderSpy("systemAdmin", "uu");
		addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "uu");
		addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "kth");

		DataGroup userGroup = userStorage.getUserByIdFromLogin(userId);

		assertOnlyCreateRoleReferenceForSystemAdminCalled(userGroup);
	}

	private void assertOnlyCreateRoleReferenceForSystemAdminCalled(DataGroup userGroup) {
		DataGroupSpy returnedUserRole = (DataGroupSpy) dataGroupRoleReferenceCreator.methodCallRecorder
				.getReturnValue("createRoleReferenceForSystemAdmin", 0);

		assertTrue(userGroup.getChildren().contains(returnedUserRole));

		assertEquals(returnedUserRole.getRepeatId(), "0");

		dataGroupRoleReferenceCreator.methodCallRecorder
				.assertMethodNotCalled("createRoleReferenceForDomainAdminUsingDomain");
		dataGroupRoleReferenceCreator.methodCallRecorder
				.assertNumberOfCallsToMethod("createRoleReferenceForSystemAdmin", 1);
	}

	@Test
	public void testCreateUserIsCalledWithSeveralDataGroupsAndSystemAdminExistsReadingRecord()
			throws Exception {
		addResponseForReadFromTableUsingConditonsReaderSpy("systemAdmin", "uu");
		addResponseForReadFromTableUsingConditonsReaderSpy("systemAdmin", "uu");
		addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "uu");
		addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "kth");

		DataGroup userGroup = recordStorage.read("", "14");

		assertOnlyCreateRoleReferenceForSystemAdminCalled(userGroup);
	}

	@Test
	public void testRolesAreNotAddedAsChildForUnimplementedReturnedGroupsForUser()
			throws Exception {
		addResponseForReadFromTableUsingConditonsReaderSpy("unimplementedGroup", "uu");

		DataGroup returnedUser = userStorage.getUserByIdFromLogin(userId);

		assertNoUserRoleGroupInDataGroup(returnedUser);
	}

	private void assertNoUserRoleGroupInDataGroup(DataGroup returnedUser) {
		List<DataElement> allChildrenWithNameInData = returnedUser
				.getAllChildrenWithNameInData("userRole");
		assertNull(allChildrenWithNameInData);
	}

	@Test
	public void testRolesAreNotAddedAsChildForUnimplementedReturnedGroupsForUserReadingRecord()
			throws Exception {
		addResponseForReadFromTableUsingConditonsReaderSpy("unimplementedGroup", "uu");

		DataGroup returnedUser = recordStorage.read("", "14");

		assertNoUserRoleGroupInDataGroup(returnedUser);
	}

	@Test
	public void testRolesAreAddedAsChildForDomainAdmin() throws Exception {
		addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "uu");

		DataGroup userGroup = userStorage.getUserByIdFromLogin(userId);

		assertRolesAreAddedAsChildForDomainAdmin(userGroup);
	}

	private void assertRolesAreAddedAsChildForDomainAdmin(DataGroup userGroup) {
		DataGroupSpy returnedUserRole = (DataGroupSpy) dataGroupRoleReferenceCreator.methodCallRecorder
				.getReturnValue("createRoleReferenceForDomainAdminUsingDomains", 0);

		assertTrue(userGroup.getChildren().contains(returnedUserRole));

		assertEquals(returnedUserRole.getRepeatId(), "0");

		List<DataGroup> userRoleGroups = userGroup.getAllGroupsWithNameInData("userRole");

		assertEquals(userRoleGroups.size(), 1);
		assertSame(returnedUserRole, userRoleGroups.get(0));
	}

	@Test
	public void testRolesAreAddedAsChildForDomainAdminReadingRecord() throws Exception {
		addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "uu");

		DataGroup userGroup = recordStorage.read("", "14");

		assertRolesAreAddedAsChildForDomainAdmin(userGroup);
	}

	@Test
	public void testReadingThroughRecordStorageMustUserNotFoundThrowException() throws Exception {
		recordReader.throwException = true;
		try {
			recordStorage.read("", "14");
		} catch (Exception e) {
			assertEquals(e.getMessage(), "Record not found for type: user and id: 14");
			assertSame(e.getCause(),
					recordReader.MCR.getReturnValue("readOneRowFromDbUsingTableAndConditions", 0));
		}
	}

	@Test(expectedExceptions = RecordNotFoundException.class, expectedExceptionsMessageRegExp = ""
			+ "Record not found for type: user and id: 15")
	public void testReadingThroughRecordStorageMustUserNotFoundThrowExceptionOtherId()
			throws Exception {
		recordReader.throwException = true;
		recordStorage.read("", "15");
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "create is not implemented for user")
	public void createNotImplementedForUser() throws Exception {
		recordStorage.create(null, null, null, null, null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "deleteByTypeAndId is not implemented for user")
	public void deleteByTypeAndIdNotImplementedForUser() throws Exception {
		recordStorage.deleteByTypeAndId(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "linksExistForRecord is not implemented for user")
	public void linksExistForRecordNotImplementedForUser() throws Exception {
		recordStorage.linksExistForRecord(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "update is not implemented for user")
	public void updateNotImplementedForUser() throws Exception {
		recordStorage.update(null, null, null, null, null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "readList is not implemented for user")
	public void readListNotImplementedForUser() throws Exception {
		recordStorage.readList(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "readAbstractList is not implemented for user")
	public void readAbstractListNotImplementedForUser() throws Exception {
		recordStorage.readAbstractList(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "readLinkList is not implemented for user")
	public void readLinkListNotImplementedForUser() throws Exception {
		recordStorage.readLinkList(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "generateLinkCollectionPointingToRecord is not implemented for user")
	public void generateLinkCollectionPointingToRecordNotImplementedForUser() throws Exception {
		recordStorage.generateLinkCollectionPointingToRecord(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "recordsExistForRecordType is not implemented for user")
	public void recordsExistForRecordTypeNotImplementedForUser() throws Exception {
		recordStorage.recordsExistForRecordType(null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "recordExistsForAbstractOrImplementingRecordTypeAndRecordId is not implemented for user")
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdNotImplementedForUser()
			throws Exception {
		recordStorage.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(null, null);
	}
}
