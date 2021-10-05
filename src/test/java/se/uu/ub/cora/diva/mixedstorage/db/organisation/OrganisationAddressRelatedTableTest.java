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
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.db.DbStatement;
import se.uu.ub.cora.sqldatabase.Row;

public class OrganisationAddressRelatedTableTest {

	private OrganisationAddressRelatedTable address;
	private SqlDatabaseFactorySpy sqlDatabaseFactory;
	private List<Row> rowsFromDb;
	private TableFacadeSpy tableFacade;

	@BeforeMethod
	public void setUp() {
		sqlDatabaseFactory = new SqlDatabaseFactorySpy();
		tableFacade = new TableFacadeSpy();
		initOrganisationRows();

		address = new OrganisationAddressRelatedTable(sqlDatabaseFactory);

	}

	private void initOrganisationRows() {
		rowsFromDb = new ArrayList<>();
		RowSpy row = new RowSpy();
		row.addColumnWithValue("organisation_id", 678);
		row.addColumnWithValue("address_id", 4);
		row.addColumnWithValue("country_code", "se");
		rowsFromDb.add(row);
	}

	@Test
	public void testInit() {
		assertSame(address.getSqlDatabaseFactory(), sqlDatabaseFactory);
	}

	@Test
	public void testNoAddressInDataGroupNoAddressInDatabase() {
		DataGroup organisation = createDataGroupWithId("678");
		setUpOrganisationRowWithoutAddress();
		List<DbStatement> dbStatements = address.handleDbForDataGroup(tableFacade, organisation,
				rowsFromDb);
		assertTrue(dbStatements.isEmpty());
	}

	private void setUpOrganisationRowWithoutAddress() {
		rowsFromDb = new ArrayList<>();
		RowSpy row = new RowSpy();
		row.addColumnWithValue("organisation_id", 678);
		rowsFromDb.add(row);
	}

	private DataGroup createDataGroupWithId(String id) {
		DataGroup dataGroup = new DataGroupSpy("organisation");
		DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", id));
		dataGroup.addChild(recordInfo);
		return dataGroup;
	}

	@Test
	public void testNoAddressInDataGroupButAddressInDatabase() {
		int organisationId = 678;
		DataGroup organisation = createDataGroupWithId("678");

		assertCorrectUpdateAndDeleteOfAddress(organisationId, organisation);

	}

	private void assertCorrectUpdateAndDeleteOfAddress(int organisationId, DataGroup organisation) {
		List<DbStatement> dbStatements = address.handleDbForDataGroup(tableFacade, organisation,
				rowsFromDb);
		assertEquals(dbStatements.size(), 2);
		DbStatement dbStatement = dbStatements.get(0);
		assertCorrectUpdateOrganisationAddressSetToNull(organisationId, dbStatement);

		assertCorrectDeletedAddress(dbStatements.get(1), 4);
	}

	private void assertCorrectUpdateOrganisationAddressSetToNull(int organisationId,
			DbStatement dbStatement) {
		assertCorrectOperationTableAndConditionForUpdateOrg(organisationId, dbStatement);
		Map<String, Object> values = dbStatement.getValues();
		assertTrue(values.containsKey("address_id"));
		assertEquals(values.get("address_id"), null);
	}

	private void assertCorrectOperationTableAndConditionForUpdateOrg(int organisationId,
			DbStatement dbStatement) {
		assertEquals(dbStatement.getOperation(), "update");
		assertEquals(dbStatement.getTableName(), "organisation");
		assertEquals(dbStatement.getConditions().get("organisation_id"), organisationId);
	}

	private void assertCorrectDeletedAddress(DbStatement dbStatement, int addressId) {
		assertEquals(dbStatement.getOperation(), "delete");
		assertEquals(dbStatement.getTableName(), "organisation_address");
		Map<String, Object> conditions = dbStatement.getConditions();
		assertEquals(conditions.get("address_id"), addressId);
		assertTrue(dbStatement.getValues().isEmpty());
	}

	@Test
	public void testEmptyAddressInDataGroupButAddressInDatabase() {
		int organisationId = 678;
		DataGroup organisation = createDataGroupWithId("678");
		createAddressGroupAndAddToOrganisation(organisation);

		assertCorrectUpdateAndDeleteOfAddress(organisationId, organisation);

	}

	@Test
	public void testCityInDataGroupAndAddressInDatabase() {
		DataGroup organisation = createDataGroupWithId("678");
		DataGroup addressGroup = new DataGroupSpy("address");
		addressGroup.addChild(new DataAtomicSpy("city", "City of rock and roll"));
		organisation.addChild(addressGroup);

		List<DbStatement> dbStatements = address.handleDbForDataGroup(tableFacade, organisation,
				rowsFromDb);
		assertEquals(dbStatements.size(), 1);
		DbStatement dbStatement = dbStatements.get(0);
		assertCorrectDataForAddressUpdate(organisation, dbStatement, 4);

	}

	private void assertCorrectDataForAddressUpdate(DataGroup organisation, DbStatement dbStatement,
			int addressId) {
		assertEquals(dbStatement.getOperation(), "update");

		assertCorrectCommonValuesForUpdateAndInsert(organisation, dbStatement);

		Map<String, Object> conditions = dbStatement.getConditions();
		assertEquals(conditions.get("address_id"), addressId);
	}

