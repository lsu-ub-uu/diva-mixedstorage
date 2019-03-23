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
package se.uu.ub.cora.diva.mixedstorage.db;

import java.util.HashMap;
import java.util.Map;

import se.uu.ub.cora.bookkeeper.data.DataGroup;

public class CoraToDbConverterSpy implements CoraToDbConverter {

	public DataGroup dataGroup;
	public PreparedStatementInfo psInfo;

	@Override
	public PreparedStatementInfo convert(DataGroup dataGroup) {
		this.dataGroup = dataGroup;
		Map<String, Object> values = new HashMap<>();
		values.put("valueKey", "valueString");
		Map<String, Object> conditions = new HashMap<>();
		conditions.put("conditionKey", "conditionString");
		psInfo = PreparedStatementInfo.withTableNameValuesAndConditions("tableNameFromSpy", values,
				conditions);
		return psInfo;
	}

}
