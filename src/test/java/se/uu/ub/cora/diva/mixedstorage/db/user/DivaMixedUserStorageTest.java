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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
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
import se.uu.ub.cora.diva.mixedstorage.db.organisation.RowSpy;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.SqlDatabaseFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.TableFacadeSpy;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.TableQuerySpy;
import se.uu.ub.cora.diva.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.sqldatabase.Row;
import se.uu.ub.cora.storage.RecordNotFoundException;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.StorageReadResult;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

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
	private SqlDatabaseFactorySpy sqlDatabaseFactory;

	@BeforeMethod
	public void setUp() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		// recordReader = new RecordReaderUserSpy();
		guestUserStorage = new UserStorageSpy();
		userConverter = new DivaDbToCoraConverterSpy();
		sqlDatabaseFactory = new SqlDatabaseFactorySpy();

		dataGroupRoleReferenceCreator = new DataGroupRoleReferenceCreatorSpy();
		createUserStorage();
		userId = "userId@user.uu.se";
		// setResponseForReadOneRowInRecordReaderSpy("342");
		recordStorage = userStorage;
	}

	@Test
	public void testInit() {
		assertSame(userStorage.getUserStorageForGuest(), guestUserStorage);
		assertSame(userStorage.getSqlDatabaseFactory(), sqlDatabaseFactory);
		assertSame(userStorage.getTableFacade(), sqlDatabaseFactory.factoredTableFacade);
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

		TableFacadeSpy tableFacade = sqlDatabaseFactory.factoredTableFacade;
		assertEquals(sqlDatabaseFactory.tableNames.get(0), "public.user");

		TableQuerySpy tableQuery = (TableQuerySpy) tableFacade.tableQueries.get(0);
		assertSame(tableQuery, sqlDatabaseFactory.factoredTableQueries.get(0));
		assertEquals(tableQuery.conditions.get("user_id"), "userId");
		assertEquals(tableQuery.conditions.get("domain"), "uu");

	}

	@Test
	public void testReadTestTableNameAndConditions() {
		userStorage.read("", "14");

		TableFacadeSpy tableFacade = sqlDatabaseFactory.factoredTableFacade;
		assertEquals(sqlDatabaseFactory.tableNames.get(0), "public.user");

		TableQuerySpy tableQuery = (TableQuerySpy) tableFacade.tableQueries.get(0);
		assertSame(tableQuery, sqlDatabaseFactory.factoredTableQueries.get(0));
		assertEquals(tableQuery.conditions.get("db_id"), 14);
		assertEquals(tableQuery.conditions.size(), 1);
	}

	@Test(expectedExceptions = RecordNotFoundException.class, expectedExceptionsMessageRegExp = ""
			+ "Can not convert id to integer for id: user:14")
	public void testIdIsNotANumberThrowsARecordNotFoundException() throws Exception {
		recordStorage.read("", "user:14");
	}

	@Test
	public void testGetUserByIdFromLoginTestReturnedDataGroup() {
		DataGroup user = userStorage.getUserByIdFromLogin(userId);

		assertAnswerFromRecordReaderUsedInConverter(user);
	}

	private void assertAnswerFromRecordReaderUsedInConverter(DataGroup user) {
		TableFacadeSpy tableFacade = sqlDatabaseFactory.factoredTableFacade;

		assertEquals(userConverter.rowToConvert, tableFacade.returnedRows.get(0));
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

	@Test(expectedExceptions = DbException.class, expectedExceptionsMessageRegExp = ""
			+ "Unrecognized format of userIdFromLogin: userId@one.two.three.four.five.six.seven.eight.nine.ten.eleven")
	public void testUnexpectedFormatOfUserIdFromLoginMoreThen10WordsInDomain() {
		userStorage.getUserByIdFromLogin(
				"userId@one.two.three.four.five.six.seven.eight.nine.ten.eleven");
	}

	@Test
	public void testIfUserFormatIsOkWithLongLoginLessThan10Words() {
		DataGroup userByIdFromLogin = userStorage
				.getUserByIdFromLogin("userId@one.two.three.four.five.six.seven.eight.nine.ten");

		assertNotNull(userByIdFromLogin);
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
	}

	@Test
	public void testReadRecordGroupUsersView() throws Exception {
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue("db_id", "917");
		sqlDatabaseFactory.rowToReturn = rowToReturn;

		createUserStorage();

		userStorage.read("", "14");

		TableFacadeSpy tableFacade = sqlDatabaseFactory.factoredTableFacade;

		TableQuerySpy userQuery = (TableQuerySpy) tableFacade.tableQueries.get(0);
		assertEquals(userQuery.conditions.get("db_id"), 14);

		RowSpy returnedUserRow = (RowSpy) tableFacade.returnedRows.get(0);
		assertEquals(returnedUserRow.reuqestedColumnNames.get(0), "db_id");

		assertEquals(sqlDatabaseFactory.tableNames.get(1), "public.groupsforuser");

		TableQuerySpy userGroupQuery = (TableQuerySpy) tableFacade.tableQueries.get(1);
		assertSame(userGroupQuery, sqlDatabaseFactory.factoredTableQueries.get(1));

		assertEquals(userGroupQuery.conditions.get("db_id"),
				returnedUserRow.getValueByColumn("db_id"));
	}

	private void createUserStorage() {
		userStorage = DivaMixedUserStorage
				.usingGuestUserStorageRecordReaderAndUserConverterAndRoleReferenceCreator(
						guestUserStorage, sqlDatabaseFactory, userConverter,
						dataGroupRoleReferenceCreator);
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
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue("group_type", "someGroupNotToAdd");
		rowToReturn.addColumnWithValue("domain", "uu");
		sqlDatabaseFactory.rowToReturn = rowToReturn;

		createUserStorage();

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
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue("group_type", "someGroupNotToAdd");
		rowToReturn.addColumnWithValue("domain", "uu");
		sqlDatabaseFactory.rowToReturn = rowToReturn;

		createUserStorage();

		recordStorage.read("", "14");

		assertRoleReferenceCreatorNOTCalled();
	}

	@Test
	public void testdataGroupRoleReferenceCreatorOnlyCalledForDomainAdminGroup() throws Exception {
		List<Row> rowsToReturn = new ArrayList<>();
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue("group_type", "someGroupNotToAdd");
		rowToReturn.addColumnWithValue("domain", "kth");
		rowsToReturn.add(rowToReturn);
		RowSpy rowToReturn2 = new RowSpy();
		rowToReturn2.addColumnWithValue("group_type", "domainAdmin");
		rowToReturn2.addColumnWithValue("domain", "uu");
		rowsToReturn.add(rowToReturn2);

		sqlDatabaseFactory.rowsToReturn = rowsToReturn;

		createUserStorage();

		userStorage.read("", "14");

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
		List<Row> rowsToReturn = new ArrayList<>();
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue("group_type", "domainAdmin");
		rowToReturn.addColumnWithValue("domain", "uu");
		rowsToReturn.add(rowToReturn);
		sqlDatabaseFactory.rowsToReturn = rowsToReturn;

		createUserStorage();

		userStorage.getUserByIdFromLogin(userId);

		assertRoleReferenceCreatorCalledCorrectlyForDomainUU();
	}

	@Test
	public void testdataGroupRoleReferenceCreatorCalledForReturnedGroupsForUserContainingDomainAdminUUReadingRecord()
			throws Exception {
		List<Row> rowsToReturn = new ArrayList<>();
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue("group_type", "domainAdmin");
		rowToReturn.addColumnWithValue("domain", "uu");
		rowsToReturn.add(rowToReturn);
		sqlDatabaseFactory.rowsToReturn = rowsToReturn;

		createUserStorage();

		userStorage.read("", "14");

		assertRoleReferenceCreatorCalledCorrectlyForDomainUU();
	}

	@Test
	public void testdataGroupRoleReferenceCreatorCalledForReturnedGroupsForUserContainingSeveralDomainAdmin()
			throws Exception {
		List<Row> rowsToReturn = new ArrayList<>();
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue("group_type", "domainAdmin");
		rowToReturn.addColumnWithValue("domain", "uu");
		rowsToReturn.add(rowToReturn);
		RowSpy rowToReturn2 = new RowSpy();
		rowToReturn2.addColumnWithValue("group_type", "domainAdmin");
		rowToReturn2.addColumnWithValue("domain", "kth");
		rowsToReturn.add(rowToReturn2);

		sqlDatabaseFactory.rowsToReturn = rowsToReturn;

		createUserStorage();

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
		List<Row> rowsToReturn = new ArrayList<>();
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue("group_type", "domainAdmin");
		rowToReturn.addColumnWithValue("domain", "uu");
		rowsToReturn.add(rowToReturn);
		RowSpy rowToReturn2 = new RowSpy();
		rowToReturn2.addColumnWithValue("group_type", "domainAdmin");
		rowToReturn2.addColumnWithValue("domain", "kth");
		rowsToReturn.add(rowToReturn2);
		sqlDatabaseFactory.rowsToReturn = rowsToReturn;
		createUserStorage();

		createUserStorage();
		// addRsesponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "kth");

		userStorage.read("", "14");

		assertRoleReferenceCreatorCalledCorrectlyForTwoDomains();
	}

	@Test
	public void testdataGroupRoleReferenceCreatorCalledForReturnedGroupsForUserContainingDomainAdminKTH()
			throws Exception {
		List<Row> rowsToReturn = new ArrayList<>();
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue("group_type", "domainAdmin");
		rowToReturn.addColumnWithValue("domain", "kth");
		rowsToReturn.add(rowToReturn);
		sqlDatabaseFactory.rowsToReturn = rowsToReturn;
		createUserStorage();

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
		List<Row> rowsToReturn = new ArrayList<>();
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue("group_type", "domainAdmin");
		rowToReturn.addColumnWithValue("domain", "kth");
		rowsToReturn.add(rowToReturn);
		sqlDatabaseFactory.rowsToReturn = rowsToReturn;
		createUserStorage();
		// addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "kth");

		userStorage.read("", "14");

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
		List<Row> rowsToReturn = new ArrayList<>();
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue("group_type", "systemAdmin");
		rowToReturn.addColumnWithValue("domain", "diva");
		rowsToReturn.add(rowToReturn);
		sqlDatabaseFactory.rowsToReturn = rowsToReturn;
		createUserStorage();
		// addResponseForReadFromTableUsingConditonsReaderSpy("systemAdmin", "diva");

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
		List<Row> rowsToReturn = new ArrayList<>();
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue("group_type", "systemAdmin");
		rowToReturn.addColumnWithValue("domain", "diva");
		rowsToReturn.add(rowToReturn);
		sqlDatabaseFactory.rowsToReturn = rowsToReturn;
		createUserStorage();
		// addResponseForReadFromTableUsingConditonsReaderSpy("systemAdmin", "diva");

		userStorage.read("", "14");

		assertRoleReferenceCreatorCalledForSystemAdminAndNOTDiVA();
	}

	@Test
	public void testDataGroupRoleReferenceCreatorCalledForReturnedGroupsForUserContainingSeveralSystemAdmin()
			throws Exception {
		List<Row> rowsToReturn = new ArrayList<>();
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue("group_type", "domainAdmin");
		rowToReturn.addColumnWithValue("domain", "kth");
		rowsToReturn.add(rowToReturn);
		RowSpy rowToReturn1 = new RowSpy();
		rowToReturn1.addColumnWithValue("group_type", "systemAdmin");
		rowToReturn1.addColumnWithValue("domain", "diva");
		rowsToReturn.add(rowToReturn1);
		RowSpy rowToReturn2 = new RowSpy();
		rowToReturn2.addColumnWithValue("group_type", "systemAdmin");
		rowToReturn2.addColumnWithValue("domain", "other");
		rowsToReturn.add(rowToReturn2);
		sqlDatabaseFactory.rowsToReturn = rowsToReturn;
		createUserStorage();

		// addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "kth");
		// addResponseForReadFromTableUsingConditonsReaderSpy("systemAdmin", "diva");
		// addResponseForReadFromTableUsingConditonsReaderSpy("systemAdmin", "other");

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
		List<Row> rowsToReturn = new ArrayList<>();
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue("group_type", "domainAdmin");
		rowToReturn.addColumnWithValue("domain", "kth");
		rowsToReturn.add(rowToReturn);
		RowSpy rowToReturn1 = new RowSpy();
		rowToReturn1.addColumnWithValue("group_type", "systemAdmin");
		rowToReturn1.addColumnWithValue("domain", "diva");
		rowsToReturn.add(rowToReturn1);
		RowSpy rowToReturn2 = new RowSpy();
		rowToReturn2.addColumnWithValue("group_type", "systemAdmin");
		rowToReturn2.addColumnWithValue("domain", "other");
		rowsToReturn.add(rowToReturn2);
		sqlDatabaseFactory.rowsToReturn = rowsToReturn;
		createUserStorage();
		// addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "kth");
		// addResponseForReadFromTableUsingConditonsReaderSpy("systemAdmin", "diva");
		// addResponseForReadFromTableUsingConditonsReaderSpy("systemAdmin", "other");

		userStorage.read("", "14");

		assertRoleReferenceCreatorCalledOnlyOnceForSystemAdmin();
	}

	@Test
	public void testCreateUserRoleChildHasNotBeenCalledIfRolesNotExist() throws Exception {
		List<Row> rowsToReturn = new ArrayList<>();
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue("group_type", "someGroupNotToAdd");
		rowToReturn.addColumnWithValue("domain", "uu");
		rowsToReturn.add(rowToReturn);
		sqlDatabaseFactory.rowsToReturn = rowsToReturn;
		createUserStorage();
		// addResponseForReadFromTableUsingConditonsReaderSpy("someGroupNotToAdd", "uu");

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
		List<Row> rowsToReturn = new ArrayList<>();
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue("group_type", "someGroupNotToAdd");
		rowToReturn.addColumnWithValue("domain", "uu");
		rowsToReturn.add(rowToReturn);
		sqlDatabaseFactory.rowsToReturn = rowsToReturn;
		createUserStorage();
		// addResponseForReadFromTableUsingConditonsReaderSpy("someGroupNotToAdd", "uu");

		userStorage.read("", "14");

		assertCreateUserRoleChildNOTCalled();
	}

	@Test
	public void testCreateUserIsCalledWithSeveralDataGroupsAndSystemAdminExists() throws Exception {
		List<Row> rowsToReturn = new ArrayList<>();
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue("group_type", "systemAdmin");
		rowToReturn.addColumnWithValue("domain", "uu");
		rowsToReturn.add(rowToReturn);
		RowSpy rowToReturn1 = new RowSpy();
		rowToReturn1.addColumnWithValue("group_type", "systemAdmin");
		rowToReturn1.addColumnWithValue("domain", "uu");
		rowsToReturn.add(rowToReturn1);
		RowSpy rowToReturn2 = new RowSpy();
		rowToReturn2.addColumnWithValue("group_type", "domainAdmin");
		rowToReturn2.addColumnWithValue("domain", "uu");
		rowsToReturn.add(rowToReturn2);
		RowSpy rowToReturn3 = new RowSpy();
		rowToReturn3.addColumnWithValue("group_type", "domainAdmin");
		rowToReturn3.addColumnWithValue("domain", "kth");
		rowsToReturn.add(rowToReturn3);
		sqlDatabaseFactory.rowsToReturn = rowsToReturn;
		createUserStorage();

		// addResponseForReadFromTableUsingConditonsReaderSpy("systemAdmin", "uu");
		// addResponseForReadFromTableUsingConditonsReaderSpy("systemAdmin", "uu");
		// addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "uu");
		// addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "kth");

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
		List<Row> rowsToReturn = new ArrayList<>();
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue("group_type", "systemAdmin");
		rowToReturn.addColumnWithValue("domain", "uu");
		rowsToReturn.add(rowToReturn);
		RowSpy rowToReturn1 = new RowSpy();
		rowToReturn1.addColumnWithValue("group_type", "systemAdmin");
		rowToReturn1.addColumnWithValue("domain", "uu");
		rowsToReturn.add(rowToReturn1);
		RowSpy rowToReturn2 = new RowSpy();
		rowToReturn2.addColumnWithValue("group_type", "domainAdmin");
		rowToReturn2.addColumnWithValue("domain", "uu");
		rowsToReturn.add(rowToReturn2);
		RowSpy rowToReturn3 = new RowSpy();
		rowToReturn3.addColumnWithValue("group_type", "domainAdmin");
		rowToReturn3.addColumnWithValue("domain", "kth");
		rowsToReturn.add(rowToReturn3);
		sqlDatabaseFactory.rowsToReturn = rowsToReturn;
		createUserStorage();
		// addResponseForReadFromTableUsingConditonsReaderSpy("systemAdmin", "uu");
		// addResponseForReadFromTableUsingConditonsReaderSpy("systemAdmin", "uu");
		// addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "uu");
		// addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "kth");

		DataGroup userGroup = userStorage.read("", "14");

		assertOnlyCreateRoleReferenceForSystemAdminCalled(userGroup);
	}

	@Test
	public void testRolesAreNotAddedAsChildForUnimplementedReturnedGroupsForUser()
			throws Exception {
		List<Row> rowsToReturn = new ArrayList<>();
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue("group_type", "unimplementedGroup");
		rowToReturn.addColumnWithValue("domain", "uu");
		rowsToReturn.add(rowToReturn);
		sqlDatabaseFactory.rowsToReturn = rowsToReturn;
		createUserStorage();
		// addResponseForReadFromTableUsingConditonsReaderSpy("unimplementedGroup", "uu");

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
		List<Row> rowsToReturn = new ArrayList<>();
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue("group_type", "unimplementedGroup");
		rowToReturn.addColumnWithValue("domain", "uu");
		rowsToReturn.add(rowToReturn);
		sqlDatabaseFactory.rowsToReturn = rowsToReturn;
		createUserStorage();
		// addResponseForReadFromTableUsingConditonsReaderSpy("unimplementedGroup", "uu");

		DataGroup returnedUser = userStorage.read("", "14");

		assertNoUserRoleGroupInDataGroup(returnedUser);
	}

	@Test
	public void testRolesAreAddedAsChildForDomainAdmin() throws Exception {
		List<Row> rowsToReturn = new ArrayList<>();
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue("group_type", "domainAdmin");
		rowToReturn.addColumnWithValue("domain", "uu");
		rowsToReturn.add(rowToReturn);
		sqlDatabaseFactory.rowsToReturn = rowsToReturn;
		createUserStorage();
		// addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "uu");

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
		List<Row> rowsToReturn = new ArrayList<>();
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue("group_type", "domainAdmin");
		rowToReturn.addColumnWithValue("domain", "uu");
		rowsToReturn.add(rowToReturn);
		sqlDatabaseFactory.rowsToReturn = rowsToReturn;
		createUserStorage();
		// addResponseForReadFromTableUsingConditonsReaderSpy("domainAdmin", "uu");

		DataGroup userGroup = userStorage.read("", "14");

		assertRolesAreAddedAsChildForDomainAdmin(userGroup);
	}

	@Test
	public void testReadingThroughRecordStorageMustUserNotFoundThrowException() throws Exception {
		sqlDatabaseFactory.tablesToThrowExceptionFor.add("public.user");
		// recordReader.tablesToThrowExceptionFor.add("public.user");
		try {
			userStorage.read("", "14");
		} catch (Exception e) {
			assertEquals(e.getMessage(), "Record not found for type: user and id: 14");
			assertEquals(e.getCause().getMessage(), "Error from spy for table public.user");
		}
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForUser() {
		boolean userExists = userStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId("user", "26");

		assertEquals(sqlDatabaseFactory.tableNames.get(0), "public.user");

		TableQuerySpy tableQuerySpy = sqlDatabaseFactory.factoredTableQueries.get(0);
		assertEquals(tableQuerySpy.conditions.get("db_id"), 26);
		assertTrue(userExists);
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForCoraUser() {
		boolean userExists = userStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId("coraUser", "26");

		assertEquals(sqlDatabaseFactory.tableNames.get(0), "public.user");

		TableQuerySpy tableQuerySpy = sqlDatabaseFactory.factoredTableQueries.get(0);
		assertEquals(tableQuerySpy.conditions.get("db_id"), 26);
		assertTrue(userExists);
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForCoraUserNotFound() {
		sqlDatabaseFactory.tablesToThrowExceptionFor.add("public.user");

		boolean userExists = userStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId("coraUser", "26");
		assertFalse(userExists);
	}

	@Test(expectedExceptions = RecordNotFoundException.class, expectedExceptionsMessageRegExp = ""
			+ "Record not found for type: user and id: 15")
	public void testReadingThroughRecordStorageMustUserNotFoundThrowExceptionOtherId()
			throws Exception {
		sqlDatabaseFactory.tablesToThrowExceptionFor.add("public.user");

		recordStorage.read("", "15");
	}

	@Test(expectedExceptions = RecordNotFoundException.class, expectedExceptionsMessageRegExp = ""
			+ "Error when reading roles for user: 15")
	public void testReadingThroughRecordStorageThrowExceptionWhenReadingRole() throws Exception {
		sqlDatabaseFactory.tablesToThrowExceptionFor.add("public.groupsforuser");

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

	@Test
	public void testReadList() throws Exception {
		List<Row> rowsToReturn = new ArrayList<>();
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue("db_id", 100);
		rowsToReturn.add(rowToReturn);
		RowSpy rowToReturn1 = new RowSpy();
		rowToReturn1.addColumnWithValue("db_id", 200);
		rowsToReturn.add(rowToReturn1);
		sqlDatabaseFactory.rowsToReturn = rowsToReturn;
		createUserStorage();
		// recordReader.responseToReadFromTable = userRows;

		StorageReadResult result = userStorage.readList("user", new DataGroupSpy(""));

		assertEquals(sqlDatabaseFactory.tableNames.get(0), "public.user");

		TableFacadeSpy tableFacade = sqlDatabaseFactory.factoredTableFacade;
		assertEquals(userConverter.rowsToConvert.get(0), tableFacade.rowsToReturn.get(0));
		assertEquals(userConverter.rowsToConvert.get(1), tableFacade.rowsToReturn.get(1));
		assertEquals(result.totalNumberOfMatches, result.listOfDataGroups.size());
	}

	// private List<Map<String, Object>> createDbResponseAndAddToSpy() {
	// List<Map<String, Object>> userRows = new ArrayList<>();
	// createAndAddUserRowUsingId(userRows, 100);
	// createAndAddUserRowUsingId(userRows, 200);
	// return userRows;
	// }
	//
	// private void createAndAddUserRowUsingId(List<Map<String, Object>> userRows, int userId) {
	// Map<String, Object> userRow = new HashMap<>();
	// userRow.put("db_id", userId);
	// userRows.add(userRow);
	// }

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
			+ "recordExistsForAbstractOrImplementingRecordTypeAndRecordId is not implemented for user")
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdNotImplementedForUser()
			throws Exception {
		recordStorage.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "getTotalNumberOfRecordsForType is not implemented for user")
	public void getTotalNumberOfRecordsForTypeNotImplementedForUser() throws Exception {
		recordStorage.getTotalNumberOfRecordsForType("anyType", new DataGroupSpy("filter"));
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "getTotalNumberOfRecordsForAbstractType is not implemented for user")
	public void getTotalNumberOfRecordsForAbstractTypeNotImplementedForUser() throws Exception {
		recordStorage.getTotalNumberOfRecordsForAbstractType("anyType", Collections.emptyList(),
				new DataGroupSpy("filter"));
	}
}