	private void assertCorrectCommonValuesForUpdateAndInsert(DataGroup organisation,
			DbStatement dbStatement) {
		assertEquals(dbStatement.getTableName(), "organisation_address");
		DataGroup addressGroup = organisation.getFirstGroupWithNameInData("address");
		Map<String, Object> values = dbStatement.getValues();
		assertEquals(values.get("city"), getAtomicValueOrNull(addressGroup, "city"));
		assertEquals(values.get("street"), getAtomicValueOrNull(addressGroup, "street"));
		assertEquals(values.get("postbox"), getAtomicValueOrNull(addressGroup, "box"));
		assertEquals(values.get("postnumber"), getAtomicValueOrNull(addressGroup, "postcode"));
		String countryCode = getAtomicValueOrNull(addressGroup, "country");
		if (null != countryCode) {
			countryCode = countryCode.toLowerCase();
		}
		assertEquals(values.get("country_code"), countryCode);
	}

	private String getAtomicValueOrNull(DataGroup dataGroup, String nameInData) {
		return dataGroup.containsChildWithNameInData(nameInData)
				? dataGroup.getFirstAtomicValueWithNameInData(nameInData)
				: null;
	}

	@Test
	public void testStreetInDataGroupAndAddressInDatabase() {
		DataGroup organisation = createDataGroupWithId("678");
		DataGroup addressGroup = createAddressGroupAndAddToOrganisation(organisation);
		addressGroup.addChild(new DataAtomicSpy("street", "Hill street"));

		List<DbStatement> dbStatements = address.handleDbForDataGroup(tableFacade, organisation,
				rowsFromDb);
		assertEquals(dbStatements.size(), 1);
		DbStatement dbStatement = dbStatements.get(0);
		assertCorrectDataForAddressUpdate(organisation, dbStatement, 4);

	}

	@Test
	public void testPostboxInDataGroupAndAddressInDatabase() {
		DataGroup organisation = createDataGroupWithId("678");
		DataGroup addressGroup = createAddressGroupAndAddToOrganisation(organisation);

		addressGroup.addChild(new DataAtomicSpy("box", "box21"));

		List<DbStatement> dbStatements = address.handleDbForDataGroup(tableFacade, organisation,
				rowsFromDb);
		assertEquals(dbStatements.size(), 1);
		DbStatement dbStatement = dbStatements.get(0);
		assertCorrectDataForAddressUpdate(organisation, dbStatement, 4);
	}

	@Test
	public void testPostnumberInDataGroupAndAddressInDatabase() {
		DataGroup organisation = createDataGroupWithId("678");
		DataGroup addressGroup = createAddressGroupAndAddToOrganisation(organisation);
		addressGroup.addChild(new DataAtomicSpy("postcode", "90210"));

		List<DbStatement> dbStatements = address.handleDbForDataGroup(tableFacade, organisation,
				rowsFromDb);
		assertEquals(dbStatements.size(), 1);
		DbStatement dbStatement = dbStatements.get(0);
		assertCorrectDataForAddressUpdate(organisation, dbStatement, 4);
	}

	@Test
	public void testCountryCodeInDataGroupAndAddressInDatabase() {
		DataGroup organisation = createDataGroupWithId("678");
		DataGroup addressGroup = createAddressGroupAndAddToOrganisation(organisation);
		addressGroup.addChild(new DataAtomicSpy("country", "SE"));

		List<DbStatement> dbStatements = address.handleDbForDataGroup(tableFacade, organisation,
				rowsFromDb);
		assertEquals(dbStatements.size(), 1);
		DbStatement dbStatement = dbStatements.get(0);
		assertCorrectDataForAddressUpdate(organisation, dbStatement, 4);
	}

	@Test
	public void testCompleteAddressInDataGroupAndAddressInDatabase() {
		DataGroup organisation = createDataGroupWithId("678");
		DataGroup addressGroup = createAddressGroupAndAddToOrganisation(organisation);

		addressGroup.addChild(new DataAtomicSpy("city", "City of rock and roll"));
		addressGroup.addChild(new DataAtomicSpy("country", "SE"));
		addressGroup.addChild(new DataAtomicSpy("postcode", "90210"));
		addressGroup.addChild(new DataAtomicSpy("box", "box21"));
		addressGroup.addChild(new DataAtomicSpy("street", "Hill street"));

		List<DbStatement> dbStatements = address.handleDbForDataGroup(tableFacade, organisation,
				rowsFromDb);
		assertEquals(dbStatements.size(), 1);
		DbStatement dbStatement = dbStatements.get(0);
		assertCorrectDataForAddressUpdate(organisation, dbStatement, 4);
	}

	@Test
	public void testAddressInDataGroupButNOAddressInDatabase() {
		int organisationId = 678;
		DataGroup organisation = createDataGroupWithId("678");
		DataGroup addressGroup = createAddressGroupAndAddToOrganisation(organisation);
		addressGroup.addChild(new DataAtomicSpy("box", "box21"));
		setUpOrganisationRowWithoutAddress();

		List<DbStatement> dbStatements = address.handleDbForDataGroup(tableFacade, organisation,
				rowsFromDb);
		assertEquals(dbStatements.size(), 2);

		assertEquals(tableFacade.sequenceName, "address_sequence");

		assertCorrectDataForAddressInsert(organisation, dbStatements.get(0), 4,
				tableFacade.nextVal);

		DbStatement orgUpdateStatement = dbStatements.get(1);
		assertCorrectOperationTableAndConditionForUpdateOrg(organisationId, orgUpdateStatement);

	}

	private DataGroup createAddressGroupAndAddToOrganisation(DataGroup organisation) {
		DataGroup addressGroup = new DataGroupSpy("address");
		organisation.addChild(addressGroup);
		return addressGroup;
	}

	private void assertCorrectDataForAddressInsert(DataGroup organisation, DbStatement dbStatement,
			int addressId, long nextVal) {
		assertEquals(dbStatement.getOperation(), "insert");
		Map<String, Object> values = dbStatement.getValues();

		assertEquals(values.get("address_id"), nextVal);
		assertCorrectCommonValuesForUpdateAndInsert(organisation, dbStatement);
	}
}
