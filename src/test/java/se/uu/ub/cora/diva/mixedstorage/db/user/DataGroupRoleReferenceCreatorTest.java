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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;

public class DataGroupRoleReferenceCreatorTest {

	private DataGroupFactorySpy dataGroupFactory;
	private DataAtomicFactorySpy dataAtomicFactory;

	@BeforeMethod
	public void setUp() {
		dataGroupFactory = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactory);
		dataAtomicFactory = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactory);

	}

	@Test
	public void testSystemAdminRole() {
		DataGroupRoleReferenceCreator refCreator = new DataGroupRoleReferenceCreatorImp();
		DataGroup systemAdminRole = refCreator.createRoleReferenceForSystemAdmin();

		assertTopGroupWithRoleLinkIsCreated("divaSystemAdminRole");

		List<DataGroupSpy> createdDataGroups = dataGroupFactory.factoredDataGroups;
		DataGroupSpy createdTopLevelgGroup = createdDataGroups.get(0);
		DataGroupSpy firstCreatedChild = createdDataGroups.get(1);

		assertTrue(systemAdminRole.getChildren().contains(firstCreatedChild));
		assertSame(createdTopLevelgGroup, systemAdminRole);
		assertSame(dataGroupFactory.factoredDataGroups.get(0), systemAdminRole);
	}

	private void assertTopGroupWithRoleLinkIsCreated(String roleName) {
		assertEquals(dataGroupFactory.usedNameInDatas.get(0), "userRole");
		assertEquals(dataGroupFactory.usedNameInDatas.get(1), "userRole");
		assertEquals(dataGroupFactory.usedRecordTypes.get(0), "permissionRole");
		assertEquals(dataGroupFactory.usedRecordIds.get(0), roleName);
	}

	@Test
	public void testDomainAdminRoleNoDomains() {
		DataGroupRoleReferenceCreator refCreator = new DataGroupRoleReferenceCreatorImp();
		DataGroup domainAdminRole = refCreator
				.createRoleReferenceForDomainAdminUsingDomain(Collections.emptyList());

		assertTopGroupWithRoleLinkIsCreated("divaDomainAdminRole");

		List<DataGroupSpy> createdDataGroups = dataGroupFactory.factoredDataGroups;
		assertEquals(createdDataGroups.size(), 2);
		DataGroupSpy createdTopLevelgGroup = createdDataGroups.get(0);
		DataGroupSpy firstCreatedChild = createdDataGroups.get(1);

		assertTrue(domainAdminRole.getChildren().contains(firstCreatedChild));
		assertSame(createdTopLevelgGroup, domainAdminRole);
	}

	// {
	// "name": "permissionTermRulePart",
	// "children": [
	// {
	// "name": "rule",
	// "children": [
	// {
	// "name": "linkedRecordType",
	// "value": "collectPermissionTerm"
	// },
	// {
	// "name": "linkedRecordId",
	// "value": "domainPermissionTerm"
	// }
	// ]
	// },
	// {
	// "name": "value",
	// "value": "domain.uu",
	// "repeatId": "0"
	// },
	// {
	// "name": "value",
	// "value": "domain.liu",
	// "repeatId": "1"
	// }
	// ],
	// "repeatId": "0"
	// }

	@Test
	public void testDomainAdminRoleWithDomains() {
		List<String> domains = new ArrayList<>();
		domains.add("uu");
		domains.add("liu");

		DataGroupRoleReferenceCreator refCreator = new DataGroupRoleReferenceCreatorImp();
		DataGroup domainAdminRole = refCreator
				.createRoleReferenceForDomainAdminUsingDomain(domains);

		assertTopGroupWithRoleLinkIsCreated("divaDomainAdminRole");

		assertEquals(dataGroupFactory.usedNameInDatas.get(2), "permissionTermRulePart");
		assertEquals(dataGroupFactory.usedNameInDatas.get(3), "rule");

		List<DataGroupSpy> createdDataGroups = dataGroupFactory.factoredDataGroups;
		DataGroupSpy createdTopLevelgGroup = createdDataGroups.get(0);
		DataGroupSpy innerUserRoleGroup = createdDataGroups.get(1);
		DataGroupSpy termRulePartGroup = createdDataGroups.get(2);
		DataGroupSpy ruleGroup = createdDataGroups.get(3);

		assertTrue(domainAdminRole.getChildren().contains(innerUserRoleGroup));
		assertTrue(domainAdminRole.getChildren().contains(termRulePartGroup));
		assertTrue(termRulePartGroup.getChildren().contains(ruleGroup));

		assertEquals(dataGroupFactory.usedRecordTypes.get(1), "collectPermissionTerm");
		assertEquals(dataGroupFactory.usedRecordIds.get(1), "domainPermissionTerm");

		assertEquals(dataAtomicFactory.usedNameInDatas.get(0), "value");

		assertSame(createdTopLevelgGroup, domainAdminRole);
	}

}
