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

import java.util.Map;

import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;

public class DivaDbToCoraOrganisationAncestryConverter {
	protected static final String PREDECESSOR_ID = "organisation_predecessor_id";
	protected static final String ORGANISATION_ID = "organisation_id";
	protected Map<String, Object> dbRow;

	protected boolean mandatoryValuesAreMissing() {
		return organisationIdIsMissing() || predecessorIdIsMissing();
	}

	protected boolean organisationIdIsMissing() {
		return !dbRowHasValueForKey(ORGANISATION_ID);
	}

	protected boolean dbRowHasValueForKey(String key) {
		Object value = dbRow.get(key);
		return value != null && !"".equals(value);
	}

	private boolean predecessorIdIsMissing() {
		return !dbRowHasValueForKey(PREDECESSOR_ID);
	}

	protected DataGroup createOrganisationLinkUsingLinkedRecordId(String organisationId) {
		DataGroup predecessor = DataGroupProvider.getDataGroupUsingNameInData("organisationLink");
		predecessor.addChild(DataAtomicProvider
				.getDataAtomicUsingNameInDataAndValue("linkedRecordType", "divaOrganisation"));
		predecessor.addChild(DataAtomicProvider
				.getDataAtomicUsingNameInDataAndValue("linkedRecordId", organisationId));
		return predecessor;
	}
}