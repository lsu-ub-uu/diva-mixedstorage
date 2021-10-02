/*
 * Copyright 2021 Uppsala University Library
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
import java.util.Set;

import se.uu.ub.cora.sqldatabase.DatabaseValues;
import se.uu.ub.cora.sqldatabase.Row;

public class RowSpy implements Row {
	public Map<String, Object> columnValues = new HashMap<>();
	public List<String> reuqestedColumnNames = new ArrayList<>();

	public void addColumnWithValue(String columnName, Object object) {
		if (object == null) {
			columnValues.put(columnName, DatabaseValues.NULL);
		} else {
			columnValues.put(columnName, object);
		}
	}

	@Override
	public Object getValueByColumn(String columnName) {
		reuqestedColumnNames.add(columnName);
		return columnValues.get(columnName);
	}

	@Override
	public Set<String> columnSet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasColumn(String columnName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasColumnWithNonEmptyValue(String columnName) {
		// TODO Auto-generated method stub
		return false;
	}

}
