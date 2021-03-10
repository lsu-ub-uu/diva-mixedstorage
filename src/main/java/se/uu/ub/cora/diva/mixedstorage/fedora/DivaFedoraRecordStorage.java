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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.StorageReadResult;

public final class DivaFedoraRecordStorage implements RecordStorage {

	private static final int OK = 200;
	private static final String PERSON = "person";
	private HttpHandlerFactory httpHandlerFactory;
	private String baseURL;
	private DivaFedoraConverterFactory converterFactory;
	private String username;
	private String password;

	private DivaFedoraRecordStorage(HttpHandlerFactory httpHandlerFactory,
			DivaFedoraConverterFactory converterFactory, String baseURL, String username,
			String password) {
		this.httpHandlerFactory = httpHandlerFactory;
		this.converterFactory = converterFactory;
		this.baseURL = baseURL;
		this.username = username;
		this.password = password;
	}

	public static DivaFedoraRecordStorage usingHttpHandlerFactoryAndConverterFactoryAndBaseURLAndUsernameAndPassword(
			HttpHandlerFactory httpHandlerFactory, DivaFedoraConverterFactory converterFactory,
			String baseURL, String username, String password) {
		return new DivaFedoraRecordStorage(httpHandlerFactory, converterFactory, baseURL, username,
				password);
	}

	@Override
	public DataGroup read(String type, String id) {
		if (PERSON.equals(type)) {
			return readAndConvertPersonFromFedora(id);
		}
		if ("personDomainPart".equals(type)) {
			return readAndConvertPersonDomainPartFromFedora(id);
		}
		throw NotImplementedException.withMessage("read is not implemented for type: " + type);
	}

	private DataGroup readAndConvertPersonFromFedora(String id) {
		HttpHandler httpHandler = createHttpHandlerForPerson(id);
		DivaFedoraToCoraConverter toCoraConverter = converterFactory.factorToCoraConverter(PERSON);
		return toCoraConverter.fromXML(httpHandler.getResponseText());
	}

	private HttpHandler createHttpHandlerForPerson(String id) {
		String url = baseURL + "objects/" + id + "/datastreams/METADATA/content";
		HttpHandler httpHandler = httpHandlerFactory.factor(url);
		httpHandler.setRequestMethod("GET");
		return httpHandler;
	}

	private DataGroup readAndConvertPersonDomainPartFromFedora(String id) {
		String personIdPart = extractPersonIdFromId(id);
		HttpHandler httpHandler = createHttpHandlerForPerson(personIdPart);
		DivaFedoraToCoraConverter toCoraConverter = converterFactory
				.factorToCoraConverter("personDomainPart");

		Map<String, Object> parameters = createParameters(id);

		return toCoraConverter.fromXMLWithParameters(httpHandler.getResponseText(), parameters);
	}

	private String extractPersonIdFromId(String id) {
		int domainStartIndex = id.lastIndexOf(":");
		return id.substring(0, domainStartIndex);
	}

