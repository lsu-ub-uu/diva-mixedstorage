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
import se.uu.ub.cora.xmlutils.transformer.CoraTransformation;
import se.uu.ub.cora.xmlutils.transformer.CoraTransformationFactory;

public class DivaFedoraConverterFactoryImp implements DivaFedoraConverterFactory {

	private static final String PERSON_XSLT_PATH = "person/coraPerson.xsl";
	private String fedoraURL;
	private CoraTransformationFactory transformationFactory;

	public static DivaFedoraConverterFactoryImp usingFedoraURL(String fedoraURL,
			CoraTransformationFactory transformationFactory) {
		return new DivaFedoraConverterFactoryImp(fedoraURL, transformationFactory);
	}

	private DivaFedoraConverterFactoryImp(String fedoraURL,
			CoraTransformationFactory transformationFactory) {
		this.fedoraURL = fedoraURL;
		this.transformationFactory = transformationFactory;
	}

	@Override
	public DivaFedoraToCoraConverter factorToCoraConverter(String type) {
		if ("person".equals(type)) {
			CoraTransformation coraTransformation = transformationFactory.factor(PERSON_XSLT_PATH);
			return new DivaFedoraToCoraPersonConverter(coraTransformation);
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
