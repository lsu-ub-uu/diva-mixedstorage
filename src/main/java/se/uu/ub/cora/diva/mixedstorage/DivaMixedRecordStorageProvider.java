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

import java.util.Map;

import javax.naming.InitialContext;

import se.uu.ub.cora.basicstorage.DataStorageException;
import se.uu.ub.cora.basicstorage.RecordStorageInMemoryReadFromDisk;
import se.uu.ub.cora.basicstorage.RecordStorageInstance;
import se.uu.ub.cora.basicstorage.RecordStorageOnDisk;
import se.uu.ub.cora.connection.ContextConnectionProviderImp;
import se.uu.ub.cora.connection.SqlConnectionProvider;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbTranslaterFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraRecordStorage;
import se.uu.ub.cora.diva.mixedstorage.fedora.DivaFedoraConverterFactory;
import se.uu.ub.cora.diva.mixedstorage.fedora.DivaFedoraConverterFactoryImp;
import se.uu.ub.cora.diva.mixedstorage.fedora.DivaFedoraRecordStorage;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.sqldatabase.RecordReaderFactoryImp;
import se.uu.ub.cora.sqldatabase.RecordUpdaterFactory;
import se.uu.ub.cora.sqldatabase.RecordUpdaterFactoryImp;
import se.uu.ub.cora.storage.MetadataStorage;
import se.uu.ub.cora.storage.MetadataStorageProvider;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.RecordStorageProvider;

public class DivaMixedRecordStorageProvider
		implements RecordStorageProvider, MetadataStorageProvider {

	private Logger log = LoggerProvider.getLoggerForClass(DivaMixedRecordStorageProvider.class);
	private Map<String, String> initInfo;

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
			startNewRecordStorageOnDiskInstance();
		} else {
			useExistingRecordStorage();
		}
	}

	private boolean noRunningRecordStorageExists() {
		return RecordStorageInstance.getInstance() == null;
	}

	private void startNewRecordStorageOnDiskInstance() {
		RecordStorage basicStorage = createBasicStorage();
		DivaFedoraRecordStorage fedoraStorage = createFedoraStorage();

		DivaDbToCoraRecordStorage dbStorage = createDbStorage();

		RecordStorage mixedRecordStorage = DivaMixedRecordStorage
				.usingBasicAndFedoraAndDbStorage(basicStorage, fedoraStorage, dbStorage);
		setStaticInstance(mixedRecordStorage);
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

	private DivaFedoraRecordStorage createFedoraStorage() {
		String fedoraURL = tryToGetInitParameterLogIfFound("fedoraURL");
		String fedoraUsername = tryToGetInitParameter("fedoraUsername");
		String fedoraPassword = tryToGetInitParameter("fedoraPassword");

		HttpHandlerFactory httpHandlerFactory = new HttpHandlerFactoryImp();

		DivaFedoraConverterFactory converterFactory = DivaFedoraConverterFactoryImp
				.usingFedoraURL(fedoraURL);

		return DivaFedoraRecordStorage
				.usingHttpHandlerFactoryAndConverterFactoryAndBaseURLAndUsernameAndPassword(
						httpHandlerFactory, converterFactory, fedoraURL, fedoraUsername,
						fedoraPassword);
	}

	private DivaDbToCoraRecordStorage createDbStorage() {
		RecordReaderFactoryImp recordReaderFactory = createRecordReaderFactory();
		RecordUpdaterFactory recordUpdaterFactory = createRecordUpdaterFactory();

		DivaDbToCoraConverterFactoryImp divaDbToCoraConverterFactory = new DivaDbToCoraConverterFactoryImp();
		DivaDbToCoraFactoryImp divaDbToCoraFactory = new DivaDbToCoraFactoryImp(recordReaderFactory,
				divaDbToCoraConverterFactory);
		DataToDbTranslaterFactoryImp dataToDbTranslaterFactory = new DataToDbTranslaterFactoryImp();

		return DivaDbToCoraRecordStorage
				.usingRecordReaderFactoryConverterFactoryDbToCoraFactoryRecordUpdaterFactoryAndTranslaterFactory(
						recordReaderFactory, divaDbToCoraConverterFactory, divaDbToCoraFactory,
						recordUpdaterFactory, dataToDbTranslaterFactory);
	}

	private RecordUpdaterFactory createRecordUpdaterFactory() {
		SqlConnectionProvider sqlConnectionProvider = tryToCreateConnectionProvider();
		return RecordUpdaterFactoryImp.usingSqlConnectionProvider(sqlConnectionProvider);
	}

	private RecordReaderFactoryImp createRecordReaderFactory() {
		SqlConnectionProvider sqlConnectionProvider = tryToCreateConnectionProvider();
		return RecordReaderFactoryImp.usingSqlConnectionProvider(sqlConnectionProvider);
	}

	private SqlConnectionProvider tryToCreateConnectionProvider() {
		try {
			InitialContext context = new InitialContext();
			String databaseLookupName = tryToGetInitParameterLogIfFound("databaseLookupName");
			return ContextConnectionProviderImp.usingInitialContextAndName(context,
					databaseLookupName);
		} catch (Exception e) {
			throw DataStorageException.withMessageAndException(e.getMessage(), e);
		}
	}

	private String tryToGetInitParameter(String parameterName) {
		throwErrorIfKeyIsMissingFromInitInfo(parameterName);
		return initInfo.get(parameterName);
	}

	private String tryToGetInitParameterLogIfFound(String parameterName) {
		String basePath = tryToGetInitParameter(parameterName);
		log.logInfoUsingMessage("Found " + basePath + " as " + parameterName);
		return basePath;
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

}
