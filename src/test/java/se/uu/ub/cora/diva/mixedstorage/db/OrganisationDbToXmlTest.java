/*
 * Copyright 2021, 2022 Uppsala University Library
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.fedora.ConverterFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.fedora.ConverterSpy;
import se.uu.ub.cora.diva.mixedstorage.fedora.TransformationFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;
import se.uu.ub.cora.xmlutils.transformer.CoraTransformationFactory;

public class OrganisationDbToXmlTest {

	private SqlDatabaseFactory sqlDatabaseFactory;
	private DbStorageSpy dbStorage;
	private LoggerFactorySpy loggerFactorySpy;
	private ConverterFactorySpy xmlConverterFactory;
	private CoraTransformationFactory transformationFactory;

	// @BeforeMethod
	// public void setUp() {
	// loggerFactorySpy = new LoggerFactorySpy();
	// LoggerProvider.setLoggerFactory(loggerFactorySpy);
	// // Change to spy later
	// DataGroupFactory dataGroupFactory = new CoraDataGroupFactory();
	// DataGroupProvider.setDataGroupFactory(dataGroupFactory);
	//
	// sqlDatabaseFactory = SqlDatabaseFactoryImp.usingUriAndUserAndPassword(
	// "jdbc:postgresql://diva-docker-mock-classic-postgresql:5432/diva", "diva", "diva");
	//
	// DivaDbToCoraConverterFactoryImp divaDbToCoraConverterFactory = new
	// DivaDbToCoraConverterFactoryImp();
	// DivaDbFactoryImp divaDbToCoraFactory = new DivaDbFactoryImp(sqlDatabaseFactory,
	// divaDbToCoraConverterFactory);
	// DivaDbUpdaterFactoryImp recordStorageForOneTypeFactory =
	// createRecordStorageForOneTypeFactory(
	// sqlDatabaseFactory);
	// dbStorage = DivaDbRecordStorage.usingRecordReaderFactoryDivaFactoryAndDivaDbUpdaterFactory(
	// sqlDatabaseFactory, divaDbToCoraFactory, recordStorageForOneTypeFactory,
	// divaDbToCoraConverterFactory);
	// xmlConverterFactory = new XmlConverterFactory();
	// transformationFactory = new XsltTransformationFactory();
	// }
	//
	// private DivaDbUpdaterFactoryImp createRecordStorageForOneTypeFactory(
	// SqlDatabaseFactory sqlDatabaseFactory) {
	// DivaDataToDbTranslaterFactoryImp translaterFactory = new DivaDataToDbTranslaterFactoryImp(
	// sqlDatabaseFactory);
	//
	// RelatedTableFactoryImp relatedFactory = RelatedTableFactoryImp
	// .usingReaderDeleterAndCreator(sqlDatabaseFactory);
	//
	// return new DivaDbUpdaterFactoryImp(translaterFactory, sqlDatabaseFactory, relatedFactory);
	// }

	@BeforeMethod
	public void setUp() {
		dbStorage = new DbStorageSpy();
		xmlConverterFactory = new ConverterFactorySpy();
		transformationFactory = new TransformationFactorySpy();
	}

	@Test
	public void testNoParents() {
		DbToXml dbToXml = new OrganisationDbToXml(dbStorage, xmlConverterFactory,
				transformationFactory);
		String xml = dbToXml.toXML("someRecordType", "1104");

		assertEquals(dbStorage.types.get(0), "someRecordType");
		DataGroup dataGroup = dbStorage.returnedDataGroups.get(0);

		ConverterSpy factoredConverter = xmlConverterFactory.factoredConverter;
		assertSame(factoredConverter.dataElements.get(0), dataGroup);
		String convertedXML = factoredConverter.returnedStrings.get(0);

		assertEquals(xml, convertedXML);
	}

	// om samma parent förekommer 2 ggr ska den bara finnas en gång i filen

	@Test
	public void testWithParents() {
		createAndSetUpDataGroupsWithParentsInSpy();
		DbToXml dbToXml = new OrganisationDbToXml(dbStorage, xmlConverterFactory,
				transformationFactory);
		String xml = dbToXml.toXML("organisation", "45");

		assertEquals(dbStorage.types.get(0), "organisation");
		assertEquals(dbStorage.ids.get(0), "45");
		assertEquals(dbStorage.ids.get(1), "12");
		assertEquals(dbStorage.ids.get(2), "890");
		assertEquals(dbStorage.ids.get(3), "67");
		// beror på om vi ska kolla om id finns i mappen innan vi läser från databasen
		assertEquals(dbStorage.ids.size(), 5);

		ConverterSpy factoredConverter = xmlConverterFactory.factoredConverter;
		List<DataGroup> returnedDataGroups = dbStorage.returnedDataGroups;
		assertSame(factoredConverter.dataElements.get(0), returnedDataGroups.get(0));
		assertSame(factoredConverter.dataElements.get(1), returnedDataGroups.get(1));
		assertSame(factoredConverter.dataElements.get(2), returnedDataGroups.get(3));
		assertSame(factoredConverter.dataElements.get(3), returnedDataGroups.get(4));

		String convertedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>some returned "
				+ "string from converter spy0some returned string from converter spy1"
				+ "some returned string from converter spy2some returned string from converter "
				+ "spy3";

		assertEquals(xml, convertedXML);
	}

	private void createAndSetUpDataGroupsWithParentsInSpy() {
		DataGroupSpy organisation = new DataGroupSpy("organisation");
		DataGroupSpy parent = createParentOrganisationLink("12");
		organisation.addChild(parent);

		DataGroupSpy parent2 = createParentOrganisationLink("67");
		organisation.addChild(parent2);
		DataGroupSpy parentSameIdAsGrandParent = createParentOrganisationLink("890");
		organisation.addChild(parentSameIdAsGrandParent);

		Map<String, DataGroup> answerToReturn = new HashMap<>();
		answerToReturn.put("organisation_45", organisation);

		DataGroupSpy parentWithParent = new DataGroupSpy("organisation");
		DataGroupSpy grandParent = createParentOrganisationLink("890");
		parentWithParent.addChild(grandParent);
		answerToReturn.put("organisation_12", parentWithParent);

		dbStorage.dataGroupsToReturn = answerToReturn;

	}

	private DataGroupSpy createParentOrganisationLink(String linkedRecordId) {
		DataGroupSpy parentOrg = new DataGroupSpy("parentOrganisation");
		DataGroupSpy organisationLink = new DataGroupSpy("organisationLink");
		organisationLink.addChild(new DataAtomicSpy("linkedRecordId", linkedRecordId));
		parentOrg.addChild(organisationLink);
		return parentOrg;
	}
}
