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

public class MultipleRowDbToDataPredecessorReaderTest {

	private static final String TABLE_NAME = "divaOrganisationPredecessor";
	private DivaDbToCoraConverterFactorySpy converterFactory;
	private MultipleRowDbToDataPredecessorReader predecessorReader;
	private SqlDatabaseFactorySpy sqlDatabaseFactory;

	@BeforeMethod
	public void BeforeMethod() {
		converterFactory = new DivaDbToCoraConverterFactorySpy();
		sqlDatabaseFactory = new SqlDatabaseFactorySpy();
		predecessorReader = new MultipleRowDbToDataPredecessorReader(sqlDatabaseFactory,
				converterFactory);
		initRowsToReturn();
	}

	private void initRowsToReturn() {
		sqlDatabaseFactory.createAndAddRowToReturn("id", 567);
	}

	@Test
	public void testInit() {
		assertNotNull(predecessorReader.getTableFacade());
		assertSame(predecessorReader.getTableFacade(), sqlDatabaseFactory.factoredTableFacade);
	}

	@Test
	public void testGetSqlDatabaseFactory() {
		assertSame(sqlDatabaseFactory, predecessorReader.getSqlDatabaseFactory());
	}

	@Test
	public void testReadPredecessorTableFactorsQueryAndSendsToFacade() {
		predecessorReader.read(TABLE_NAME, "567");
		TableFacadeSpy tableFacade = (TableFacadeSpy) predecessorReader.getTableFacade();

		assertEquals(sqlDatabaseFactory.tableName, TABLE_NAME);
		TableQuerySpy tableQuery = sqlDatabaseFactory.factoredTableQuery;
		assertSame(tableFacade.tableQueries.get(0), tableQuery);
	}

	@Test
	public void testReadPredecessorConditionsForPredecessorTable() throws Exception {
		predecessorReader.read(TABLE_NAME, "567");
		TableQuerySpy tableQuery = sqlDatabaseFactory.factoredTableQuery;
		assertEquals(tableQuery.conditions.get("organisation_id"), 567);
	}

	@Test
	public void testReadPredecessorConverterIsFactored() throws Exception {
		predecessorReader.read(TABLE_NAME, "567");
		DivaDbToCoraConverterSpy divaDbToCoraConverter = converterFactory.factoredConverters.get(0);
		assertNotNull(divaDbToCoraConverter);
		assertEquals(converterFactory.factoredTypes.get(0), "divaOrganisationPredecessor");
	}

	@Test
	public void testReadPredecessorNoPredecessorsFound() throws Exception {
		sqlDatabaseFactory.rowsToReturn = Collections.emptyList();
		predecessorReader = new MultipleRowDbToDataPredecessorReader(sqlDatabaseFactory,
				converterFactory);

		List<DataGroup> readPredecessors = predecessorReader.read(TABLE_NAME, "567");
		assertTrue(readPredecessors.isEmpty());
		assertTrue(converterFactory.factoredConverters.isEmpty());
	}

	@Test
	public void testPredecessorConverterIsCalledWithReadPredecessorFromDbStorage() {
		predecessorReader.read(TABLE_NAME, "567");
		TableFacadeSpy tableFacade = sqlDatabaseFactory.factoredTableFacade;

		DivaDbToCoraConverterSpy divaDbToCoraConverter = converterFactory.factoredConverters.get(0);
		assertNotNull(divaDbToCoraConverter.rowToConvert);
		assertEquals(tableFacade.rowsToReturn.get(0), divaDbToCoraConverter.rowToConvert);

	}

	@Test
	public void testPredecessorConverterIsCalledWithMultipleReadPredecessorFromDbStorage() {
		sqlDatabaseFactory.createAndAddRowToReturn("id", 123);
		sqlDatabaseFactory.createAndAddRowToReturn("id", 765);
		predecessorReader.read(TABLE_NAME, "567");

		TableFacadeSpy tableFacade = sqlDatabaseFactory.factoredTableFacade;

		List<DivaDbToCoraConverterSpy> factoredConverters = converterFactory.factoredConverters;
		DivaDbToCoraConverterSpy divaDbToCoraConverter = factoredConverters.get(0);
		assertNotNull(divaDbToCoraConverter.rowToConvert);
		assertEquals(factoredConverters.get(0).rowToConvert, tableFacade.rowsToReturn.get(0));
		assertEquals(factoredConverters.get(1).rowToConvert, tableFacade.rowsToReturn.get(1));
		assertEquals(factoredConverters.get(2).rowToConvert, tableFacade.rowsToReturn.get(2));
	}

	@Test
	public void testReadPredecessorMultiplePredecessorsFound() throws Exception {
		sqlDatabaseFactory.createAndAddRowToReturn("id", 123);
		sqlDatabaseFactory.createAndAddRowToReturn("id", 765);
		List<DataGroup> readPredecessors = predecessorReader.read(TABLE_NAME, "567");
		assertEquals(readPredecessors.size(), 3);

		assertEquals(readPredecessors.get(0).getRepeatId(), "0");
		assertEquals(readPredecessors.get(1).getRepeatId(), "1");
		assertEquals(readPredecessors.get(2).getRepeatId(), "2");
	}
}
