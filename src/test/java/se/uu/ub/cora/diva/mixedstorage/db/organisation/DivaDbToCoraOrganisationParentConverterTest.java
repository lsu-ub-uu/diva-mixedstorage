/*
 * Copyright 2019, 2020, 2021 Uppsala University Library
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

public class DivaDbToCoraOrganisationParentConverterTest {
	private DivaDbToCoraOrganisationParentConverter converter;
	private RowSpy rowFromDb;
	private DataGroupFactory dataGroupFactory;
	private DataAtomicFactory dataAtomicFactory;
	private DataRecordLinkFactory dataRecordLinkFactory;

	@BeforeMethod
	public void beforeMethod() {
		dataGroupFactory = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactory);
		dataAtomicFactory = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactory);
		dataRecordLinkFactory = new DataRecordLinkFactorySpy();
		DataRecordLinkProvider.setDataRecordLinkFactory(dataRecordLinkFactory);
		rowFromDb = new RowSpy();
		rowFromDb.addColumnWithValue("organisation_id", 1567);

		rowFromDb.addColumnWithValue("organisation_parent_id", 52);
		rowFromDb.addColumnWithValue("coraorganisationtype", "subOrganisation");
		converter = new DivaDbToCoraOrganisationParentConverter();

	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation parent to Cora organisation parent: Map does not contain mandatory values for organisation id and parent id")
	public void testEmptyMap() {
		rowFromDb = new RowSpy();
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertNull(organisation);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation parent to Cora organisation parent: "
			+ "Map does not contain mandatory values for organisation id and parent id")
	public void testMapWithEmptyValueForOrganisationIdThrowsError() {
		rowFromDb = new RowSpy();
		rowFromDb.addColumnWithValue("organisation_id", "");
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation parent to Cora organisation parent:"
			+ " Map does not contain mandatory values for organisation id and parent id")
	public void testMapWithMissingParentIdThrowsError() {
		rowFromDb = new RowSpy();
		rowFromDb.addColumnWithValue("organisation_id", "someOrgId");
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation parent to Cora organisation parent: "
			+ "Map does not contain mandatory values for organisation id and parent id")
	public void testMapWithEmptyValueForParentIdThrowsError() {
		rowFromDb = new RowSpy();
		rowFromDb.addColumnWithValue("organisation_id", "someOrgId");
		rowFromDb.addColumnWithValue("organisation_parent_id", "");
		converter.fromMap(rowFromDb);
	}

	@Test
	public void testMinimalValuesReturnsDataGroupWithCorrectStructure() {
		DataGroup parent = converter.fromMap(rowFromDb);
		assertEquals(parent.getNameInData(), "parentOrganisation");
		DataRecordLinkSpy linkedOrganisation = (DataRecordLinkSpy) parent
				.getFirstGroupWithNameInData("organisationLink");

		assertEquals(linkedOrganisation.recordType, "subOrganisation");
		assertEquals(linkedOrganisation.recordId, "52");
	}

	@Test
	public void testCoraOrganisationTypeTopOrganisation() {
		rowFromDb.addColumnWithValue("coraorganisationtype", "topOrganisation");
		DataGroup parent = converter.fromMap(rowFromDb);
		assertEquals(parent.getNameInData(), "parentOrganisation");
		DataRecordLinkSpy linkedOrganisation = (DataRecordLinkSpy) parent
				.getFirstGroupWithNameInData("organisationLink");

		assertEquals(linkedOrganisation.recordType, "topOrganisation");
		assertEquals(linkedOrganisation.recordId, "52");
	}

}
