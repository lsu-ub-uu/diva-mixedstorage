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
package se.uu.ub.cora.diva.mixedstorage.db.organisation;

import java.util.List;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DbException;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbFactory;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbReader;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverter;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterFactory;
import se.uu.ub.cora.sqldatabase.Row;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;
import se.uu.ub.cora.sqldatabase.table.TableFacade;
import se.uu.ub.cora.sqldatabase.table.TableQuery;

public class DivaDbOrganisationReader implements DivaDbReader {

	private DivaDbToCoraConverterFactory converterFactory;
	private DivaDbFactory divaDbFactory;
	private static final String DEFAULT_TABLENAME = "organisationview";
	private SqlDatabaseFactory sqlDatabaseFactory;

	public DivaDbOrganisationReader(DivaDbToCoraConverterFactory converterFactory,
			DivaDbFactory divaDbFactory, SqlDatabaseFactory sqlDatabaseFactory) {
		this.converterFactory = converterFactory;
		this.divaDbFactory = divaDbFactory;
		this.sqlDatabaseFactory = sqlDatabaseFactory;
	}

	public static DivaDbOrganisationReader usingRecordReaderFactoryAndConverterFactory(
			DivaDbToCoraConverterFactory converterFactory, DivaDbFactory divaDbFactory,
			SqlDatabaseFactory sqlDatabaseFactory) {
		return new DivaDbOrganisationReader(converterFactory, divaDbFactory, sqlDatabaseFactory);
	}

	@Override
	public DataGroup read(TableFacade tableFacade, String type, String id) {
		String tableName = getTableName(type);
		Row readRow = readOneRowFromDbUsingTypeAndId(tableFacade, tableName, id);
		DataGroup organisation = convertOneMapFromDbToDataGroup(type, readRow);
		tryToReadAndConvertParents(tableFacade, type, id, organisation);
		tryToReadAndConvertPredecessors(tableFacade, id, organisation);
		return organisation;
	}

	private String getTableName(String type) {
		if ("rootOrganisation".equals(type)) {
			return "rootorganisationview";
		} else if ("topOrganisation".equals(type)) {
			return "toporganisationview";
		} else if ("subOrganisation".equals(type)) {
			return "suborganisationview";
		}
		return DEFAULT_TABLENAME;
	}

	private Row readOneRowFromDbUsingTypeAndId(TableFacade tableFacade, String type, String id) {
		throwDbExceptionIfIdNotAnIntegerValue(id);

		TableQuery tableQuery = sqlDatabaseFactory.factorTableQuery(type);
		tableQuery.addCondition("id", Integer.valueOf(id));
		return tableFacade.readOneRowForQuery(tableQuery);
	}

	private void throwDbExceptionIfIdNotAnIntegerValue(String id) {
		try {
			Integer.valueOf(id);
		} catch (NumberFormatException ne) {
			throw DbException.withMessageAndException("Record not found: " + id, ne);
		}
	}

	private DataGroup convertOneMapFromDbToDataGroup(String type, Row readRow) {
		DivaDbToCoraConverter dbToCoraConverter = converterFactory.factor(type);
		return dbToCoraConverter.fromRow(readRow);
	}

	private void tryToReadAndConvertParents(TableFacade tableFacade, String organisationType,
			String id, DataGroup organisation) {
		String type = "divaOrganisationParent";
		MultipleRowDbToDataReader parentReader = divaDbFactory.factorMultipleReader(type);
		List<DataGroup> convertedParents = parentReader.read(tableFacade, type, id);
		for (DataGroup convertedParent : convertedParents) {
			removeRepeatIdIfParentInTopOrganisation(organisationType, convertedParent);
			organisation.addChild(convertedParent);
		}
	}

	private void removeRepeatIdIfParentInTopOrganisation(String organisationType,
			DataGroup convertedParent) {
		if ("topOrganisation".equals(organisationType)) {
			convertedParent.setRepeatId(null);
		}
	}

	private void tryToReadAndConvertPredecessors(TableFacade tableFacade, String stringId,
			DataGroup organisation) {
		String type = "divaOrganisationPredecessor";
		MultipleRowDbToDataReader prededcessorReader = divaDbFactory.factorMultipleReader(type);
		List<DataGroup> convertedPredecessors = prededcessorReader.read(tableFacade, type,
				stringId);

		for (DataGroup convertedPredecessor : convertedPredecessors) {
			organisation.addChild(convertedPredecessor);
		}
	}

	public DivaDbToCoraConverterFactory getConverterFactory() {
		// for testing
		return converterFactory;
	}

	public DivaDbFactory getDbFactory() {
		// for testing
		return divaDbFactory;
	}

	public SqlDatabaseFactory getSqlDatabaseFactory() {
		return sqlDatabaseFactory;
	}
}
