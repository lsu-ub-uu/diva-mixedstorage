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
import java.util.List;

import se.uu.ub.cora.sqldatabase.DatabaseFacade;
import se.uu.ub.cora.sqldatabase.Row;
import se.uu.ub.cora.sqldatabase.SqlDatabaseException;

public class DatabaseFacadeSpy implements DatabaseFacade {

	public List<String> sqls = new ArrayList<>();
	public List<List<Object>> valuesList = new ArrayList<>();
	public boolean startTransactionWasCalled = false;
	public boolean endTransactionWasCalled = false;
	public boolean throwErrorInDatabaseFacade = false;
	public boolean rollbackWasCalled = false;

	@Override
	public List<Row> readUsingSqlAndValues(String sql, List<Object> values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Row readOneRowOrFailUsingSqlAndValues(String sql, List<Object> values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int executeSqlWithValues(String sql, List<Object> values) {
		valuesList.add(values);
		sqls.add(sql);

		if (throwErrorInDatabaseFacade) {
			throw SqlDatabaseException.withMessage("Error from databaseFacadeSpy");
		}
		return 0;
	}

	@Override
	public void startTransaction() {
		startTransactionWasCalled = true;
	}

	@Override
	public void endTransaction() {
		endTransactionWasCalled = true;
	}

	@Override
	public void rollback() {
		rollbackWasCalled = true;

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

}
