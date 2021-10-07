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
package se.uu.ub.cora.diva.mixedstorage;

import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraUserConverter;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.SqlDatabaseFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.user.DataGroupRoleReferenceCreatorImp;
import se.uu.ub.cora.diva.mixedstorage.db.user.DivaMixedUserStorage;
import se.uu.ub.cora.diva.mixedstorage.db.user.UserStorageSpy;
import se.uu.ub.cora.diva.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.gatekeeper.user.UserStorage;
import se.uu.ub.cora.logger.LoggerFactory;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;

public class DivaStorageFactoryTest {

	private UserStorage guestUserStorage;
	private DivaStorageFactoryImp factory;
	private SqlDatabaseFactory sqlDatabaseFactory;

	@BeforeMethod
	public void setUp() {
		guestUserStorage = new UserStorageSpy();
		// recordReaderFactory = new RecordReaderFactorySpy();
		sqlDatabaseFactory = new SqlDatabaseFactorySpy();
		LoggerFactory loggerFactory = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactory);
		factory = DivaStorageFactoryImp.usingGuestUserStorageAndSqlDatabaseFactory(guestUserStorage,
				sqlDatabaseFactory);
	}

	@Test
	public void testFactorDivaMixedUserStorage() {
		DivaMixedUserStorage factoredStorage = (DivaMixedUserStorage) factory
				.factorForRecordType("user");
		assertTrue(factoredStorage
				.getDataGroupRoleReferenceCreator() instanceof DataGroupRoleReferenceCreatorImp);
		assertTrue(factoredStorage.getDbToCoraUserConverter() instanceof DivaDbToCoraUserConverter);

		assertSame(factoredStorage.getUserStorageForGuest(), guestUserStorage);

		assertSame(factoredStorage.getSqlDatabaseFactory(), sqlDatabaseFactory);
		assertTrue(factoredStorage instanceof DivaMixedUserStorage);
	}

	@Test
	public void testFactorDivaMixedUserStorageForCoraUser() {
		DivaMixedUserStorage factoredStorage = (DivaMixedUserStorage) factory
				.factorForRecordType("coraUser");
		assertTrue(factoredStorage
				.getDataGroupRoleReferenceCreator() instanceof DataGroupRoleReferenceCreatorImp);
		assertTrue(factoredStorage.getDbToCoraUserConverter() instanceof DivaDbToCoraUserConverter);

		assertSame(factoredStorage.getUserStorageForGuest(), guestUserStorage);

		assertSame(factoredStorage.getSqlDatabaseFactory(), sqlDatabaseFactory);
		assertTrue(factoredStorage instanceof DivaMixedUserStorage);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "No storage implemented for: someType")
	public void testNotImplemented() {
		factory.factorForRecordType("someType");
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "No storage implemented for: someOtherNotImplementedType")
	public void testOtherNotImplemented() {
		factory.factorForRecordType("someOtherNotImplementedType");
	}

}
