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
package se.uu.ub.cora.diva.mixedstorage.db;

import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.OrganisationAlternativeNameDataToDbTranslater;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.OrganisationDataToDbTranslater;

public class DataToDbTranslaterFactoryTest {

	private RecordReaderFactorySpy recordReaderFactory;
	private DataToDbTranslaterFactory factory;

	@BeforeMethod
	public void setUp() {
		recordReaderFactory = new RecordReaderFactorySpy();
		factory = new DivaDataToDbTranslaterFactoryImp(recordReaderFactory);
	}

	@Test
	public void testInitFactory() {
		assertTrue(factory instanceof DataToDbTranslaterFactory);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = "No translater implemented for: nonExistingTable")
	public void factorNonExisting() {
		factory.factorForTableName("nonExistingTable");
	}

	@Test
	public void testFactorForOrganisation() {
		OrganisationDataToDbTranslater translater = (OrganisationDataToDbTranslater) factory
				.factorForTableName("organisation");
		assertTrue(translater instanceof OrganisationDataToDbTranslater);
		assertSame(recordReaderFactory.factored, translater.getRecordReader());
	}

	@Test
	public void testFactorForOrganisationName() {
		DataToDbTranslater translater = factory.factorForTableName("organisation_name");
		assertTrue(translater instanceof OrganisationAlternativeNameDataToDbTranslater);

	}
}
