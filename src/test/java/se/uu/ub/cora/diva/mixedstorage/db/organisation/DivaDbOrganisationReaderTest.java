/*
 * Copyright 2019, 2021 Uppsala University Library
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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupFactory;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.mixedstorage.DataGroupFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.db.DbException;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverter;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterSpy;
import se.uu.ub.cora.diva.mixedstorage.db.MultipleRowDbToDataReaderSpy;

public class DivaDbOrganisationReaderTest {

	private static final String TYPE = "organisation";
	private DivaDbToCoraConverterFactorySpy converterFactory;
	private DivaDbOrganisationReader divaDbOrganisationReader;
	private DataGroupFactory dataGroupFactory;
	private DivaDbFactorySpy divaDbFactorySpy;
	private TableFacadeSpy tableFacade;
	private SqlDatabaseFactorySpy sqlDatabaseFactory;

	@BeforeMethod
	public void BeforeMethod() {
		dataGroupFactory = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactory);
		converterFactory = new DivaDbToCoraConverterFactorySpy();
		divaDbFactorySpy = new DivaDbFactorySpy();
		sqlDatabaseFactory = new SqlDatabaseFactorySpy();
		tableFacade = new TableFacadeSpy();

		divaDbOrganisationReader = DivaDbOrganisationReader
				.usingRecordReaderFactoryAndConverterFactory(converterFactory, divaDbFactorySpy,
						sqlDatabaseFactory);
		// tableFacade = (TableFacadeSpy) divaDbOrganisationReader.getTableFacade();
	}

	@Test
	public void testInit() {
		assertSame(divaDbOrganisationReader.getTableFacade(),
				sqlDatabaseFactory.factoredTableFacade);
	}

	@Test
	public void testReadOrgansiationTableRequestedTableFacade() throws Exception {
		divaDbOrganisationReader.read(tableFacade, TYPE, "567");

		assertTrue(tableFacade.readOneRowForQueryWasCalled);

		TableQuerySpy tableQuery = (TableQuerySpy) tableFacade.tableQueries.get(0);
		assertSame(tableQuery, sqlDatabaseFactory.factoredTableQuery);

		assertEquals(sqlDatabaseFactory.tableName, "organisationview");
		assertEquals(tableQuery.conditions.get("id"), 567);

	}

	@Test
	public void testReadRootOrgansiationTableRequestedTableFacade() throws Exception {
		divaDbOrganisationReader.read(tableFacade, "rootOrganisation", "567");

		assertTrue(tableFacade.readOneRowForQueryWasCalled);

		TableQuerySpy tableQuery = (TableQuerySpy) tableFacade.tableQueries.get(0);
		assertSame(tableQuery, sqlDatabaseFactory.factoredTableQuery);

		assertEquals(sqlDatabaseFactory.tableName, "rootorganisationview");
		assertEquals(tableQuery.conditions.get("id"), 567);

	}

	@Test
	public void testReadTopOrgansiationTableRequestedFromReader() throws Exception {
		divaDbOrganisationReader.read(tableFacade, "topOrganisation", "567");

		assertTrue(tableFacade.readOneRowForQueryWasCalled);

		TableQuerySpy tableQuery = (TableQuerySpy) tableFacade.tableQueries.get(0);
		assertSame(tableQuery, sqlDatabaseFactory.factoredTableQuery);

		assertEquals(sqlDatabaseFactory.tableName, "toporganisationview");
		assertEquals(tableQuery.conditions.get("id"), 567);
	}

	@Test
	public void testReadSubOrgansiationTableRequestedFromReader() throws Exception {
		divaDbOrganisationReader.read(tableFacade, "subOrganisation", "567");
		assertTrue(tableFacade.readOneRowForQueryWasCalled);

		TableQuerySpy tableQuery = (TableQuerySpy) tableFacade.tableQueries.get(0);
		assertSame(tableQuery, sqlDatabaseFactory.factoredTableQuery);

		assertEquals(sqlDatabaseFactory.tableName, "suborganisationview");
		assertEquals(tableQuery.conditions.get("id"), 567);
	}

	@Test(expectedExceptions = DbException.class, expectedExceptionsMessageRegExp = ""
			+ "Record not found: notAnInt")
	public void testReadOrganisationConditionsForOrganisationTable() throws Exception {
		divaDbOrganisationReader.read(tableFacade, TYPE, "notAnInt");
	}

	@Test
	public void testReadOrganisationCorrectIntegerId() throws Exception {
		divaDbOrganisationReader.read(tableFacade, TYPE, "567");
		TableQuerySpy tableQuery = (TableQuerySpy) tableFacade.tableQueries.get(0);
		assertEquals(tableQuery.conditions.get("id"), 567);
	}

	@Test
	public void testReadOrganisationConverterIsFactored() throws Exception {
		divaDbOrganisationReader.read(tableFacade, TYPE, "567");
		DivaDbToCoraConverter divaDbToCoraConverter = converterFactory.factoredConverters.get(0);
		assertNotNull(divaDbToCoraConverter);
	}

	@Test
	public void testReadOrganisationConverterIsCalledWithDataFromDbStorage() throws Exception {
		divaDbOrganisationReader.read(tableFacade, TYPE, "567");
		DivaDbToCoraConverterSpy divaDbToCoraConverter = converterFactory.factoredConverters.get(0);

		assertNotNull(divaDbToCoraConverter.rowToConvert);
		assertEquals(tableFacade.returnedRows.get(0), divaDbToCoraConverter.rowToConvert);
	}

	@Test
	public void testConvertedOrganisationIsReturned() throws Exception {
		DataGroup convertedOrganisation = divaDbOrganisationReader.read(tableFacade, TYPE, "567");
		DivaDbToCoraConverterSpy organisationConverter = converterFactory.factoredConverters.get(0);
		assertEquals(convertedOrganisation, organisationConverter.convertedDbDataGroup);
	}

	@Test
	public void testMultipleRowDbReaderIsFactoredCorrectlyForParent() throws Exception {
		divaDbOrganisationReader.read(tableFacade, TYPE, "567");
		MultipleRowDbToDataReader multipleDbToDataReader = divaDbFactorySpy.listOfFactoredMultiples
				.get(0);
		assertNotNull(multipleDbToDataReader);
		String usedType = divaDbFactorySpy.usedTypes.get(0);
		assertEquals(usedType, "divaOrganisationParent");
	}

	@Test
	public void testParentMultipleRowDbReaderIsCalledCorrectly() throws Exception {
		divaDbOrganisationReader.read(tableFacade, TYPE, "567");
		MultipleRowDbToDataReaderSpy multipleDbToDataReader = divaDbFactorySpy.listOfFactoredMultiples
				.get(0);
		assertEquals(multipleDbToDataReader.usedId, "567");
		assertSame(multipleDbToDataReader.tableFacade, tableFacade);
	}

	@Test
	public void testNoParentAreAddedToOrganisation() {
		divaDbFactorySpy.returnEmptyResult = true;
		DataGroup organisation = divaDbOrganisationReader.read(tableFacade, TYPE, "567");

		assertFalse(organisation.containsChildWithNameInData("divaOrganisationParentChildFromSpy"));
	}

	@Test
	public void testConvertedParentAreAddedToOrganisation() throws Exception {
		DataGroup organisation = divaDbOrganisationReader.read(tableFacade, TYPE, "567");

		MultipleRowDbToDataReaderSpy multipleDbToDataReader = divaDbFactorySpy.listOfFactoredMultiples
				.get(0);
		List<DataGroup> returnedListFromSpy = multipleDbToDataReader.returnedList;
		List<DataGroup> parentChildren = organisation
				.getAllGroupsWithNameInData("divaOrganisationParentChildFromSpy");
		assertSame(parentChildren.get(0), returnedListFromSpy.get(0));
		assertSame(parentChildren.get(1), returnedListFromSpy.get(1));
	}

	@Test
	public void testMultipleRowDbReaderIsFactoredCorrectlyForPredecessor() throws Exception {
		divaDbOrganisationReader.read(tableFacade, TYPE, "567");
		MultipleRowDbToDataReader multipleDbToDataReader = divaDbFactorySpy.listOfFactoredMultiples
				.get(1);
		assertNotNull(multipleDbToDataReader);
		String usedType = divaDbFactorySpy.usedTypes.get(1);
		assertEquals(usedType, "divaOrganisationPredecessor");
	}

	@Test
	public void testPredecessorMultipleRowDbReaderIsCalledCorrectly() throws Exception {
		divaDbOrganisationReader.read(tableFacade, TYPE, "567");
		MultipleRowDbToDataReaderSpy multipleDbToDataReader = divaDbFactorySpy.listOfFactoredMultiples
				.get(1);
		assertEquals(multipleDbToDataReader.usedId, "567");
		assertSame(multipleDbToDataReader.tableFacade, tableFacade);
	}

	@Test
	public void testNoPredecessorAreAddedToOrganisation() {
		divaDbFactorySpy.returnEmptyResult = true;
		DataGroup organisation = divaDbOrganisationReader.read(tableFacade, TYPE, "567");

		assertFalse(organisation
				.containsChildWithNameInData("divaOrganisationPredecessorChildFromSpy"));
	}

	@Test
	public void testConvertedPredecessorAreAddedToOrganisation() throws Exception {
		DataGroup organisation = divaDbOrganisationReader.read(tableFacade, TYPE, "567");

		MultipleRowDbToDataReaderSpy multipleDbToDataReader = divaDbFactorySpy.listOfFactoredMultiples
				.get(1);
		List<DataGroup> returnedListFromSpy = multipleDbToDataReader.returnedList;
		List<DataGroup> predecessorChildren = organisation
				.getAllGroupsWithNameInData("divaOrganisationPredecessorChildFromSpy");
		assertSame(predecessorChildren.get(0), returnedListFromSpy.get(0));
		assertSame(predecessorChildren.get(1), returnedListFromSpy.get(1));
	}

	@Test
	public void testReadSubOrgansiationRepeatIdNOTRemovedForParent() throws Exception {
		divaDbOrganisationReader.read(tableFacade, "subOrganisation", "567");

		MultipleRowDbToDataReaderSpy multipleDbToDataReader = divaDbFactorySpy.listOfFactoredMultiples
				.get(0);
		List<DataGroup> returnedList = multipleDbToDataReader.returnedList;
		DataGroupSpy returnedParent = (DataGroupSpy) returnedList.get(0);
		assertFalse(returnedParent.setRepeatIdWasCalled);
	}

	@Test
	public void testReadTopOrgansiationRemovedRepeatIdForParent() throws Exception {
		divaDbOrganisationReader.read(tableFacade, "topOrganisation", "567");

		MultipleRowDbToDataReaderSpy multipleDbToDataReader = divaDbFactorySpy.listOfFactoredMultiples
				.get(0);
		List<DataGroup> returnedList = multipleDbToDataReader.returnedList;
		DataGroupSpy returnedParent = (DataGroupSpy) returnedList.get(0);
		assertTrue(returnedParent.setRepeatIdWasCalled);
		assertNull(returnedParent.getRepeatId());
	}

}
