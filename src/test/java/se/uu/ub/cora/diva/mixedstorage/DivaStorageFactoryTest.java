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
package se.uu.ub.cora.diva.mixedstorage;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraUserConverter;
import se.uu.ub.cora.diva.mixedstorage.db.RecordReaderFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.user.DataGroupRoleReferenceCreatorImp;
import se.uu.ub.cora.diva.mixedstorage.db.user.DivaMixedUserStorage;
import se.uu.ub.cora.diva.mixedstorage.db.user.UserStorageSpy;
import se.uu.ub.cora.diva.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.gatekeeper.user.UserStorage;
import se.uu.ub.cora.logger.LoggerFactory;
import se.uu.ub.cora.logger.LoggerProvider;

public class DivaStorageFactoryTest {

	private UserStorage guestUserStorage;
	private RecordReaderFactorySpy recordReaderFactory;
	private DivaStorageFactoryImp factory;

	@BeforeMethod
	public void setUp() {
		guestUserStorage = new UserStorageSpy();
		recordReaderFactory = new RecordReaderFactorySpy();
		LoggerFactory loggerFactory = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactory);
		factory = DivaStorageFactoryImp.usingGuestUserStorageAndRecordReader(guestUserStorage,
				recordReaderFactory);
	}

	@Test
	public void testFactorDivaMixedUserStorage() {
		DivaMixedUserStorage factoredStorage = (DivaMixedUserStorage) factory
				.factorForRecordType("user");
		assertTrue(factoredStorage
				.getDataGroupRoleReferenceCreator() instanceof DataGroupRoleReferenceCreatorImp);
		assertTrue(factoredStorage.getDbToCoraUserConverter() instanceof DivaDbToCoraUserConverter);

		assertSame(factoredStorage.getUserStorageForGuest(), guestUserStorage);

		assertNotNull(factoredStorage.getRecordReader());
		assertSame(factoredStorage.getRecordReader(), recordReaderFactory.factored);
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
