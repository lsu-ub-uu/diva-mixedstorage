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
package se.uu.ub.cora.diva.mixedstorage.classic;

import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;

/**
 * RepeatableRelatedLinkCollector collects all records that are linked from a list of DataGroups
 */
public interface RepeatableRelatedLinkCollector {

	/**
	 * collectLinks collects the DataGroup representation of all records that are linked from all
	 * DataGroups in a list. The collected DataGroups are returned in a collection grouped by the
	 * recordType of the links.
	 * 
	 * @param list
	 *            of {@link DataGroup} to collect the links from
	 * 
	 * @return a Map containing the collected DataGroups
	 */
	Map<String, List<DataGroup>> collectLinks(List<DataGroup> groupsContainingLinks);

}
