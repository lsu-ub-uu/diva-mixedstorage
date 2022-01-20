/*
 * Copyright 2019, 2021, 2022 Uppsala University Library
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

import java.util.Map;

import se.uu.ub.cora.basicstorage.DataStorageException;
import se.uu.ub.cora.basicstorage.RecordStorageInMemoryReadFromDisk;
import se.uu.ub.cora.basicstorage.RecordStorageInstance;
import se.uu.ub.cora.basicstorage.RecordStorageOnDisk;
import se.uu.ub.cora.diva.mixedstorage.classic.ClassicIndexerFactory;
import se.uu.ub.cora.diva.mixedstorage.classic.ClassicIndexerFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDataToDbTranslaterFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbRecordStorage;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbUpdaterFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.user.DivaMixedUserStorageProvider;
import se.uu.ub.cora.diva.mixedstorage.fedora.ClassicFedoraUpdaterFactory;
import se.uu.ub.cora.diva.mixedstorage.fedora.ClassicFedoraUpdaterFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.fedora.FedoraConnectionInfo;
import se.uu.ub.cora.diva.mixedstorage.internal.RelatedLinkCollectorFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.internal.RelatedTableFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.internal.RepeatableRelatedLinkCollectorImp;
import se.uu.ub.cora.gatekeeper.user.UserStorage;
import se.uu.ub.cora.gatekeeper.user.UserStorageProvider;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactoryImp;
import se.uu.ub.cora.sqlstorage.DatabaseRecordStorage;
import se.uu.ub.cora.sqlstorage.DatabaseStorageProvider;
import se.uu.ub.cora.storage.MetadataStorage;
import se.uu.ub.cora.storage.MetadataStorageProvider;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.RecordStorageProvider;

public class DivaMixedRecordStorageProvider
		implements RecordStorageProvider, MetadataStorageProvider {

	private Logger log = LoggerProvider.getLoggerForClass(DivaMixedRecordStorageProvider.class);
	private Map<String, String> initInfo;
	private UserStorageProvider userStorageProvider = new DivaMixedUserStorageProvider();
	private DivaStorageFactory divaStorageFactory;
	private UserStorage guestUserStorage;
	private SqlDatabaseFactory sqlDatabaseFactory;

	@Override
	public int getOrderToSelectImplementionsBy() {
		return 1;
	}

	@Override
	public void startUsingInitInfo(Map<String, String> initInfo) {
		this.initInfo = initInfo;
		log.logInfoUsingMessage(
				"DivaMixedRecordStorageProvider starting DivaMixedRecordStorage...");
		startRecordStorage();
		log.logInfoUsingMessage("DivaMixedRecordStorageProvider started DivaMixedRecordStorage");
	}

	private void startRecordStorage() {
		if (noRunningRecordStorageExists()) {
			startNewMixedRecordStorageInstance();
		} else {
			useExistingRecordStorage();
		}
	}

	private boolean noRunningRecordStorageExists() {
		return RecordStorageInstance.getInstance() == null;
	}

	private void startNewMixedRecordStorageInstance() {
		initializeAndStartMixedRecordStorage();
	}

	private void initializeAndStartMixedRecordStorage() {
		DivaMixedDependencies divaMixedDependencies = new DivaMixedDependencies();
		createAndSetBasicStorage(divaMixedDependencies);
		DivaDbRecordStorage classicDbStorage = createAndSetClassicDbStorage(divaMixedDependencies);
		createAndSetUserStorage(divaMixedDependencies);

		DatabaseRecordStorage databaseRecordStorage = createAndSetDatabaseStorage(
				divaMixedDependencies);

		createAndSetClassicFedoraUpdaterFactory(databaseRecordStorage, classicDbStorage,
				divaMixedDependencies);

		createAndSetClassicIndexerFactory(divaMixedDependencies);

		RecordStorage mixedRecordStorage = DivaMixedRecordStorage
				.usingDivaMixedDependencies(divaMixedDependencies);

		setStaticInstance(mixedRecordStorage);
	}

	private void createAndSetBasicStorage(DivaMixedDependencies divaMixedDependencies) {
		String basePath = tryToGetInitParameterLogIfFound("storageOnDiskBasePath");
		String type = tryToGetInitParameterLogIfFound("storageType");
		RecordStorage basicStorage;
		if ("memory".equals(type)) {
			basicStorage = RecordStorageInMemoryReadFromDisk
					.createRecordStorageOnDiskWithBasePath(basePath);
		} else {
			basicStorage = RecordStorageOnDisk.createRecordStorageOnDiskWithBasePath(basePath);
		}
		divaMixedDependencies.setBasicStorage(basicStorage);
	}

	private String tryToGetInitParameterLogIfFound(String parameterName) {
		String basePath = tryToGetInitParameter(parameterName);
		log.logInfoUsingMessage("Found " + basePath + " as " + parameterName);
		return basePath;
	}

	private String tryToGetInitParameter(String parameterName) {
		throwErrorIfKeyIsMissingFromInitInfo(parameterName);
		return initInfo.get(parameterName);
	}

	private DivaDbRecordStorage createAndSetClassicDbStorage(
			DivaMixedDependencies divaMixedDependencies) {
		try {
			String databaseLookupName = tryToGetInitParameterLogIfFound("databaseLookupName");
			sqlDatabaseFactory = SqlDatabaseFactoryImp
					.usingLookupNameFromContext(databaseLookupName);

		} catch (Exception e) {
			throw DataStorageException.withMessageAndException(e.getMessage(), e);
		}
		DivaDbRecordStorage classicDbStorage = createDbStorage(sqlDatabaseFactory);
		divaMixedDependencies.setClassicDbStorage(classicDbStorage);
		return classicDbStorage;
	}

	private DatabaseRecordStorage createAndSetDatabaseStorage(
			DivaMixedDependencies divaMixedDependencies) {
		DatabaseStorageProvider databaseStorageProvider = new DatabaseStorageProvider();
		databaseStorageProvider.startUsingInitInfo(initInfo);
		DatabaseRecordStorage databaseRecordStorage = databaseStorageProvider.getRecordStorage();
		divaMixedDependencies.setDatabaseStorage(databaseRecordStorage);
		return databaseRecordStorage;
	}

	private void createAndSetClassicFedoraUpdaterFactory(DatabaseRecordStorage recordStorage,
			DivaDbRecordStorage classicDbStorage, DivaMixedDependencies divaMixedDependencies) {
		HttpHandlerFactoryImp httpHandlerFactory = new HttpHandlerFactoryImp();

		RepeatableRelatedLinkCollector repeatableLinkCollector = createRepeatableLinkCollector(
				recordStorage, classicDbStorage);
		FedoraConnectionInfo fedoraConnectionInfo = createFedoraConnectionInfo();
		ClassicFedoraUpdaterFactory classicFedoraUpdaterFactory = new ClassicFedoraUpdaterFactoryImp(
				httpHandlerFactory, repeatableLinkCollector, fedoraConnectionInfo);

		divaMixedDependencies.setClassicFedoraUpdaterFactory(classicFedoraUpdaterFactory);
	}

	private RepeatableRelatedLinkCollector createRepeatableLinkCollector(
			DatabaseRecordStorage recordStorage, DivaDbRecordStorage classicDbStorage) {
		RelatedLinkCollectorFactory linkCollectorFactory = new RelatedLinkCollectorFactoryImp(
				recordStorage, classicDbStorage);
		return new RepeatableRelatedLinkCollectorImp(linkCollectorFactory);
	}

	private FedoraConnectionInfo createFedoraConnectionInfo() {
		String fedoraURL = tryToGetInitParameterLogIfFound("fedoraURL");
		String fedoraUsername = tryToGetInitParameter("fedoraUsername");
		String fedoraPassword = tryToGetInitParameter("fedoraPassword");
		return new FedoraConnectionInfo(fedoraURL, fedoraUsername, fedoraPassword);
	}

	private void createAndSetClassicIndexerFactory(DivaMixedDependencies divaMixedDependencies) {
		String classicAuthorityIndexUrl = tryToGetInitParameterLogIfFound("authorityIndexUrl");

		ClassicIndexerFactory classicIndexerFactory = new ClassicIndexerFactoryImp(
				classicAuthorityIndexUrl);
		divaMixedDependencies.setClassicIndexerFactory(classicIndexerFactory);
	}

	private void createAndSetUserStorage(DivaMixedDependencies divaMixedDependencies) {
		guestUserStorage = getUserStorage();
		startDivaStorageFactory();
		RecordStorage userStorage = divaStorageFactory.factorForRecordType("user");
		divaMixedDependencies.setUserStorage(userStorage);
	}

	private void startDivaStorageFactory() {
		if (ifNotDivaStorageFactoryAlreadySetFromTest()) {
			divaStorageFactory = DivaStorageFactoryImp.usingGuestUserStorageAndSqlDatabaseFactory(
					guestUserStorage, sqlDatabaseFactory);
		}
	}

	private boolean ifNotDivaStorageFactoryAlreadySetFromTest() {
		return divaStorageFactory == null;
	}

	private DivaDbRecordStorage createDbStorage(SqlDatabaseFactory recordReaderFactory) {
		DivaDbToCoraConverterFactoryImp divaDbToCoraConverterFactory = new DivaDbToCoraConverterFactoryImp();
		DivaDbFactoryImp divaDbToCoraFactory = new DivaDbFactoryImp(recordReaderFactory,
				divaDbToCoraConverterFactory);
		DivaDbUpdaterFactoryImp recordStorageForOneTypeFactory = createRecordStorageForOneTypeFactory(
				recordReaderFactory);
		return DivaDbRecordStorage.usingRecordReaderFactoryDivaFactoryAndDivaDbUpdaterFactory(
				recordReaderFactory, divaDbToCoraFactory, recordStorageForOneTypeFactory,
				divaDbToCoraConverterFactory);
	}

	private DivaDbUpdaterFactoryImp createRecordStorageForOneTypeFactory(
			SqlDatabaseFactory sqlDatabaseFactory) {
		DivaDataToDbTranslaterFactoryImp translaterFactory = new DivaDataToDbTranslaterFactoryImp(
				sqlDatabaseFactory);

		RelatedTableFactoryImp relatedFactory = RelatedTableFactoryImp
				.usingReaderDeleterAndCreator(sqlDatabaseFactory);

		return new DivaDbUpdaterFactoryImp(translaterFactory, sqlDatabaseFactory, relatedFactory);
	}

	static void setStaticInstance(RecordStorage recordStorage) {
		RecordStorageInstance.setInstance(recordStorage);
	}

	private void useExistingRecordStorage() {
		log.logInfoUsingMessage("Using previously started RecordStorage as RecordStorage");
	}

	private void throwErrorIfKeyIsMissingFromInitInfo(String key) {
		if (!initInfo.containsKey(key)) {
			String errorMessage = "InitInfo must contain " + key;
			log.logFatalUsingMessage(errorMessage);
			throw DataStorageException.withMessage(errorMessage);
		}
	}

	private UserStorage getUserStorage() {
		userStorageProvider.startUsingInitInfo(initInfo);
		return userStorageProvider.getUserStorage();
	}

	@Override
	public MetadataStorage getMetadataStorage() {
		DivaMixedRecordStorage mixedStorage = (DivaMixedRecordStorage) RecordStorageInstance
				.getInstance();
		return (MetadataStorage) mixedStorage.getBasicStorage();
	}

	@Override
	public RecordStorage getRecordStorage() {
		return RecordStorageInstance.getInstance();
	}

	void setUserStorageProvider(UserStorageProvider userStorageProvider) {
		this.userStorageProvider = userStorageProvider;
	}

	UserStorageProvider getUserStorageProvider() {
		return userStorageProvider;
	}

	DivaStorageFactory getDivaStorageFactory() {
		// Needed for test
		return divaStorageFactory;
	}

	public UserStorage getGuestUserStorage() {
		// Needed for test
		return guestUserStorage;
	}

	void setDivaStorageFactory(DivaStorageFactory divaStorageFactory) {
		// Needed for test
		this.divaStorageFactory = divaStorageFactory;
	}

	SqlDatabaseFactory getSqlDatabaseFactory() {
		return sqlDatabaseFactory;
	}

}
