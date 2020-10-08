/*
 * Copyright 2018, 2019 Uppsala University Library
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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.mixedstorage.ParseException;

public class DivaXmlParserFedoraToCoraPersonConverter implements DivaXmlParserFedoraToCoraConverter {

	private XMLXPathParser parser;

	@Override
	public DataGroup fromXML(String xml) {
		try {
			parser = XMLXPathParser.forXML(xml);
			return tryToCreateDataGroupFromDocument();
		} catch (Exception e) {
			throw ParseException.withMessageAndException(
					"Error converting person to Cora person: " + e.getMessage(), e);
		}
	}

	private DataGroup tryToCreateDataGroupFromDocument() {
		DataGroup person = DataGroupProvider.getDataGroupUsingNameInData("authorityPerson");
		createRecordInfoAndAddToPerson(person);

		createDefaultNameAndAddToPerson(person);
		createAlternativeNamesAndAddToPerson(person);
		createAndAddPublicRecord(person);
		return person;
	}

	private void createRecordInfoAndAddToPerson(DataGroup place) {
		DataGroup recordInfo = DivaFedoraToCoraRecordInfoConverter.createRecordInfo(parser);
		place.addChild(recordInfo);
	}

	private String getStringFromDocumentUsingXPath(String xpathString) {
		return parser.getStringFromDocumentUsingXPath(xpathString);
	}

	private void createDefaultNameAndAddToPerson(DataGroup person) {
		DataGroup defaultName = DataGroupProvider.getDataGroupUsingNameInData("authorizedName");
		createName(defaultName);
		if (dataGroupHasChildren(defaultName)) {
			person.addChild(defaultName);
		}
	}

	private boolean dataGroupHasChildren(DataGroup dataGroup) {
		return !dataGroup.getChildren().isEmpty();
	}

	private void createName(DataGroup nameGroup) {
		String lastName = getDefaultNamePartFromXML("lastname");
		possiblyAddChildToGroupUsingNameInDataAndValue(nameGroup, "familyName", lastName);
		String firstName = getDefaultNamePartFromXML("firstname");
		possiblyAddChildToGroupUsingNameInDataAndValue(nameGroup, "givenName", firstName);
	}

	private void possiblyAddChildToGroupUsingNameInDataAndValue(DataGroup nameGroup,
			String childNameInData, String value) {
		if (valueContainsData(value)) {
			nameGroup.addChild(DataAtomicProvider
					.getDataAtomicUsingNameInDataAndValue(childNameInData, value));
		}
	}

	private String getDefaultNamePartFromXML(String xmlTagName) {
		return getStringFromDocumentUsingXPath(
				"/authorityPerson/defaultName/" + xmlTagName + "/text()");
	}

	private boolean valueContainsData(String value) {
		return !"".equals(value);
	}

	private void createAlternativeNamesAndAddToPerson(DataGroup person) {
		NodeList list = parser
				.getNodeListFromDocumentUsingXPath("/authorityPerson/alternativeNames/nameForm");
		createAndAddAllAlternativeNamesToPersonUsingNodeListAndPerson(list, person);
	}

	private void createAndAddAllAlternativeNamesToPersonUsingNodeListAndPerson(NodeList list,
			DataGroup person) {
		for (int i = 0; i < list.getLength(); i++) {
			Node nameForm = list.item(i);
			addAlternativeNameToPersonUsingNodeAndPersonAndRepeatId(nameForm, person,
					String.valueOf(i));
		}
	}

	private void addAlternativeNameToPersonUsingNodeAndPersonAndRepeatId(Node nameForm,
			DataGroup person, String repeatId) {
		DataGroup alternativeName = DataGroupProvider
				.getDataGroupUsingNameInData("alternativeName");
		addChildrenToAlternativeName(nameForm, alternativeName);

		if (dataGroupHasChildren(alternativeName)) {
			alternativeName.setRepeatId(repeatId);
			person.addChild(alternativeName);
		}

	}

	private void addChildrenToAlternativeName(Node nameForm, DataGroup alternativeName) {
		String lastName = getAlternativeNamePartFromXMLUsingNodeAndXPathPart(nameForm, "lastname");
		possiblyAddChildToGroupUsingNameInDataAndValue(alternativeName, "familyName", lastName);

		String firstName = getAlternativeNamePartFromXMLUsingNodeAndXPathPart(nameForm,
				"firstname");
		possiblyAddChildToGroupUsingNameInDataAndValue(alternativeName, "givenName", firstName);
	}

	private String getAlternativeNamePartFromXMLUsingNodeAndXPathPart(Node nameForm,
			String xmlTagName) {
		return parser.getStringFromDocumentUsingNodeAndXPath(nameForm,
				"./" + xmlTagName + "/text()");
	}

	private void createAndAddPublicRecord(DataGroup person) {
		String publicRecord = getStringFromDocumentUsingXPath("/authorityPerson/publicRecord");
		String publicValue = "true".equals(publicRecord) ? "yes" : "no";
		person.addChild(
				DataAtomicProvider.getDataAtomicUsingNameInDataAndValue("public", publicValue));
	}
}
