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
import static org.testng.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

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
	private Map<String, Object> rowFromDb;

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
		rowFromDb = new HashMap<>();
		rowFromDb.put("organisation_id", "someOrgId");
		rowFromDb.put("organisation_predecessor_id", 7788);
		converter = new DivaDbToCoraOrganisationPredecessorConverter();

	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation predecessor to Cora organisation predecessor: Map does not contain mandatory values for organisation id and predecessor id")
	public void testEmptyMap() {
		rowFromDb = new HashMap<>();
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertNull(organisation);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation predecessor to Cora organisation predecessor: Map does not contain mandatory values for organisation id and predecessor id")
	public void testMapWithEmptyValueForOrganisationIdThrowsError() {
		rowFromDb = new HashMap<>();
		rowFromDb.put("organisation_id", "");
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation predecessor to Cora organisation predecessor: Map does not contain mandatory values for organisation id and predecessor id")
	public void testMapWithMissingPredecessorIdThrowsError() {
		rowFromDb = new HashMap<>();
		rowFromDb.put("organisation_id", "someOrgId");
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation predecessor to Cora organisation predecessor: Map does not contain mandatory values for organisation id and predecessor id")
	public void testMapWithEmptyValueForPredecessorIdThrowsError() {
		rowFromDb = new HashMap<>();
		rowFromDb.put("organisation_id", "someOrgId");
		rowFromDb.put("predecessorid", "");
		converter.fromMap(rowFromDb);
	}

	@Test
	public void testMinimalValuesReturnsDataGroupWithCorrectChildren() {
		DataGroup predecessor = converter.fromMap(rowFromDb);
		assertEquals(predecessor.getNameInData(), "formerName");
		DataRecordLinkSpy linkedOrganisation = (DataRecordLinkSpy) predecessor
				.getFirstGroupWithNameInData("organisationLink");

		assertFalse(predecessor.containsChildWithNameInData("organisationComment"));

		assertEquals(linkedOrganisation.recordType, "organisation");
		assertEquals(linkedOrganisation.recordId, "7788");
	}

	@Test
	public void testMinimalValuesWithEmptyValueForDescriptionReturnsDataGroupWithCorrectChildren() {
		rowFromDb.put("description", "");
		DataGroup predecessor = converter.fromMap(rowFromDb);
		assertEquals(predecessor.getNameInData(), "formerName");
		DataRecordLinkSpy linkedOrganisation = (DataRecordLinkSpy) predecessor
				.getFirstGroupWithNameInData("organisationLink");
		assertFalse(predecessor.containsChildWithNameInData("organisationComment"));

		assertEquals(linkedOrganisation.recordType, "organisation");
		assertEquals(linkedOrganisation.recordId, "7788");
	}

	@Test
	public void testMinimalValuesWithNullValueForDescriptionReturnsDataGroupWithCorrectChildren() {
		rowFromDb.put("description", null);
		DataGroup predecessor = converter.fromMap(rowFromDb);
		assertEquals(predecessor.getNameInData(), "formerName");

		DataRecordLinkSpy linkedOrganisation = (DataRecordLinkSpy) predecessor
				.getFirstGroupWithNameInData("organisationLink");
		assertFalse(predecessor.containsChildWithNameInData("organisationComment"));

		assertEquals(linkedOrganisation.recordType, "organisation");
		assertEquals(linkedOrganisation.recordId, "7788");
	}

	@Test
	public void testCompleteValuesReturnsDataGroupWithCorrectChildren() {
		rowFromDb.put("description", "some description text");
		DataGroup predecessor = converter.fromMap(rowFromDb);
		assertEquals(predecessor.getNameInData(), "formerName");
		assertEquals(predecessor.getFirstAtomicValueWithNameInData("organisationComment"),
				"some description text");

		DataRecordLinkSpy linkedOrganisation = (DataRecordLinkSpy) predecessor
				.getFirstGroupWithNameInData("organisationLink");
		assertEquals(linkedOrganisation.recordType, "organisation");
		assertEquals(linkedOrganisation.recordId, "7788");

	}

}
