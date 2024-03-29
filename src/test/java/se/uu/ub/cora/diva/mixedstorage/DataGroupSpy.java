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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAttribute;
import se.uu.ub.cora.data.DataChild;
import se.uu.ub.cora.data.DataGroup;

public class DataGroupSpy implements DataGroup {

	public String nameInData;
	public List<DataChild> children = new ArrayList<>();
	public String repeatId;
	public Set<DataAttribute> addedAttributes = new HashSet<>();
	public String recordType;
	public String recordId;
	public boolean setRepeatIdWasCalled = false;

	public DataGroupSpy(String nameInData) {
		this.nameInData = nameInData;
	}

	public DataGroupSpy(String nameInData, String recordType, String recordId) {
		this.nameInData = nameInData;
		this.recordType = recordType;
		this.recordId = recordId;
	}

	@Override
	public String getRepeatId() {
		return repeatId;
	}

	@Override
	public String getNameInData() {
		return nameInData;
	}

	@Override
	public String getFirstAtomicValueWithNameInData(String nameInData) {
		for (DataChild dataElement : children) {
			if (nameInData.equals(dataElement.getNameInData())) {
				if (dataElement instanceof DataAtomic) {
					return ((DataAtomic) dataElement).getValue();
				}
			}
		}
		throw new RuntimeException("Atomic value not found for childNameInData:" + nameInData);
	}

	@Override
	public DataGroup getFirstGroupWithNameInData(String childNameInData) {
		for (DataChild dataElement : children) {
			if (childNameInData.equals(dataElement.getNameInData())) {
				if (dataElement instanceof DataGroup) {
					return ((DataGroup) dataElement);
				}
			}
		}
		return null;
	}

	@Override
	public void addChild(DataChild dataElement) {
		children.add(dataElement);

	}

	@Override
	public List<DataChild> getChildren() {
		return children;
	}

	@Override
	public boolean containsChildWithNameInData(String nameInData) {
		for (DataChild dataElement : children) {
			if (nameInData.equals(dataElement.getNameInData())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void setRepeatId(String repeatId) {
		setRepeatIdWasCalled = true;
		this.repeatId = repeatId;

	}

	@Override
	public void addAttributeByIdWithValue(String id, String value) {
		addedAttributes.add(new DataAttributeSpy(id, value));

	}

	@Override
	public DataChild getFirstChildWithNameInData(String nameInData) {
		for (DataChild dataElement : children) {
			if (nameInData.equals(dataElement.getNameInData())) {
				return dataElement;
			}
		}
		return null;
	}

	@Override
	public List<DataGroup> getAllGroupsWithNameInData(String nameInData) {
		List<DataGroup> matchingDataGroups = new ArrayList<>();
		for (DataChild dataElement : children) {
			if (nameInData.equals(dataElement.getNameInData())
					&& dataElement instanceof DataGroup) {
				matchingDataGroups.add((DataGroup) dataElement);
			}
		}
		return matchingDataGroups;
	}

	@Override
	public DataAttribute getAttribute(String attributeId) {
		for (DataAttribute dataAttribute : addedAttributes) {
			if (dataAttribute.getNameInData().equals(attributeId)) {
				return dataAttribute;
			}
		}
		return null;
	}

	@Override
	public Collection<DataAttribute> getAttributes() {
		return addedAttributes;
	}

	@Override
	public List<DataAtomic> getAllDataAtomicsWithNameInData(String childNameInData) {
		List<DataAtomic> matchingDataAtomics = new ArrayList<>();
		for (DataChild dataElement : children) {
			if (childNameInData.equals(dataElement.getNameInData())
					&& dataElement instanceof DataAtomic) {
				matchingDataAtomics.add((DataAtomic) dataElement);
			}
		}
		return matchingDataAtomics;
	}

	@Override
	public boolean removeFirstChildWithNameInData(String childNameInData) {
		for (DataChild dataElement : getChildren()) {
			if (dataElementsNameInDataIs(dataElement, childNameInData)) {
				getChildren().remove(dataElement);
				return true;
			}
		}
		return false;
	}

	private boolean dataElementsNameInDataIs(DataChild dataElement, String childNameInData) {
		return dataElement.getNameInData().equals(childNameInData);
	}

	@Override
	public Collection<DataGroup> getAllGroupsWithNameInDataAndAttributes(String childNameInData,
			DataAttribute... childAttributes) {

		List<DataGroup> foundDataGroups = new ArrayList<>();
		List<DataGroup> allGroupsWithNameInData = getAllGroupsWithNameInData(childNameInData);
		for (DataGroup childDataGroup : allGroupsWithNameInData) {
			boolean addGroup = false;
			for (DataAttribute requestedAttribute : childAttributes) {
				String childAttribute = childDataGroup
						.getAttribute(requestedAttribute.getNameInData()).getValue();
				if (childAttribute != null
						&& childAttribute.equals(requestedAttribute.getValue())) {
					addGroup = true;
				}
			}
			if (addGroup) {
				foundDataGroups.add(childDataGroup);
			}

		}
		return foundDataGroups;
		// return null;
	}

	@Override
	public boolean hasChildren() {
		return children.size() > 0;
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
		for (DataChild dataElement : children) {
			if (childNameInData.equals(dataElement.getNameInData())) {
				if (dataElement instanceof DataAtomic) {
					return (DataAtomic) dataElement;
				}
			}
		}
		throw new RuntimeException("Atomic value not found for childNameInData:" + childNameInData);
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
