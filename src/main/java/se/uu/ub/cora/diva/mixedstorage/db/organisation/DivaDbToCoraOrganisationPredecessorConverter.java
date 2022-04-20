/*
 * Copyright 2019, 2020, 2021 Uppsala University Library
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

import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.data.DataRecordLink;
import se.uu.ub.cora.diva.mixedstorage.db.ConversionException;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverter;
import se.uu.ub.cora.sqldatabase.Row;

public class DivaDbToCoraOrganisationPredecessorConverter
		extends DivaDbToCoraOrganisationAncestryConverter implements DivaDbToCoraConverter {

	private static final String DESCRIPTION = "description";

	@Override
	public DataGroup fromRow(Row dbRow) {
		this.dbRow = dbRow;
		if (mandatoryValuesAreMissing()) {
			throw ConversionException.withMessageAndException(
					"Error converting organisation predecessor to Cora organisation predecessor: Map does not "
							+ "contain mandatory values for organisation id and predecessor id",
					null);
		}
		return createDataGroup();
	}

	private DataGroup createDataGroup() {
		DataGroup earlierOrganisation = DataGroupProvider
				.getDataGroupUsingNameInData("earlierOrganisation");
		addPredecessorLink(earlierOrganisation);
		possiblyAddDescription(earlierOrganisation);
		return earlierOrganisation;
	}

	private void addPredecessorLink(DataGroup predecessorGroup) {
		String coraOrganisationType = (String) dbRow.getValueByColumn("coraorganisationtype");
		DataRecordLink predecessorLink = createOrganisationLinkUsingLinkedRecordIdAndRecordType(
				String.valueOf(dbRow.getValueByColumn(PREDECESSOR_ID)), coraOrganisationType);
		predecessorGroup.addChild(predecessorLink);
	}

	private void possiblyAddDescription(DataGroup earlierOrganisation) {
		if (predecessorHasDescription()) {
			earlierOrganisation.addChild(DataAtomicProvider.getDataAtomicUsingNameInDataAndValue(
					"internalNote", (String) dbRow.getValueByColumn(DESCRIPTION)));
		}
	}

	private boolean predecessorHasDescription() {
		return dbRowHasValueForKey(DESCRIPTION);
	}
}
