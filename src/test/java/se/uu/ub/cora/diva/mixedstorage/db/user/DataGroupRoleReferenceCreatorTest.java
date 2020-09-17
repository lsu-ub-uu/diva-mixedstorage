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

import java.util.Collections;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupFactory;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.mixedstorage.DataGroupFactorySpy;

public class DataGroupRoleReferenceCreatorTest {

	@BeforeMethod
	public void setUp() {
		DataGroupFactory dataGroupFactory = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactory);
	}

	@Test
	public void testSystemAdminRole() {
		DataGroupRoleReferenceCreator refCreator = new DataGroupRoleReferenceCreatorImp();
		refCreator.createRoleReferenceForDomainAdminUsingDomain(Collections.emptyList());
		DataGroup systemAdminRole = refCreator.createRoleReferenceForSystemAdmin();
		assertEquals(systemAdminRole.getNameInData(), "userRole");
		DataGroup innerUserRole = systemAdminRole.getFirstGroupWithNameInData("userRole");

		// assertEquals(innerUserRole);
	}

}
