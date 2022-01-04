/*
 * Copyright 2022 Uppsala University Library
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
package se.uu.ub.cora.diva.mixedstorage.id;

import java.util.Map;

import se.uu.ub.cora.diva.mixedstorage.fedora.FedoraConnectionInfo;
import se.uu.ub.cora.diva.mixedstorage.fedora.FedoraException;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.storage.RecordIdGenerator;
import se.uu.ub.cora.storage.RecordIdGeneratorProvider;

public class DivaIdGeneratorProvider implements RecordIdGeneratorProvider {
	private Logger logger = LoggerProvider.getLoggerForClass(DivaIdGeneratorProvider.class);

	private RecordIdGenerator recordIdGenerator;
	private Map<String, String> initInfo;

	@Override
	public int getOrderToSelectImplementionsBy() {
		return 10;
	}

	@Override
	public void startUsingInitInfo(Map<String, String> initInfo) {
		logger.logInfoUsingMessage("DivaIdGeneratorProvider starting DivaIdGenerator...");
		this.initInfo = initInfo;
		FedoraConnectionInfo fedoraConnectionInfo = createFedoraConnectionInfo();
		recordIdGenerator = DivaIdGeneratorFactory.factor(fedoraConnectionInfo);
		logger.logInfoUsingMessage("DivaIdGeneratorProvider started DivaIdGenerator");
	}

	private FedoraConnectionInfo createFedoraConnectionInfo() {
		String fedoraBaseUrl = tryToGetInitParameter("fedoraURL");
		logger.logInfoUsingMessage("Found " + fedoraBaseUrl + " as fedoraURL");
		String fedoraUserName = tryToGetInitParameter("fedoraUsername");
		String fedoraPassword = tryToGetInitParameter("fedoraPassword");
		return new FedoraConnectionInfo(fedoraBaseUrl, fedoraUserName, fedoraPassword);
	}

	private String tryToGetInitParameter(String parameterName) {
		throwErrorIfKeyIsMissingFromInitInfo(parameterName);
		return initInfo.get(parameterName);
	}

	private void throwErrorIfKeyIsMissingFromInitInfo(String key) {
		if (!initInfo.containsKey(key)) {
			String errorMessage = "InitInfo must contain " + key;
			logger.logFatalUsingMessage(errorMessage);
			throw FedoraException.withMessage(errorMessage);
		}
	}

	@Override
	public RecordIdGenerator getRecordIdGenerator() {
		return recordIdGenerator;
	}

}
