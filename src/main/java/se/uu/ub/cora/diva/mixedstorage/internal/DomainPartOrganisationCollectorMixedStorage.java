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
package se.uu.ub.cora.diva.mixedstorage.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.RelatedLinkCollector;
import se.uu.ub.cora.storage.RecordStorage;

public class DomainPartOrganisationCollectorMixedStorage implements RelatedLinkCollector {

	private RecordStorage dbStorage;
	private RecordStorage classicDbStorage;

	public DomainPartOrganisationCollectorMixedStorage(RecordStorage dbStorage,
			RecordStorage classicDbStorage) {
		this.dbStorage = dbStorage;
		this.classicDbStorage = classicDbStorage;
	}

	@Override
	public Map<String, Map<String, DataGroup>> collectLinks(DataGroup personDomainPartLink) {
		DataGroup personDomainPart = readPersonDomainPart(personDomainPartLink);

		Map<String, Map<String, DataGroup>> links = new HashMap<>();
		String domainPartId = getDomainPartId(personDomainPartLink);
		Map<String, DataGroup> domainParts = new HashMap<>();
		domainParts.put(domainPartId, personDomainPart);
		links.put("personDomainPart", domainParts);

		Map<String, DataGroup> collectedOrganisationsFromLinks = collectOrganisations(
				personDomainPart);
		links.put("organisation", collectedOrganisationsFromLinks);
		return links;
	}

	private DataGroup readPersonDomainPart(DataGroup personDomainPartLink) {
		String linkedRecordType = personDomainPartLink
				.getFirstAtomicValueWithNameInData("linkedRecordType");
		String linkedRecordId = getDomainPartId(personDomainPartLink);
		return dbStorage.read(linkedRecordType, linkedRecordId);
	}

	private Map<String, DataGroup> collectOrganisations(DataGroup personDomainPart) {
		List<DataGroup> affiliations = personDomainPart.getAllGroupsWithNameInData("affiliation");
		Map<String, DataGroup> collectedOrganisationsFromLinks = new HashMap<>();
		collectLinksFromAffiliations(collectedOrganisationsFromLinks, affiliations);
		return collectedOrganisationsFromLinks;
	}

	private String getDomainPartId(DataGroup personDomainPartLink) {
		return personDomainPartLink.getFirstAtomicValueWithNameInData("linkedRecordId");
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
		DataGroup readOrganisation = classicDbStorage.read("organisation", organisationId);
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

	RecordStorage getClassicDbStorage() {
		return classicDbStorage;
	}
}
