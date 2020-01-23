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

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.DbOrganisationMainTable;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.ReferenceTableFactory;
import se.uu.ub.cora.sqldatabase.RecordUpdater;
import se.uu.ub.cora.sqldatabase.RecordUpdaterFactory;

public class DbMainTableFactoryImp implements DbMainTableFactory {

	private DataToDbTranslaterFactory translaterFactory;
	private RecordUpdaterFactory recordUpdaterFactory;
	private RelatedTableFactory relatedTableFactory;
	private ReferenceTableFactory referenceTableFactory;

	public DbMainTableFactoryImp(DataToDbTranslaterFactory translaterFactory,
			RecordUpdaterFactory recordUpdaterFactory, RelatedTableFactory relatedTableFactory,
			ReferenceTableFactory referenceTableFactory) {
		this.translaterFactory = translaterFactory;
		this.recordUpdaterFactory = recordUpdaterFactory;
		this.relatedTableFactory = relatedTableFactory;
		this.referenceTableFactory = referenceTableFactory;
	}

	@Override
	public DbMainTable factor(String tableName) {
		if (tableName.equals("organisation")) {
			RecordUpdater recordUpdater = recordUpdaterFactory.factor();
			DataToDbTranslater translater = translaterFactory.factorForTableName("organisation");
			return new DbOrganisationMainTable(translater, recordUpdater, relatedTableFactory,
					referenceTableFactory);
		}
		throw NotImplementedException.withMessage("Main table not implemented for " + tableName);
	}

	public DataToDbTranslaterFactory getTranslaterFactory() {
		// needed for test
		return translaterFactory;
	}

	public RecordUpdaterFactory getRecordUpdaterFactory() {
		// needed for test
		return recordUpdaterFactory;
	}

	public RelatedTableFactory getRelatedTableFactory() {
		// needed for test
		return relatedTableFactory;
	}

	public ReferenceTableFactory getReferenceTableFactory() {
		// needed for test
		return referenceTableFactory;
	}

}
