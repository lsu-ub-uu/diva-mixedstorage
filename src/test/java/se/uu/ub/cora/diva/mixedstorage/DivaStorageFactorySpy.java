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

import se.uu.ub.cora.storage.RecordStorage;

public class DivaStorageFactorySpy implements DivaStorageFactory {

	public String type;
	public RecordStorage factored;
	public boolean readWasCalled = false;
	public boolean factorNotFound = false;
	public boolean recordExists = false;

	@Override
	public RecordStorage factorForRecordType(String type) {
		readWasCalled = true;
		this.type = type;
		if (factorNotFound) {
			factored = new DivaDbToCoraStorageNotFoundSpy();
		} else {
			factored = new RecordStorageSpy();
			((RecordStorageSpy) factored).linkExistsInStorage = recordExists;
		}
		return factored;
	}

}
