/*
 * Copyright 2018, 2019 Uppsala University Library
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
package se.uu.ub.cora.diva.mixedstorage.fedora;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static se.uu.ub.cora.diva.mixedstorage.fedora.DivaFedoraToCoraPersonConverterTestHelper.assertCorrectCreatedByUsingRecordInfoAndUserId;
import static se.uu.ub.cora.diva.mixedstorage.fedora.DivaFedoraToCoraPersonConverterTestHelper.assertCorrectIdUsingRecordInfoAndId;
import static se.uu.ub.cora.diva.mixedstorage.fedora.DivaFedoraToCoraPersonConverterTestHelper.assertCorrectTsCreatedUsingRecordInfoAndTsCreated;
import static se.uu.ub.cora.diva.mixedstorage.fedora.DivaFedoraToCoraPersonConverterTestHelper.assertCorrectTsUpdatedUsingUpdatedAndTsUpdated;
import static se.uu.ub.cora.diva.mixedstorage.fedora.DivaFedoraToCoraPersonConverterTestHelper.assertCorrectUpdatedByUsingUpdatedAndUserId;
import static se.uu.ub.cora.diva.mixedstorage.fedora.DivaFedoraToCoraPersonConverterTestHelper.assertRecordInfoPersonInDiva;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataAtomicFactory;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupFactory;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.ParseException;

public class DivaFedoraToCoraPersonConverterOldTest {

	private static final String TOP_DATAGROUP_NAMEINDATA = "authorityPerson";
	private DivaFedoraToCoraPersonConverterOld converter;

	private DataGroupFactory dataGroupFactorySpy;
	private DataAtomicFactory dataAtomicFactory;

	@BeforeMethod
	public void beforeMethod() {
		dataGroupFactorySpy = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactorySpy);
		dataAtomicFactory = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactory);
		converter = new DivaFedoraToCoraPersonConverterOld();
	}

	@Test(expectedExceptions = ParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting person to Cora person: Can not read xml: "
			+ "The element type \"pid\" must be terminated by the matching end-tag \"</pid>\".")
	public void parseExceptionShouldBeThrownOnMalformedXML() throws Exception {
		String xml = "<pid></notPid>";
		converter.fromXML(xml);
	}

	@Test
	public void convertFromXML() throws Exception {
		DataGroup personDataGroup = converter
				.fromXML(DivaFedoraToCoraPersonConverterTestData.person11685XML);
		assertEquals(personDataGroup.getNameInData(), TOP_DATAGROUP_NAMEINDATA);
		DataGroup recordInfo = personDataGroup.getFirstGroupWithNameInData("recordInfo");
		assertRecordInfoPersonInDiva(recordInfo);

		assertCorrectIdUsingRecordInfoAndId(recordInfo, "authority-person:11685");

		assertCorrectCreatedByUsingRecordInfoAndUserId(recordInfo, "12345");
		assertCorrectTsCreatedUsingRecordInfoAndTsCreated(recordInfo, "2016-09-02 10:59:47.428");

		assertCorrectHardCodedUpdatedInRecordInfo(recordInfo);

		DataGroup name = personDataGroup.getFirstGroupWithNameInData("authorizedName");
		assertCorrectName(name, "Testsson", "Test", null);

		List<DataGroup> allGroupsWithNameInData = personDataGroup
				.getAllGroupsWithNameInData("alternativeName");

		assertCorrectName(allGroupsWithNameInData.get(0), "Erixon", "Karl", "0");
		assertCorrectName(allGroupsWithNameInData.get(1), "Testsson", "Test", "1");
		assertCorrectName(allGroupsWithNameInData.get(2), "Testsson2", "Test2", "2");
		assertEquals(allGroupsWithNameInData.size(), 3);

		assertEquals(personDataGroup.getFirstAtomicValueWithNameInData("public"), "yes");
	}

	private void assertCorrectHardCodedUpdatedInRecordInfo(DataGroup recordInfo) {
		DataGroup updatedGroup = recordInfo.getFirstGroupWithNameInData("updated");
		assertCorrectUpdatedByUsingUpdatedAndUserId(updatedGroup, "12345");
		assertCorrectTsUpdatedUsingUpdatedAndTsUpdated(updatedGroup, "2018-02-08 10:16:19.538");
		assertEquals(updatedGroup.getRepeatId(), "0");
	}

	private void assertCorrectName(DataGroup dataGroup, String expectedLastName,
			String expectedFirstName, String repeatId) {
		String lastName = dataGroup.getFirstAtomicValueWithNameInData("familyName");
		assertEquals(lastName, expectedLastName);
		String firstName = dataGroup.getFirstAtomicValueWithNameInData("givenName");
		assertEquals(firstName, expectedFirstName);
		if (repeatId != null) {
			assertEquals(dataGroup.getRepeatId(), repeatId);
		}
	}

	@Test
	public void convertFromXMLPerson10000() throws Exception {
		DataGroup personDataGroup = converter
				.fromXML(DivaFedoraToCoraPersonConverterTestData.person10000XML);
		assertEquals(personDataGroup.getNameInData(), TOP_DATAGROUP_NAMEINDATA);
		DataGroup recordInfo = personDataGroup.getFirstGroupWithNameInData("recordInfo");
		assertRecordInfoPersonInDiva(recordInfo);

		assertCorrectIdUsingRecordInfoAndId(recordInfo, "authority-person:10000");

		assertCorrectCreatedByUsingRecordInfoAndUserId(recordInfo, "12345");
		assertCorrectTsCreatedUsingRecordInfoAndTsCreated(recordInfo, "2018-02-19 10:10:43.448");

		assertCorrectHardCodedUpdatedInRecordInfo(recordInfo);

		DataGroup name = personDataGroup.getFirstGroupWithNameInData("authorizedName");
		assertCorrectName(name, "Svensson", "Sven", null);

		List<DataGroup> allGroupsWithNameInData = personDataGroup
				.getAllGroupsWithNameInData("alternativeName");

		assertCorrectName(allGroupsWithNameInData.get(0), "Karlsson", "Sven", "0");
		assertEquals(allGroupsWithNameInData.size(), 1);

	}

	@Test
	public void convertFromXMLPersonNoFirstName() throws Exception {
		DataGroup personDataGroup = converter
				.fromXML(DivaFedoraToCoraPersonConverterTestData.personNoFirstNameXML);
		assertEquals(personDataGroup.getNameInData(), TOP_DATAGROUP_NAMEINDATA);
		DataGroup recordInfo = personDataGroup.getFirstGroupWithNameInData("recordInfo");
		assertRecordInfoPersonInDiva(recordInfo);

		assertCorrectIdUsingRecordInfoAndId(recordInfo, "authority-person:10000");

		assertCorrectCreatedByUsingRecordInfoAndUserId(recordInfo, "12345");
		assertCorrectTsCreatedUsingRecordInfoAndTsCreated(recordInfo, "2018-02-19 10:10:43.448");

		assertCorrectHardCodedUpdatedInRecordInfo(recordInfo);

		DataGroup name = personDataGroup.getFirstGroupWithNameInData("authorizedName");
		assertFalse(name.containsChildWithNameInData("givenName"));
		assertEquals(name.getFirstAtomicValueWithNameInData("familyName"), "Svensson");

	}

	@Test
	public void convertFromXMLPersonNoLastName() throws Exception {
		DataGroup personDataGroup = converter
				.fromXML(DivaFedoraToCoraPersonConverterTestData.personNoLastNameXML);
		assertEquals(personDataGroup.getNameInData(), TOP_DATAGROUP_NAMEINDATA);
		DataGroup recordInfo = personDataGroup.getFirstGroupWithNameInData("recordInfo");
		assertRecordInfoPersonInDiva(recordInfo);

		assertCorrectIdUsingRecordInfoAndId(recordInfo, "authority-person:10000");

		assertCorrectCreatedByUsingRecordInfoAndUserId(recordInfo, "12345");
		assertCorrectTsCreatedUsingRecordInfoAndTsCreated(recordInfo, "2018-02-19 10:10:43.448");

		assertCorrectHardCodedUpdatedInRecordInfo(recordInfo);

		DataGroup name = personDataGroup.getFirstGroupWithNameInData("authorizedName");
		assertFalse(name.containsChildWithNameInData("familyName"));
		assertEquals(name.getFirstAtomicValueWithNameInData("givenName"), "Sven");

	}

	@Test
	public void convertFromXMLPersonNoName() throws Exception {
		DataGroup personDataGroup = converter
				.fromXML(DivaFedoraToCoraPersonConverterTestData.personNoNameXML);
		assertEquals(personDataGroup.getNameInData(), TOP_DATAGROUP_NAMEINDATA);
		DataGroup recordInfo = personDataGroup.getFirstGroupWithNameInData("recordInfo");
		assertRecordInfoPersonInDiva(recordInfo);

		assertCorrectIdUsingRecordInfoAndId(recordInfo, "authority-person:10000");

		assertCorrectCreatedByUsingRecordInfoAndUserId(recordInfo, "12345");
		assertCorrectTsCreatedUsingRecordInfoAndTsCreated(recordInfo, "2018-02-19 10:10:43.448");

		assertCorrectHardCodedUpdatedInRecordInfo(recordInfo);
		assertFalse(personDataGroup.containsChildWithNameInData("authorizedName"));
	}

	@Test
	public void convertFromXMLPersonNoFirstNameInAlternativeName() throws Exception {
		DataGroup personDataGroup = converter
				.fromXML(DivaFedoraToCoraPersonConverterTestData.personNoFirstNameAlternativeXML);
		assertEquals(personDataGroup.getNameInData(), TOP_DATAGROUP_NAMEINDATA);
		DataGroup recordInfo = personDataGroup.getFirstGroupWithNameInData("recordInfo");
		assertRecordInfoPersonInDiva(recordInfo);

		assertCorrectIdUsingRecordInfoAndId(recordInfo, "authority-person:10000");

		assertCorrectCreatedByUsingRecordInfoAndUserId(recordInfo, "12345");
		assertCorrectTsCreatedUsingRecordInfoAndTsCreated(recordInfo, "2018-02-19 10:10:43.448");

		assertCorrectHardCodedUpdatedInRecordInfo(recordInfo);

		List<DataGroup> allGroupsWithNameInData = personDataGroup
				.getAllGroupsWithNameInData("alternativeName");

		DataGroup alternativeName = allGroupsWithNameInData.get(0);
		assertFalse(alternativeName.containsChildWithNameInData("givenName"));
		assertEquals(alternativeName.getFirstAtomicValueWithNameInData("familyName"), "Karlsson");

	}

	@Test
	public void convertFromXMLPersonNoLastNameInAlternativeName() throws Exception {
		DataGroup personDataGroup = converter
				.fromXML(DivaFedoraToCoraPersonConverterTestData.personNoLastNameAlternativeXML);
		assertEquals(personDataGroup.getNameInData(), TOP_DATAGROUP_NAMEINDATA);
		DataGroup recordInfo = personDataGroup.getFirstGroupWithNameInData("recordInfo");
		assertRecordInfoPersonInDiva(recordInfo);

		assertCorrectIdUsingRecordInfoAndId(recordInfo, "authority-person:10000");

		assertCorrectCreatedByUsingRecordInfoAndUserId(recordInfo, "12345");
		assertCorrectTsCreatedUsingRecordInfoAndTsCreated(recordInfo, "2018-02-19 10:10:43.448");

		assertCorrectHardCodedUpdatedInRecordInfo(recordInfo);

		List<DataGroup> allGroupsWithNameInData = personDataGroup
				.getAllGroupsWithNameInData("alternativeName");

		DataGroup alternativeName = allGroupsWithNameInData.get(0);
		assertFalse(alternativeName.containsChildWithNameInData("familyName"));
		assertEquals(alternativeName.getFirstAtomicValueWithNameInData("givenName"), "Sven");

	}

	@Test
	public void convertFromXMLPersonNoAlternativeName() throws Exception {
		DataGroup personDataGroup = converter
				.fromXML(DivaFedoraToCoraPersonConverterTestData.personNoAlternativeNameXML);
		assertEquals(personDataGroup.getNameInData(), TOP_DATAGROUP_NAMEINDATA);
		DataGroup recordInfo = personDataGroup.getFirstGroupWithNameInData("recordInfo");
		assertRecordInfoPersonInDiva(recordInfo);

		assertCorrectIdUsingRecordInfoAndId(recordInfo, "authority-person:10000");

		assertCorrectCreatedByUsingRecordInfoAndUserId(recordInfo, "12345");
		assertCorrectTsCreatedUsingRecordInfoAndTsCreated(recordInfo, "2018-02-19 10:10:43.448");

		assertCorrectHardCodedUpdatedInRecordInfo(recordInfo);

		assertFalse(personDataGroup.containsChildWithNameInData("alternativeName"));

	}

	@Test
	public void convertFromXMLNotPublic() throws Exception {
		DataGroup personDataGroup = converter
				.fromXML(DivaFedoraToCoraPersonConverterTestData.person10000XML);

		assertEquals(personDataGroup.getFirstAtomicValueWithNameInData("public"), "no");

	}
}
