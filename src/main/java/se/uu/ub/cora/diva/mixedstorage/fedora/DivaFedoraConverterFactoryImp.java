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

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;

public class DivaFedoraConverterFactoryImp implements DivaFedoraConverterFactory {

	private String fedoraURL;

	public static DivaFedoraConverterFactoryImp usingFedoraURL(String fedoraURL) {
		return new DivaFedoraConverterFactoryImp(fedoraURL);
	}

	private DivaFedoraConverterFactoryImp(String fedoraURL) {
		this.fedoraURL = fedoraURL;
	}

	@Override
	public DivaXmlParserFedoraToCoraConverter factorToCoraConverter(String type) {
		if ("person".equals(type)) {
			return new DivaXmlParserFedoraToCoraPersonConverter();
		}
		throw NotImplementedException.withMessage("No converter implemented for: " + type);
	}

	@Override
	public DivaCoraToFedoraConverter factorToFedoraConverter(String type) {
		if ("person".equals(type)) {
			HttpHandlerFactoryImp httpHandlerFactory = new HttpHandlerFactoryImp();
			return DivaCoraToFedoraPersonConverter
					.usingHttpHandlerFactoryAndFedoraUrl(httpHandlerFactory, fedoraURL);
		}
		throw NotImplementedException.withMessage("No converter implemented for: " + type);
	}

	public String getFedoraURL() {
		// needed for tests
		return fedoraURL;
	}

}
