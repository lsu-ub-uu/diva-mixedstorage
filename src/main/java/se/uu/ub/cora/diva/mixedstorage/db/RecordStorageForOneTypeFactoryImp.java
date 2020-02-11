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

import se.uu.ub.cora.connection.SqlConnectionProvider;
import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.OrganisationDbRecordStorage;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;

public class RecordStorageForOneTypeFactoryImp implements RecordStorageForOneTypeFactory {

	private DataToDbTranslaterFactory translaterFactory;
	private RelatedTableFactory relatedTableFactory;
	private RecordReaderFactory recordReaderFactory;
	private SqlConnectionProvider sqlConnectionProvider;

	public RecordStorageForOneTypeFactoryImp(DataToDbTranslaterFactory translaterFactory,
			RecordReaderFactory recordReaderFactory, RelatedTableFactory relatedTableFactory,
			SqlConnectionProvider sqlConnectionProvider) {
		this.translaterFactory = translaterFactory;
		this.recordReaderFactory = recordReaderFactory;
		this.relatedTableFactory = relatedTableFactory;
		this.sqlConnectionProvider = sqlConnectionProvider;
	}

	@Override
	public RecordStorageForOneType factor(String tableName) {
		if (tableName.equals("organisation")) {
			return factorForOrganisation();
		}
		throw NotImplementedException.withMessage("Main table not implemented for " + tableName);
	}

	private RecordStorageForOneType factorForOrganisation() {
		StatementExecutor preparedStatementCreator = new PreparedStatementExecutorImp();
		DataToDbTranslater translater = translaterFactory.factorForTableName("organisation");
		return new OrganisationDbRecordStorage(translater, recordReaderFactory, relatedTableFactory,
				sqlConnectionProvider, preparedStatementCreator);
	}

	public DataToDbTranslaterFactory getTranslaterFactory() {
		// needed for test
		return translaterFactory;
	}

	public RelatedTableFactory getRelatedTableFactory() {
		// needed for test
		return relatedTableFactory;
	}

	public SqlConnectionProvider getSqlConnectionProvider() {
		// needed for test
		return sqlConnectionProvider;
	}

	public RecordReaderFactory getRecordReaderFactory() {
		// needed for test
		return recordReaderFactory;
	}

}
