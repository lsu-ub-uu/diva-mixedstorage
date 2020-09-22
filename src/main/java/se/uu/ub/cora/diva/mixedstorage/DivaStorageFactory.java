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

/**
 * DivaStorageFactory is used to get the right RecordStorage for different recordTypes.
 */
public interface DivaStorageFactory {
	/**
	 * factorForRecordType should return the correct implementation for the requested recordType.
	 * Each call to this method should return a RecordStorage that can be expected to serve one
	 * thread at a time, if this is a new instance for each call or if some type reuse the same
	 * instance is dependent on the returned implementation.
	 * 
	 * @param recordType
	 *            the record type to factor a RecordStorage for
	 * @return A factored RecordStorage, that is expected to be used only by one thread at once.
	 */
	RecordStorage factorForRecordType(String recordType);

}
