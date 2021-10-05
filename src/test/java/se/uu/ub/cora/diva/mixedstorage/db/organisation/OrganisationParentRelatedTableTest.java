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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.db.DbStatement;
import se.uu.ub.cora.sqldatabase.Row;

public class OrganisationParentRelatedTableTest {

	private OrganisationParentRelatedTable parent;
	private List<Row> parentRows;
	private SqlDatabaseFactorySpy sqlDatabaseFactory;
	private TableFacadeSpy tableFacade;

	@BeforeMethod
	public void setUp() {
		sqlDatabaseFactory = new SqlDatabaseFactorySpy();
		tableFacade = new TableFacadeSpy();
		parent = new OrganisationParentRelatedTable(sqlDatabaseFactory);
		initParentRows();
	}

	private void initParentRows() {
		parentRows = new ArrayList<>();
		RowSpy row = new RowSpy();
		row.addColumnWithValue("organisation_id", 678);
		row.addColumnWithValue("organisation_parent_id", 234);
		parentRows.add(row);
	}

	private DataGroup createDataGroupWithId(String id) {
		DataGroup dataGroup = new DataGroupSpy("organisation");
		DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", id));
		dataGroup.addChild(recordInfo);
		return dataGroup;
	}

	@Test
	public void testGetSqlDatabaseFactory() {
		assertSame(parent.getSqlDatabaseFactory(), sqlDatabaseFactory);
	}

	@Test
	public void testNoParentInDbNoParentInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		List<DbStatement> dbStatements = parent.handleDbForDataGroup(tableFacade, organisation,
				Collections.emptyList());
		assertTrue(dbStatements.isEmpty());
	}

	@Test
	public void testOneParentInDbButNoParentInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		List<DbStatement> dbStatements = parent.handleDbForDataGroup(tableFacade, organisation,
				parentRows);
		assertEquals(dbStatements.size(), 1);
		assertCorrectDelete(dbStatements.get(0), 234);

	}

	@Test
	public void testOneParentInDbSameParentInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		addParent(organisation, "234", "0");

		List<DbStatement> dbStatements = parent.handleDbForDataGroup(tableFacade, organisation,
				parentRows);
		assertTrue(dbStatements.isEmpty());

	}

	private void addParent(DataGroup organisation, String parentId, String repeatId) {
		DataGroup parent = new DataGroupSpy("parentOrganisation");
		parent.setRepeatId(repeatId);
		DataGroupSpy parentLink = new DataGroupSpy("organisationLink");
		parentLink.addChild(new DataAtomicSpy("linkedRecordType", "divaOrganisation"));
		parentLink.addChild(new DataAtomicSpy("linkedRecordId", parentId));
		parent.addChild(parentLink);
		organisation.addChild(parent);
	}

	@Test
	public void testOneParentInDbDifferentParentInDataGroupDeleteAndInsert() {
		DataGroup organisation = createDataGroupWithId("678");
		addParent(organisation, "22234", "0");

		List<DbStatement> dbStatements = parent.handleDbForDataGroup(tableFacade, organisation,
				parentRows);

		assertEquals(dbStatements.size(), 2);

		assertCorrectInsert(dbStatements.get(0), 22234);
		assertCorrectDelete(dbStatements.get(1), 234);

	}

	@Test
	public void testNoParentInDbButNameInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		addParent(organisation, "234", "0");

		List<DbStatement> dbStatements = parent.handleDbForDataGroup(tableFacade, organisation,
				Collections.emptyList());
		assertCorrectInsert(dbStatements.get(0), 234);
	}

	@Test
	public void testMultipleParentsInDbDifferentAndSameNamesInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		addParent(organisation, "23", "0");
		addParent(organisation, "234", "1");
		addParent(organisation, "22234", "2");
		addParent(organisation, "44444", "2");

		List<Row> multipleParents = new ArrayList<>();
		addParenRow(multipleParents, 678, 234);
		addParenRow(multipleParents, 678, 22234);
		addParenRow(multipleParents, 678, 2444);
		addParenRow(multipleParents, 678, 2222);

		List<DbStatement> dbStatements = parent.handleDbForDataGroup(tableFacade, organisation,
				multipleParents);
		assertEquals(dbStatements.size(), 4);

		assertCorrectInsert(dbStatements.get(0), 23);
		assertCorrectInsert(dbStatements.get(1), 44444);

		assertCorrectDelete(dbStatements.get(2), 2444);
		assertCorrectDelete(dbStatements.get(3), 2222);

	}

	private void assertCorrectInsert(DbStatement insertStatement, int parentId) {
		assertEquals(insertStatement.getOperation(), "insert");
		assertEquals(insertStatement.getTableName(), "organisation_parent");
		Map<String, Object> insertValues = insertStatement.getValues();
		assertEquals(insertValues.get("organisation_id"), 678);
		assertEquals(insertValues.get("organisation_parent_id"), parentId);
	}

	private void assertCorrectDelete(DbStatement deleteStatement, int parentId) {
		assertEquals(deleteStatement.getOperation(), "delete");
		assertEquals(deleteStatement.getTableName(), "organisation_parent");
		Map<String, Object> deleteConditions = deleteStatement.getConditions();
		assertEquals(deleteConditions.get("organisation_id"), 678);
		assertEquals(deleteConditions.get("organisation_parent_id"), parentId);
	}

	private void addParenRow(List<Row> multipleParents, int organisationId, int parentId) {

		RowSpy parentRow = new RowSpy();
		parentRow.addColumnWithValue("organisation_id", organisationId);
		parentRow.addColumnWithValue("organisation_parent_id", parentId);
		multipleParents.add(parentRow);
	}
}
