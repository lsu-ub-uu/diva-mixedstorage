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
package se.uu.ub.cora.diva.mixedstorage.db.organisation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbFactory;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbReader;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverter;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterFactory;
import se.uu.ub.cora.sqldatabase.RecordReader;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;

public class DivaDbOrganisationReader implements DivaDbReader {

	private RecordReaderFactory recordReaderFactory;
	private DivaDbToCoraConverterFactory converterFactory;
	private RecordReader recordReader;
	private DivaDbFactory divaDbFactory;

	public DivaDbOrganisationReader(RecordReaderFactory recordReaderFactory,
			DivaDbToCoraConverterFactory converterFactory, DivaDbFactory divaDbFactory) {
		this.recordReaderFactory = recordReaderFactory;
		this.converterFactory = converterFactory;
		this.divaDbFactory = divaDbFactory;
	}

	public static DivaDbOrganisationReader usingRecordReaderFactoryAndConverterFactory(
			RecordReaderFactory recordReaderFactory, DivaDbToCoraConverterFactory converterFactory,
			DivaDbFactory divaDbFactory) {
		return new DivaDbOrganisationReader(recordReaderFactory, converterFactory, divaDbFactory);
	}

	@Override
	public DataGroup read(String type, String id) {
		recordReader = recordReaderFactory.factor();
		Map<String, Object> readRow = readOneRowFromDbUsingTypeAndId(type, id);
		DataGroup organisation = convertOneMapFromDbToDataGroup(type, readRow);
		tryToReadAndConvertParents(id, organisation);
		tryToReadAndConvertPredecessors(id, organisation);
		return organisation;
	}

	private Map<String, Object> readOneRowFromDbUsingTypeAndId(String type, String id) {
		Map<String, Object> conditions = new HashMap<>();
		conditions.put("id", id);
		return recordReader.readOneRowFromDbUsingTableAndConditions(type, conditions);
	}

	private DataGroup convertOneMapFromDbToDataGroup(String type, Map<String, Object> readRow) {
		DivaDbToCoraConverter dbToCoraConverter = converterFactory.factor(type);
		return dbToCoraConverter.fromMap(readRow);
	}

	private void tryToReadAndConvertParents(String id, DataGroup organisation) {
		String type = "divaOrganisationParent";
		MultipleRowDbToDataReader parentReader = divaDbFactory.factorMultipleReader(type);
		List<DataGroup> convertedParents = parentReader.read(type, id);
		for (DataGroup convertedParent : convertedParents) {
			organisation.addChild(convertedParent);
		}
	}

	private void tryToReadAndConvertPredecessors(String stringId, DataGroup organisation) {
		String type = "divaOrganisationPredecessor";
		MultipleRowDbToDataReader prededcessorReader = divaDbFactory.factorMultipleReader(type);
		List<DataGroup> convertedPredecessors = prededcessorReader.read(type, stringId);

		for (DataGroup convertedPredecessor : convertedPredecessors) {
			organisation.addChild(convertedPredecessor);
		}
	}

	public RecordReaderFactory getRecordReaderFactory() {
		// for testing
		return recordReaderFactory;
	}

	public DivaDbToCoraConverterFactory getConverterFactory() {
		// for testing
		return converterFactory;
	}

	public DivaDbFactory getDbFactory() {
		// for testing
		return divaDbFactory;
	}
}