	private Map<String, Object> createParameters(String id) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("domain", extractDomainFromId(id));
		return parameters;
	}

	private String extractDomainFromId(String id) {
		int domainStartIndex = id.lastIndexOf(":");
		return id.substring(domainStartIndex + 1);
	}

	@Override
	public void create(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		throw NotImplementedException.withMessage("create is not implemented");
	}

	@Override
	public void deleteByTypeAndId(String type, String id) {
		throw NotImplementedException.withMessage("deleteByTypeAndId is not implemented");
	}

	@Override
	public boolean linksExistForRecord(String type, String id) {
		throw NotImplementedException.withMessage("linksExistForRecord is not implemented");
	}

	@Override
	public void update(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		if (PERSON.equals(type)) {
			convertAndWritePersonToFedora(type, id, record);
		} else {
			throw NotImplementedException
					.withMessage("update is not implemented for type: " + type);
		}
	}

	private void convertAndWritePersonToFedora(String type, String id, DataGroup record) {
		try {
			tryToConvertAndWritePersonToFedora(type, id, record);
		} catch (Exception e) {
			throw FedoraException
					.withMessageAndException("update to fedora failed for record: " + id, e);
		}
	}

	private void tryToConvertAndWritePersonToFedora(String type, String id, DataGroup record) {
		String url = createUrlForWritingMetadataStreamToFedora(id);
		HttpHandler httpHandler = createHttpHandlerForUpdatingDatastreamUsingURL(url);
		String fedoraXML = convertRecordToFedoraXML(type, record);
		httpHandler.setOutput(fedoraXML);
		int responseCode = httpHandler.getResponseCode();
		throwErrorIfNotOkFromFedora(id, responseCode);
	}

	private void throwErrorIfNotOkFromFedora(String id, int responseCode) {
		if (OK != responseCode) {
			throw FedoraException.withMessage("update to fedora failed for record: " + id
					+ ", with response code: " + responseCode);
		}
	}

	private String createUrlForWritingMetadataStreamToFedora(String id) {
		return baseURL + "objects/" + id + "/datastreams/METADATA?format=?xml&controlGroup=M"
				+ "&logMessage=coraWritten&checksumType=SHA-512";
	}

	private HttpHandler createHttpHandlerForUpdatingDatastreamUsingURL(String url) {
		HttpHandler httpHandler = httpHandlerFactory.factor(url);
		setRequestMethodForUpdatingDatastreamInFedora(httpHandler);
		setAutorizationInHttpHandler(httpHandler);
		return httpHandler;
	}

	private void setRequestMethodForUpdatingDatastreamInFedora(HttpHandler httpHandler) {
		httpHandler.setRequestMethod("PUT");
	}

	private void setAutorizationInHttpHandler(HttpHandler httpHandler) {
		String encoded = Base64.getEncoder()
				.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
		httpHandler.setRequestProperty("Authorization", "Basic " + encoded);
	}

	private String convertRecordToFedoraXML(String type, DataGroup record) {
		DivaCoraToFedoraConverter converter = converterFactory.factorToFedoraConverter(type);
		return converter.toXML(record);
	}

	@Override
	public StorageReadResult readList(String type, DataGroup filter) {
		if (PERSON.equals(type)) {
			return readAndConvertPersonListFromFedora();
		}
		throw NotImplementedException.withMessage("readList is not implemented for type: " + type);
	}

	private StorageReadResult readAndConvertPersonListFromFedora() {
		try {
			return tryGetStorageReadResultFromFedoraPersonListConversion();
		} catch (Exception e) {
			throw FedoraException.withMessageAndException(
					"Unable to read list of persons: " + e.getMessage(), e);
		}
	}

	private StorageReadResult tryGetStorageReadResultFromFedoraPersonListConversion() {
		StorageReadResult storageReadResult = new StorageReadResult();
		storageReadResult.listOfDataGroups = (List<DataGroup>) tryReadAndConvertPersonListFromFedora();
		return storageReadResult;
	}

	private Collection<DataGroup> tryReadAndConvertPersonListFromFedora() {
		String personListXML = getPersonListXMLFromFedora();
		NodeList list = extractNodeListWithPidsFromXML(personListXML);
		return constructCollectionOfPersonFromFedora(list);
	}

	private String getPersonListXMLFromFedora() {
		HttpHandler httpHandler = createHttpHandlerForPersonList();
		return httpHandler.getResponseText();
	}

	private HttpHandler createHttpHandlerForPersonList() {
		String url = baseURL
				+ "objects?pid=true&maxResults=100&resultFormat=xml&query=pid%7Eauthority-person:*";
		HttpHandler httpHandler = httpHandlerFactory.factor(url);
		httpHandler.setRequestMethod("GET");
		return httpHandler;
	}

	private NodeList extractNodeListWithPidsFromXML(String personListXML) {
		XMLXPathParser parser = XMLXPathParser.forXML(personListXML);
		return parser
				.getNodeListFromDocumentUsingXPath("/result/resultList/objectFields/pid/text()");
	}

	private Collection<DataGroup> constructCollectionOfPersonFromFedora(NodeList list) {
		Collection<DataGroup> personList = new ArrayList<>(list.getLength());
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			String pid = node.getTextContent();
			personList.add(readAndConvertPersonFromFedora(pid));
		}
		return personList;
	}

	@Override
	public StorageReadResult readAbstractList(String type, DataGroup filter) {
		throw NotImplementedException.withMessage("readAbstractList is not implemented");
	}

	@Override
	public DataGroup readLinkList(String type, String id) {
		throw NotImplementedException.withMessage("readLinkList is not implemented");
	}

	@Override
	public Collection<DataGroup> generateLinkCollectionPointingToRecord(String type, String id) {
		throw NotImplementedException
				.withMessage("generateLinkCollectionPointingToRecord is not implemented");
	}

	@Override
	public boolean recordsExistForRecordType(String type) {
		throw NotImplementedException.withMessage("recordsExistForRecordType is not implemented");
	}

	@Override
	public boolean recordExistsForAbstractOrImplementingRecordTypeAndRecordId(String type,
			String id) {
		throw NotImplementedException.withMessage(
				"recordExistsForAbstractOrImplementingRecordTypeAndRecordId is not implemented");
	}

	public HttpHandlerFactory getHttpHandlerFactory() {
		// needed for test
		return httpHandlerFactory;
	}

	public DivaFedoraConverterFactory getDivaFedoraConverterFactory() {
		// needed for test
		return converterFactory;
	}

	public String getBaseURL() {
		// needed for test
		return baseURL;
	}

	public String getFedoraUsername() {
		// needed for test
		return username;
	}

	public String getFedoraPassword() {
		// needed for test
		return password;
	}

}
