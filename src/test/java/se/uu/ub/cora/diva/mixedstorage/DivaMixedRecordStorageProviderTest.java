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
package se.uu.ub.cora.diva.mixedstorage;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.basicstorage.DataStorageException;
import se.uu.ub.cora.basicstorage.RecordStorageInMemoryReadFromDisk;
import se.uu.ub.cora.basicstorage.RecordStorageInstance;
import se.uu.ub.cora.basicstorage.RecordStorageOnDisk;
import se.uu.ub.cora.data.DataGroupFactory;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDataToDbTranslaterFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbRecordStorage;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbUpdaterFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.RelatedLinkCollectorFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.RelatedTableFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.RepeatableRelatedLinkCollectorImp;
import se.uu.ub.cora.diva.mixedstorage.db.user.DivaMixedUserStorageProvider;
import se.uu.ub.cora.diva.mixedstorage.fedora.ClassicFedoraUpdaterFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactoryImp;
import se.uu.ub.cora.sqlstorage.DatabaseRecordStorage;
import se.uu.ub.cora.storage.MetadataStorage;
import se.uu.ub.cora.storage.MetadataStorageProvider;
import se.uu.ub.cora.storage.RecordStorage;

public class DivaMixedRecordStorageProviderTest {
	private Map<String, String> initInfo = new HashMap<>();
	private String basePath = "/tmp/recordStorageOnDiskTempBasicStorageProvider/";
	private LoggerFactorySpy loggerFactorySpy;
	private String testedClassName = "DivaMixedRecordStorageProvider";
	private DivaMixedRecordStorageProvider divaMixedRecordStorageProvider;
	private DataGroupFactory dataGroupFactory;
	private UserStorageProviderSpy userStorageProvider;

	@BeforeMethod
	public void beforeMethod() throws Exception {
		setUpFactories();
		setUpDefaultInitInfo();

		makeSureBasePathExistsAndIsEmpty();
		divaMixedRecordStorageProvider = new DivaMixedRecordStorageProvider();
		userStorageProvider = new UserStorageProviderSpy();
		divaMixedRecordStorageProvider.setUserStorageProvider(userStorageProvider);
		RecordStorageInstance.setInstance(null);
	}

