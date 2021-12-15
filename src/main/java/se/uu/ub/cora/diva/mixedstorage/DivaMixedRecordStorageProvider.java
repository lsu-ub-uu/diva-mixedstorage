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

import java.util.Map;

import se.uu.ub.cora.basicstorage.DataStorageException;
import se.uu.ub.cora.basicstorage.RecordStorageInMemoryReadFromDisk;
import se.uu.ub.cora.basicstorage.RecordStorageInstance;
import se.uu.ub.cora.basicstorage.RecordStorageOnDisk;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDataToDbTranslaterFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbRecordStorage;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbUpdaterFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.RelatedLinkCollectorFactory;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.RelatedLinkCollectorFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.RelatedTableFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.RepeatableRelatedLinkCollector;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.RepeatableRelatedLinkCollectorImp;
import se.uu.ub.cora.diva.mixedstorage.db.user.DivaMixedUserStorageProvider;
import se.uu.ub.cora.diva.mixedstorage.fedora.ClassicFedoraUpdaterFactory;
import se.uu.ub.cora.diva.mixedstorage.fedora.ClassicFedoraUpdaterFactoryImp;
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
		RecordStorage basicStorage = createBasicStorage();
		try {
			String databaseLookupName = tryToGetInitParameterLogIfFound("databaseLookupName");
			sqlDatabaseFactory = SqlDatabaseFactoryImp
					.usingLookupNameFromContext(databaseLookupName);

		} catch (Exception e) {
			throw DataStorageException.withMessageAndException(e.getMessage(), e);
		}
		DivaDbRecordStorage classicDbStorage = createDbStorage(sqlDatabaseFactory);
		RecordStorage userStorage = createUserStorage();
		DatabaseStorageProvider databaseStorageProvider = new DatabaseStorageProvider();
		databaseStorageProvider.startUsingInitInfo(initInfo);
		DatabaseRecordStorage recordStorage = databaseStorageProvider.getRecordStorage();

		ClassicFedoraUpdaterFactory fedoraUpdaterFactory = createClassicFedoraUpdaterFactory(
				recordStorage, classicDbStorage);

		RecordStorage mixedRecordStorage = DivaMixedRecordStorage
				.usingBasicStorageClassicDbStorageUserStorageAndDatabaseStorage(basicStorage,
						classicDbStorage, userStorage, recordStorage, fedoraUpdaterFactory);
		setStaticInstance(mixedRecordStorage);
	}

	private ClassicFedoraUpdaterFactory createClassicFedoraUpdaterFactory(
			DatabaseRecordStorage recordStorage, DivaDbRecordStorage classicDbStorage) {
		HttpHandlerFactoryImp httpHandlerFactory = new HttpHandlerFactoryImp();

		RelatedLinkCollectorFactory linkCollectorFactory = new RelatedLinkCollectorFactoryImp(
				recordStorage, classicDbStorage);
		RepeatableRelatedLinkCollector repeatableLinkCollector = new RepeatableRelatedLinkCollectorImp(
				linkCollectorFactory);
		String fedoraURL = tryToGetInitParameterLogIfFound("fedoraURL");
		String fedoraUsername = tryToGetInitParameter("fedoraUsername");
		String fedoraPassword = tryToGetInitParameter("fedoraPassword");
		return new ClassicFedoraUpdaterFactoryImp(httpHandlerFactory, repeatableLinkCollector,
				fedoraURL, fedoraUsername, fedoraPassword);
	}

	private RecordStorage createBasicStorage() {
		String basePath = tryToGetInitParameterLogIfFound("storageOnDiskBasePath");
		String type = tryToGetInitParameterLogIfFound("storageType");
		if ("memory".equals(type)) {
			return RecordStorageInMemoryReadFromDisk
					.createRecordStorageOnDiskWithBasePath(basePath);
		}
		return RecordStorageOnDisk.createRecordStorageOnDiskWithBasePath(basePath);
	}

	private String tryToGetInitParameterLogIfFound(String parameterName) {
		String basePath = tryToGetInitParameter(parameterName);
		log.logInfoUsingMessage("Found " + basePath + " as " + parameterName);
		return basePath;
	}

	private RecordStorage createUserStorage() {
		guestUserStorage = getUserStorage();
		startDivaStorageFactory();
		return divaStorageFactory.factorForRecordType("user");
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

	private String tryToGetInitParameter(String parameterName) {
		throwErrorIfKeyIsMissingFromInitInfo(parameterName);
		return initInfo.get(parameterName);
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
