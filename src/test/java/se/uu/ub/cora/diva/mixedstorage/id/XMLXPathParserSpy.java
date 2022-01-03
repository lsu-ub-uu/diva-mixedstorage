/*
 * Copyright 2022 Uppsala University Library
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
package se.uu.ub.cora.diva.mixedstorage.id;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import se.uu.ub.cora.fedora.parser.XMLXPathParser;
import se.uu.ub.cora.fedora.parser.XMLXPathParserException;

public class XMLXPathParserSpy implements XMLXPathParser {

	public String xpathString;
	public String returnedPidFromPathParser = "pid:23456";
	public String responseXmlToHandle;
	public boolean throwException = false;

	@Override
	public void setupToHandleResponseXML(String xml) throws XMLXPathParserException {
		this.responseXmlToHandle = xml;

	}

	@Override
	public boolean hasNode(String xPath) throws XMLXPathParserException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getStringFromDocumentUsingXPath(String xpathString)
			throws XMLXPathParserException {
		if (throwException) {
			throw new XMLXPathParserException("some error from spy");
		}
		this.xpathString = xpathString;
		return returnedPidFromPathParser;
	}

	@Override
	public String getStringFromNodeUsingXPath(Node node, String xpathString)
			throws XMLXPathParserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeList getNodeListFromDocumentUsingXPath(String xpathString)
			throws XMLXPathParserException {
		// TODO Auto-generated method stub
		return null;
	}

}
