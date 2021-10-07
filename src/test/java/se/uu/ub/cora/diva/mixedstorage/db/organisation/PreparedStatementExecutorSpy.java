/*
 * Copyright 2020, 2021 Uppsala University Library
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.diva.mixedstorage.db.DbStatement;
import se.uu.ub.cora.diva.mixedstorage.db.PreparedStatementSpy;
import se.uu.ub.cora.diva.mixedstorage.db.StatementExecutor;
import se.uu.ub.cora.sqldatabase.DatabaseFacade;
import se.uu.ub.cora.sqldatabase.SqlDatabaseException;

public class PreparedStatementExecutorSpy implements StatementExecutor {

	public List<DbStatement> dbStatements;
	public boolean createWasCalled = false;
	public List<PreparedStatement> preparedStatements;
	public boolean throwErrorFromPreparedStatement = false;
	public Connection connection;
	// public List<Connection> connections = new ArrayList<>();
	public boolean throwExceptionOnGenerateStatement = false;
	public DatabaseFacade databaseFacade;

	@Override
	public void executeDbStatmentUsingDatabaseFacade(List<DbStatement> dbStatements,
			DatabaseFacade databaseFacade) {
		this.dbStatements = dbStatements;
		this.databaseFacade = databaseFacade;
		if (throwExceptionOnGenerateStatement) {
			throw SqlDatabaseException.withMessageAndException(
					"Error executing statement: error from spy", new Exception());
		}
		// else {
		// connections.add(connection);
		createWasCalled = true;
		preparedStatements = new ArrayList<>();
		for (int i = 0; i < dbStatements.size(); i++) {
			PreparedStatementSpy preparedStatementSpy = new PreparedStatementSpy();
			preparedStatements.add(preparedStatementSpy);
		}
		// }
	}
}
