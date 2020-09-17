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
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.spy.MethodCallRecorder;

public class DataGroupRoleReferenceCreatorSpy implements DataGroupRoleReferenceCreator {
	public MethodCallRecorder MCR = new MethodCallRecorder();

	@Override
	public DataGroup createRoleReferenceForDomainAdminUsingDomain(List<String> domains) {
		MCR.addCall("domains", domains);

		DataGroupSpy dataGroupSpy = new DataGroupSpy("userDomainAdminRole");
		MCR.addReturned(dataGroupSpy);
		return dataGroupSpy;
	}

	@Override
	public DataGroup createRoleReferenceForSystemAdmin() {
		MCR.addCall();

		DataGroupSpy dataGroupSpy = new DataGroupSpy("userSystemAdminRole");
		MCR.addReturned(dataGroupSpy);
		return dataGroupSpy;
	}

	@Override
	public DataGroup createUserRoleChild(List<DataGroup> rolesList) {
		MCR.addCall("rolesList", rolesList);

		DataGroupSpy dataGroupSpy = new DataGroupSpy("userRole");
		MCR.addReturned(dataGroupSpy);
		return dataGroupSpy;
	}
}
