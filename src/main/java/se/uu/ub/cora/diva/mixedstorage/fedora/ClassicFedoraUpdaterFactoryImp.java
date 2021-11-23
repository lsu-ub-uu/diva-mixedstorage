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

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;

public class ClassicFedoraUpdaterFactoryImp implements ClassicFedoraUpdateFactory {

	private HttpHandlerFactory httpHandlerFactory;
	private DivaFedoraConverterFactory toFedoraConverterFactory;
	private String baseUrl;
	private String username;
	private String password;

	public ClassicFedoraUpdaterFactoryImp(HttpHandlerFactory httpHandlerFactory,
			DivaFedoraConverterFactory toFedoraConverterFactory, String baseUrl, String username,
			String password) {
		this.httpHandlerFactory = httpHandlerFactory;
		this.toFedoraConverterFactory = toFedoraConverterFactory;
		this.baseUrl = baseUrl;
		this.username = username;
		this.password = password;
	}

	@Override
	public ClassicFedoraUpdater factor(String recordType) {
		if ("person".equals(recordType)) {
			return new ClassicFedoraPersonUpdater(httpHandlerFactory, toFedoraConverterFactory,
					baseUrl, username, password);
		}
		throw NotImplementedException
				.withMessage("Factor ClassicFedoraUpdater not implemented for " + recordType);
	}

}
