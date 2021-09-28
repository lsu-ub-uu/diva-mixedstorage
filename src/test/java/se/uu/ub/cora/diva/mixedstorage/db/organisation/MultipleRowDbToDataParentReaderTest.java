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

	@BeforeMethod
	public void BeforeMethod() {
		converterFactory = new DivaDbToCoraConverterFactorySpy();
		sqlDatabaseFactory = new SqlDatabaseFactorySpy();
		parentReader = new MultipleRowDbToDataParentReader(sqlDatabaseFactory, converterFactory);
	}

	@Test
	public void testInit() {
		assertSame(parentReader.getTableFacade(), sqlDatabaseFactory.factoredTableFacade);
	}

	@Test
	public void testGetSqlDatabaseFactory() {
		assertSame(sqlDatabaseFactory, parentReader.getSqlDatabaseFactory());
	}

	@Test
	public void testReadParentTableFactorsQueryAndSendsToFacade() {
		parentReader.read(TABLE_NAME, "567");
		TableFacadeSpy tableFacade = (TableFacadeSpy) parentReader.getTableFacade();

		assertEquals(sqlDatabaseFactory.tableName, TABLE_NAME);
		TableQuerySpy tableQuery = sqlDatabaseFactory.factoredTableQuery;
		assertSame(tableFacade.tableQuery, tableQuery);
	}

	@Test
	public void testReadParentConditionsForParentTable() throws Exception {
		parentReader.read(TABLE_NAME, "567");
		TableQuerySpy tableQuery = sqlDatabaseFactory.factoredTableQuery;
		assertEquals(tableQuery.conditions.get("organisation_id"), 567);

	}

	@Test
	public void testReadParentConverterIsFactored() throws Exception {
		parentReader.read(TABLE_NAME, "567");
		DivaDbToCoraConverterSpy divaDbToCoraConverter = converterFactory.factoredConverters.get(0);
		assertNotNull(divaDbToCoraConverter);
		assertEquals(converterFactory.factoredTypes.get(0), "divaOrganisationParent");
	}

	@Test
	public void testReadParentNoParentsFound() throws Exception {
		sqlDatabaseFactory.numToReturn = 0;
		parentReader = new MultipleRowDbToDataParentReader(sqlDatabaseFactory, converterFactory);

		List<DataGroup> readParents = parentReader.read(TABLE_NAME, "567");
		assertTrue(readParents.isEmpty());
		assertTrue(converterFactory.factoredConverters.isEmpty());
	}

	@Test
	public void testParentConverterIsCalledWithReadParentFromDbStorage() throws Exception {
		parentReader.read(TABLE_NAME, "567");
		TableFacadeSpy tableFacade = sqlDatabaseFactory.factoredTableFacade;

		DivaDbToCoraConverterSpy divaDbToCoraConverter = converterFactory.factoredConverters.get(0);
		assertNotNull(divaDbToCoraConverter.rowToConvert);
		assertEquals(tableFacade.returnedRows.get(0), divaDbToCoraConverter.rowToConvert);
	}

	@Test
	public void testParentConverterIsCalledWithMultipleReadParentFromDbStorage() throws Exception {
		parentReader.read(TABLE_NAME, "567");

		TableFacadeSpy tableFacade = sqlDatabaseFactory.factoredTableFacade;

		List<DivaDbToCoraConverterSpy> factoredConverters = converterFactory.factoredConverters;
		DivaDbToCoraConverterSpy divaDbToCoraConverter = factoredConverters.get(0);
		assertNotNull(divaDbToCoraConverter.rowToConvert);
		assertEquals(factoredConverters.get(0).rowToConvert, tableFacade.returnedRows.get(0));
		assertEquals(factoredConverters.get(1).rowToConvert, tableFacade.returnedRows.get(1));
		assertEquals(factoredConverters.get(2).rowToConvert, tableFacade.returnedRows.get(2));
	}

	@Test
	public void testReadParentMultipleParentsFound() throws Exception {
		List<DataGroup> readParents = parentReader.read(TABLE_NAME, "567");
		assertEquals(readParents.size(), 3);

		assertEquals(readParents.get(0).getRepeatId(), "0");
		assertEquals(readParents.get(1).getRepeatId(), "1");
		assertEquals(readParents.get(2).getRepeatId(), "2");
	}

	// @Test
	// public void testReadUsingTableNameAndConditionsFactorDbReader() {
	// // recordReaderFactory.numToReturn = 3;
	//
	// Map<String, Object> conditions = new HashMap<>();
	// conditions.put("organisation_id", "567");
	//
	// List<DataGroup> result = parentReader.read(TABLE_NAME, conditions);
	//
	// assertTrue(recordReaderFactory.factorWasCalled);
	//
	// OrganisationMultipleRowsRecordReaderSpy factoredRecordReader = recordReaderFactory.factored;
	// assertEquals(factoredRecordReader.usedTableName, TABLE_NAME);
	// assertEquals(factoredRecordReader.usedConditions, conditions);
	//
	// assertDataSentFromReaderToConverter(factoredRecordReader, result, 0);
	// assertDataSentFromReaderToConverter(factoredRecordReader, result, 1);
	//
	// }

	// private void assertDataSentFromReaderToConverter(
	// OrganisationMultipleRowsRecordReaderSpy factoredRecordReader, List<DataGroup> result,
	// int index) {
	//
	// List<DivaDbToCoraConverterSpy> factoredConverters = converterFactory.factoredConverters;
	// DivaDbToCoraConverterSpy divaDbToCoraConverter = factoredConverters.get(index);
	//
	// List<Map<String, Object>> returnedList = factoredRecordReader.returnedList;
	// Map<String, Object> firstRowFromReader = returnedList.get(index);
	//
	// // Map<String, Object> firstMapSentToConverter = divaDbToCoraConverter.rowToConvert;
	// // assertEquals(firstRowFromReader, firstMapSentToConverter);
	// //
	// // DataGroup dataGroupReturnedFromConverter = divaDbToCoraConverter.convertedDbDataGroup;
	// // DataGroup dataGroupInResult = result.get(index);
	// // assertSame(dataGroupReturnedFromConverter, dataGroupInResult);
	// // assertEquals(dataGroupInResult.getRepeatId(), String.valueOf(index));
	// }
}
