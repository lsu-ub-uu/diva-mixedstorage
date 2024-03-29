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
import static org.testng.Assert.assertNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataAtomicFactory;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupFactory;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.data.DataRecordLinkFactory;
import se.uu.ub.cora.data.DataRecordLinkProvider;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.ConversionException;

public class DivaDbToCoraOrganisationPredecessorConverterTest {
	private DivaDbToCoraOrganisationPredecessorConverter converter;
	private RowSpy rowFromDb;

	private DataGroupFactory dataGroupFactorySpy;
	private DataAtomicFactory dataAtomicFactorySpy;
	private DataRecordLinkFactory dataRecordLinkFactory;

	@BeforeMethod
	public void beforeMethod() {
		dataGroupFactorySpy = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactorySpy);
		dataAtomicFactorySpy = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactorySpy);
		dataRecordLinkFactory = new DataRecordLinkFactorySpy();
		DataRecordLinkProvider.setDataRecordLinkFactory(dataRecordLinkFactory);
		rowFromDb = new RowSpy();
		rowFromDb.addColumnWithValue("organisation_id", 1234);
		rowFromDb.addColumnWithValue("organisation_predecessor_id", 7788);
		rowFromDb.addColumnWithValue("coraorganisationtype", "subOrganisation");
		converter = new DivaDbToCoraOrganisationPredecessorConverter();

	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation predecessor to Cora organisation predecessor: Map does not contain mandatory values for organisation id and predecessor id")
	public void testEmptyMap() {
		rowFromDb = new RowSpy();
		DataGroup organisation = converter.fromRow(rowFromDb);
		assertNull(organisation);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation predecessor to Cora organisation predecessor: Map does not contain mandatory values for organisation id and predecessor id")
	public void testMapWithEmptyValueForOrganisationIdThrowsError() {
		rowFromDb = new RowSpy();
		rowFromDb.addColumnWithValue("organisation_id", "");
		converter.fromRow(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation predecessor to Cora organisation predecessor: Map does not contain mandatory values for organisation id and predecessor id")
	public void testMapWithMissingPredecessorIdThrowsError() {
		rowFromDb = new RowSpy();
		rowFromDb.addColumnWithValue("organisation_id", 2134);
		converter.fromRow(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation predecessor to Cora organisation predecessor: Map does not contain mandatory values for organisation id and predecessor id")
	public void testMapWithEmptyValueForPredecessorIdThrowsError() {
		rowFromDb = new RowSpy();
		rowFromDb.addColumnWithValue("organisation_id", 1234);
		rowFromDb.addColumnWithValue("predecessorid", "");
		converter.fromRow(rowFromDb);
	}

	@Test
	public void testMinimalValuesReturnsDataGroupWithCorrectChildren() {
		DataGroup predecessor = converter.fromRow(rowFromDb);
		assertEquals(predecessor.getNameInData(), "earlierOrganisation");
		DataRecordLinkSpy linkedOrganisation = (DataRecordLinkSpy) predecessor
				.getFirstGroupWithNameInData("organisationLink");

		assertFalse(predecessor.containsChildWithNameInData("internalNote"));

		assertEquals(linkedOrganisation.recordType, "subOrganisation");
		assertEquals(linkedOrganisation.recordId, "7788");
	}

	@Test
	public void testMinimalValuesWithEmptyValueForDescriptionReturnsDataGroupWithCorrectChildren() {
		rowFromDb.addColumnWithValue("description", "");
		DataGroup predecessor = converter.fromRow(rowFromDb);
		assertEquals(predecessor.getNameInData(), "earlierOrganisation");
		DataRecordLinkSpy linkedOrganisation = (DataRecordLinkSpy) predecessor
				.getFirstGroupWithNameInData("organisationLink");
		assertFalse(predecessor.containsChildWithNameInData("internalNote"));

		assertEquals(linkedOrganisation.recordType, "subOrganisation");
		assertEquals(linkedOrganisation.recordId, "7788");
	}

	@Test
	public void testMinimalValuesWithNullValueForDescriptionReturnsDataGroupWithCorrectChildren() {
		rowFromDb.addColumnWithValue("description", null);
		DataGroup predecessor = converter.fromRow(rowFromDb);
		assertEquals(predecessor.getNameInData(), "earlierOrganisation");

		DataRecordLinkSpy linkedOrganisation = (DataRecordLinkSpy) predecessor
				.getFirstGroupWithNameInData("organisationLink");
		assertFalse(predecessor.containsChildWithNameInData("internalNote"));

		assertEquals(linkedOrganisation.recordType, "subOrganisation");
		assertEquals(linkedOrganisation.recordId, "7788");
	}

	@Test
	public void testCompleteValuesReturnsDataGroupWithCorrectChildren() {
		rowFromDb.addColumnWithValue("description", "some description text");
		DataGroup predecessor = converter.fromRow(rowFromDb);
		assertEquals(predecessor.getNameInData(), "earlierOrganisation");
		assertEquals(predecessor.getFirstAtomicValueWithNameInData("internalNote"),
				"some description text");

		DataRecordLinkSpy linkedOrganisation = (DataRecordLinkSpy) predecessor
				.getFirstGroupWithNameInData("organisationLink");
		assertEquals(linkedOrganisation.recordType, "subOrganisation");
		assertEquals(linkedOrganisation.recordId, "7788");

	}

	@Test
	public void testCoraOrganisationTypeTopOrganisation() {
		rowFromDb.addColumnWithValue("description", null);
		rowFromDb.addColumnWithValue("coraorganisationtype", "topOrganisation");

		DataGroup predecessor = converter.fromRow(rowFromDb);

		DataRecordLinkSpy linkedOrganisation = (DataRecordLinkSpy) predecessor
				.getFirstGroupWithNameInData("organisationLink");

		assertEquals(linkedOrganisation.recordType, "topOrganisation");
		assertEquals(linkedOrganisation.recordId, "7788");
	}

}
