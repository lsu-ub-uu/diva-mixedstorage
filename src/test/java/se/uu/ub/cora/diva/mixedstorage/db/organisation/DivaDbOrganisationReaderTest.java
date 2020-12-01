/*
 * Copyright 2019 Uppsala University Library
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
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupFactory;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.mixedstorage.DataGroupFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.DbException;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverter;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterSpy;
import se.uu.ub.cora.diva.mixedstorage.db.MultipleRowDbToDataReaderSpy;
import se.uu.ub.cora.diva.mixedstorage.db.RecordReaderFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.RecordReaderSpy;

public class DivaDbOrganisationReaderTest {

	private static final String TYPE = "organisation";
	private DivaDbToCoraConverterFactorySpy converterFactory;
	private RecordReaderFactorySpy recordReaderFactory;
	private DivaDbOrganisationReader divaDbOrganisationReader;
	private DataGroupFactory dataGroupFactory;
	private DivaDbFactorySpy divaDbFactorySpy;

	@BeforeMethod
	public void BeforeMethod() {
		dataGroupFactory = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactory);
		converterFactory = new DivaDbToCoraConverterFactorySpy();
		recordReaderFactory = new RecordReaderFactorySpy();
		divaDbFactorySpy = new DivaDbFactorySpy();
		divaDbOrganisationReader = DivaDbOrganisationReader
				.usingRecordReaderFactoryAndConverterFactory(recordReaderFactory, converterFactory,
						divaDbFactorySpy);
	}

	@Test
	public void testReadOrgansiationFactorDbReader() throws Exception {
		divaDbOrganisationReader.read(TYPE, "567");
		assertTrue(recordReaderFactory.factorWasCalled);
	}

	@Test
	public void testReadOrgansiationTableRequestedFromReader() throws Exception {
		divaDbOrganisationReader.read(TYPE, "567");
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		assertEquals(recordReader.usedTableNames.get(0), "organisationview");
	}

	@Test
	public void testReadRootOrgansiationTableRequestedFromReader() throws Exception {
		divaDbOrganisationReader.read("rootOrganisation", "567");
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		assertEquals(recordReader.usedTableNames.get(0), "rootorganisationview");
	}

	@Test
	public void testReadTopOrgansiationTableRequestedFromReader() throws Exception {
		divaDbOrganisationReader.read("topOrganisation", "567");
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		assertEquals(recordReader.usedTableNames.get(0), "toporganisationview");
	}

	@Test
	public void testReadSubOrgansiationTableRequestedFromReader() throws Exception {
		divaDbOrganisationReader.read("subOrganisation", "567");
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		assertEquals(recordReader.usedTableNames.get(0), "suborganisationview");
	}

	@Test(expectedExceptions = DbException.class, expectedExceptionsMessageRegExp = ""
			+ "Record not found: notAnInt")
	public void testReadOrganisationConditionsForOrganisationTable() throws Exception {
		divaDbOrganisationReader.read(TYPE, "notAnInt");
	}

	@Test
	public void testReadOrganisationNotAnIntegerId() throws Exception {
		divaDbOrganisationReader.read(TYPE, "567");
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		Map<String, Object> conditions = recordReader.usedConditionsList.get(0);
		assertEquals(conditions.get("id"), 567);
	}

	@Test
	public void testReadOrganisationConverterIsFactored() throws Exception {
		divaDbOrganisationReader.read(TYPE, "567");
		DivaDbToCoraConverter divaDbToCoraConverter = converterFactory.factoredConverters.get(0);
		assertNotNull(divaDbToCoraConverter);
	}

	@Test
	public void testReadOrganisationConverterIsCalledWithDataFromDbStorage() throws Exception {
		divaDbOrganisationReader.read(TYPE, "567");
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		DivaDbToCoraConverterSpy divaDbToCoraConverter = converterFactory.factoredConverters.get(0);
		assertNotNull(divaDbToCoraConverter.mapToConvert);
		assertEquals(recordReader.returnedList.get(0), divaDbToCoraConverter.mapToConvert);
	}

	@Test
	public void testConvertedOrganisationIsReturned() throws Exception {
		DataGroup convertedOrganisation = divaDbOrganisationReader.read(TYPE, "567");
		DivaDbToCoraConverterSpy organisationConverter = converterFactory.factoredConverters.get(0);
		assertEquals(convertedOrganisation, organisationConverter.convertedDbDataGroup);
	}

	@Test
	public void testMultipleRowDbReaderIsFactoredCorrectlyForParent() throws Exception {
		divaDbOrganisationReader.read(TYPE, "567");
		MultipleRowDbToDataReader multipleDbToDataReader = divaDbFactorySpy.listOfFactoredMultiples
				.get(0);
		assertNotNull(multipleDbToDataReader);
		String usedType = divaDbFactorySpy.usedTypes.get(0);
		assertEquals(usedType, "divaOrganisationParent");
	}

	@Test
	public void testParentMultipleRowDbReaderIsCalledCorrectly() throws Exception {
		divaDbOrganisationReader.read(TYPE, "567");
		MultipleRowDbToDataReaderSpy multipleDbToDataReader = divaDbFactorySpy.listOfFactoredMultiples
				.get(0);
		// assertEquals(multipleDbToDataReader.usedType, "divaOrganisationParent");
		assertEquals(multipleDbToDataReader.usedId, "567");
	}

	@Test
	public void testNoParentAreAddedToOrganisation() {
		divaDbFactorySpy.returnEmptyResult = true;
		DataGroup organisation = divaDbOrganisationReader.read(TYPE, "567");

		assertFalse(organisation.containsChildWithNameInData("divaOrganisationParentChildFromSpy"));
	}

	@Test
	public void testConvertedParentAreAddedToOrganisation() throws Exception {
		DataGroup organisation = divaDbOrganisationReader.read(TYPE, "567");

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
		divaDbOrganisationReader.read(TYPE, "567");
		MultipleRowDbToDataReader multipleDbToDataReader = divaDbFactorySpy.listOfFactoredMultiples
				.get(1);
		assertNotNull(multipleDbToDataReader);
		String usedType = divaDbFactorySpy.usedTypes.get(1);
		assertEquals(usedType, "divaOrganisationPredecessor");
	}

	@Test
	public void testPredecessorMultipleRowDbReaderIsCalledCorrectly() throws Exception {
		divaDbOrganisationReader.read(TYPE, "567");
		MultipleRowDbToDataReaderSpy multipleDbToDataReader = divaDbFactorySpy.listOfFactoredMultiples
				.get(1);
		// assertEquals(multipleDbToDataReader.usedType, "divaOrganisationPredecessor");
		assertEquals(multipleDbToDataReader.usedId, "567");
	}

	@Test
	public void testNoPredecessorAreAddedToOrganisation() {
		divaDbFactorySpy.returnEmptyResult = true;
		DataGroup organisation = divaDbOrganisationReader.read(TYPE, "567");

		assertFalse(organisation
				.containsChildWithNameInData("divaOrganisationPredecessorChildFromSpy"));
	}

	@Test
	public void testConvertedPredecessorAreAddedToOrganisation() throws Exception {
		DataGroup organisation = divaDbOrganisationReader.read(TYPE, "567");

		MultipleRowDbToDataReaderSpy multipleDbToDataReader = divaDbFactorySpy.listOfFactoredMultiples
				.get(1);
		List<DataGroup> returnedListFromSpy = multipleDbToDataReader.returnedList;
		List<DataGroup> predecessorChildren = organisation
				.getAllGroupsWithNameInData("divaOrganisationPredecessorChildFromSpy");
		assertSame(predecessorChildren.get(0), returnedListFromSpy.get(0));
		assertSame(predecessorChildren.get(1), returnedListFromSpy.get(1));
	}

}
