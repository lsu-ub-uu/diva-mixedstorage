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

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.OrganisationAlternativeNameDataToDbTranslater;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.OrganisationDataToDbTranslater;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;

public class DivaDataToDbTranslaterFactoryImp implements DataToDbTranslaterFactory {

	private SqlDatabaseFactory sqlDatabaseFactory;

	public DivaDataToDbTranslaterFactoryImp(SqlDatabaseFactory sqlDatabaseFactory) {
		this.sqlDatabaseFactory = sqlDatabaseFactory;
	}

	@Override
	public DataToDbTranslater factorForTableName(String tableName) {
		if ("organisation".contentEquals(tableName)) {
			// RecordReader recordReader = recordReaderFactory.factor();
			return new OrganisationDataToDbTranslater(sqlDatabaseFactory);
		}
		if ("organisation_name".equals(tableName)) {
			return new OrganisationAlternativeNameDataToDbTranslater();
		}
		throw NotImplementedException.withMessage("No translater implemented for: " + tableName);
	}

	public SqlDatabaseFactory getSqlDatabaseFactory() {
		// needed for test
		return sqlDatabaseFactory;
	}

}
