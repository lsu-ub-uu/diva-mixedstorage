/*
 * Copyright 2019 Uppsala University Library
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
package se.uu.ub.cora.diva.mixedstorage;

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupFactory;

public class DataGroupFactorySpy implements DataGroupFactory {

	public DataGroupSpy factoredDataGroup;
	public List<DataGroupSpy> factoredDataGroups = new ArrayList<>();
	public List<String> usedNameInDatas = new ArrayList<>();
	public List<String> usedRecordTypes = new ArrayList<>();
	public List<String> usedRecordIds = new ArrayList<>();

	@Override
	public DataGroup factorUsingNameInData(String nameInData) {
		usedNameInDatas.add(nameInData);
		factoredDataGroup = new DataGroupSpy(nameInData);
		factoredDataGroups.add(factoredDataGroup);
		return factoredDataGroup;
	}

	@Override
	public DataGroup factorAsLinkWithNameInDataTypeAndId(String nameInData, String recordType,
			String recordId) {
		usedNameInDatas.add(nameInData);
		usedRecordTypes.add(recordType);
		usedRecordIds.add(recordId);
		factoredDataGroup = new DataGroupSpy(nameInData, recordType, recordId);
		factoredDataGroups.add(factoredDataGroup);
		return factoredDataGroup;
	}

}
