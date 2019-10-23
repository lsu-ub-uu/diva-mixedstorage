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
package se.uu.ub.cora.diva.mixedstorage.db;

import java.util.Map;

import se.uu.ub.cora.data.DataGroup;

public class DivaDbToCoraOrganisationParentConverter
		extends DivaDbToCoraOrganisationAncestryConverter implements DivaDbToCoraConverter {

	@Override
	public DataGroup fromMap(Map<String, Object> dbRow) {
		this.dbRow = dbRow;
		if (mandatoryValuesAreMissing()) {
			throw ConversionException.withMessageAndException(
					"Error converting organisation parent to Cora organisation parent: Map does not "
							+ "contain mandatory values for organisation id and parent id",
					null);
		}
		return createDataGroup();
	}

	@Override
	protected boolean mandatoryValuesAreMissing() {
		return organisationIdIsMissing() || parentIdIsMissing();
	}

	protected boolean parentIdIsMissing() {
		return !dbRowHasValueForKey("organisation_parent_id");
	}

	private DataGroup createDataGroup() {
		DataGroup parent = DataGroup.withNameInData("parentOrganisation");
		addParentLink(parent);
		return parent;
	}

	private void addParentLink(DataGroup formerName) {
		String predecessorId = (String) dbRow.get("organisation_parent_id");
		DataGroup predecessor = createOrganisationLinkUsingLinkedRecordId(predecessorId);
		formerName.addChild(predecessor);
	}
}
