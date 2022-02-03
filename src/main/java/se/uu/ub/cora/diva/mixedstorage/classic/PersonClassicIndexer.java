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
package se.uu.ub.cora.diva.mixedstorage.classic;

import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;

public class PersonClassicIndexer implements ClassicIndexer {

	private static final int OK = 200;

	private Logger logger = LoggerProvider.getLoggerForClass(PersonClassicIndexer.class);

	private HttpHandlerFactory httpHandlerFactory;
	private String baseUrl;

	public PersonClassicIndexer(HttpHandlerFactory httpHandlerFactory, String baseUrl) {
		this.httpHandlerFactory = httpHandlerFactory;
		this.baseUrl = baseUrl;
	}

	@Override
	public void index(String recordId) {
		if (baseUrl.isEmpty()) {
			logger.logInfoUsingMessage("No call to classic indexer, due to empty URL.");
		} else {
			callIndexerInClassic(recordId);
		}

	}

	private void callIndexerInClassic(String recordId) {
		String url = baseUrl + "authority/person/index/" + recordId;
		HttpHandler httpHandler = httpHandlerFactory.factor(url);
		httpHandler.setRequestMethod("GET");
		logErrorIfResponseNotOK(httpHandler, recordId);
	}

	private void logErrorIfResponseNotOK(HttpHandler httpHandler, String recordId) {
		int responseCode = httpHandler.getResponseCode();
		if (responseCode != OK) {
			logger.logErrorUsingMessage("Unable to index record in classic, recordId: " + recordId);
		}
	}

	public HttpHandlerFactory getHttpHandlerFactory() {
		return httpHandlerFactory;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

}
