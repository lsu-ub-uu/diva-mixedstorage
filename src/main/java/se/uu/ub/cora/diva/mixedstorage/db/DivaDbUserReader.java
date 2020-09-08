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
package se.uu.ub.cora.diva.mixedstorage.db;

import java.util.HashMap;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.sqldatabase.RecordReader;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;

public class DivaDbUserReader implements DivaDbReader {

	private RecordReaderFactory readerFactory;
	private DivaDbToCoraConverterFactory converterFactory;

	public DivaDbUserReader(RecordReaderFactory readerFactory,
			DivaDbToCoraConverterFactory converterFactory) {
		this.readerFactory = readerFactory;
		this.converterFactory = converterFactory;
	}

	@Override
	public DataGroup read(String type, String id) {
		RecordReader reader = readerFactory.factor();
		DivaDbToCoraConverter dbToCoraConverter = converterFactory.factor(type);
		Map<String, Object> conditions = createConditions(id);
		return readAndConvertRow(type, reader, dbToCoraConverter, conditions);
	}

	private Map<String, Object> createConditions(String id) {
		Map<String, Object> conditions = new HashMap<>();
		conditions.put("db_id", id);
		return conditions;
	}

	private DataGroup readAndConvertRow(String type, RecordReader reader,
			DivaDbToCoraConverter dbToCoraConverter, Map<String, Object> conditions) {
		Map<String, Object> readRow = reader.readOneRowFromDbUsingTableAndConditions(type,
				conditions);
		return dbToCoraConverter.fromMap(readRow);
	}

	public RecordReaderFactory getRecordReaderFactory() {
		return readerFactory;
	}

	public DivaDbToCoraConverterFactory getConverterFactory() {
		return converterFactory;
	}

}
