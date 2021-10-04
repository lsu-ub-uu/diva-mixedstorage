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

import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverter;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraUserConverter;
import se.uu.ub.cora.diva.mixedstorage.db.user.DataGroupRoleReferenceCreator;
import se.uu.ub.cora.diva.mixedstorage.db.user.DataGroupRoleReferenceCreatorImp;
import se.uu.ub.cora.diva.mixedstorage.db.user.DivaMixedUserStorage;
import se.uu.ub.cora.gatekeeper.user.UserStorage;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;
import se.uu.ub.cora.storage.RecordStorage;

public class DivaStorageFactoryImp implements DivaStorageFactory {

	private UserStorage guestUserStorage;
	private SqlDatabaseFactory sqlDatabaseFactory;

	public static DivaStorageFactoryImp usingGuestUserStorageAndRecordReader(
			UserStorage guestUserStorage, SqlDatabaseFactory sqlDatabaseFactory) {
		return new DivaStorageFactoryImp(guestUserStorage, sqlDatabaseFactory);
	}

	private DivaStorageFactoryImp(UserStorage guestUserStorage,
			SqlDatabaseFactory sqlDatabaseFactory) {
		this.guestUserStorage = guestUserStorage;
		this.sqlDatabaseFactory = sqlDatabaseFactory;
	}

	@Override
	public RecordStorage factorForRecordType(String recordType) {
		if ("user".equals(recordType) || "coraUser".equals(recordType)) {
			return factorRecordUserStorage();
		}
		throw NotImplementedException.withMessage("No storage implemented for: " + recordType);
	}

	private RecordStorage factorRecordUserStorage() {
		DivaDbToCoraConverter userConverter = new DivaDbToCoraUserConverter();
		DataGroupRoleReferenceCreator dataGroupRoleReferenceCreator = new DataGroupRoleReferenceCreatorImp();
		return DivaMixedUserStorage
				.usingGuestUserStorageDatabaseFactoryAndUserConverterAndRoleReferenceCreator(
						guestUserStorage, sqlDatabaseFactory, userConverter,
						dataGroupRoleReferenceCreator);
	}

	public UserStorage getGuestUserStorage() {
		return guestUserStorage;
	}
}
