/*
 * Copyright 2018, 2019, 2020 Uppsala University Library
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
package se.uu.ub.cora.diva.mixedstorage.db;

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.DivaDbToCoraOrganisationConverter;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.DivaDbToCoraOrganisationParentConverter;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.DivaDbToCoraOrganisationPredecessorConverter;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.DivaDbToCoraOrganisationSuccessorConverter;

public class DivaDbToCoraConverterFactoryImp implements DivaDbToCoraConverterFactory {

	@Override
	public DivaDbToCoraConverter factor(String type) {
		if (isOrganisation(type)) {
			// TODO:skicka med en DefaultConverterFactory??
			return new DivaDbToCoraOrganisationConverter(null);
		}
		if ("divaOrganisationParent".equals(type)) {
			return new DivaDbToCoraOrganisationParentConverter();
		}
		if ("divaOrganisationPredecessor".equals(type)) {
			return new DivaDbToCoraOrganisationPredecessorConverter();
		}
		if ("divaOrganisationSuccessor".equals(type)) {
			return new DivaDbToCoraOrganisationSuccessorConverter();
		}
		if ("user".equals(type)) {
			return new DivaDbToCoraUserConverter();
		}
		throw NotImplementedException.withMessage("No converter implemented for: " + type);
	}

	private boolean isOrganisation(String type) {
		return "organisation".equals(type) || "rootOrganisation".equals(type)
				|| "commonOrganisation".equals(type);
	}

}
