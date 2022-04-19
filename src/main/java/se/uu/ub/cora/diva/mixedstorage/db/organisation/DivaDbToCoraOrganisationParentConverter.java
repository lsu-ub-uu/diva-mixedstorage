/*
 * Copyright 2019, 2020 Uppsala University Library
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

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.data.DataRecordLink;
import se.uu.ub.cora.diva.mixedstorage.db.ConversionException;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverter;
import se.uu.ub.cora.sqldatabase.Row;

public class DivaDbToCoraOrganisationParentConverter
		extends DivaDbToCoraOrganisationAncestryConverter implements DivaDbToCoraConverter {

	@Override
	public DataGroup fromRow(Row dbRow) {
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
		DataGroup parent = DataGroupProvider.getDataGroupUsingNameInData("parentOrganisation");
		addParentLink(parent);
		return parent;
	}

	private void addParentLink(DataGroup parentGroup) {
		String parentId = String.valueOf(dbRow.getValueByColumn("organisation_parent_id"));
		String coraOrganisationType = (String) dbRow.getValueByColumn("coraorganisationtype");
		DataRecordLink organisationLink = createOrganisationLinkUsingLinkedRecordIdAndRecordType(
				parentId, coraOrganisationType);
		parentGroup.addChild(organisationLink);
	}
}
