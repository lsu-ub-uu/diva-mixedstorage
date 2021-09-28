/*
 * Copyright 2019, 2020, 2021 Uppsala University Library
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
import se.uu.ub.cora.diva.mixedstorage.db.organisation.DivaDbOrganisationReader;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.MultipleRowDbToDataParentReader;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.MultipleRowDbToDataPredecessorReader;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.MultipleRowDbToDataReader;
//import se.uu.ub.cora.sqldatabase.RecordReaderFactory;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;

public class DivaDbFactoryImp implements DivaDbFactory {

	// private RecordReaderFactory readerFactory;
	private DivaDbToCoraConverterFactory converterFactory;
	private SqlDatabaseFactory sqlDatabaseFactory;

	public DivaDbFactoryImp(SqlDatabaseFactory sqlDatabaseFactory,
			DivaDbToCoraConverterFactory converterFactory) {
		this.sqlDatabaseFactory = sqlDatabaseFactory;
		this.converterFactory = converterFactory;
	}

	@Override
	public DivaDbReader factor(String type) {
		if (isOrganisation(type)) {
			DivaDbFactory divaDbFactory = new DivaDbFactoryImp(sqlDatabaseFactory,
					converterFactory);
			return DivaDbOrganisationReader.usingRecordReaderFactoryAndConverterFactory(
					converterFactory, divaDbFactory, sqlDatabaseFactory);
		}
		throw NotImplementedException.withMessage("No implementation found for: " + type);
	}

	private boolean isOrganisation(String type) {
		return "organisation".equals(type) || "rootOrganisation".equals(type)
				|| "topOrganisation".equals(type) || "subOrganisation".equals(type);
	}

	public SqlDatabaseFactory getSqlDatabaseFactory() {
		// for testing
		return sqlDatabaseFactory;
	}

	public DivaDbToCoraConverterFactory getConverterFactory() {
		// for testing
		return converterFactory;
	}

	@Override
	public MultipleRowDbToDataReader factorMultipleReader(String type) {
		if ("divaOrganisationParent".equals(type)) {
			return new MultipleRowDbToDataParentReader(sqlDatabaseFactory, converterFactory);

		}
		if ("divaOrganisationPredecessor".equals(type)) {
			return new MultipleRowDbToDataPredecessorReader(sqlDatabaseFactory, converterFactory);

		}
		throw NotImplementedException.withMessage("No implementation found for: " + type);
	}

}
