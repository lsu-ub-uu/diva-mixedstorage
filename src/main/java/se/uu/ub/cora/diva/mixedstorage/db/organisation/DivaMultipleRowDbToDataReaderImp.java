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
package se.uu.ub.cora.diva.mixedstorage.db.organisation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverter;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterFactory;
import se.uu.ub.cora.sqldatabase.Row;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;
import se.uu.ub.cora.sqldatabase.table.TableFacade;
import se.uu.ub.cora.sqldatabase.table.TableQuery;

public abstract class DivaMultipleRowDbToDataReaderImp implements MultipleRowDbToDataReader {

	protected DivaDbToCoraConverterFactory converterFactory;
	protected SqlDatabaseFactory sqlDatabaseFactory;
	protected TableFacade tableFacade;

	protected List<DataGroup> convertToDataGroups(List<Row> readRows) {
		int repeatId = 0;
		List<DataGroup> convertedDataGroups = new ArrayList<>(readRows.size());
		for (Row readRow : readRows) {
			DataGroup convertedParent = convertToDataGroup(repeatId, readRow);
			convertedDataGroups.add(convertedParent);
			repeatId++;
		}
		return convertedDataGroups;
	}

	private DataGroup convertToDataGroup(int repeatId, Row readRow) {
		DivaDbToCoraConverter converter = converterFactory.factor(getTableName());
		DataGroup parent = converter.fromRow(readRow);
		parent.setRepeatId(String.valueOf(repeatId));
		return parent;
	}

	protected abstract String getTableName();

	public SqlDatabaseFactory getSqlDatabaseFactory() {
		// needed for test
		return sqlDatabaseFactory;
	}

	public DivaDbToCoraConverterFactory getConverterFactory() {
		// needed for test
		return converterFactory;
	}

	protected abstract Map<String, Object> getConditions(String id);

	@Override
	public List<DataGroup> read(String tableName, Map<String, Object> conditions) {
		TableQuery tableQuery = sqlDatabaseFactory.factorTableQuery(tableName);
		for (Entry<String, Object> condition : conditions.entrySet()) {
			tableQuery.addCondition(condition.getKey(), condition.getValue());
		}
		List<Row> readRows = tableFacade.readRowsForQuery(tableQuery);
		return convertToDataGroups(readRows);
	}

	@Override
	public List<DataGroup> read(String type, String id) {
		return read(getTableName(), getConditions(id));
	}

	public TableFacade getTableFacade() {
		return tableFacade;
	}

}
