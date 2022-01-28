/*
 * Copyright 2021, 2022 Uppsala University Library
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

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.converter.ExternallyConvertibleToStringConverter;
import se.uu.ub.cora.data.ExternallyConvertible;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;

public class ConverterSpy implements ExternallyConvertibleToStringConverter {

	public ExternallyConvertible dataElement;
	public List<ExternallyConvertible> dataElements = new ArrayList<>();
	public String dataString;

	public String commonStringToReturn = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>some returned string from converter spy";
	public List<String> returnedStrings = new ArrayList<>();
	public DataGroupSpy dataGroupToReturn;
	private int counter = 0;

	@Override
	public String convert(ExternallyConvertible externallyConvertible) {
		dataElements.add(externallyConvertible);
		this.dataElement = externallyConvertible;
		String stringToReturn = commonStringToReturn + counter;
		counter++;
		returnedStrings.add(stringToReturn);

		return stringToReturn;
	}

	@Override
	public String convertWithLinks(ExternallyConvertible externallyConvertible, String baseUrl) {
		// TODO Auto-generated method stub
		return null;
	}

}
