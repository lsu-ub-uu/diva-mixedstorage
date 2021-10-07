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
package se.uu.ub.cora.diva.mixedstorage.db.organisation;

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTable;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTableFactory;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;

public class RelatedTableFactoryImp implements RelatedTableFactory {

	private SqlDatabaseFactory sqlDatabaseFactory;

	public static RelatedTableFactoryImp usingReaderDeleterAndCreator(
			SqlDatabaseFactory sqlDatabaseFactory) {
		return new RelatedTableFactoryImp(sqlDatabaseFactory);
	}

	private RelatedTableFactoryImp(SqlDatabaseFactory sqlDatabaseFactory) {
		this.sqlDatabaseFactory = sqlDatabaseFactory;
	}

	@Override
	public RelatedTable factor(String relatedTableName) {
		if ("organisationAlternativeName".equals(relatedTableName)) {
			return new OrganisationAlternativeNameRelatedTable(sqlDatabaseFactory);
		}
		if ("organisationAddress".equals(relatedTableName)) {
			return new OrganisationAddressRelatedTable(sqlDatabaseFactory);
		}

		if ("organisationParent".equals(relatedTableName)) {
			return new OrganisationParentRelatedTable(sqlDatabaseFactory);
		}
		if ("organisationPredecessor".equals(relatedTableName)) {
			return new OrganisationPredecessorRelatedTable(sqlDatabaseFactory);
		}

		throw NotImplementedException
				.withMessage("Related table not implemented for " + relatedTableName);
	}

	public SqlDatabaseFactory getSqlDatabaseFactory() {
		return sqlDatabaseFactory;
	}

}
