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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterSpy;

public class MultipleRowDbToDataParentReaderTest {

	private static final String TABLE_NAME = "divaOrganisationParent";
	private DivaDbToCoraConverterFactorySpy converterFactory;
	private RecordOrgansationParentReaderFactorySpy recordReaderFactory;
	private MultipleRowDbToDataReader parentReader;

	@BeforeMethod
	public void BeforeMethod() {
		converterFactory = new DivaDbToCoraConverterFactorySpy();
		recordReaderFactory = new RecordOrgansationParentReaderFactorySpy();
		parentReader = new MultipleRowDbToDataParentReader(recordReaderFactory, converterFactory);
	}

	@Test
	public void testReadParentFactorDbReader() {
		parentReader.read(TABLE_NAME, "567");
		assertTrue(recordReaderFactory.factorWasCalled);
	}

	@Test
	public void testReadParentTableRequestedFromReader() {
		parentReader.read(TABLE_NAME, "567");
		OrganisationMultipleRowsRecordReaderSpy recordReader = recordReaderFactory.factored;
		assertEquals(recordReader.usedTableName, TABLE_NAME);
	}

	@Test
	public void testReadParentConditionsForParentTable() throws Exception {
		parentReader.read(TABLE_NAME, "567");
		OrganisationMultipleRowsRecordReaderSpy recordReader = recordReaderFactory.factored;
		Map<String, Object> conditions = recordReader.usedConditions;
		assertEquals(conditions.get("organisation_id"), "567");
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
		recordReaderFactory.numToReturn = 0;
		List<DataGroup> readParents = parentReader.read(TABLE_NAME, "567");
		assertTrue(readParents.isEmpty());
		assertTrue(converterFactory.factoredConverters.isEmpty());
	}

	@Test
	public void testParentConverterIsCalledWithReadParentFromDbStorage() throws Exception {
		parentReader.read(TABLE_NAME, "567");
		OrganisationMultipleRowsRecordReaderSpy recordReader = recordReaderFactory.factored;
		DivaDbToCoraConverterSpy divaDbToCoraConverter = converterFactory.factoredConverters.get(0);
		assertNotNull(divaDbToCoraConverter.mapToConvert);
		assertEquals(recordReader.returnedList.get(0), divaDbToCoraConverter.mapToConvert);
	}

	@Test
	public void testParentConverterIsCalledWithMultipleReadParentFromDbStorage() throws Exception {
		recordReaderFactory.numToReturn = 3;
		parentReader.read(TABLE_NAME, "567");

		OrganisationMultipleRowsRecordReaderSpy recordReader = recordReaderFactory.factored;
		List<DivaDbToCoraConverterSpy> factoredConverters = converterFactory.factoredConverters;
		DivaDbToCoraConverterSpy divaDbToCoraConverter = factoredConverters.get(0);
		assertNotNull(divaDbToCoraConverter.mapToConvert);
		assertEquals(recordReader.returnedList.get(0), factoredConverters.get(0).mapToConvert);
		assertEquals(recordReader.returnedList.get(1), factoredConverters.get(1).mapToConvert);
		assertEquals(recordReader.returnedList.get(2), factoredConverters.get(2).mapToConvert);
	}

	@Test
	public void testReadParentMultipleParentsFound() throws Exception {
		recordReaderFactory.numToReturn = 3;
		List<DataGroup> readParents = parentReader.read(TABLE_NAME, "567");
		assertEquals(readParents.size(), 3);

		assertEquals(readParents.get(0).getRepeatId(), "0");
		assertEquals(readParents.get(1).getRepeatId(), "1");
		assertEquals(readParents.get(2).getRepeatId(), "2");
	}

	@Test
	public void testReadUsingTableNameAndConditionsFactorDbReader() {
		recordReaderFactory.numToReturn = 3;

		Map<String, Object> conditions = new HashMap<>();
		conditions.put("organisation_id", "567");

		List<DataGroup> result = parentReader.read(TABLE_NAME, conditions);

		assertTrue(recordReaderFactory.factorWasCalled);

		OrganisationMultipleRowsRecordReaderSpy factoredRecordReader = recordReaderFactory.factored;
		assertEquals(factoredRecordReader.usedTableName, TABLE_NAME);
		assertEquals(factoredRecordReader.usedConditions, conditions);

		assertDataSentFromReaderToConverter(factoredRecordReader, result, 0);
		assertDataSentFromReaderToConverter(factoredRecordReader, result, 1);

	}

	private void assertDataSentFromReaderToConverter(
			OrganisationMultipleRowsRecordReaderSpy factoredRecordReader, List<DataGroup> result,
			int index) {

		List<DivaDbToCoraConverterSpy> factoredConverters = converterFactory.factoredConverters;
		DivaDbToCoraConverterSpy divaDbToCoraConverter = factoredConverters.get(index);

		List<Map<String, Object>> returnedList = factoredRecordReader.returnedList;
		Map<String, Object> firstRowFromReader = returnedList.get(index);

		Map<String, Object> firstMapSentToConverter = divaDbToCoraConverter.mapToConvert;
		assertEquals(firstRowFromReader, firstMapSentToConverter);

		DataGroup dataGroupReturnedFromConverter = divaDbToCoraConverter.convertedDbDataGroup;
		DataGroup dataGroupInResult = result.get(index);
		assertSame(dataGroupReturnedFromConverter, dataGroupInResult);
		assertEquals(dataGroupInResult.getRepeatId(), String.valueOf(index));
	}
}
