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

import se.uu.ub.cora.sqldatabase.table.TableQuery;

public class TableQuerySpy implements TableQuery {

	public Map<String, Object> conditions = new HashMap<>();
	public boolean throwException = false;
	public String tableName;
	public List<String> ascOrdersBy = new ArrayList<>();
	public List<Long> fromNumbers = new ArrayList<>();
	public List<Long> toNumbers = new ArrayList<>();

	public TableQuerySpy(String tableName) {
		this.tableName = tableName;
	}

	@Override
	public void addParameter(String name, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addCondition(String name, Object value) {
		conditions.put(name, value);
	}

	@Override
	public void setFromNo(Long fromNo) {
		fromNumbers.add(fromNo);
	}

	@Override
	public void setToNo(Long toNo) {
		toNumbers.add(toNo);
	}

	@Override
	public void addOrderByAsc(String column) {
		ascOrdersBy.add(column);
		// TODO Auto-generated method stub

	}

	@Override
	public void addOrderByDesc(String column) {
		// TODO Auto-generated method stub

	}

	@Override
	public String assembleCreateSql() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String assembleReadSql() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String assembleUpdateSql() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String assembleDeleteSql() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object> getQueryValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String assembleCountSql() {
		// TODO Auto-generated method stub
		return null;
	}

}
