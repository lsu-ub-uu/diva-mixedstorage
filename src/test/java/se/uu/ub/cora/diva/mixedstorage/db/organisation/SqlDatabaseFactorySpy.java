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
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;
import se.uu.ub.cora.sqldatabase.table.TableFacade;
import se.uu.ub.cora.sqldatabase.table.TableQuery;

public class SqlDatabaseFactorySpy implements SqlDatabaseFactory {

	public List<TableQuerySpy> factoredTableQueries = new ArrayList<>();
	public TableQuerySpy factoredTableQuery;
	public List<String> tableNames = new ArrayList<>();
	public String tableName;
	public TableFacadeSpy factoredTableFacade;
	public int numToReturn = 3;
	public RowSpy rowToReturn = null;
	public DatabaseFacadeSpy factoredDatabaseFacade;
	public List<Row> rowsToReturn = new ArrayList<>();
	public List<String> tablesToThrowExceptionFor = new ArrayList<>();
	public boolean throwErrorInDatabaseFacade = false;

	@Override
	public DatabaseFacade factorDatabaseFacade() {
		factoredDatabaseFacade = new DatabaseFacadeSpy();
		factoredDatabaseFacade.throwErrorInDatabaseFacade = throwErrorInDatabaseFacade;
		return factoredDatabaseFacade;
	}

	@Override
	public TableFacade factorTableFacade() {
		factoredTableFacade = new TableFacadeSpy();
		if (rowsToReturn != null) {
			factoredTableFacade.rowsToReturn = rowsToReturn;
		}
		if (rowToReturn != null) {
			factoredTableFacade.rowToReturn = rowToReturn;
		}
		factoredTableFacade.numToReturn = numToReturn;
		return factoredTableFacade;
	}

	@Override
	public TableQuery factorTableQuery(String tableName) {
		this.tableName = tableName;
		tableNames.add(tableName);
		factoredTableQuery = new TableQuerySpy(tableName);
		if (tablesToThrowExceptionFor.contains(tableName)) {
			factoredTableQuery.throwException = true;
		}
		factoredTableQueries.add(factoredTableQuery);
		return factoredTableQuery;
	}

	public RowSpy createAndAddRowToReturn(String columnName, Object columnValue) {
		RowSpy rowToReturn = new RowSpy();
		rowToReturn.addColumnWithValue(columnName, columnValue);
		rowsToReturn.add(rowToReturn);
		return rowToReturn;
	}

}
