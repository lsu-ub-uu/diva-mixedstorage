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
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.db.DbException;
import se.uu.ub.cora.diva.mixedstorage.db.DbStatement;
import se.uu.ub.cora.sqldatabase.Row;

public class OrganisationAlternativeNameRelatedTableTest {

	private OrganisationAlternativeNameRelatedTable alternativeName;
	private SqlDatabaseFactorySpy sqlDatabaseFactory;
	private List<Row> rowsFromDb;
	private TableFacadeSpy tableFacade;

	@BeforeMethod
	public void setUp() {
		sqlDatabaseFactory = new SqlDatabaseFactorySpy();
		tableFacade = new TableFacadeSpy();
		initAlternativeNameRows();
		alternativeName = new OrganisationAlternativeNameRelatedTable(sqlDatabaseFactory);
	}

	private void initAlternativeNameRows() {
		rowsFromDb = new ArrayList<>();
		RowSpy row = new RowSpy();
		row.addColumnWithValue("organisation_name_id", 234);
		row.addColumnWithValue("organisation_id", 678);
		row.addColumnWithValue("organisation_name", "some english name");
		row.addColumnWithValue("locale", "en");
		rowsFromDb.add(row);
	}

	@Test
	public void testGetSqlDatabaseFactory() {
		assertSame(alternativeName.getSqlDatabaseFactory(), sqlDatabaseFactory);
	}

	@Test(expectedExceptions = DbException.class, expectedExceptionsMessageRegExp = ""
			+ "Organisation must have alternative name")
	public void testNoNameInDataGroupThrowsException() {
		DataGroup organisation = createDataGroupWithId("678");
		alternativeName.handleDbForDataGroup(tableFacade, organisation, rowsFromDb);
	}

	private DataGroup createDataGroupWithId(String id) {
		DataGroup dataGroup = new DataGroupSpy("organisation");
		DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", id));
		dataGroup.addChild(recordInfo);
		return dataGroup;
	}

	@Test(expectedExceptions = DbException.class, expectedExceptionsMessageRegExp = ""
			+ "Organisation must have alternative name")
	public void testIncompleteNameInDataGroupThrowsException() {
		DataGroup organisation = createDataGroupWithId("678");
		DataGroupSpy alternativeNameGroup = new DataGroupSpy("organisationAlternativeName");
		organisation.addChild(alternativeNameGroup);

		alternativeName.handleDbForDataGroup(tableFacade, organisation, rowsFromDb);
	}

	@Test(expectedExceptions = DbException.class, expectedExceptionsMessageRegExp = ""
			+ "Organisation can not have more than one alternative name")
	public void testMoreThanOneNameInDbRows() {
		DataGroup organisation = createDataGroupWithId("678");
		addAlternativeName(organisation, "some english name");
		RowSpy secondRow = new RowSpy();
		secondRow.addColumnWithValue("organisation_name_id", 234234);
		rowsFromDb.add(secondRow);
		alternativeName.handleDbForDataGroup(tableFacade, organisation, rowsFromDb);
	}

	@Test
	public void testOneNameInDbSameNameInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		addAlternativeName(organisation, "some english name");

		List<DbStatement> dbStatments = alternativeName.handleDbForDataGroup(tableFacade,
				organisation, rowsFromDb);
		assertEquals(dbStatments.size(), 0);
	}

	@Test
	public void testOneNameInDbDifferentNameInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		String newAlternativeName = "some other english name";
		addAlternativeName(organisation, newAlternativeName);

		List<DbStatement> dbStatements = alternativeName.handleDbForDataGroup(tableFacade,
				organisation, rowsFromDb);
		assertEquals(dbStatements.size(), 1);
		DbStatement dbStatement = dbStatements.get(0);
		assertEquals(dbStatement.getOperation(), "update");
		assertEquals(dbStatement.getTableName(), "organisation_name");

		Map<String, Object> values = dbStatement.getValues();
		assertEquals(values.get("locale"), "en");
		assertEquals(values.get("organisation_id"), 678);

		String lastUpdatedString = extractTimestampFromValues(values);
		assertTrue(lastUpdatedString
				.matches("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\.\\d{1,3}"));
		assertEquals(values.get("organisation_name"), newAlternativeName);

		Map<String, Object> conditions = dbStatement.getConditions();
		assertEquals(conditions.get("organisation_name_id"), 234);

	}

	private String extractTimestampFromValues(Map<String, Object> values) {
		Timestamp lastUpdated = (Timestamp) values.get("last_updated");
		String lastUpdatedString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
				.format(lastUpdated);
		return lastUpdatedString;
	}

	private void addAlternativeName(DataGroup organisation, String name) {
		DataGroupSpy alternativeNameGroup = new DataGroupSpy("organisationAlternativeName");
		alternativeNameGroup.addChild(new DataAtomicSpy("name", name));
		alternativeNameGroup.addChild(new DataAtomicSpy("language", "en"));
		organisation.addChild(alternativeNameGroup);
	}

	@Test
	public void testNoNameInDbButNameInDataGroup() {
		alternativeName = new OrganisationAlternativeNameRelatedTable(sqlDatabaseFactory);

		DataGroup organisation = createDataGroupWithId("678");
		DataGroupSpy alternativeNameGroup = new DataGroupSpy("organisationAlternativeName");
		String newAlternativeName = "some english name";
		alternativeNameGroup.addChild(new DataAtomicSpy("name", newAlternativeName));
		alternativeNameGroup.addChild(new DataAtomicSpy("language", "en"));
		organisation.addChild(alternativeNameGroup);

		List<DbStatement> dbStatements = alternativeName.handleDbForDataGroup(tableFacade,
				organisation, Collections.emptyList());

		assertEquals(tableFacade.sequenceName, "name_sequence");

		assertEquals(dbStatements.size(), 1);
		DbStatement dbStatement = dbStatements.get(0);
		assertEquals(dbStatement.getOperation(), "insert");
		assertEquals(dbStatement.getTableName(), "organisation_name");

		Map<String, Object> values = dbStatement.getValues();

		assertEquals(values.get("organisation_name_id"), tableFacade.nextVal);

		assertEquals(values.get("locale"), "en");
		assertEquals(values.get("organisation_id"), 678);

		String lastUpdatedString = extractTimestampFromValues(values);
		assertTrue(lastUpdatedString
				.matches("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\.\\d{1,3}"));
		assertEquals(values.get("organisation_name"), newAlternativeName);

		assertTrue(dbStatement.getConditions().isEmpty());
	}
}
