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
package se.uu.ub.cora.diva.mixedstorage.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.sqldatabase.Row;

public class RelatedTableSpy implements RelatedTable {

	public List<Row> dbRows;
	private List<DbStatement> dbStatements;
	public DataGroup dataGroup;

	@Override
	public List<DbStatement> handleDbForDataGroup(DataGroup dataGroup, List<Row> dbRows) {
		this.dataGroup = dataGroup;
		this.dbRows = dbRows;
		dbStatements = new ArrayList<>();
		DbStatement dbStatement = new DbStatement("operationFromSpy", "spyTableName",
				Collections.emptyMap(), Collections.emptyMap());
		dbStatements.add(dbStatement);
		return dbStatements;

	}

}
