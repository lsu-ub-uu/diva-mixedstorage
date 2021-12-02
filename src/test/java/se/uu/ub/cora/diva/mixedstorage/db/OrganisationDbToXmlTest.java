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

	@Test
	public void testWithParents() {
		setUpDataGroupsWithParentsInSpy();
		DbToXml dbToXml = new OrganisationDbToXml(dbStorage, xmlConverterFactory,
				transformationFactory);
		String xml = dbToXml.toXML("organisation", "45");

		assertEquals(dbStorage.types.get(0), "organisation");
		assertEquals(dbStorage.ids.get(0), "45");
		assertEquals(dbStorage.ids.get(1), "12");
		assertEquals(dbStorage.ids.get(2), "890");
		assertEquals(dbStorage.ids.get(3), "67");
		assertEquals(dbStorage.ids.size(), 4);

		ConverterSpy factoredConverter = xmlConverterFactory.factoredConverter;
		List<DataGroup> returnedDataGroups = dbStorage.returnedDataGroups;
		assertSame(factoredConverter.dataElements.get(0), returnedDataGroups.get(0));
		assertSame(factoredConverter.dataElements.get(1), returnedDataGroups.get(1));
		assertSame(factoredConverter.dataElements.get(2), returnedDataGroups.get(2));

		String convertedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>some returned "
				+ "string from converter spy0some returned string from converter spy1"
				+ "some returned string from converter spy2some returned string from converter "
				+ "spy3";

		assertEquals(xml, convertedXML);
	}

	private void setUpDataGroupsWithParentsInSpy() {
		DataGroupSpy organisation = new DataGroupSpy("organisation");
		DataGroupSpy parent = createParentOrganisationLink("12");
		organisation.addChild(parent);

		DataGroupSpy parent2 = createParentOrganisationLink("67");
		// DataGroupSpy grandParent = createParentOrganisation("167");
		// parent2.addChild(grandParent);
		organisation.addChild(parent2);

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
