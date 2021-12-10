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
package se.uu.ub.cora.diva.mixedstorage.fedora;

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.RepeatableRelatedLinkCollector;

public class RepeatableLinkCollectorSpy implements RepeatableRelatedLinkCollector {

	public List<DataGroup> groupsContainingLinks = new ArrayList<>();
	public int numberOfGroupsToReturn = 0;
	public List<DataGroup> listToReturn = new ArrayList<>();

	@Override
	public List<DataGroup> collectLinks(List<DataGroup> groupsContainingLinks) {
		this.groupsContainingLinks = groupsContainingLinks;

		for (int i = 0; i < numberOfGroupsToReturn; i++) {
			listToReturn.add(new DataGroupSpy("personDomainPart"));
		}

		return listToReturn;
	}

}