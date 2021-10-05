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
import se.uu.ub.cora.diva.mixedstorage.db.DbStatement;
import se.uu.ub.cora.sqldatabase.Row;

public class OrganisationPredecessorRelatedTableTest {

	private OrganisationPredecessorRelatedTable predecessor;
	private List<Row> predecessorRows;
	private SqlDatabaseFactorySpy sqlDatabaseFactory;
	private TableFacadeSpy tableFacade;

	@BeforeMethod
	public void setUp() {
		sqlDatabaseFactory = new SqlDatabaseFactorySpy();
		tableFacade = new TableFacadeSpy();
		initPredecessorRows();
		predecessor = new OrganisationPredecessorRelatedTable(sqlDatabaseFactory);
	}

	private void initPredecessorRows() {
		predecessorRows = new ArrayList<>();
		RowSpy row = new RowSpy();
		row.addColumnWithValue("organisation_id", 678);
		row.addColumnWithValue("organisation_predecessor_id", 234);
		predecessorRows.add(row);
	}

	private DataGroup createDataGroupWithId(String id) {
		DataGroup dataGroup = new DataGroupSpy("organisation");
		DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", id));
		dataGroup.addChild(recordInfo);
		return dataGroup;
	}

	@Test
	public void testInit() {
		assertSame(predecessor.getSqlDatabaseFactory(), sqlDatabaseFactory);
		// assertSame(predecessor.getTableFacade(), sqlDatabaseFactory.factoredTableFacade);
	}

