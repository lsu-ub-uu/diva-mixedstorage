/*
 * Copyright 2020 Uppsala University Library
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
package se.uu.ub.cora.diva.mixedstorage.db.user;

import java.util.HashMap;
import java.util.Map;

import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterFactory;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.DivaMultipleRowDbToDataReaderImp;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.MultipleRowDbToDataReader;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;

public class MultipleRowDbToDataUserGroupReader extends DivaMultipleRowDbToDataReaderImp
		implements MultipleRowDbToDataReader {

	public MultipleRowDbToDataUserGroupReader(RecordReaderFactory readerFactory,
			DivaDbToCoraConverterFactory converterFactory) {
		this.recordReaderFactory = readerFactory;
		this.converterFactory = converterFactory;
	}

	@Override
	protected String getTableName() {
		return "groupsforuser";
	}

	@Override
	protected Map<String, Object> getConditions(String id) {
		Map<String, Object> conditions = new HashMap<>();
		conditions.put("db_id", Integer.valueOf(id));
		return conditions;
	}
}
