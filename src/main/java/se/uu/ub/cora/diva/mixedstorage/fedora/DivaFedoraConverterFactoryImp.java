/*
 * Copyright 2019, 2021 Uppsala University Library
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
import se.uu.ub.cora.diva.mixedstorage.RepeatableRelatedLinkCollector;
import se.uu.ub.cora.xmlutils.transformer.CoraTransformationFactory;

public class DivaFedoraConverterFactoryImp implements DivaFedoraConverterFactory {

	private String fedoraURL;
	private CoraTransformationFactory coraTransformationFactory;
	private RepeatableRelatedLinkCollector repeatableLinkCollector;

	public static DivaFedoraConverterFactoryImp usingFedoraURLAndTransformerFactory(
			String fedoraURL, CoraTransformationFactory transformationFactory,
			RepeatableRelatedLinkCollector repeatableLinkCollector) {
		return new DivaFedoraConverterFactoryImp(fedoraURL, transformationFactory,
				repeatableLinkCollector);
	}

	private DivaFedoraConverterFactoryImp(String fedoraURL,
			CoraTransformationFactory coraTransformationFactory,
			RepeatableRelatedLinkCollector repeatableLinkCollector) {
		this.fedoraURL = fedoraURL;
		this.coraTransformationFactory = coraTransformationFactory;
		this.repeatableLinkCollector = repeatableLinkCollector;
	}

	@Override
	public DivaCoraToFedoraConverter factorToFedoraConverter(String type) {
		if ("person".equals(type)) {
			return new DivaCoraToFedoraPersonConverter(coraTransformationFactory,
					repeatableLinkCollector);
		}
		throw NotImplementedException.withMessage("No converter implemented for: " + type);
	}

	public String getFedoraURL() {
		// needed for tests
		return fedoraURL;
	}

	public CoraTransformationFactory getCoraTransformerFactory() {
		return coraTransformationFactory;
	}

	public RepeatableRelatedLinkCollector getRepeatableRelatedLinkCollector() {
		return repeatableLinkCollector;
	}
}
