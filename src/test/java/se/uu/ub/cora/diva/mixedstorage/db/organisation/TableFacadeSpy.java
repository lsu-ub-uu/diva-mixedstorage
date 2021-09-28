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

import se.uu.ub.cora.sqldatabase.Row;
import se.uu.ub.cora.sqldatabase.table.TableFacade;
import se.uu.ub.cora.sqldatabase.table.TableQuery;

public class TableFacadeSpy implements TableFacade {

	public boolean readOneRowForQueryWasCalled = false;
	public TableQuery tableQuery;
	public List<Row> returnedRows = new ArrayList<>();
	public int numToReturn;
	public RowSpy rowToReturn = new RowSpy();

	@Override
	public void insertRowUsingQuery(TableQuery tableQuery) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Row> readRowsForQuery(TableQuery tableQuery) {
		this.tableQuery = tableQuery;
		for (int i = 0; i < numToReturn; i++) {
			returnedRows.add(new RowSpy());
		}
		return returnedRows;
	}

	@Override
	public Row readOneRowForQuery(TableQuery tableQuery) {
		readOneRowForQueryWasCalled = true;
		this.tableQuery = tableQuery;
		// Row rowToReturn = new RowSpy();
		returnedRows.add(rowToReturn);
		return rowToReturn;
	}

	@Override
	public long readNumberOfRows(TableQuery tableQuery) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void updateRowsUsingQuery(TableQuery tableQuery) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteRowsForQuery(TableQuery tableQuery) {
		// TODO Auto-generated method stub

	}

	@Override
	public long nextValueFromSequence(String sequenceName) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void startTransaction() {
		// TODO Auto-generated method stub

	}

	@Override
	public void endTransaction() {
		// TODO Auto-generated method stub

	}

	@Override
	public void rollback() {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

}
