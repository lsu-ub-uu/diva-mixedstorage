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
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterSpy;

public class MultipleRowDbToDataPredecessorReaderTest {

	private static final String TABLE_NAME = "divaOrganisationPredecessor";
	private DivaDbToCoraConverterFactorySpy converterFactory;
	private RecordOrgansationParentReaderFactorySpy recordReaderFactory;
	private MultipleRowDbToDataReader predecessorReader;

	@BeforeMethod
	public void BeforeMethod() {
		converterFactory = new DivaDbToCoraConverterFactorySpy();
		recordReaderFactory = new RecordOrgansationParentReaderFactorySpy();
		predecessorReader = new MultipleRowDbToDataPredecessorReader(recordReaderFactory,
				converterFactory);
	}

	@Test
	public void testReadPredecessorFactorDbReader() {
		predecessorReader.read(TABLE_NAME, "567");
		assertTrue(recordReaderFactory.factorWasCalled);
	}

	@Test
	public void testReadPredecessorTableRequestedFromReader() {
		predecessorReader.read(TABLE_NAME, "567");
		OrganisationMultipleRowsRecordReaderSpy recordReader = recordReaderFactory.factored;
		assertEquals(recordReader.usedTableName, TABLE_NAME);
	}

	@Test
	public void testReadPredecessorConditionsForPredecessorTable() throws Exception {
		predecessorReader.read(TABLE_NAME, "567");
		OrganisationMultipleRowsRecordReaderSpy recordReader = recordReaderFactory.factored;
		Map<String, Object> conditions = recordReader.usedConditions;
		assertEquals(conditions.get("organisation_id"), 567);
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
		recordReaderFactory.numToReturn = 0;
		List<DataGroup> readPredecessors = predecessorReader.read(TABLE_NAME, "567");
		assertTrue(readPredecessors.isEmpty());
		assertTrue(converterFactory.factoredConverters.isEmpty());
	}

	@Test
	public void testPredecessorConverterIsCalledWithReadPredecessorFromDbStorage()
			throws Exception {
		predecessorReader.read(TABLE_NAME, "567");
		OrganisationMultipleRowsRecordReaderSpy recordReader = recordReaderFactory.factored;
		DivaDbToCoraConverterSpy divaDbToCoraConverter = converterFactory.factoredConverters.get(0);
		assertNotNull(divaDbToCoraConverter.rowToConvert);
		assertEquals(recordReader.returnedList.get(0), divaDbToCoraConverter.rowToConvert);
	}

	@Test
	public void testPredecessorConverterIsCalledWithMultipleReadPredecessorFromDbStorage()
			throws Exception {
		recordReaderFactory.numToReturn = 3;
		predecessorReader.read(TABLE_NAME, "567");

		OrganisationMultipleRowsRecordReaderSpy recordReader = recordReaderFactory.factored;
		List<DivaDbToCoraConverterSpy> factoredConverters = converterFactory.factoredConverters;
		DivaDbToCoraConverterSpy divaDbToCoraConverter = factoredConverters.get(0);
		assertNotNull(divaDbToCoraConverter.rowToConvert);
		assertEquals(recordReader.returnedList.get(0), factoredConverters.get(0).rowToConvert);
		assertEquals(recordReader.returnedList.get(1), factoredConverters.get(1).rowToConvert);
		assertEquals(recordReader.returnedList.get(2), factoredConverters.get(2).rowToConvert);
	}

	@Test
	public void testReadPredecessorMultiplePredecessorsFound() throws Exception {
		recordReaderFactory.numToReturn = 3;
		List<DataGroup> readPredecessors = predecessorReader.read(TABLE_NAME, "567");
		assertEquals(readPredecessors.size(), 3);

		assertEquals(readPredecessors.get(0).getRepeatId(), "0");
		assertEquals(readPredecessors.get(1).getRepeatId(), "1");
		assertEquals(readPredecessors.get(2).getRepeatId(), "2");
	}
}
