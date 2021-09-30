/*
 * Copyright 2020, 2021 Uppsala University Library
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
import se.uu.ub.cora.diva.mixedstorage.db.organisation.DivaDbOrganisationUpdater;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;

public class DivaDbUpdaterFactoryImp implements DivaDbUpdaterFactory {

	private DataToDbTranslaterFactory translaterFactory;
	private RelatedTableFactory relatedTableFactory;
	private SqlDatabaseFactory sqlDatabaseFactory;

	public DivaDbUpdaterFactoryImp(DataToDbTranslaterFactory translaterFactory,
			SqlDatabaseFactory sqlDatabaseFactory, RelatedTableFactory relatedTableFactory) {
		this.translaterFactory = translaterFactory;
		this.sqlDatabaseFactory = sqlDatabaseFactory;
		this.relatedTableFactory = relatedTableFactory;
	}

	@Override
	public DivaDbUpdater factor(String type) {
		if (isOrganisation(type)) {
			return factorForOrganisation();
		}
		throw NotImplementedException.withMessage("Updater not implemented for " + type);
	}

	private boolean isOrganisation(String type) {
		return "organisation".equals(type) || "rootOrganisation".equals(type)
				|| "topOrganisation".equals(type) || "subOrganisation".equals(type);
	}

	private DivaDbUpdater factorForOrganisation() {
		StatementExecutor preparedStatementCreator = new PreparedStatementExecutorImp();
		DataToDbTranslater translater = translaterFactory.factorForTableName("organisation");

		return new DivaDbOrganisationUpdater(translater, sqlDatabaseFactory, relatedTableFactory,
				preparedStatementCreator);
	}

	public DataToDbTranslaterFactory getTranslaterFactory() {
		// needed for test
		return translaterFactory;
	}

	public RelatedTableFactory getRelatedTableFactory() {
		// needed for test
		return relatedTableFactory;
	}
}
