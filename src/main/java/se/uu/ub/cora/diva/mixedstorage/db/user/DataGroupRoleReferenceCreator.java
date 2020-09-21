/*
 * Copyright 2020 Uppsala University Library
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

package se.uu.ub.cora.diva.mixedstorage.db.user;

import java.util.List;

import se.uu.ub.cora.data.DataGroup;

/**
 * DataGroupRoleReferenceCreator creates the dataGroupRoleReference part of a user, when that user
 * is read from DiVA classics database.
 */
public interface DataGroupRoleReferenceCreator {

	/**
	 * createRoleReferenceForDomainAdminUsingDomains creates the dataGroupRoleReference part for a
	 * user that have domainGroups in DiVA classic, these users are known as a DomainAdmin.<br>
	 * The created dataGroupRoleReference part has a link to a permissionRole, that is predefined in
	 * DiVA powered by Cora, "divaDomainAdminRole", and one or more permissionTermRuleParts that
	 * uses the collectPermissionTerm, "domainPermissionTerm" with one value for each domain as:
	 * "domain.X" where X are the domains from the domains list. <br>
	 * If the list of domains is empty will dataGroupRoleReference part be returned without
	 * permissionTermRuleParts.
	 * 
	 * @param domains
	 *            A List of domains to add to the dataGroupRoleReference part
	 * @return A DataGroup with a created dataGroupRoleReference part as described.
	 */
	DataGroup createRoleReferenceForDomainAdminUsingDomains(List<String> domains);

	/**
	 * createRoleReferenceForSystemAdmin creates the dataGroupRoleReference part for a user that
	 * have systemAdmin in DiVA classic, these users are known as a SystemAdmin.<br>
	 * The created dataGroupRoleReference part has a link to a permissionRole, that is predefined in
	 * DiVA powered by Cora, "divaSystemAdminRole".
	 * 
	 * @return A DataGroup with a created dataGroupRoleReference part as described.
	 */
	DataGroup createRoleReferenceForSystemAdmin();

}
