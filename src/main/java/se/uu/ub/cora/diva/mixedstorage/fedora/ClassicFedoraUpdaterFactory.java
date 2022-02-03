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

/**
 * ClassicFedoraUpdaterFactory factors a ClassicFedoraUpdater
 */
public interface ClassicFedoraUpdaterFactory {

	/**
	 * factor factors a ClassicFedoraUpdater, using recordType to determine which
	 * ClassicFedoraUpdater to return
	 * 
	 * If no ClassicFedoraUpdater can be factored, a NotImplementedException SHOULD be thrown
	 * 
	 * @param String
	 *            recordType, the type used to determine which ClassicFedoraUpdater to return
	 * 
	 * @return a ClassicFedoraUpdater, suitable for the recordType
	 */
	ClassicFedoraUpdater factor(String recordType);

}
