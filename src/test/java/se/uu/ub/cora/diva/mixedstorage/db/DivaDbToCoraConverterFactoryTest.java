/*
 * Copyright 2018, 2020 Uppsala University Library
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

import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.DefaultConverterFactory;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.DivaDbToCoraOrganisationConverter;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.DivaDbToCoraOrganisationParentConverter;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.DivaDbToCoraOrganisationPredecessorConverter;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.DivaDbToCoraOrganisationSuccessorConverter;

public class DivaDbToCoraConverterFactoryTest {
	private DivaDbToCoraConverterFactory divaDbToCoraConverterFactoryImp;

	@BeforeMethod
	public void beforeMethod() {
		divaDbToCoraConverterFactoryImp = new DivaDbToCoraConverterFactoryImp();
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "No converter implemented for: someType")
	public void factorUnknownTypeThrowsException() throws Exception {
		divaDbToCoraConverterFactoryImp.factor("someType");
	}

	@Test
	public void testFactoryOrganisation() throws Exception {
		testCorrectConverterForOrganisationType("organisation");
	}

	private void testCorrectConverterForOrganisationType(String type) {
		DivaDbToCoraOrganisationConverter converter = (DivaDbToCoraOrganisationConverter) divaDbToCoraConverterFactoryImp
				.factor(type);
		DefaultConverterFactory defaultConverterFactory = converter.getDefaultConverterFactory();
		assertTrue(defaultConverterFactory instanceof DefaultConverterFactoryImp);
	}

	@Test
	public void testFactoryRootOrganisation() throws Exception {
		testCorrectConverterForOrganisationType("rootOrganisation");
	}

	@Test
	public void testFactoryTopOrganisation() throws Exception {
		testCorrectConverterForOrganisationType("topOrganisation");
	}

	@Test
	public void testFactorySubOrganisation() throws Exception {
		testCorrectConverterForOrganisationType("subOrganisation");
	}

	@Test
	public void testFactoryOrganisationParent() throws Exception {
		DivaDbToCoraConverter converter = divaDbToCoraConverterFactoryImp
				.factor("divaOrganisationParent");
		assertTrue(converter instanceof DivaDbToCoraOrganisationParentConverter);
	}

	@Test
	public void testFactoryOrganisationPredecessor() throws Exception {
		DivaDbToCoraConverter converter = divaDbToCoraConverterFactoryImp
				.factor("divaOrganisationPredecessor");
		assertTrue(converter instanceof DivaDbToCoraOrganisationPredecessorConverter);
	}

	@Test
	public void testFactoryOrganisationSuccessor() throws Exception {
		DivaDbToCoraConverter converter = divaDbToCoraConverterFactoryImp
				.factor("divaOrganisationSuccessor");
		assertTrue(converter instanceof DivaDbToCoraOrganisationSuccessorConverter);
	}

	@Test
	public void testFactoryUser() throws Exception {
		DivaDbToCoraConverter converter = divaDbToCoraConverterFactoryImp.factor("user");
		assertTrue(converter instanceof DivaDbToCoraUserConverter);
	}
}
