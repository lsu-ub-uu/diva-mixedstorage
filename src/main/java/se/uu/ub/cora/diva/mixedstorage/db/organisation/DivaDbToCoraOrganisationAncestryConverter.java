/*
 * Copyright 2019, 2021 Uppsala University Library
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

import se.uu.ub.cora.data.DataRecordLink;
import se.uu.ub.cora.data.DataRecordLinkProvider;
import se.uu.ub.cora.sqldatabase.DatabaseValues;
import se.uu.ub.cora.sqldatabase.Row;

public class DivaDbToCoraOrganisationAncestryConverter {
	protected static final String PREDECESSOR_ID = "organisation_predecessor_id";
	protected static final String ORGANISATION_ID = "organisation_id";
	protected Row dbRow;

	protected boolean mandatoryValuesAreMissing() {
		return organisationIdIsMissing() || predecessorIdIsMissing();
	}

	protected boolean organisationIdIsMissing() {
		return !dbRowHasValueForKey(ORGANISATION_ID);
	}

	protected boolean dbRowHasValueForKey(String key) {
		Object value = dbRow.getValueByColumn(key);
		return value != null && !(value.equals(DatabaseValues.NULL)) && !"".equals(value);
	}

	private boolean predecessorIdIsMissing() {
		return !dbRowHasValueForKey(PREDECESSOR_ID);
	}

	protected DataRecordLink createOrganisationLinkUsingLinkedRecordIdAndRecordType(
			String organisationId, String recordType) {
		return DataRecordLinkProvider.getDataRecordLinkAsLinkUsingNameInDataTypeAndId(
				"organisationLink", recordType, organisationId);
	}
}