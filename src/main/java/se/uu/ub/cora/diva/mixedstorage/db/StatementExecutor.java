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

import java.util.List;

import se.uu.ub.cora.sqldatabase.DatabaseFacade;

/**
 * StatementsExecutor executes dbStatements
 */
public interface StatementExecutor {

	/**
	 * The user of this interface can choose an implementation of StatementExecutor, to execute
	 * DbStatements. Different implementations may choose how they turn the dbStatemetns into
	 * executeable sql statements, for example this might be as preparedStatements.
	 * 
	 * Implementations are required to use the provided connection to execute the statements.
	 * 
	 * @param dbStatements
	 *            statements to execute
	 * @param databaseFacade
	 *            to use to execute statements
	 */
	void executeDbStatmentUsingDatabaseFacade(List<DbStatement> dbStatements,
			DatabaseFacade databaseFacade);

}
