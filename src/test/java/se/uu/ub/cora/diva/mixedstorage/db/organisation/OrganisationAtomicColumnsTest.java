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

import org.testng.annotations.Test;

public class OrganisationAtomicColumnsTest {
	@Test
	public void testEnum() {
		// small hack to get 100% coverage on enum
		OrganisationAtomicColumns.valueOf(OrganisationAtomicColumns.ORGANISATION_CODE.toString());
		assertEquals(OrganisationAtomicColumns.ORGANISATION_CODE.coraName, "organisationCode");
		assertEquals(OrganisationAtomicColumns.ORGANISATION_CODE.type, "string");
		assertEquals(OrganisationAtomicColumns.ORGANISATION_NUMBER.dbName, "orgnumber");
		assertEquals(OrganisationAtomicColumns.ORGANISATION_NUMBER.coraName, "organisationNumber");
		assertEquals(OrganisationAtomicColumns.ORGANISATION_NUMBER.type, "string");
		assertEquals(OrganisationAtomicColumns.CLOSED_DATE.dbName, "closed_date");
		assertEquals(OrganisationAtomicColumns.CLOSED_DATE.coraName, "closedDate");
		assertEquals(OrganisationAtomicColumns.CLOSED_DATE.type, "date");
		assertEquals(OrganisationAtomicColumns.URL.dbName, "organisation_homepage");
		assertEquals(OrganisationAtomicColumns.URL.coraName, "URL");
		assertEquals(OrganisationAtomicColumns.URL.type, "string");

	}
}
