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

import se.uu.ub.cora.data.DataGroup;

/**
 * ClassicFedoraUpdater is used to synchronize updates between storage in Cora and fedora storage in
 * classic
 */
public interface ClassicFedoraUpdater {

	/**
	 * updateInFedora updates a record in classic fedora storage
	 * 
	 * If update of fedora fails, a FedoraExceptionFedoraException SHOULD be thrown
	 * 
	 * @param String
	 *            recordType, the recordType of the record to be updated
	 * 
	 * @param String
	 *            recordId, the record id of the record to be updated
	 * 
	 * @param DataGroup
	 *            dataGroup, the dataGroup to convert and store in Fedora
	 */
	void updateInFedora(String recordType, String recordId, DataGroup dataGroup);

}
