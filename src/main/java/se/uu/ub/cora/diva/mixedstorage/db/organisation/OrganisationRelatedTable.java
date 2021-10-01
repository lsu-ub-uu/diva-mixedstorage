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

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Set;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbHelper;
import se.uu.ub.cora.diva.mixedstorage.db.DbStatement;
import se.uu.ub.cora.sqldatabase.Row;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;

public abstract class OrganisationRelatedTable {

	protected int organisationId;
	protected SqlDatabaseFactory sqlDatabaseFactory;

	protected void setIdAsInt(DataGroup organisation) {
		String organisationIdAsString = DataToDbHelper.extractIdFromDataGroup(organisation);
		DataToDbHelper.throwDbExceptionIfIdNotAnIntegerValue(organisationIdAsString);
		organisationId = Integer.parseInt(organisationIdAsString);
	}

	protected void handleDeleteAndCreate(List<DbStatement> dbStatements,
			List<Row> allCurrentRowsInDb, Set<String> idsFromDataGroup) {
		Set<String> idsInDatabase = getIdsForCurrentRowsInDatabase(allCurrentRowsInDb);

		if (idsInDatabase.isEmpty()) {
			addToDb(dbStatements, idsFromDataGroup);
		} else {
			Set<String> originalIdsFromDataGroup = Set.copyOf(idsFromDataGroup);
			addDataFromDataGroupNotAlreadyInDb(dbStatements, idsFromDataGroup, idsInDatabase);
			removeRowsNoLongerPresentInDataGroup(dbStatements, idsInDatabase,
					originalIdsFromDataGroup);
		}
	}

	protected Timestamp getCurrentTimestamp() {
		Date today = new Date();
		long time = today.getTime();
		return new Timestamp(time);

	}

	protected abstract void removeRowsNoLongerPresentInDataGroup(List<DbStatement> dbStatements,
			Set<String> idsInDatabase, Set<String> originalIdsFromDataGroup);

	protected abstract void addDataFromDataGroupNotAlreadyInDb(List<DbStatement> dbStatements,
			Set<String> idsFromDataGroup, Set<String> idsInDatabase);

	protected abstract void addToDb(List<DbStatement> dbStatements, Set<String> idsFromDataGroup);

	protected abstract Set<String> getIdsForCurrentRowsInDatabase(List<Row> allCurrentRowsInDb);

}
