/*
 * Copyright 2019 Uppsala University Library
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

public enum OrganisationAtomicColumns {
	CLOSED_DATE("closed_date", "closedDate", "date"), ORGANISATION_CODE("organisation_code",
			"organisationCode", "string"), ORGANISATION_NUMBER("orgnumber", "organisationNumber",
					"string"), URL("organisation_homepage", "URL", "string");

	public final String dbName;
	public final String coraName;
	public final String type;

	OrganisationAtomicColumns(String dbName, String coraName, String type) {
		this.dbName = dbName;
		this.coraName = coraName;
		this.type = type;
	}

}
