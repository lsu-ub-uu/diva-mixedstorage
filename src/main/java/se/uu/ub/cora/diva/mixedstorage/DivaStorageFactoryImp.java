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

import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverter;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraUserConverter;
import se.uu.ub.cora.diva.mixedstorage.db.user.DataGroupRoleReferenceCreator;
import se.uu.ub.cora.diva.mixedstorage.db.user.DataGroupRoleReferenceCreatorImp;
import se.uu.ub.cora.diva.mixedstorage.db.user.DivaMixedUserStorage;
import se.uu.ub.cora.gatekeeper.user.UserStorage;
import se.uu.ub.cora.sqldatabase.RecordReader;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;
import se.uu.ub.cora.storage.RecordStorage;

public class DivaStorageFactoryImp implements DivaStorageFactory {

	private UserStorage guestUserStorage;
	private RecordReaderFactory recordReaderFactory;

	public static DivaStorageFactoryImp usingGuestUserStorageAndRecordReader(
			UserStorage guestUserStorage, RecordReaderFactory recordReaderFactory) {
		return new DivaStorageFactoryImp(guestUserStorage, recordReaderFactory);
	}

	private DivaStorageFactoryImp(UserStorage guestUserStorage,
			RecordReaderFactory recordReaderFactory) {
		this.guestUserStorage = guestUserStorage;
		this.recordReaderFactory = recordReaderFactory;
	}

	@Override
	public RecordStorage factorForRecordType(String recordType) {
		if (recordType.equals("user")) {
			return factorUserStorage();
		}
		throw NotImplementedException.withMessage("No storage implemented for: " + recordType);
	}

	private RecordStorage factorUserStorage() {
		DivaDbToCoraConverter userConverter = new DivaDbToCoraUserConverter();
		DataGroupRoleReferenceCreator dataGroupRoleReferenceCreator = new DataGroupRoleReferenceCreatorImp();
		RecordReader recordReader = recordReaderFactory.factor();
		return DivaMixedUserStorage
				.usingGuestUserStorageRecordReaderAndUserConverterAndRoleReferenceCreator(
						guestUserStorage, recordReader, userConverter,
						dataGroupRoleReferenceCreator);
	}

	public UserStorage getGuestUserStorage() {
		return guestUserStorage;
	}

	public RecordReaderFactory getReaderFactory() {
		return recordReaderFactory;
	}

}
