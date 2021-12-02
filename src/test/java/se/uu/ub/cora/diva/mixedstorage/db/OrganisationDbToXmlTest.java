package se.uu.ub.cora.diva.mixedstorage.db;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.basicdata.CoraDataGroupFactory;
import se.uu.ub.cora.data.DataGroupFactory;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.RelatedTableFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactoryImp;
import se.uu.ub.cora.xmlconverter.XmlConverterFactory;
import se.uu.ub.cora.xmlutils.transformer.XsltTransformationFactory;

public class OrganisationDbToXmlTest {

	private SqlDatabaseFactory sqlDatabaseFactory;
	private DivaDbRecordStorage dbStorage;
	private LoggerFactorySpy loggerFactorySpy;
	private XmlConverterFactory xmlConverterFactory;
	private XsltTransformationFactory transformationFactory;

	@BeforeMethod
	public void setUp() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		// Change to spy later
		DataGroupFactory dataGroupFactory = new CoraDataGroupFactory();
		DataGroupProvider.setDataGroupFactory(dataGroupFactory);

		sqlDatabaseFactory = SqlDatabaseFactoryImp.usingUriAndUserAndPassword(
				"jdbc:postgresql://diva-docker-mock-classic-postgresql:5432/diva", "diva", "diva");

		DivaDbToCoraConverterFactoryImp divaDbToCoraConverterFactory = new DivaDbToCoraConverterFactoryImp();
		DivaDbFactoryImp divaDbToCoraFactory = new DivaDbFactoryImp(sqlDatabaseFactory,
				divaDbToCoraConverterFactory);
		DivaDbUpdaterFactoryImp recordStorageForOneTypeFactory = createRecordStorageForOneTypeFactory(
				sqlDatabaseFactory);
		dbStorage = DivaDbRecordStorage.usingRecordReaderFactoryDivaFactoryAndDivaDbUpdaterFactory(
				sqlDatabaseFactory, divaDbToCoraFactory, recordStorageForOneTypeFactory,
				divaDbToCoraConverterFactory);
		xmlConverterFactory = new XmlConverterFactory();
		transformationFactory = new XsltTransformationFactory();

		// RecordStorage dbStorage = DivaDbRecordStorage
		// .usingRecordReaderFactoryDivaFactoryAndDivaDbUpdaterFactory(sqlDatabaseFactory,
		// null, null, null);

	}

	private DivaDbUpdaterFactoryImp createRecordStorageForOneTypeFactory(
			SqlDatabaseFactory sqlDatabaseFactory) {
		DivaDataToDbTranslaterFactoryImp translaterFactory = new DivaDataToDbTranslaterFactoryImp(
				sqlDatabaseFactory);

		RelatedTableFactoryImp relatedFactory = RelatedTableFactoryImp
				.usingReaderDeleterAndCreator(sqlDatabaseFactory);

		return new DivaDbUpdaterFactoryImp(translaterFactory, sqlDatabaseFactory, relatedFactory);
	}

	@Test
	public void testInit() {
		DbToXml dbToXml = new OrganisationDbToXml(dbStorage, xmlConverterFactory,
				transformationFactory);
		String xml = dbToXml.read("someRecordType", "1104");
		assertEquals(xml, "");
	}
}
