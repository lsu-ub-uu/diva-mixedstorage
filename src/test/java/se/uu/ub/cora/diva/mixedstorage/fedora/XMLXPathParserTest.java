/*
 * Copyright 2018 Uppsala University Library
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

import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.ParseException;

public class XMLXPathParserTest {

	@Test(expectedExceptions = ParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to use xpathString: javax.xml.transform.TransformerException: Extra illegal tokens: 'not'")
	public void testMalformedXPathForNodeList() throws Exception {
		XMLXPathParser parser = XMLXPathParser.forXML("<pid></pid>");
		parser.getNodeListFromDocumentUsingXPath("/broken/xpath/string not");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "Error setting string value on node")
	public void testErrorWhenSettingString() throws Exception {
		XMLXPathParser parser = XMLXPathParser.forXML("<pid></pid>");
		parser.setStringInDocumentUsingXPath("/broken/xpath/string not", "dummy");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting node to String")
	public void testBrokenGetDocumentAsString() throws Exception {
		XMLXPathParser parser = XMLXPathParser.forXML("<pid></pid>");
		parser.getDocumentAsString("/broken/xpath/string not");
	}
}
