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
package se.uu.ub.cora.diva.mixedstorage.db;

import java.util.Map;

import se.uu.ub.cora.sqldatabase.DataUpdater;
import se.uu.ub.cora.sqldatabase.RecordDeleter;

public class RecordDeleterSpy implements RecordDeleter {

	public boolean deleteWasCalled = false;
	public Map<String, Object> usedConditions;
	public String usedTableName;

	@Override
	public void deleteFromTableUsingConditions(String tableName, Map<String, Object> conditions) {
		usedTableName = tableName;
		usedConditions = conditions;
		deleteWasCalled = true;

	}

	@Override
	public DataUpdater getDataUpdater() {
		// TODO Auto-generated method stub
		return null;
	}

}