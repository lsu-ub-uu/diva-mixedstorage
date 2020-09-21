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

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;

public class DataGroupRoleReferenceCreatorImp implements DataGroupRoleReferenceCreator {

	@Override
	public DataGroup createRoleReferenceForDomainAdminUsingDomains(List<String> domains) {
		DataGroup role = DataGroupProvider.getDataGroupUsingNameInData("userRole");
		DataGroup child = DataGroupProvider.getDataGroupAsLinkUsingNameInDataTypeAndId("userRole",
				"permissionRole", "divaDomainAdminRole");
		role.addChild(child);
		if (domainsExist(domains)) {
			addPermissionRulePart(domains, role);
		}
		return role;
	}

	private boolean domainsExist(List<String> domains) {
		return !domains.isEmpty();
	}

	private void addPermissionRulePart(List<String> domains, DataGroup role) {
		DataGroup rulePart = DataGroupProvider
				.getDataGroupUsingNameInData("permissionTermRulePart");

		DataGroup ruleGroup = DataGroupProvider.getDataGroupAsLinkUsingNameInDataTypeAndId("rule",
				"collectPermissionTerm", "domainPermissionTerm");
		rulePart.addChild(ruleGroup);

		addValuePartsForDomains(domains, rulePart);

		role.addChild(rulePart);
	}

	private void addValuePartsForDomains(List<String> domains, DataGroup rulePart) {
		int repeatId = 0;
		for (String domain : domains) {
			createAndAddValuePartForDomain(rulePart, domain, repeatId);
			repeatId++;
		}
	}

	private void createAndAddValuePartForDomain(DataGroup rulePart, String domain, int repeatId) {
		DataAtomic domainValue = createValuePartForDomain(repeatId, domain);
		rulePart.addChild(domainValue);
	}

	private DataAtomic createValuePartForDomain(int repeatId, String domain) {
		return DataAtomicProvider.getDataAtomicUsingNameInDataAndValueAndRepeatId("value",
				"domain." + domain, String.valueOf(repeatId));
	}

	@Override
	public DataGroup createRoleReferenceForSystemAdmin() {
		DataGroup role = DataGroupProvider.getDataGroupUsingNameInData("userRole");
		DataGroup child = DataGroupProvider.getDataGroupAsLinkUsingNameInDataTypeAndId("userRole",
				"permissionRole", "divaSystemAdminRole");
		role.addChild(child);
		return role;
	}
}
