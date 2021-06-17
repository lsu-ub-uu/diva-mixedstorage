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
package se.uu.ub.cora.diva.mixedstorage.db.organisation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.sqldatabase.RecordReader;
import se.uu.ub.cora.sqldatabase.ResultDelimiter;

public class OrganisationMultipleRowsRecordReaderSpy implements RecordReader {

	public String usedTableName;
	public Map<String, Object> usedConditions;
	public List<Map<String, Object>> returnedList = new ArrayList<>();
	public int numToReturn = 1;

	@Override
	public List<Map<String, Object>> readAllFromTable(String tableName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> readFromTableUsingConditions(String tableName,
			Map<String, Object> conditions) {
		usedTableName = tableName;
		usedConditions = conditions;

		List<Map<String, Object>> listToReturn = new ArrayList<>();
		for (int i = 0; i < numToReturn; i++) {
			Map<String, Object> map = new HashMap<>();
			map.put("someKey" + i, "someValue" + i);
			returnedList.add(map);
			listToReturn.add(map);
		}
		return listToReturn;
	}

	@Override
	public Map<String, Object> readOneRowFromDbUsingTableAndConditions(String tableName,
			Map<String, Object> conditions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> readNextValueFromSequence(String sequenceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> readAllFromTable(String tableName,
			ResultDelimiter resultDelimiter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long readNumberOfRows(String tableName, Map<String, Object> conditions) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long readNumberOfRows(String tableName, Map<String, Object> conditions, Integer fromNo,
			Integer toNo) {
		// TODO Auto-generated method stub
		return 0;
	}

}
