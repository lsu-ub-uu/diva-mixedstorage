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
package se.uu.ub.cora.diva.mixedstorage.db.organisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.Action;
import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAttribute;
import se.uu.ub.cora.data.DataChild;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataRecordLink;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;

public class DataRecordLinkSpy implements DataGroup, DataRecordLink {
	public Map<String, String> atomicValues = new HashMap<>();
	public List<DataChild> children = new ArrayList<>();
	public Map<String, DataGroup> dataGroups = new HashMap<>();

	public String nameInData;
	public String recordType;
	public String recordId;

	public DataRecordLinkSpy(String nameInData) {
		this.nameInData = nameInData;
	}

	public DataRecordLinkSpy(String nameInData, String recordType, String recordId) {
		this.nameInData = nameInData;
		this.recordType = recordType;
		this.recordId = recordId;
		addChild(new DataAtomicSpy("linkedRecordType", recordType));
		addChild(new DataAtomicSpy("linkedRecordId", recordId));
	}

	@Override
	public void addAction(Action action) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getNameInData() {
		return nameInData;
	}

	@Override
	public String getRepeatId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFirstAtomicValueWithNameInData(String nameInData) {
		if (atomicValues.containsKey(nameInData)) {
			return atomicValues.get(nameInData);
		}
		return null;
	}

	@Override
	public DataGroup getFirstGroupWithNameInData(String childNameInData) {
		if (dataGroups.containsKey(childNameInData)) {
			return dataGroups.get(childNameInData);
		}
		return new DataGroupSpy(childNameInData);
	}

	@Override
	public void addChild(DataChild dataElement) {
		if (dataElement instanceof DataAtomicSpy) {
			DataAtomicSpy atomicSpyChild = (DataAtomicSpy) dataElement;
			atomicValues.put(atomicSpyChild.nameInData, atomicSpyChild.value);

		} else if (dataElement instanceof DataGroupSpy) {
			DataGroupSpy dataGroup = (DataGroupSpy) dataElement;
			String dataGroupNameInData = dataGroup.nameInData;
			dataGroups.put(dataGroupNameInData, dataGroup);

		}
		children.add(dataElement);
	}

	@Override
	public List<DataChild> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsChildWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setRepeatId(String repeatId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addAttributeByIdWithValue(String id, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public DataChild getFirstChildWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DataGroup> getAllGroupsWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataAttribute getAttribute(String attributeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DataAtomic> getAllDataAtomicsWithNameInData(String childNameInData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeFirstChildWithNameInData(String childNameInData) {
		// TODO Auto-generated method stub
		return false;

	}

	@Override
	public Collection<DataGroup> getAllGroupsWithNameInDataAndAttributes(String childNameInData,
			DataAttribute... childAttributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<DataAttribute> getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addChildren(Collection<DataChild> dataElements) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<DataChild> getAllChildrenWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeAllChildrenWithNameInData(String childNameInData) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DataAtomic getFirstDataAtomicWithNameInData(String childNameInData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DataChild> getAllChildrenWithNameInDataAndAttributes(String nameInData,
			DataAttribute... childAttributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeAllChildrenWithNameInDataAndAttributes(String childNameInData,
			DataAttribute... childAttributes) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasReadAction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getLinkedRecordId() {
		return atomicValues.get("linkedRecordId");
	}

	@Override
	public String getLinkedRecordType() {
		return atomicValues.get("linkedRecordType");
	}

	@Override
	public boolean hasAttributes() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<DataAtomic> getAllDataAtomicsWithNameInDataAndAttributes(
			String childNameInData, DataAttribute... childAttributes) {
		// TODO Auto-generated method stub
		return null;
	}

}
