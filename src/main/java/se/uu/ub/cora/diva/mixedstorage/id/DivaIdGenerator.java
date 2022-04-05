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

import se.uu.ub.cora.diva.mixedstorage.ParseException;
import se.uu.ub.cora.fedoralegacy.FedoraConnectionInfo;
import se.uu.ub.cora.fedoralegacy.FedoraException;
import se.uu.ub.cora.fedoralegacy.parser.XMLXPathParser;
import se.uu.ub.cora.fedoralegacy.parser.XMLXPathParserException;
import se.uu.ub.cora.fedoralegacy.parser.XMLXPathParserFactory;
import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.storage.RecordIdGenerator;

public class DivaIdGenerator implements RecordIdGenerator {

	private static final int OK = 200;
	private HttpHandlerFactory httpHandlerFactory;
	private XMLXPathParserFactory parserFactory;
	private FedoraConnectionInfo fedoraConnectionInfo;

	public DivaIdGenerator(HttpHandlerFactory httpHandlerFactory,
			FedoraConnectionInfo fedoraConnectionInfo, XMLXPathParserFactory parserFactory) {
		this.httpHandlerFactory = httpHandlerFactory;
		this.fedoraConnectionInfo = fedoraConnectionInfo;
		this.parserFactory = parserFactory;
	}

	@Override
	public String getIdForType(String type) {
		if ("person".equals(type)) {
			return getNextPidForPerson();
		}
		return type + ":" + System.nanoTime();
	}

	private String getNextPidForPerson() {
		HttpHandler httpHandler = createHttpHandler();
		throwErrorIfPidCouldNotBeFetched(httpHandler);
		String nextPidXML = httpHandler.getResponseText();
		return tryToParseResult(nextPidXML);
	}

	private HttpHandler createHttpHandler() {
		HttpHandler httpHandler = factorHttpHandler();
		httpHandler.setRequestMethod("POST");
		httpHandler.setBasicAuthorization(fedoraConnectionInfo.fedoraUsername,
				fedoraConnectionInfo.fedoraPassword);
		return httpHandler;
	}

	private HttpHandler factorHttpHandler() {
		String url = fedoraConnectionInfo.fedoraUrl
				+ "objects/nextPID?namespace=authority-person&format=xml";
		return httpHandlerFactory.factor(url);
	}

	private void throwErrorIfPidCouldNotBeFetched(HttpHandler httpHandlerForPid) {
		if (httpHandlerForPid.getResponseCode() != OK) {
			throw FedoraException.withMessage("getting next pid from fedora failed"
					+ ", with response code: " + httpHandlerForPid.getResponseCode());
		}
	}

	private String tryToParseResult(String nextPidXML) {
		try {
			return parseResult(nextPidXML);
		} catch (XMLXPathParserException e) {
			throw ParseException
					.withMessage("Error parsing response from fedora: " + e.getMessage());
		}
	}

	private String parseResult(String nextPidXML) throws XMLXPathParserException {
		XMLXPathParser pathParser = parserFactory.factor();
		pathParser.setupToHandleResponseXML(nextPidXML);
		return pathParser.getStringFromDocumentUsingXPath("/pidList/pid/text()");
	}

	public FedoraConnectionInfo getFedoraConnectionInfo() {
		return fedoraConnectionInfo;
	}

	public HttpHandlerFactory getHttpHandlerFactory() {
		return httpHandlerFactory;
	}

	public XMLXPathParserFactory getXMLXPathParserFactory() {
		return parserFactory;
	}

}
