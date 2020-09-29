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
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
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
		DataGroupSpy innerUserRoleGroup = createdDataGroups.get(1);
		assertTrue(systemAdminRole.getChildren().contains(innerUserRoleGroup));
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
				.createRoleReferenceForDomainAdminUsingDomains(Collections.emptyList());

		assertTopGroupWithRoleLinkIsCreated("divaDomainAdminRole");

		List<DataGroupSpy> createdDataGroups = dataGroupFactory.factoredDataGroups;
		assertEquals(createdDataGroups.size(), 2);
		DataGroupSpy createdTopLevelgGroup = createdDataGroups.get(0);
		DataGroupSpy innerUserRoleGroup = createdDataGroups.get(1);
		assertTrue(domainAdminRole.getChildren().contains(innerUserRoleGroup));
		assertSame(createdTopLevelgGroup, domainAdminRole);
	}

	@Test
	public void testDomainAdminRoleWithDomains() {
		List<String> domains = new ArrayList<>();
		domains.add("uu");
		domains.add("liu");

		DataGroupRoleReferenceCreator refCreator = new DataGroupRoleReferenceCreatorImp();
		DataGroup domainAdminRole = refCreator
				.createRoleReferenceForDomainAdminUsingDomains(domains);

		List<DataGroupSpy> createdDataGroups = dataGroupFactory.factoredDataGroups;

		assertTopLevelGroupIsCreatedCorrectlyAndContainsCorrectChildren(domainAdminRole,
				createdDataGroups);

		assertInnerUserRoleIsCreatedCorrectly();
		assertTermRulePartIsCreatedCorrectlyAndContainsCorrectChildren(createdDataGroups);
		assertRuleGroupIsCreatedCorrectly();
	}

	private void assertTopLevelGroupIsCreatedCorrectlyAndContainsCorrectChildren(
			DataGroup domainAdminRole, List<DataGroupSpy> createdDataGroups) {
		assertEquals(dataGroupFactory.usedNameInDatas.get(0), "userRole");
		DataGroupSpy createdTopLevelGroup = createdDataGroups.get(0);
		assertSame(createdTopLevelGroup, domainAdminRole);
		assertTrue(domainAdminRole.getChildren().contains(createdDataGroups.get(1)));
		assertTrue(domainAdminRole.getChildren().contains(createdDataGroups.get(2)));
	}

	private void assertInnerUserRoleIsCreatedCorrectly() {
		assertEquals(dataGroupFactory.usedNameInDatas.get(1), "userRole");
		assertEquals(dataGroupFactory.usedRecordTypes.get(0), "permissionRole");
		assertEquals(dataGroupFactory.usedRecordIds.get(0), "divaDomainAdminRole");
	}

	private void assertTermRulePartIsCreatedCorrectlyAndContainsCorrectChildren(
			List<DataGroupSpy> createdDataGroups) {
		assertEquals(dataGroupFactory.usedNameInDatas.get(2), "permissionTermRulePart");

		assertEquals(dataGroupFactory.usedNameInDatas.get(3), "rule");
		DataGroupSpy termRulePartGroup = createdDataGroups.get(2);
		assertTrue(termRulePartGroup.getChildren().contains(createdDataGroups.get(3)));
		assertAtomicValueIsFactoredCorrectlyAndAddedToRulePart(termRulePartGroup, 0, "domain.uu");
		assertAtomicValueIsFactoredCorrectlyAndAddedToRulePart(termRulePartGroup, 1, "domain.liu");
	}

	private void assertAtomicValueIsFactoredCorrectlyAndAddedToRulePart(
			DataGroupSpy termRulePartGroup, int index, String domain) {
		assertEquals(dataAtomicFactory.usedNameInDatas.get(index), "value");
		assertEquals(dataAtomicFactory.usedValues.get(index), domain);
		assertEquals(dataAtomicFactory.usedRepeatIds.get(index), String.valueOf(index));

		DataAtomicSpy valueForDomainUU = dataAtomicFactory.factoredDataAtomics.get(index);
		assertTrue(termRulePartGroup.children.contains(valueForDomainUU));
	}

	private void assertRuleGroupIsCreatedCorrectly() {
		assertEquals(dataGroupFactory.usedRecordTypes.get(1), "collectPermissionTerm");
		assertEquals(dataGroupFactory.usedRecordIds.get(1), "domainPermissionTerm");
	}

}
