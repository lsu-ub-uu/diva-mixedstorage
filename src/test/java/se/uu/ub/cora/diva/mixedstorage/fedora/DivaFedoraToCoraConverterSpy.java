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
package se.uu.ub.cora.diva.mixedstorage.fedora;

import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;

public class DivaFedoraToCoraConverterSpy implements DivaFedoraToCoraConverter {

	public String xml;
	public DataGroup convertedDataGroup;
	public boolean fromXMLWithParametersWasCalled = false;
	public Map<String, Object> parameters;

	@Override
	public DataGroup fromXML(String xml) {
		this.xml = xml;
		convertedDataGroup = new DataGroupSpy("Converted xml");
		return convertedDataGroup;
	}

	@Override
	public DataGroup fromXMLWithParameters(String xml, Map<String, Object> parameters) {
		this.xml = xml;
		this.parameters = parameters;
		convertedDataGroup = new DataGroupSpy("Converted xml");
		fromXMLWithParametersWasCalled = true;
		return convertedDataGroup;
	}

}
