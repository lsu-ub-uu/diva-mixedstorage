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
package se.uu.ub.cora.diva.mixedstorage.internal;

import static org.testng.Assert.assertSame;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.OrganisationAddressRelatedTable;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.OrganisationAlternativeNameRelatedTable;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.OrganisationParentRelatedTable;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.OrganisationPredecessorRelatedTable;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.SqlDatabaseFactorySpy;

public class RelatedTableFactoryTest {

	private RelatedTableFactoryImp factory;
	private SqlDatabaseFactorySpy sqlDatabaseFactory;

	@BeforeMethod
	public void setUp() {
		sqlDatabaseFactory = new SqlDatabaseFactorySpy();
		factory = RelatedTableFactoryImp.usingReaderDeleterAndCreator(sqlDatabaseFactory);
	}

	@Test
	public void testInit() {
		assertSame(factory.getSqlDatabaseFactory(), sqlDatabaseFactory);
	}

	@Test
	public void testFactorOrganisationAlternativeName() {
		OrganisationAlternativeNameRelatedTable factoredTable = (OrganisationAlternativeNameRelatedTable) factory
				.factor("organisationAlternativeName");
		assertSame(factoredTable.getSqlDatabaseFactory(), sqlDatabaseFactory);
	}

	@Test
	public void testFactorOrganisationAddress() {
		OrganisationAddressRelatedTable factoredTable = (OrganisationAddressRelatedTable) factory
				.factor("organisationAddress");
		assertSame(factoredTable.getSqlDatabaseFactory(), sqlDatabaseFactory);
	}

	@Test
	public void testFactorOrganisationParent() {
		OrganisationParentRelatedTable factoredTable = (OrganisationParentRelatedTable) factory
				.factor("organisationParent");
		assertSame(factoredTable.getSqlDatabaseFactory(), sqlDatabaseFactory);
	}

	@Test
	public void testFactorOrganisationPredecessor() {
		OrganisationPredecessorRelatedTable factoredTable = (OrganisationPredecessorRelatedTable) factory
				.factor("organisationPredecessor");
		assertSame(factoredTable.getSqlDatabaseFactory(), sqlDatabaseFactory);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "Related table not implemented for someNonExistingRelatedTable")
	public void testNotImplemented() {
		factory.factor("someNonExistingRelatedTable");
	}
}
