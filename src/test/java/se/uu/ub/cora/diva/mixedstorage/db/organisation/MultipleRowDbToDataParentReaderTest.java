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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterSpy;

public class MultipleRowDbToDataParentReaderTest {

	private static final String TABLE_NAME = "divaOrganisationParent";
	private DivaDbToCoraConverterFactorySpy converterFactory;
	private MultipleRowDbToDataParentReader parentReader;
	private SqlDatabaseFactorySpy sqlDatabaseFactory;
	private TableFacadeSpy tableFacade;

	@BeforeMethod
	public void BeforeMethod() {
		converterFactory = new DivaDbToCoraConverterFactorySpy();
		sqlDatabaseFactory = new SqlDatabaseFactorySpy();
		tableFacade = new TableFacadeSpy();

		parentReader = new MultipleRowDbToDataParentReader(sqlDatabaseFactory, converterFactory);
		initRowsToReturn();
	}

	private void initRowsToReturn() {
		tableFacade.createAndAddRowToReturn("id", 567);
	}

	@Test
	public void testGetSqlDatabaseFactory() {
		assertSame(sqlDatabaseFactory, parentReader.getSqlDatabaseFactory());
	}

	@Test
	public void testReadParentTableFactorsQueryAndSendsToFacade() {
		parentReader.read(tableFacade, TABLE_NAME, "567");
		assertEquals(sqlDatabaseFactory.tableName, TABLE_NAME);
		TableQuerySpy tableQuery = sqlDatabaseFactory.factoredTableQuery;
		assertSame(tableFacade.tableQueries.get(0), tableQuery);
	}

	@Test
	public void testReadParentConditionsForParentTable() throws Exception {
		parentReader.read(tableFacade, TABLE_NAME, "567");
		TableQuerySpy tableQuery = sqlDatabaseFactory.factoredTableQuery;
		assertEquals(tableQuery.conditions.get("organisation_id"), 567);

	}

	@Test
	public void testReadParentConverterIsFactored() throws Exception {
		parentReader.read(tableFacade, TABLE_NAME, "567");
		DivaDbToCoraConverterSpy divaDbToCoraConverter = converterFactory.factoredConverters.get(0);
		assertNotNull(divaDbToCoraConverter);
		assertEquals(converterFactory.factoredTypes.get(0), "divaOrganisationParent");
	}

	@Test
	public void testReadParentNoParentsFound() throws Exception {
		tableFacade.rowsToReturn = Collections.emptyList();
		parentReader = new MultipleRowDbToDataParentReader(sqlDatabaseFactory, converterFactory);

		List<DataGroup> readParents = parentReader.read(tableFacade, TABLE_NAME, "567");
		assertTrue(readParents.isEmpty());
		assertTrue(converterFactory.factoredConverters.isEmpty());
	}

	@Test
	public void testParentConverterIsCalledWithReadParentFromDbStorage() throws Exception {
		parentReader.read(tableFacade, TABLE_NAME, "567");

		DivaDbToCoraConverterSpy divaDbToCoraConverter = converterFactory.factoredConverters.get(0);
		assertNotNull(divaDbToCoraConverter.rowToConvert);
		assertEquals(tableFacade.rowsToReturn.get(0), divaDbToCoraConverter.rowToConvert);
	}

	@Test
	public void testParentConverterIsCalledWithMultipleReadParentFromDbStorage() throws Exception {
		tableFacade.createAndAddRowToReturn("id", 123);
		tableFacade.createAndAddRowToReturn("id", 765);
		parentReader.read(tableFacade, TABLE_NAME, "567");

		List<DivaDbToCoraConverterSpy> factoredConverters = converterFactory.factoredConverters;
		DivaDbToCoraConverterSpy divaDbToCoraConverter = factoredConverters.get(0);
		assertNotNull(divaDbToCoraConverter.rowToConvert);
		assertEquals(factoredConverters.get(0).rowToConvert, tableFacade.rowsToReturn.get(0));
		assertEquals(factoredConverters.get(1).rowToConvert, tableFacade.rowsToReturn.get(1));
		assertEquals(factoredConverters.get(2).rowToConvert, tableFacade.rowsToReturn.get(2));
	}

	@Test
	public void testReadParentMultipleParentsFound() throws Exception {
		tableFacade.createAndAddRowToReturn("id", 123);
		tableFacade.createAndAddRowToReturn("id", 765);
		List<DataGroup> readParents = parentReader.read(tableFacade, TABLE_NAME, "567");
		assertEquals(readParents.size(), 3);

		assertEquals(readParents.get(0).getRepeatId(), "0");
		assertEquals(readParents.get(1).getRepeatId(), "1");
		assertEquals(readParents.get(2).getRepeatId(), "2");
	}
}
