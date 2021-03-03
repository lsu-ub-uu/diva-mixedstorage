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
package se.uu.ub.cora.diva.mixedstorage.fedora;

import se.uu.ub.cora.converter.Converter;
import se.uu.ub.cora.data.DataElement;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;

public class ConverterSpy implements Converter {

	public DataElement dataElement;
	public String dataString;
	public String stringToReturn = "some returned string from converter spy";
	public DataGroupSpy dataGroupToReturn;

	@Override
	public String convert(DataElement dataElement) {
		this.dataElement = dataElement;
		return stringToReturn;
	}

	@Override
	public DataElement convert(String dataString) {
		this.dataString = dataString;
		dataGroupToReturn = new DataGroupSpy("someNameInData");
		return dataGroupToReturn;
	}

}
