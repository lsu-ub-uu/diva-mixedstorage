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

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;

public class OldDivaCoraToFedoraPersonConverter implements DivaCoraToFedoraConverter {
	private HttpHandlerFactory httpHandlerFactory;
	private String fedoraURL;
	private XMLXPathParser parser;

	public static OldDivaCoraToFedoraPersonConverter usingHttpHandlerFactoryAndFedoraUrl(
			HttpHandlerFactory httpHandlerFactory, String fedoraURL) {
		return new OldDivaCoraToFedoraPersonConverter(httpHandlerFactory, fedoraURL);
	}

	private OldDivaCoraToFedoraPersonConverter(HttpHandlerFactory httpHandlerFactory,
			String fedoraURL) {
		this.httpHandlerFactory = httpHandlerFactory;
		this.fedoraURL = fedoraURL;
	}

	@Override
	public String toXML(DataGroup dataRecord) {
		String recordId = getIdFromRecord(dataRecord);
		String fedoraXML = getXMLForRecordFromFedora(recordId);
		parser = XMLXPathParser.forXML(fedoraXML);
		convertNames(dataRecord);
		return parser.getDocumentAsString("/");
	}

	private String getIdFromRecord(DataGroup dataRecord) {
		DataGroup recordInfo = dataRecord.getFirstGroupWithNameInData("recordInfo");
		return recordInfo.getFirstAtomicValueWithNameInData("id");
	}

	private String getXMLForRecordFromFedora(String recordId) {
		String url = fedoraURL + "objects/" + recordId + "/datastreams/METADATA/content";
		HttpHandler httpHandler = httpHandlerFactory.factor(url);
		httpHandler.setRequestMethod("GET");
		return httpHandler.getResponseText();
	}

	private void convertNames(DataGroup dataRecord) {
		DataGroup authorizedNameGroup = dataRecord.getFirstGroupWithNameInData("authorizedName");
		updateFamilyName(authorizedNameGroup);
		updateGivenName(authorizedNameGroup);
	}

	private void updateFamilyName(DataGroup authorizedNameGroup) {
		String familyNameFromPersonRecord = authorizedNameGroup
				.getFirstAtomicValueWithNameInData("familyName");
		setStringFromDocumentUsingXPath("/authorityPerson/defaultName/lastname",
				familyNameFromPersonRecord);
	}

	private void updateGivenName(DataGroup authorizedNameGroup) {
		String givenNameFromPersonRecord = authorizedNameGroup
				.getFirstAtomicValueWithNameInData("givenName");
		setStringFromDocumentUsingXPath("/authorityPerson/defaultName/firstname",
				givenNameFromPersonRecord);
	}

	private void setStringFromDocumentUsingXPath(String xpathString, String newValue) {
		parser.setStringInDocumentUsingXPath(xpathString, newValue);
	}

	public HttpHandlerFactory getHttpHandlerFactory() {
		// needed for tests
		return httpHandlerFactory;
	}

	public String getFedorURL() {
		// needed for tests
		return fedoraURL;
	}

}