	private void setUpFactories() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		dataGroupFactory = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactory);
	}

	private void setUpDefaultInitInfo() {
		initInfo = new HashMap<>();
		initInfo.put("storageType", "memory");
		initInfo.put("storageOnDiskBasePath", basePath);
		initInfo.put("databaseLookupName", "java:/comp/env/jdbc/postgres");
		initInfo.put("coraDatabaseLookupName", "java:/comp/env/jdbc/coraPostgres");
		initInfo.put("fedoraURL", "http://diva-cora-fedora:8088/fedora/");
		initInfo.put("fedoraUsername", "fedoraUser");
		initInfo.put("fedoraPassword", "fedoraPass");

	}

	public void makeSureBasePathExistsAndIsEmpty() throws IOException {
		File dir = new File(basePath);
		dir.mkdir();
		deleteFiles(basePath);

	}

	private void deleteFiles(String path) throws IOException {
		Stream<Path> list;
		list = Files.list(Paths.get(path));

		list.forEach(p -> deleteFile(p));
		list.close();
	}

	private void deleteFile(Path path) {
		try {
			if (path.toFile().isDirectory()) {
				deleteFiles(path.toString());
			}
			Files.delete(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetOrderToSelectImplementationsByIsOne() {
		assertEquals(divaMixedRecordStorageProvider.getOrderToSelectImplementionsBy(), 1);
	}

	@Test
	public void testNormalStartupReturnsDivaMixedRecordStorage() {
		divaMixedRecordStorageProvider.startUsingInitInfo(initInfo);
		RecordStorage recordStorage = divaMixedRecordStorageProvider.getRecordStorage();
		assertTrue(recordStorage instanceof DivaMixedRecordStorage);
	}

	@Test
	public void testDivaMixedRecordStorageContainsCorrectBasicStorageinMemory() {
		DivaMixedRecordStorage recordStorage = startRecordStorage();
		RecordStorage basicStorage = recordStorage.getBasicStorage();
		assertTrue(basicStorage instanceof RecordStorageInMemoryReadFromDisk);
		assertEquals(((RecordStorageInMemoryReadFromDisk) basicStorage).getBasePath(),
				initInfo.get("storageOnDiskBasePath"));
	}

	private DivaMixedRecordStorage startRecordStorage() {
		divaMixedRecordStorageProvider.startUsingInitInfo(initInfo);
		DivaMixedRecordStorage recordStorage = (DivaMixedRecordStorage) divaMixedRecordStorageProvider
				.getRecordStorage();
		return recordStorage;
	}

	@Test
	public void testDivaMixedRecordStorageContainsCorrectBasicStorageOnDisk() {
		initInfo.put("storageType", "disk");
		DivaMixedRecordStorage recordStorage = startRecordStorage();

		RecordStorage basicStorage = recordStorage.getBasicStorage();
		assertTrue(basicStorage instanceof RecordStorageOnDisk);
		assertFalse(basicStorage instanceof RecordStorageInMemoryReadFromDisk);
		assertEquals(((RecordStorageOnDisk) basicStorage).getBasePath(),
				initInfo.get("storageOnDiskBasePath"));
	}

	@Test
	public void testDivaMixedRecordStorageContainsCorrectDbStorage() {
		DivaMixedRecordStorage recordStorage = startRecordStorage();

		RecordStorage classicDbStorage = recordStorage.getClassicDbStorage();
		assertTrue(classicDbStorage instanceof DivaDbRecordStorage);

		DivaDbRecordStorage divaClassicDbStorage = (DivaDbRecordStorage) classicDbStorage;

		SqlDatabaseFactoryImp sqlDatabaseFactory = assertCorrectSqlDatabaseFactory(
				divaClassicDbStorage);

		assertTrue(divaClassicDbStorage
				.getConverterFactory() instanceof DivaDbToCoraConverterFactoryImp);
		assertCorrectRecordStorageForOneTypeFactory(divaClassicDbStorage, sqlDatabaseFactory);

		DivaDbFactoryImp divaDbToCoraFactory = (DivaDbFactoryImp) divaClassicDbStorage
				.getDivaDbToCoraFactory();
		assertSame(divaDbToCoraFactory.getConverterFactory(),
				divaClassicDbStorage.getConverterFactory());
	}

	private void assertCorrectRecordStorageForOneTypeFactory(DivaDbRecordStorage dbStorage,
			SqlDatabaseFactoryImp sqlDatabaseFactory) {
		DivaDbUpdaterFactoryImp recordStorageForOneTypeFactory = (DivaDbUpdaterFactoryImp) dbStorage
				.getRecordStorageForOneTypeFactory();
		assertTrue(recordStorageForOneTypeFactory instanceof DivaDbUpdaterFactoryImp);

		DivaDataToDbTranslaterFactoryImp translaterFactory = (DivaDataToDbTranslaterFactoryImp) recordStorageForOneTypeFactory
				.getTranslaterFactory();
		assertTrue(translaterFactory instanceof DivaDataToDbTranslaterFactoryImp);

		RelatedTableFactoryImp relatedTableFactory = (RelatedTableFactoryImp) recordStorageForOneTypeFactory
				.getRelatedTableFactory();
		assertTrue(relatedTableFactory.getSqlDatabaseFactory() instanceof SqlDatabaseFactoryImp);
		assertSame(relatedTableFactory.getSqlDatabaseFactory(), sqlDatabaseFactory);
	}

	private SqlDatabaseFactoryImp assertCorrectSqlDatabaseFactory(DivaDbRecordStorage dbStorage) {
		SqlDatabaseFactoryImp sqlDatabaseFactory = (SqlDatabaseFactoryImp) dbStorage
				.getSqlDatabaseFactory();
		assertEquals(sqlDatabaseFactory.onlyForTestGetLookupName(),
				initInfo.get("databaseLookupName"));
		return sqlDatabaseFactory;
	}

	@Test
	public void testDivaMixedRecordStorageContainsCorrectDatabaseStorageProvider() {
		DivaMixedRecordStorage recordStorage = startRecordStorage();

		assertTrue(recordStorage.getDatabaseStorage() instanceof DatabaseRecordStorage);

	}

	@Test
	public void testMixedUserStorageProviderIsDefault() throws Exception {
		divaMixedRecordStorageProvider = new DivaMixedRecordStorageProvider();
		assertTrue(divaMixedRecordStorageProvider
				.getUserStorageProvider() instanceof DivaMixedUserStorageProvider);
	}

	@Test
	public void testStorageFactoryOnInit() {
		initInfo.put("storageType", "disk");
		divaMixedRecordStorageProvider.startUsingInitInfo(initInfo);
		assertTrue(divaMixedRecordStorageProvider
				.getDivaStorageFactory() instanceof DivaStorageFactoryImp);

		DivaStorageFactoryImp divaStorageFactory = (DivaStorageFactoryImp) divaMixedRecordStorageProvider
				.getDivaStorageFactory();
		assertSame(divaStorageFactory.getGuestUserStorage(),
				divaMixedRecordStorageProvider.getGuestUserStorage());
		assertSame(divaStorageFactory.getSqldatabaseFactory(),
				divaMixedRecordStorageProvider.getSqlDatabaseFactory());
	}

	@Test
	public void testDivaMixedRecordStorageContainsCorrectDivaStorage() {
		initInfo.put("storageType", "disk");
		DivaStorageFactorySpy divaStorageFactory = new DivaStorageFactorySpy();
		divaMixedRecordStorageProvider.setDivaStorageFactory(divaStorageFactory);
		divaMixedRecordStorageProvider.startUsingInitInfo(initInfo);
		DivaMixedRecordStorage recordStorage = (DivaMixedRecordStorage) divaMixedRecordStorageProvider
				.getRecordStorage();

		RecordStorageSpy userStorage = (RecordStorageSpy) recordStorage.getUserStorage();
		assertSame(userStorage, divaStorageFactory.factored);
	}

	@Test
	public void testNormalStartupReturnsTheSameRecordStorageForMultipleCalls() {
		divaMixedRecordStorageProvider.startUsingInitInfo(initInfo);
		RecordStorage recordStorage = divaMixedRecordStorageProvider.getRecordStorage();
		RecordStorage recordStorage2 = divaMixedRecordStorageProvider.getRecordStorage();
		assertSame(recordStorage, recordStorage2);
	}

	@Test
	public void testRecordStorageStartedByOtherProviderIsReturned() {
		RecordStorageSpy recordStorageSpy = new RecordStorageSpy();
		RecordStorageInstance.setInstance(recordStorageSpy);
		divaMixedRecordStorageProvider.startUsingInitInfo(initInfo);
		RecordStorage recordStorage = divaMixedRecordStorageProvider.getRecordStorage();
		assertSame(recordStorage, recordStorageSpy);
	}

	@Test
	public void testLoggingNormalStartup() {
		divaMixedRecordStorageProvider.startUsingInitInfo(initInfo);
		assertTrue(loggerFactorySpy.infoLogMessageUsingClassNameExists(testedClassName,
				"DivaMixedRecordStorageProvider starting DivaMixedRecordStorage..."));
		assertTrue(loggerFactorySpy.infoLogMessageUsingClassNameExists(testedClassName,
				"Found /tmp/recordStorageOnDiskTempBasicStorageProvider/ as storageOnDiskBasePath"));
		assertTrue(loggerFactorySpy.infoLogMessageUsingClassNameExists(testedClassName,
				"Found memory as storageType"));
		assertTrue(loggerFactorySpy.infoLogMessageUsingClassNameExists(testedClassName,
				"Found java:/comp/env/jdbc/postgres as databaseLookupName"));
		assertTrue(loggerFactorySpy.infoLogMessageUsingClassNameExists(testedClassName,
				"DivaMixedRecordStorageProvider started DivaMixedRecordStorage"));
	}

	@Test
	public void testLoggingRecordStorageStartedByOtherProvider() {
		RecordStorageSpy recordStorageSpy = new RecordStorageSpy();
		RecordStorageInstance.setInstance(recordStorageSpy);
		divaMixedRecordStorageProvider.startUsingInitInfo(initInfo);
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"DivaMixedRecordStorageProvider starting DivaMixedRecordStorage...");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 1),
				"Using previously started RecordStorage as RecordStorage");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 2),
				"DivaMixedRecordStorageProvider started DivaMixedRecordStorage");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassName(testedClassName), 3);
	}

	@Test
	public void testRecordStorageIsAccessibleToOthers() {
		divaMixedRecordStorageProvider.startUsingInitInfo(initInfo);
		RecordStorage recordStorage = divaMixedRecordStorageProvider.getRecordStorage();
		assertSame(recordStorage, RecordStorageInstance.getInstance());
	}

	@Test
	public void testMetadataStorageIsRecordStorage() {
		divaMixedRecordStorageProvider.startUsingInitInfo(initInfo);
		MetadataStorageProvider metadataStorageProvider = divaMixedRecordStorageProvider;
		DivaMixedRecordStorage recordStorage = (DivaMixedRecordStorage) divaMixedRecordStorageProvider
				.getRecordStorage();
		MetadataStorage metadataStorage = metadataStorageProvider.getMetadataStorage();
		assertSame(metadataStorage, recordStorage.getBasicStorage());
	}

	@Test
	public void testCorrectFedoraUpdaterFactory() {
		divaMixedRecordStorageProvider.startUsingInitInfo(initInfo);
		DivaMixedRecordStorage recordStorage = (DivaMixedRecordStorage) divaMixedRecordStorageProvider
				.getRecordStorage();
		ClassicFedoraUpdaterFactoryImp fedoraUpdaterFactory = (ClassicFedoraUpdaterFactoryImp) recordStorage
				.getFedoraUpdaterFactory();
		assertTrue(fedoraUpdaterFactory.getHttpHandlerFactory() instanceof HttpHandlerFactoryImp);
		assertEquals(fedoraUpdaterFactory.getFedoraUrl(), initInfo.get("fedoraURL"));
		assertEquals(fedoraUpdaterFactory.getUserName(), initInfo.get("fedoraUsername"));
		assertEquals(fedoraUpdaterFactory.getPassword(), initInfo.get("fedoraPassword"));

		RepeatableRelatedLinkCollectorImp repeatableRelatedLinkCollector = (RepeatableRelatedLinkCollectorImp) fedoraUpdaterFactory
				.getRepeatableRelatedLinkCollector();
		RelatedLinkCollectorFactoryImp linkCollectorFactory = (RelatedLinkCollectorFactoryImp) repeatableRelatedLinkCollector
				.getRelatedLinkCollectorFactory();

		assertSame(linkCollectorFactory.getRecordStorage(), recordStorage.getDatabaseStorage());

	}

	@Test
	public void testLoggingAndErrorIfMissingStartParameterStorageOnDiskBasePath() {
		initInfo.remove("storageOnDiskBasePath");
		try {
			divaMixedRecordStorageProvider.startUsingInitInfo(initInfo);
		} catch (Exception e) {

		}
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"DivaMixedRecordStorageProvider starting DivaMixedRecordStorage...");
		assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
				"InitInfo must contain storageOnDiskBasePath");
		assertEquals(loggerFactorySpy.getNoOfFatalLogMessagesUsingClassName(testedClassName), 1);
	}

	@Test
	public void testLoggingAndErrorIfMissingStartParameterDatabaseLookupName() {
		initInfo.remove("databaseLookupName");
		String errorMessage = "InitInfo must contain " + "databaseLookupName";
		try {
			divaMixedRecordStorageProvider.startUsingInitInfo(initInfo);
		} catch (Exception e) {
			assertTrue(e instanceof DataStorageException);
			assertEquals(e.getMessage(), errorMessage);
			assertTrue(e.getCause() instanceof DataStorageException);

		}
		assertTrue(loggerFactorySpy.infoLogMessageUsingClassNameExists(testedClassName,
				"DivaMixedRecordStorageProvider starting DivaMixedRecordStorage..."));
		assertTrue(loggerFactorySpy.fatalLogMessageUsingClassNameExists(testedClassName,
				errorMessage));
		assertEquals(loggerFactorySpy.getNoOfFatalLogMessagesUsingClassName(testedClassName), 1);
	}

}
