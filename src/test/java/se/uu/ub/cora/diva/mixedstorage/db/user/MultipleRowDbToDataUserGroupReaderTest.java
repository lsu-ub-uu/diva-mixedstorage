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
package se.uu.ub.cora.diva.mixedstorage.db.user;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.DivaMultipleRowDbToDataReaderImp;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.SqlDatabaseFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.TableFacadeSpy;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.TableQuerySpy;

public class MultipleRowDbToDataUserGroupReaderTest {

	private DivaDbToCoraConverterFactorySpy converterFactory;
	private SqlDatabaseFactorySpy sqlDatabaseFactory;
	private TableFacadeSpy tableFacade;

	@BeforeMethod
	public void setUp() {
		sqlDatabaseFactory = new SqlDatabaseFactorySpy();
		converterFactory = new DivaDbToCoraConverterFactorySpy();
		tableFacade = new TableFacadeSpy();
	}

	@Test
	public void testInit() {
		DivaMultipleRowDbToDataReaderImp userGroupReader = new MultipleRowDbToDataUserGroupReader(
				sqlDatabaseFactory, converterFactory);
		assertSame(userGroupReader.getSqlDatabaseFactory(), sqlDatabaseFactory);

	}

	@Test
	public void testRead() {
		DivaMultipleRowDbToDataReaderImp userGroupReader = new MultipleRowDbToDataUserGroupReader(
				sqlDatabaseFactory, converterFactory);
		userGroupReader.read(tableFacade, "", "67");

		assertEquals(sqlDatabaseFactory.tableName, "groupsforuser");
		TableQuerySpy tableQuery = sqlDatabaseFactory.factoredTableQuery;
		assertSame(tableFacade.tableQueries.get(0), tableQuery);

		assertEquals(tableQuery.conditions.get("db_id"), 67);
	}

}
