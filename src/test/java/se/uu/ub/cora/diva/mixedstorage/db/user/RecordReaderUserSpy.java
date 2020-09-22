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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.diva.mixedstorage.spy.MethodCallRecorder;
import se.uu.ub.cora.sqldatabase.RecordReader;
import se.uu.ub.cora.sqldatabase.SqlStorageException;

public class RecordReaderUserSpy implements RecordReader {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public Map<String, Object> responseToReadOneRowFromDbUsingTableAndConditions = new HashMap<>();
	public List<Map<String, Object>> responseToReadFromTableUsingConditions = new ArrayList<>();
	public boolean throwException = false;

	@Override
	public List<Map<String, Object>> readAllFromTable(String tableName) {
		MCR.addCall("tableName", tableName);
		return null;
	}

	@Override
	public List<Map<String, Object>> readFromTableUsingConditions(String tableName,
			Map<String, Object> conditions) {
		MCR.addCall("tableName", tableName, "conditions", conditions);
		if (throwException) {
			SqlStorageException exception = SqlStorageException.withMessageAndException(
					"Exception from RecordReaderUserSpy", new RuntimeException());
			MCR.addReturned(exception);
			throw exception;
		}

		MCR.addReturned(responseToReadFromTableUsingConditions);
		return responseToReadFromTableUsingConditions;
	}

	@Override
	public Map<String, Object> readOneRowFromDbUsingTableAndConditions(String tableName,
			Map<String, Object> conditions) {
		MCR.addCall("tableName", tableName, "conditions", conditions);
		if (throwException) {
			SqlStorageException exception = SqlStorageException.withMessageAndException(
					"Exception from RecordReaderUserSpy", new RuntimeException());
			MCR.addReturned(exception);
			throw exception;
		}
		MCR.addReturned(responseToReadOneRowFromDbUsingTableAndConditions);
		return responseToReadOneRowFromDbUsingTableAndConditions;
	}

	@Override
	public Map<String, Object> readNextValueFromSequence(String sequenceName) {
		MCR.addCall("sequenceName", sequenceName);
		MCR.addReturned(null);
		return null;
	}

}
