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
package se.uu.ub.cora.diva.mixedstorage;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
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

import javax.naming.InitialContext;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.basicstorage.DataStorageException;
import se.uu.ub.cora.basicstorage.RecordStorageInMemoryReadFromDisk;
import se.uu.ub.cora.basicstorage.RecordStorageInstance;
import se.uu.ub.cora.basicstorage.RecordStorageOnDisk;
import se.uu.ub.cora.connection.ContextConnectionProviderImp;
import se.uu.ub.cora.data.DataGroupFactory;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDataToDbTranslaterFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbRecordStorage;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbUpdaterFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.RelatedTableFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.user.DivaMixedUserStorageProvider;
import se.uu.ub.cora.diva.mixedstorage.fedora.DivaFedoraConverterFactory;
import se.uu.ub.cora.diva.mixedstorage.fedora.DivaFedoraConverterFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.fedora.DivaFedoraRecordStorage;
import se.uu.ub.cora.diva.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.gatekeeper.user.UserStorage;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.sqldatabase.RecordCreatorFactoryImp;
import se.uu.ub.cora.sqldatabase.RecordDeleterFactoryImp;
import se.uu.ub.cora.sqldatabase.RecordReaderFactoryImp;
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
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		dataGroupFactory = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactory);
		initInfo = new HashMap<>();
		initInfo.put("storageType", "memory");
		initInfo.put("storageOnDiskBasePath", basePath);
		initInfo.put("fedoraURL", "http://diva-cora-fedora:8088/fedora/");
		initInfo.put("fedoraUsername", "fedoraUser");
		initInfo.put("fedoraPassword", "fedoraPass");
		initInfo.put("databaseLookupName", "java:/comp/env/jdbc/postgres");

		makeSureBasePathExistsAndIsEmpty();
		divaMixedRecordStorageProvider = new DivaMixedRecordStorageProvider();

		userStorageProvider = new UserStorageProviderSpy();
		divaMixedRecordStorageProvider.setUserStorageProvider(userStorageProvider);
		RecordStorageInstance.setInstance(null);
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
		divaMixedRecordStorageProvider.startUsingInitInfo(initInfo);
		DivaMixedRecordStorage recordStorage = (DivaMixedRecordStorage) divaMixedRecordStorageProvider
				.getRecordStorage();
		RecordStorage basicStorage = recordStorage.getBasicStorage();
		assertTrue(basicStorage instanceof RecordStorageInMemoryReadFromDisk);
		assertEquals(((RecordStorageInMemoryReadFromDisk) basicStorage).getBasePath(),
				initInfo.get("storageOnDiskBasePath"));
	}

	@Test
	public void testDivaMixedRecordStorageContainsCorrectBasicStorageOnDisk() {
		initInfo.put("storageType", "disk");
		divaMixedRecordStorageProvider.startUsingInitInfo(initInfo);
		DivaMixedRecordStorage recordStorage = (DivaMixedRecordStorage) divaMixedRecordStorageProvider
				.getRecordStorage();
		RecordStorage basicStorage = recordStorage.getBasicStorage();
		assertTrue(basicStorage instanceof RecordStorageOnDisk);
		assertFalse(basicStorage instanceof RecordStorageInMemoryReadFromDisk);
		assertEquals(((RecordStorageOnDisk) basicStorage).getBasePath(),
				initInfo.get("storageOnDiskBasePath"));
	}

	@Test
	public void testDivaMixedRecordStorageContainsCorrectFedoraStorage() {
		divaMixedRecordStorageProvider.startUsingInitInfo(initInfo);
		DivaMixedRecordStorage recordStorage = (DivaMixedRecordStorage) divaMixedRecordStorageProvider
				.getRecordStorage();
		RecordStorage fedoraStorage = recordStorage.getFedoraStorage();
		assertTrue(fedoraStorage instanceof DivaFedoraRecordStorage);

		DivaFedoraRecordStorage fedoraToCoraStorage = (DivaFedoraRecordStorage) fedoraStorage;
		assertTrue(fedoraToCoraStorage.getHttpHandlerFactory() instanceof HttpHandlerFactoryImp);

		DivaFedoraConverterFactory divaFedoraConverterFactory = fedoraToCoraStorage
				.getDivaFedoraConverterFactory();
		assertTrue(divaFedoraConverterFactory instanceof DivaFedoraConverterFactoryImp);
		String fedoraURLInConverter = ((DivaFedoraConverterFactoryImp) divaFedoraConverterFactory)
				.getFedoraURL();
		assertEquals(fedoraURLInConverter, initInfo.get("fedoraURL"));

		String baseURLInFedoraToCoraStorage = fedoraToCoraStorage.getBaseURL();
		assertEquals(baseURLInFedoraToCoraStorage, initInfo.get("fedoraURL"));

		String fedoraUsername = fedoraToCoraStorage.getFedoraUsername();
		assertEquals(fedoraUsername, initInfo.get("fedoraUsername"));

		String fedoraPassword = fedoraToCoraStorage.getFedoraPassword();
		assertEquals(fedoraPassword, initInfo.get("fedoraPassword"));
	}

	@Test
	public void testDivaMixedRecordStorageContainsCorrectDbStorage() {
		divaMixedRecordStorageProvider.startUsingInitInfo(initInfo);
		DivaMixedRecordStorage recordStorage = (DivaMixedRecordStorage) divaMixedRecordStorageProvider
				.getRecordStorage();
		RecordStorage dbStorageInRecordStorage = recordStorage.getDbStorage();
		assertTrue(dbStorageInRecordStorage instanceof DivaDbRecordStorage);

		DivaDbRecordStorage dbStorage = (DivaDbRecordStorage) dbStorageInRecordStorage;

		RecordReaderFactoryImp recordReaderFactory = assertCorrectRecordReaderFactory(dbStorage);

		assertTrue(dbStorage.getConverterFactory() instanceof DivaDbToCoraConverterFactoryImp);
		assertCorrectRecordStorageForOneTypeFactory(dbStorage);

		DivaDbFactoryImp divaDbToCoraFactory = (DivaDbFactoryImp) dbStorage
				.getDivaDbToCoraFactory();
		assertSame(divaDbToCoraFactory.getReaderFactory(), recordReaderFactory);
		assertSame(divaDbToCoraFactory.getConverterFactory(), dbStorage.getConverterFactory());
	}

	private void assertCorrectRecordStorageForOneTypeFactory(DivaDbRecordStorage dbStorage) {
		DivaDbUpdaterFactoryImp recordStorageForOneTypeFactory = (DivaDbUpdaterFactoryImp) dbStorage
				.getRecordStorageForOneTypeFactory();
		assertTrue(recordStorageForOneTypeFactory instanceof DivaDbUpdaterFactoryImp);

		DivaDataToDbTranslaterFactoryImp translaterFactory = (DivaDataToDbTranslaterFactoryImp) recordStorageForOneTypeFactory
				.getTranslaterFactory();
		assertTrue(translaterFactory instanceof DivaDataToDbTranslaterFactoryImp);
		assertTrue(translaterFactory.getRecordReaderFactory() instanceof RecordReaderFactoryImp);

		ContextConnectionProviderImp connectionProvider = (ContextConnectionProviderImp) recordStorageForOneTypeFactory
				.getSqlConnectionProvider();
		assertCorrectSqlConnectionProvider(connectionProvider);
		assertTrue(recordStorageForOneTypeFactory
				.getRecordReaderFactory() instanceof RecordReaderFactoryImp);
		assertCorrectRelatedTableFactory(recordStorageForOneTypeFactory);

	}

	private void assertCorrectRelatedTableFactory(
			DivaDbUpdaterFactoryImp recordStorageForOneTypeFactory) {
		RelatedTableFactoryImp relatedTableFactory = (RelatedTableFactoryImp) recordStorageForOneTypeFactory
				.getRelatedTableFactory();

		assertTrue(relatedTableFactory.getRecordReaderFactory() instanceof RecordReaderFactoryImp);
		assertTrue(
				relatedTableFactory.getRecordDeleterFactory() instanceof RecordDeleterFactoryImp);
		assertTrue(
				relatedTableFactory.getRecordCreatorFactory() instanceof RecordCreatorFactoryImp);
	}

	private RecordReaderFactoryImp assertCorrectRecordReaderFactory(DivaDbRecordStorage dbStorage) {
		RecordReaderFactoryImp recordReaderFactory = (RecordReaderFactoryImp) dbStorage
				.getRecordReaderFactory();

		ContextConnectionProviderImp readerConnectionProvider = (ContextConnectionProviderImp) recordReaderFactory
				.getSqlConnectionProvider();

		assertCorrectSqlConnectionProvider(readerConnectionProvider);
		return recordReaderFactory;
	}

	private void assertCorrectSqlConnectionProvider(
			ContextConnectionProviderImp connectionProvider) {
		assertEquals(connectionProvider.getName(), initInfo.get("databaseLookupName"));
		assertTrue(connectionProvider.getContext() instanceof InitialContext);
	}

	@Test
	public void testMixedUserStorageProviderIsDefault() throws Exception {
		divaMixedRecordStorageProvider = new DivaMixedRecordStorageProvider();
		assertTrue(divaMixedRecordStorageProvider
				.getUserStorageProvider() instanceof DivaMixedUserStorageProvider);
	}

	@Test
	public void testDivaMixedRecordStorageContainsCorrectDivaStorageFactory() {
		initInfo.put("storageType", "disk");
		divaMixedRecordStorageProvider.startUsingInitInfo(initInfo);
		DivaMixedRecordStorage recordStorage = (DivaMixedRecordStorage) divaMixedRecordStorageProvider
				.getRecordStorage();

		DivaStorageFactoryImp storageFactory = (DivaStorageFactoryImp) recordStorage
				.getStorageFactory();
		assertTrue(storageFactory instanceof DivaStorageFactoryImp);

		RecordReaderFactoryImp recordReaderFactory = (RecordReaderFactoryImp) storageFactory
				.getReaderFactory();

		ContextConnectionProviderImp readerConnectionProvider = (ContextConnectionProviderImp) recordReaderFactory
				.getSqlConnectionProvider();
		assertCorrectSqlConnectionProvider(readerConnectionProvider);

		UserStorage guestUserStorage = storageFactory.getGuestUserStorage();
		assertNotNull(guestUserStorage);
		assertSame(guestUserStorage, userStorageProvider.userStorageSpy);
		assertSame(userStorageProvider.initInfo, initInfo);
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
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"DivaMixedRecordStorageProvider starting DivaMixedRecordStorage...");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 1),
				"Found /tmp/recordStorageOnDiskTempBasicStorageProvider/ as storageOnDiskBasePath");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 2),
				"Found memory as storageType");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 3),
				"Found http://diva-cora-fedora:8088/fedora/ as fedoraURL");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 4),
				"Found java:/comp/env/jdbc/postgres as databaseLookupName");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 5),
				"Found java:/comp/env/jdbc/postgres as databaseLookupName");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 6),
				"Found java:/comp/env/jdbc/postgres as databaseLookupName");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 7),
				"Found java:/comp/env/jdbc/postgres as databaseLookupName");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 8),
				"DivaMixedRecordStorageProvider started DivaMixedRecordStorage");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassName(testedClassName), 9);
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
	public void testLoggingAndErrorIfMissingStartParameterStorageOnDiskBasePath() {
		initInfo.remove("storageOnDiskBasePath");
		try {
			divaMixedRecordStorageProvider.startUsingInitInfo(initInfo);
		} catch (Exception e) {

		}
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"DivaMixedRecordStorageProvider starting DivaMixedRecordStorage...");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassName(testedClassName), 1);
		assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
				"InitInfo must contain storageOnDiskBasePath");
		assertEquals(loggerFactorySpy.getNoOfFatalLogMessagesUsingClassName(testedClassName), 1);
	}

	@Test
	public void testLoggingAndErrorIfMissingStartParameterFedoraURL() {
		assertCorrectErrorAndLogOnMissingParameter("fedoraURL", 3);
	}

	private void assertCorrectErrorAndLogOnMissingParameter(String parameter,
			int noOfInfoMessages) {
		initInfo.remove(parameter);
		String errorMessage = "InitInfo must contain " + parameter;
		try {
			divaMixedRecordStorageProvider.startUsingInitInfo(initInfo);
		} catch (Exception e) {
			assertTrue(e instanceof DataStorageException);
			assertEquals(e.getMessage(), errorMessage);
		}
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"DivaMixedRecordStorageProvider starting DivaMixedRecordStorage...");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassName(testedClassName),
				noOfInfoMessages);
		assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
				errorMessage);
		assertEquals(loggerFactorySpy.getNoOfFatalLogMessagesUsingClassName(testedClassName), 1);
	}

	@Test
	public void testLoggingAndErrorIfMissingStartParameterFedoraUsername() {
		assertCorrectErrorAndLogOnMissingParameter("fedoraUsername", 4);
	}

	@Test
	public void testLoggingAndErrorIfMissingStartParameterFedoraPassword() {
		assertCorrectErrorAndLogOnMissingParameter("fedoraPassword", 4);
	}

	@Test
	public void testLoggingAndErrorIfMissingStartParameterDatabaseLookupName() {
		assertCorrectErrorAndLogOnMissingParameterAndPassedOnError("databaseLookupName", 4);
	}

	private void assertCorrectErrorAndLogOnMissingParameterAndPassedOnError(String parameter,
			int noOfInfoMessages) {
		initInfo.remove(parameter);
		String errorMessage = "InitInfo must contain " + parameter;
		try {
			divaMixedRecordStorageProvider.startUsingInitInfo(initInfo);
		} catch (Exception e) {
			assertTrue(e instanceof DataStorageException);
			assertEquals(e.getMessage(), errorMessage);
			assertTrue(e.getCause() instanceof DataStorageException);

		}
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"DivaMixedRecordStorageProvider starting DivaMixedRecordStorage...");
		assertEquals(loggerFactorySpy.getNoOfInfoLogMessagesUsingClassName(testedClassName),
				noOfInfoMessages);
		assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
				errorMessage);
		assertEquals(loggerFactorySpy.getNoOfFatalLogMessagesUsingClassName(testedClassName), 1);
	}
}
