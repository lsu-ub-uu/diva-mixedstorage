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
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.MultipleRowDbToDataReader;
import se.uu.ub.cora.sqldatabase.RecordReader;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;

public class DivaDbUserReader implements DivaDbReader {

	private RecordReaderFactory readerFactory;
	private DivaDbToCoraConverterFactory converterFactory;
	private DivaDbFactory divaDbFactory;

	public static DivaDbUserReader usingReaderFactoryConverterFactoryAndDivaDbFactory(
			RecordReaderFactory readerFactory, DivaDbToCoraConverterFactory converterFactory,
			DivaDbFactory divaDbFactory) {
		return new DivaDbUserReader(readerFactory, converterFactory, divaDbFactory);
	}

	private DivaDbUserReader(RecordReaderFactory readerFactory,
			DivaDbToCoraConverterFactory converterFactory, DivaDbFactory divaDbFactory) {
		this.readerFactory = readerFactory;
		this.converterFactory = converterFactory;
		this.divaDbFactory = divaDbFactory;
	}

	@Override
	public DataGroup read(String type, String id) {
		RecordReader reader = readerFactory.factor();
		DivaDbToCoraConverter dbToCoraConverter = converterFactory.factor(type);
		Map<String, Object> conditions = createConditions(id);
		DataGroup userDataGroup = readAndConvertRow(reader, dbToCoraConverter, conditions);

		readAndAddRoles(id, userDataGroup);
		return userDataGroup;
	}

	private void readAndAddRoles(String id, DataGroup userDataGroup) {
		MultipleRowDbToDataReader groupReader = divaDbFactory.factorMultipleReader("groupsforuser");
		List<DataGroup> userGroups = groupReader.read("", id);
		for (DataGroup group : userGroups) {
			userDataGroup.addChild(group);
		}
	}

	private Map<String, Object> createConditions(String id) {
		Map<String, Object> conditions = new HashMap<>();
		conditions.put("db_id", id);
		return conditions;
	}

	private DataGroup readAndConvertRow(RecordReader reader,
			DivaDbToCoraConverter dbToCoraConverter, Map<String, Object> conditions) {
		String tableNameInDatabase = "public.user";

		Map<String, Object> readRow = reader
				.readOneRowFromDbUsingTableAndConditions(tableNameInDatabase, conditions);
		return dbToCoraConverter.fromMap(readRow);
	}

	public RecordReaderFactory getRecordReaderFactory() {
		// needed for test
		return readerFactory;
	}

	public DivaDbToCoraConverterFactory getConverterFactory() {
		// needed for test
		return converterFactory;
	}

	public DivaDbFactory getDivaDbFactory() {
		// needed for test
		return divaDbFactory;
	}

}
