/*
 * Copyright 2018, 2019, 2020 Uppsala University Library
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
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.ConversionException;

public class DivaDbToCoraOrganisationConverterTest {

	private DivaDbToCoraOrganisationConverter converter;
	private RowSpy rowFromDb;
	private DataGroupFactorySpy dataGroupFactorySpy;
	private DataAtomicFactorySpy dataAtomicFactorySpy;
	private DefaultConverterFactorySpy converterFactory;

	@BeforeMethod
	public void beforeMethod() {
		converterFactory = new DefaultConverterFactorySpy();
		dataGroupFactorySpy = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactorySpy);
		dataAtomicFactorySpy = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactorySpy);
		rowFromDb = new RowSpy();
		rowFromDb.addColumnWithValue("id", 57);
		rowFromDb.addColumnWithValue("type_code", "unit");
		rowFromDb.addColumnWithValue("top_level", false);
		converter = new DivaDbToCoraOrganisationConverter(converterFactory);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation to Cora organisation: Map does not contain value for id")
	public void testEmptyMap() {
		rowFromDb = new RowSpy();
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertNull(organisation);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation to Cora organisation: Map does not contain value for id")
	public void testMapWithEmptyValueThrowsError() {
		rowFromDb = new RowSpy();
		rowFromDb.addColumnWithValue("id", "");
		// rowFromDb.put("id", "");
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation to Cora organisation: Map does not contain value for id")
	public void testMapWithNonEmptyValueANDEmptyValueThrowsError() {
		rowFromDb = new RowSpy();
		rowFromDb.addColumnWithValue("defaultname", "someName");
		rowFromDb.addColumnWithValue("id", "");
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation to Cora organisation: Map does not contain value for id")
	public void mapDoesNotContainOrganisationIdValue() {
		rowFromDb = new RowSpy();
		rowFromDb.addColumnWithValue("defaultname", "someName");
		converter.fromMap(rowFromDb);
	}

	@Test
	public void testConverterIsFactored() {
		DataGroup organisation = converter.fromMap(rowFromDb);

		DefaultConverterSpy factoredConverter = (DefaultConverterSpy) converterFactory.factoredConverter;
		assertSame(factoredConverter.rowFromDb, rowFromDb);
		assertSame(organisation, factoredConverter.returnedDataGroup);

	}

	@Test
	public void testRootDoesNotIncludeMoreThanDefault() {
		rowFromDb.addColumnWithValue("type_code", "root");
		rowFromDb.addColumnWithValue("organisation_code", "someCode");
		rowFromDb.addColumnWithValue("organisation_homepage", "www.someaddress.com");
		rowFromDb.addColumnWithValue("show_in_defence", true);
		rowFromDb.addColumnWithValue("orgnumber", "33445566");
		rowFromDb.addColumnWithValue("not_eligible", true);
		DataGroup organisation = converter.fromMap(rowFromDb);
		DefaultConverterSpy factoredConverter = (DefaultConverterSpy) converterFactory.factoredConverter;
		assertSame(organisation, factoredConverter.returnedDataGroup);

		assertFalse(organisation.containsChildWithNameInData("address"));
		assertFalse(organisation.containsChildWithNameInData("organisationCode"));
		assertFalse(organisation.containsChildWithNameInData("URL"));
		assertFalse(organisation.containsChildWithNameInData("doctoralDegreeGrantor"));
		assertFalse(organisation.containsChildWithNameInData("organisationNumber"));
		assertFalse(organisation.containsChildWithNameInData("organisationType"));

	}

	@Test
	public void testTopLevelIncludeCorrectChildren() {
		rowFromDb.addColumnWithValue("type_code", "university");
		rowFromDb.addColumnWithValue("top_level", true);
		rowFromDb.addColumnWithValue("organisation_code", "someCode");
		rowFromDb.addColumnWithValue("organisation_homepage", "www.someaddress.com");
		rowFromDb.addColumnWithValue("show_in_defence", true);
		rowFromDb.addColumnWithValue("orgnumber", "33445566");
		rowFromDb.addColumnWithValue("country_code", "fi");
		rowFromDb.addColumnWithValue("city", "Uppsala");
		rowFromDb.addColumnWithValue("street", "Övre slottsgatan 1");
		rowFromDb.addColumnWithValue("postbox", "Box5435");
		rowFromDb.addColumnWithValue("postnumber", "345 34");
		rowFromDb.addColumnWithValue("not_eligible", false);
		rowFromDb.addColumnWithValue("type_code", "university");
		DataGroup organisation = converter.fromMap(rowFromDb);
		DefaultConverterSpy factoredConverter = (DefaultConverterSpy) converterFactory.factoredConverter;
		assertSame(organisation, factoredConverter.returnedDataGroup);

		assertCorrectCompleteAddress(organisation);

		assertEquals(organisation.getFirstAtomicValueWithNameInData("organisationCode"),
				"someCode");
		assertEquals(organisation.getFirstAtomicValueWithNameInData("URL"), "www.someaddress.com");
		assertEquals(organisation.getFirstAtomicValueWithNameInData("doctoralDegreeGrantor"),
				"yes");
		assertEquals(organisation.getFirstAtomicValueWithNameInData("organisationNumber"),
				"33445566");
		assertEquals(organisation.getFirstAtomicValueWithNameInData("organisationType"),
				"university");

	}

	private void assertCorrectCompleteAddress(DataGroup organisation) {
		assertTrue(organisation.containsChildWithNameInData("address"));
		DataGroup addressGroup = organisation.getFirstGroupWithNameInData("address");
		assertEquals(addressGroup.getFirstAtomicValueWithNameInData("country"), "FI");
		assertEquals(addressGroup.getFirstAtomicValueWithNameInData("city"), "Uppsala");
		assertEquals(addressGroup.getFirstAtomicValueWithNameInData("street"),
				"Övre slottsgatan 1");
		assertEquals(addressGroup.getFirstAtomicValueWithNameInData("box"), "Box5435");
		assertEquals(addressGroup.getFirstAtomicValueWithNameInData("postcode"), "345 34");
	}

	@Test
	public void testTopLevelDoesNotIncludeAddressWhenNoPartOfAddressInDb() {
		rowFromDb.addColumnWithValue("type_code", "university");
		rowFromDb.addColumnWithValue("top_level", true);
		rowFromDb.addColumnWithValue("organisation_code", "someCode");
		rowFromDb.addColumnWithValue("organisation_homepage", "www.someaddress.com");
		rowFromDb.addColumnWithValue("show_in_defence", true);
		rowFromDb.addColumnWithValue("orgnumber", "33445566");
		rowFromDb.addColumnWithValue("street", "");
		DataGroup organisation = converter.fromMap(rowFromDb);
		DefaultConverterSpy factoredConverter = (DefaultConverterSpy) converterFactory.factoredConverter;
		assertSame(organisation, factoredConverter.returnedDataGroup);

		assertFalse(organisation.containsChildWithNameInData("address"));

	}

	@Test
	public void testTopLevelDoesNotIncludeChildrenNotPresentInDb() {
		rowFromDb.addColumnWithValue("type_code", "university");
		rowFromDb.addColumnWithValue("top_level", true);
		DataGroup organisation = converter.fromMap(rowFromDb);
		DefaultConverterSpy factoredConverter = (DefaultConverterSpy) converterFactory.factoredConverter;
		assertSame(organisation, factoredConverter.returnedDataGroup);

		assertFalse(organisation.containsChildWithNameInData("organisationCode"));
		assertFalse(organisation.containsChildWithNameInData("URL"));
		assertFalse(organisation.containsChildWithNameInData("doctoralDegreeGrantor"));
		assertFalse(organisation.containsChildWithNameInData("organisationNumber"));

	}

	@Test
	public void testDoctoralDegreeGrantorFalse() {
		rowFromDb.addColumnWithValue("type_code", "university");
		rowFromDb.addColumnWithValue("top_level", true);
		rowFromDb.addColumnWithValue("organisation_code", "someCode");
		rowFromDb.addColumnWithValue("organisation_homepage", "www.someaddress.com");
		rowFromDb.addColumnWithValue("show_in_defence", false);
		rowFromDb.addColumnWithValue("orgnumber", "33445566");
		rowFromDb.addColumnWithValue("not_eligible", false);
		DataGroup organisation = converter.fromMap(rowFromDb);
		DefaultConverterSpy factoredConverter = (DefaultConverterSpy) converterFactory.factoredConverter;
		assertSame(organisation, factoredConverter.returnedDataGroup);

		assertEquals(organisation.getFirstAtomicValueWithNameInData("doctoralDegreeGrantor"), "no");

	}

	@Test
	public void testSubOrganisationIncludeCorrectChildren() {
		rowFromDb.addColumnWithValue("type_code", "unit");
		rowFromDb.addColumnWithValue("top_level", false);
		rowFromDb.addColumnWithValue("organisation_code", "someCode");
		rowFromDb.addColumnWithValue("organisation_homepage", "www.someaddress.com");
		rowFromDb.addColumnWithValue("show_in_defence", true);
		rowFromDb.addColumnWithValue("orgnumber", "33445566");
		rowFromDb.addColumnWithValue("country_code", "fi");
		rowFromDb.addColumnWithValue("city", "Uppsala");
		rowFromDb.addColumnWithValue("street", "Övre slottsgatan 1");
		rowFromDb.addColumnWithValue("postbox", "Box5435");
		rowFromDb.addColumnWithValue("postnumber", "345 34");
		rowFromDb.addColumnWithValue("not_eligible", false);
		DataGroup organisation = converter.fromMap(rowFromDb);
		DefaultConverterSpy factoredConverter = (DefaultConverterSpy) converterFactory.factoredConverter;
		assertSame(organisation, factoredConverter.returnedDataGroup);

		assertFalse(organisation.containsChildWithNameInData("doctoralDegreeGrantor"));
		assertFalse(organisation.containsChildWithNameInData("organisationNumber"));

		assertCorrectCompleteAddress(organisation);
		assertEquals(organisation.getFirstAtomicValueWithNameInData("organisationCode"),
				"someCode");
		assertEquals(organisation.getFirstAtomicValueWithNameInData("URL"), "www.someaddress.com");
		assertEquals(organisation.getFirstAtomicValueWithNameInData("organisationType"), "unit");

	}

}