	@Test
	public void testNoPredecessorInDbNoPredecessorInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		List<DbStatement> dbStatements = predecessor.handleDbForDataGroup(tableFacade, organisation,
				Collections.emptyList());
		assertTrue(dbStatements.isEmpty());
	}

	@Test
	public void testOnePredecessorInDbButNoPredecessorInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");

		List<DbStatement> dbStatements = predecessor.handleDbForDataGroup(tableFacade, organisation,
				predecessorRows);
		assertEquals(dbStatements.size(), 2);
		assertCorrectDeleteForPredecessorDescription(dbStatements.get(0), 678, 234);
		assertCorrectDeleteForPredecessor(dbStatements.get(1), 678, 234);

	}

	private void assertCorrectDeleteForPredecessorDescription(DbStatement deleteStatment, int orgId,
			int predecessorId) {
		assertEquals(deleteStatment.getOperation(), "delete");
		assertEquals(deleteStatment.getTableName(), "organisation_predecessor_description");
		Map<String, Object> deleteConditions = deleteStatment.getConditions();
		assertEquals(deleteConditions.get("organisation_id"), orgId);
		assertEquals(deleteConditions.get("predecessor_id"), predecessorId);
		assertTrue(deleteStatment.getValues().isEmpty());
	}

	private void assertCorrectDeleteForPredecessor(DbStatement dbStatement, int organisationId,
			int predecessorId) {
		assertEquals(dbStatement.getOperation(), "delete");
		assertEquals(dbStatement.getTableName(), "organisation_predecessor");
		Map<String, Object> deleteConditions = dbStatement.getConditions();
		assertEquals(deleteConditions.get("organisation_id"), organisationId);
		assertEquals(deleteConditions.get("organisation_predecessor_id"), predecessorId);
		assertTrue(dbStatement.getValues().isEmpty());
	}

	@Test
	public void testOnePredecessorInDbSamePredecessorInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		addPredecessor(organisation, "234", "0");

		List<DbStatement> dbStatements = predecessor.handleDbForDataGroup(tableFacade, organisation,
				predecessorRows);
		assertTrue(dbStatements.isEmpty());
	}

	private void addPredecessor(DataGroup organisation, String predecessorId, String repeatId) {
		DataGroup predecessorGroup = createPredecessorGroupWithRepeatId(repeatId);
		DataGroupSpy predecessorLink = createPredecessorLink(predecessorId);
		predecessorGroup.addChild(predecessorLink);
		organisation.addChild(predecessorGroup);
	}

	private DataGroup createPredecessorGroupWithRepeatId(String repeatId) {
		DataGroup predecessorGroup = new DataGroupSpy("earlierOrganisation");
		predecessorGroup.setRepeatId(repeatId);
		return predecessorGroup;
	}

	private DataGroupSpy createPredecessorLink(String predecessorId) {
		DataGroupSpy predecessorLink = new DataGroupSpy("organisationLink");
		predecessorLink.addChild(new DataAtomicSpy("linkedRecordType", "divaOrganisation"));
		predecessorLink.addChild(new DataAtomicSpy("linkedRecordId", predecessorId));
		return predecessorLink;
	}

	private void addPredecessorWithDescription(DataGroup organisation, String predecessorId,
			String repeatId) {
		DataGroup predecessorGroup = createPredecessorGroupWithRepeatId(repeatId);
		DataGroupSpy predecessorLink = createPredecessorLink(predecessorId);
		predecessorGroup.addChild(predecessorLink);
		predecessorGroup.addChild(new DataAtomicSpy("internalNote", "some description"));
		organisation.addChild(predecessorGroup);
	}

	@Test
	public void testOnePredecessorInDbDifferentPredecessorInDataGroupDeleteAndInsert() {
		DataGroup organisation = createDataGroupWithId("678");
		addPredecessor(organisation, "22234", "0");

		List<DbStatement> dbStatements = predecessor.handleDbForDataGroup(tableFacade, organisation,
				predecessorRows);
		assertEquals(dbStatements.size(), 3);

		assertCorrectPredecessorInsert(dbStatements.get(0), 678, 22234);
		assertCorrectDeleteForPredecessorDescription(dbStatements.get(1), 678, 234);
		assertCorrectDeleteForPredecessor(dbStatements.get(2), 678, 234);
	}

	private void assertCorrectPredecessorInsert(DbStatement createStatement, int organisationId,
			int parentId) {
		assertEquals(createStatement.getOperation(), "insert");
		assertEquals(createStatement.getTableName(), "organisation_predecessor");
		Map<String, Object> values = createStatement.getValues();
		assertEquals(values.get("organisation_id"), organisationId);
		assertEquals(values.get("organisation_predecessor_id"), parentId);
		assertTrue(createStatement.getConditions().isEmpty());
	}

	@Test
	public void testNoPredecessorInDbButPredecessorInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		addPredecessor(organisation, "234", "0");

		List<DbStatement> dbStatements = predecessor.handleDbForDataGroup(tableFacade, organisation,
				Collections.emptyList());
		assertEquals(dbStatements.size(), 1);
		assertCorrectPredecessorInsert(dbStatements.get(0), 678, 234);

	}

	@Test
	public void testMultiplePredecessorsInDbDifferentAndSamePredecessorsInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		addMultiplePredecessors(organisation);

		List<Row> multiplePredecessorRows = new ArrayList<>();
		addPredecessorRow(multiplePredecessorRows, 678, 234);
		addPredecessorRow(multiplePredecessorRows, 678, 22234);
		addPredecessorRow(multiplePredecessorRows, 678, 2444);
		addPredecessorRow(multiplePredecessorRows, 678, 2222);

		List<DbStatement> dbStatements = predecessor.handleDbForDataGroup(tableFacade, organisation,
				multiplePredecessorRows);
		assertEquals(dbStatements.size(), 6);
		assertCorrectPredecessorInsert(dbStatements.get(0), 678, 23);
		assertCorrectPredecessorInsert(dbStatements.get(1), 678, 44444);
		assertCorrectDeleteForPredecessorDescription(dbStatements.get(2), 678, 2444);
		assertCorrectDeleteForPredecessor(dbStatements.get(3), 678, 2444);
		assertCorrectDeleteForPredecessorDescription(dbStatements.get(4), 678, 2222);
		assertCorrectDeleteForPredecessor(dbStatements.get(5), 678, 2222);

	}

	private void addMultiplePredecessors(DataGroup organisation) {
		addPredecessor(organisation, "23", "0");
		addPredecessor(organisation, "234", "1");
		addPredecessor(organisation, "22234", "2");
		addPredecessor(organisation, "44444", "2");
	}

	private void addPredecessorRow(List<Row> multiplePredecessors, int organisationId,
			int predecessorId) {
		Row predecessorRow = createRowWithOrgIdAndPredecessorId(organisationId, predecessorId);
		multiplePredecessors.add(predecessorRow);
	}

	private Row createRowWithOrgIdAndPredecessorId(int organisationId, int predecessorId) {
		RowSpy predecessorRow = new RowSpy();

		predecessorRow.addColumnWithValue("organisation_id", organisationId);
		predecessorRow.addColumnWithValue("organisation_predecessor_id", predecessorId);
		return predecessorRow;
	}

	/***************************
	 * With description
	 **************************************************/
	@Test
	public void testNoPredecessorInDbButPredecessorWithDescriptionInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		addPredecessorWithDescription(organisation, "234", "0");

		List<DbStatement> dbStatements = predecessor.handleDbForDataGroup(tableFacade, organisation,
				Collections.emptyList());

		TableFacadeSpy tableFacade = sqlDatabaseFactory.factoredTableFacade;
		assertEquals(tableFacade.sequenceName, "organisation_predecessor_description_sequence");

		assertEquals(dbStatements.size(), 2);
		assertCorrectPredecessorInsert(dbStatements.get(0), 678, 234);
		assertCorrectPredecessorDescriptionInsert(dbStatements.get(1), 678, 234, "some description",
				tableFacade);

	}

	private void assertCorrectPredecessorDescriptionInsert(DbStatement createStatement,
			int organisationId, int predecessorId, String description, TableFacadeSpy tableFacade) {
		assertEquals(createStatement.getOperation(), "insert");
		assertEquals(createStatement.getTableName(), "organisation_predecessor_description");

		Map<String, Object> values = createStatement.getValues();
		assertEquals(values.get("organisation_predecessor_id"), tableFacade.nextVal);
		assertEquals(values.get("organisation_id"), organisationId);
		assertEquals(values.get("predecessor_id"), predecessorId);
		assertEquals(values.get("description"), description);

		assertLastUpdatedIsInCorrectFormat(values);

		assertTrue(createStatement.getConditions().isEmpty());
	}

	private void assertLastUpdatedIsInCorrectFormat(Map<String, Object> values) {
		Timestamp lastUpdated = (Timestamp) values.get("last_updated");
		String lastUpdatedString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
				.format(lastUpdated);
		assertTrue(lastUpdatedString
				.matches("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\.\\d{1,3}"));
	}

	@Test
	public void testNoPredecessorInDataGroupButPredecessorWithDescriptionInDb() {
		DataGroup organisation = createDataGroupWithId("678");

		List<Row> predecessorWithDescriptionRows = new ArrayList<>();
		addPredecessorRowWithDesciption(predecessorWithDescriptionRows, 678, 234, 33,
				"some description for descriptionId 33");

		List<DbStatement> dbStatements = predecessor.handleDbForDataGroup(tableFacade, organisation,
				predecessorWithDescriptionRows);
		assertEquals(dbStatements.size(), 2);
		assertCorrectDeleteForPredecessorDescription(dbStatements.get(0), 678, 234);
		assertCorrectDeleteForPredecessor(dbStatements.get(1), 678, 234);
	}

	private void addPredecessorRowWithDesciption(List<Row> multiplePredecessors, int organisationId,
			int predecessorId, int predecessorDescriptionId, String description) {
		RowSpy predecessorRow = (RowSpy) createRowWithOrgIdAndPredecessorId(organisationId,
				predecessorId);
		predecessorRow.addColumnWithValue("predecessordescriptionid", predecessorDescriptionId);
		predecessorRow.addColumnWithValue("description", description);
		multiplePredecessors.add(predecessorRow);
	}

	@Test
	public void testOnePredecessorInDbSamePredecessorInDataGroupSameComment() {
		DataGroup organisation = createDataGroupWithId("678");
		addPredecessorWithDescription(organisation, "234", "0");

		RowSpy predecessorRow = (RowSpy) predecessorRows.get(0);
		predecessorRow.addColumnWithValue("predecessordescriptionid", 7777);
		predecessorRow.addColumnWithValue("description", "some description");

		List<DbStatement> dbStatements = predecessor.handleDbForDataGroup(tableFacade, organisation,
				predecessorRows);
		assertTrue(dbStatements.isEmpty());
	}

	@Test
	public void testOnePredecessorInDbSamePredecessorInDataGroupDifferentComment() {
		DataGroup organisation = createDataGroupWithId("678");
		addPredecessorWithDescription(organisation, "234", "0");

		RowSpy predecessorRow = (RowSpy) predecessorRows.get(0);
		predecessorRow.addColumnWithValue("predecessordescriptionid", 7778);
		predecessorRow.addColumnWithValue("description", "some OTHER description");

		List<DbStatement> dbStatements = predecessor.handleDbForDataGroup(tableFacade, organisation,
				predecessorRows);

		TableFacadeSpy tableFacade = sqlDatabaseFactory.factoredTableFacade;
		assertEquals(tableFacade.sequenceName, "organisation_predecessor_description_sequence");

		assertEquals(dbStatements.size(), 2);
		assertCorrectDeleteForPredecessorDescription(dbStatements.get(0), 678, 234);
		assertCorrectPredecessorDescriptionInsert(dbStatements.get(1), 678, 234, "some description",
				tableFacade);

	}

	@Test
	public void testOnePredecessorInDbSamePredecessorInDataGroupNoCommentInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		addPredecessor(organisation, "234", "0");

		RowSpy predecessorRow = (RowSpy) predecessorRows.get(0);
		predecessorRow.addColumnWithValue("predecessordescriptionid", 7778);
		predecessorRow.addColumnWithValue("description", "some OTHER description");

		List<DbStatement> dbStatements = predecessor.handleDbForDataGroup(tableFacade, organisation,
				predecessorRows);
		assertEquals(dbStatements.size(), 1);
		assertCorrectDeleteForPredecessorDescription(dbStatements.get(0), 678, 234);
	}

	@Test
	public void testOnePredecessorInDbSamePredecessorInDataGroupNoCommentInDb() {
		DataGroup organisation = createDataGroupWithId("678");
		addPredecessorWithDescription(organisation, "234", "0");

		List<DbStatement> dbStatements = predecessor.handleDbForDataGroup(tableFacade, organisation,
				predecessorRows);

		TableFacadeSpy tableFacade = sqlDatabaseFactory.factoredTableFacade;
		assertEquals(tableFacade.sequenceName, "organisation_predecessor_description_sequence");

		assertEquals(dbStatements.size(), 1);
		assertCorrectPredecessorDescriptionInsert(dbStatements.get(0), 678, 234, "some description",
				tableFacade);
	}

	@Test
	public void testOnePredecessorInDbWithDescriptionSameAndAddedPredecessorInDataGroupWithDescription() {
		DataGroup organisation = createDataGroupWithId("678");
		addPredecessorWithDescription(organisation, "22234", "0");
		addPredecessorWithDescription(organisation, "234", "1");

		RowSpy predecessorRow = (RowSpy) predecessorRows.get(0);
		predecessorRow.addColumnWithValue("predecessordescriptionid", 7778);
		predecessorRow.addColumnWithValue("description", "some description");

		List<DbStatement> dbStatements = predecessor.handleDbForDataGroup(tableFacade, organisation,
				predecessorRows);

		TableFacadeSpy tableFacade = sqlDatabaseFactory.factoredTableFacade;
		assertEquals(tableFacade.sequenceName, "organisation_predecessor_description_sequence");

		assertEquals(dbStatements.size(), 2);
		assertCorrectPredecessorInsert(dbStatements.get(0), 678, 22234);
		assertCorrectPredecessorDescriptionInsert(dbStatements.get(1), 678, 22234,
				"some description", tableFacade);

	}

}
