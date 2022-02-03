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

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;

public class ClassicIndexerFactoryImp implements ClassicIndexerFactory {

	private String baseUrl;

	public ClassicIndexerFactoryImp(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	@Override
	public ClassicIndexer factor(String type) {
		if ("person".equals(type)) {
			HttpHandlerFactory httpHandlerFactory = new HttpHandlerFactoryImp();
			return new PersonClassicIndexer(httpHandlerFactory, baseUrl);
		}
		throw NotImplementedException.withMessage("No ClassicIndexer implementation for otherType");
	}

	public String getBaseUrl() {
		return baseUrl;
	}

}
