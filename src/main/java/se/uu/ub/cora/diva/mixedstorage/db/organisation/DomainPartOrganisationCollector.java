/*
 * Copyright 2021 Uppsala University Library
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.storage.RecordStorage;

public class DomainPartOrganisationCollector implements RelatedLinkCollector {

	private RecordStorage dbStorage;

	public DomainPartOrganisationCollector(RecordStorage dbStorage) {
		this.dbStorage = dbStorage;
	}

	@Override
	public Map<String, DataGroup> collectLinks(DataGroup personDomainPart) {
		List<DataGroup> affiliations = personDomainPart.getAllGroupsWithNameInData("affiliation");

		Map<String, DataGroup> collectedDataGroupsFromLinks = new HashMap<>();
		collectLinksFromAffiliations(collectedDataGroupsFromLinks, affiliations);
		return collectedDataGroupsFromLinks;
	}

	private void collectLinksFromAffiliations(Map<String, DataGroup> collectedLinks,
			List<DataGroup> affiliations) {
		for (DataGroup affiliation : affiliations) {
			collectLinkFromAffiliation(collectedLinks, affiliation);
		}
	}

	private void collectLinkFromAffiliation(Map<String, DataGroup> collectedLinks,
			DataGroup affiliation) {
		String organisationId = extractId(affiliation);
		DataGroup readOrganisation = dbStorage.read("organisation", organisationId);
		collectedLinks.put(organisationId, readOrganisation);
		possiblyAddParents(collectedLinks, readOrganisation);
	}

	private String extractId(DataGroup affiliation) {
		DataGroup organisationLink = affiliation.getFirstGroupWithNameInData("organisationLink");
		return organisationLink.getFirstAtomicValueWithNameInData("linkedRecordId");
	}

	private void possiblyAddParents(Map<String, DataGroup> collectedLinks,
			DataGroup readOrganisation) {
		if (readOrganisation.containsChildWithNameInData("parentOrganisation")) {
			List<DataGroup> parentOrganisations = readOrganisation
					.getAllGroupsWithNameInData("parentOrganisation");
			collectLinksFromAffiliations(collectedLinks, parentOrganisations);
		}
	}

	RecordStorage getDbStorage() {
		return dbStorage;
	}
}
