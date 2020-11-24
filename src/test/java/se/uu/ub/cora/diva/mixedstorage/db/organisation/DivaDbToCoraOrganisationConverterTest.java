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

import java.util.HashMap;
import java.util.Map;

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
	private Map<String, Object> rowFromDb;
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
		rowFromDb = new HashMap<>();
		rowFromDb.put("id", 57);
		rowFromDb.put("type_code", "unit");
		rowFromDb.put("top_level", false);
		converter = new DivaDbToCoraOrganisationConverter(converterFactory);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation to Cora organisation: Map does not contain value for id")
	public void testEmptyMap() {
		rowFromDb = new HashMap<>();
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertNull(organisation);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation to Cora organisation: Map does not contain value for id")
	public void testMapWithEmptyValueThrowsError() {
		rowFromDb = new HashMap<>();
		rowFromDb.put("id", "");
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation to Cora organisation: Map does not contain value for id")
	public void testMapWithNonEmptyValueANDEmptyValueThrowsError() {
		Map<String, Object> rowFromDb = new HashMap<>();
		rowFromDb.put("defaultname", "someName");
		rowFromDb.put("id", "");
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation to Cora organisation: Map does not contain value for id")
	public void mapDoesNotContainOrganisationIdValue() {
		rowFromDb = new HashMap<>();
		rowFromDb.put("defaultname", "someName");
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
		rowFromDb.put("type_code", "root");
		rowFromDb.put("organisation_code", "someCode");
		rowFromDb.put("organisation_homepage", "www.someaddress.com");
		rowFromDb.put("show_in_defence", true);
		rowFromDb.put("orgnumber", "33445566");
		rowFromDb.put("not_eligible", true);
		DataGroup organisation = converter.fromMap(rowFromDb);
		DefaultConverterSpy factoredConverter = (DefaultConverterSpy) converterFactory.factoredConverter;
		assertSame(organisation, factoredConverter.returnedDataGroup);

		assertFalse(organisation.containsChildWithNameInData("address"));
		assertFalse(organisation.containsChildWithNameInData("organisationCode"));
		assertFalse(organisation.containsChildWithNameInData("URL"));
		assertFalse(organisation.containsChildWithNameInData("doctoralDegreeGrantor"));
		assertFalse(organisation.containsChildWithNameInData("organisationNumber"));
		assertFalse(organisation.containsChildWithNameInData("eligible"));

	}

	@Test
	public void testTopLevelIncludeCorrectChildren() {
		rowFromDb.put("type_code", "university");
		rowFromDb.put("top_level", true);
		rowFromDb.put("organisation_code", "someCode");
		rowFromDb.put("organisation_homepage", "www.someaddress.com");
		rowFromDb.put("show_in_defence", true);
		rowFromDb.put("orgnumber", "33445566");
		rowFromDb.put("country_code", "fi");
		rowFromDb.put("city", "Uppsala");
		rowFromDb.put("street", "Övre slottsgatan 1");
		rowFromDb.put("postbox", "Box5435");
		rowFromDb.put("postnumber", "345 34");
		rowFromDb.put("not_eligible", false);
		DataGroup organisation = converter.fromMap(rowFromDb);
		DefaultConverterSpy factoredConverter = (DefaultConverterSpy) converterFactory.factoredConverter;
		assertSame(organisation, factoredConverter.returnedDataGroup);

		assertTrue(organisation.containsChildWithNameInData("address"));
		assertTrue(organisation.containsChildWithNameInData("organisationCode"));
		assertTrue(organisation.containsChildWithNameInData("URL"));
		assertTrue(organisation.containsChildWithNameInData("doctoralDegreeGrantor"));
		assertEquals(organisation.getFirstAtomicValueWithNameInData("doctoralDegreeGrantor"),
				"yes");
		assertTrue(organisation.containsChildWithNameInData("organisationNumber"));
		assertTrue(organisation.containsChildWithNameInData("eligible"));

	}

	@Test
	public void testTopLevelDoesNotIncludeAddressWhenNoPartOfAddressInDb() {
		rowFromDb.put("type_code", "university");
		rowFromDb.put("top_level", true);
		rowFromDb.put("organisation_code", "someCode");
		rowFromDb.put("organisation_homepage", "www.someaddress.com");
		rowFromDb.put("show_in_defence", true);
		rowFromDb.put("orgnumber", "33445566");
		rowFromDb.put("street", "");
		DataGroup organisation = converter.fromMap(rowFromDb);
		DefaultConverterSpy factoredConverter = (DefaultConverterSpy) converterFactory.factoredConverter;
		assertSame(organisation, factoredConverter.returnedDataGroup);

		assertFalse(organisation.containsChildWithNameInData("address"));

	}

	@Test
	public void testTopLevelDoesNotIncludeChildrenNotPresentInDb() {
		rowFromDb.put("type_code", "university");
		rowFromDb.put("top_level", true);
		DataGroup organisation = converter.fromMap(rowFromDb);
		DefaultConverterSpy factoredConverter = (DefaultConverterSpy) converterFactory.factoredConverter;
		assertSame(organisation, factoredConverter.returnedDataGroup);

		assertFalse(organisation.containsChildWithNameInData("organisationCode"));
		assertFalse(organisation.containsChildWithNameInData("URL"));
		assertFalse(organisation.containsChildWithNameInData("doctoralDegreeGrantor"));
		assertFalse(organisation.containsChildWithNameInData("organisationNumber"));
		assertFalse(organisation.containsChildWithNameInData("eligible"));

	}

	@Test
	public void testDoctoralDegreeGrantorFalse() {
		rowFromDb.put("type_code", "university");
		rowFromDb.put("top_level", true);
		rowFromDb.put("organisation_code", "someCode");
		rowFromDb.put("organisation_homepage", "www.someaddress.com");
		rowFromDb.put("show_in_defence", false);
		rowFromDb.put("orgnumber", "33445566");
		rowFromDb.put("not_eligible", false);
		DataGroup organisation = converter.fromMap(rowFromDb);
		DefaultConverterSpy factoredConverter = (DefaultConverterSpy) converterFactory.factoredConverter;
		assertSame(organisation, factoredConverter.returnedDataGroup);

		assertEquals(organisation.getFirstAtomicValueWithNameInData("doctoralDegreeGrantor"), "no");

	}

	@Test
	public void testSubOrganisationIncludeCorrectChildren() {
		rowFromDb.put("type_code", "unit");
		rowFromDb.put("top_level", false);
		rowFromDb.put("organisation_code", "someCode");
		rowFromDb.put("organisation_homepage", "www.someaddress.com");
		rowFromDb.put("show_in_defence", true);
		rowFromDb.put("orgnumber", "33445566");
		rowFromDb.put("country_code", "fi");
		rowFromDb.put("city", "Uppsala");
		rowFromDb.put("street", "Övre slottsgatan 1");
		rowFromDb.put("postbox", "Box5435");
		rowFromDb.put("postnumber", "345 34");
		rowFromDb.put("not_eligible", false);
		DataGroup organisation = converter.fromMap(rowFromDb);
		DefaultConverterSpy factoredConverter = (DefaultConverterSpy) converterFactory.factoredConverter;
		assertSame(organisation, factoredConverter.returnedDataGroup);

		assertTrue(organisation.containsChildWithNameInData("address"));
		assertTrue(organisation.containsChildWithNameInData("organisationCode"));
		assertTrue(organisation.containsChildWithNameInData("URL"));
		assertTrue(organisation.containsChildWithNameInData("eligible"));
		assertFalse(organisation.containsChildWithNameInData("doctoralDegreeGrantor"));
		assertFalse(organisation.containsChildWithNameInData("organisationNumber"));

	}

	@Test
	public void testOrganisationNotEligible() {
		rowFromDb.put("not_eligible", true);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getFirstAtomicValueWithNameInData("eligible"), "no");
	}

}
