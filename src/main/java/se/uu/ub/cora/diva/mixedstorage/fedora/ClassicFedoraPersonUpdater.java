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

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;

public class ClassicFedoraPersonUpdater implements ClassicFedoraUpdater {

	private static final int OK = 200;
	private HttpHandlerFactory httpHandlerFactory;
	private DivaFedoraConverterFactory divaCoraToFedoraConverterFactory;
	private String baseUrl;
	private String username;
	private String password;

	public ClassicFedoraPersonUpdater(HttpHandlerFactory httpHandlerFactory,
			DivaFedoraConverterFactory divaCoraToFedoraConverterFactory, String baseUrl,
			String username, String password) {
		this.httpHandlerFactory = httpHandlerFactory;
		this.divaCoraToFedoraConverterFactory = divaCoraToFedoraConverterFactory;
		this.baseUrl = baseUrl;
		this.username = username;
		this.password = password;
	}

	@Override
	public void updateInFedora(String recordType, String recordId, DataGroup dataGroup) {
		HttpHandler httpHandler = setUpHttpHandlerForUpdate(recordId);
		String fedoraXML = convertRecordToFedoraXML(recordType, dataGroup);
		httpHandler.setOutput(fedoraXML);
		int responseCode = httpHandler.getResponseCode();
		throwErrorIfNotOkFromFedora(recordId, responseCode);
	}

	private void throwErrorIfNotOkFromFedora(String id, int responseCode) {
		if (responseCode != OK) {
			throw FedoraException.withMessage("update to fedora failed for record: " + id
					+ ", with response code: " + responseCode);
		}
	}

	private HttpHandler setUpHttpHandlerForUpdate(String recordId) {
		String url = createURL(recordId);
		HttpHandler httpHandler = httpHandlerFactory.factor(url);
		httpHandler.setRequestMethod("PUT");
		setAutorizationInHttpHandler(httpHandler);
		return httpHandler;
	}

	private void setAutorizationInHttpHandler(HttpHandler httpHandler) {
		String encoded = Base64.getEncoder()
				.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
		httpHandler.setRequestProperty("Authorization", "Basic " + encoded);
	}

	private String convertRecordToFedoraXML(String recordType, DataGroup dataGroup) {
		DivaCoraToFedoraConverter toFedoraConverter = divaCoraToFedoraConverterFactory
				.factorToFedoraConverter(recordType);
		return toFedoraConverter.toXML(dataGroup);
	}

	private String createURL(String recordId) {
		return baseUrl + "objects/" + recordId + "/datastreams/METADATA?format=?xml&controlGroup=M"
				+ "&logMessage=coraWritten&checksumType=SHA-512";
	}

	HttpHandlerFactory getHttpHandlerFactory() {
		return httpHandlerFactory;
	}

	DivaFedoraConverterFactory getDivaCoraToFedoraConverterFactory() {
		return divaCoraToFedoraConverterFactory;
	}

	String getBaseUrl() {
		return baseUrl;
	}

	String getUsername() {
		return username;
	}

	String getPassword() {
		return password;
	}

}
