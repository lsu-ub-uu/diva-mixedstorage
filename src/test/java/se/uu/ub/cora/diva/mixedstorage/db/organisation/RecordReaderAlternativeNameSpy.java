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

public class RecordReaderAlternativeNameSpy implements RecordReader {

	public String usedTableName;
	public Map<String, Object> usedConditions;
	public Map<String, Object> nameToReturn = new HashMap<>();
	public List<Map<String, Object>> namesToReturn = new ArrayList<>();
	public String sequenceName;
	public Map<String, Object> nextVal;

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
		return namesToReturn;
	}

	@Override
	public Map<String, Object> readOneRowFromDbUsingTableAndConditions(String tableName,
			Map<String, Object> conditions) {
		usedTableName = tableName;
		usedConditions = conditions;
		return nameToReturn;
	}

	@Override
	public Map<String, Object> readNextValueFromSequence(String sequenceName) {
		this.sequenceName = sequenceName;
		nextVal = new HashMap<>();
		nextVal.put("nextval", 3);
		return nextVal;
	}

}
