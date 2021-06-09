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
import se.uu.ub.cora.diva.mixedstorage.util.URLEncoder;
import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.storage.RecordNotFoundException;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.StorageReadResult;

public final class DivaFedoraRecordStorage implements RecordStorage {

	private static final int OK = 200;
	private static final int NOT_FOUND = 404;
	private static final String PERSON = "person";
	private static final String PERSON_DOMAIN_PART = "personDomainPart";
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
			return readOnePerson(id);
		}
		if (PERSON_DOMAIN_PART.equals(type)) {
			return readOnePersonDomainPart(id);
		}
		throw NotImplementedException.withMessage("read is not implemented for type: " + type);
	}

	private DataGroup readOnePerson(String id) {
		ensurePersonIsNotDeleted(id);
		return readAndConvertPersonFromFedora(id);
	}

	private DataGroup readOnePersonDomainPart(String id) {
		String personIdPart = extractPersonIdFromId(id);
		ensurePersonIsNotDeleted(personIdPart);
		return readAndConvertPersonDomainPartFromFedora(id);
	}

	private void ensurePersonIsNotDeleted(String id) {
		String query = "state=A pid=" + id;
		NodeList list = readPersonListFromFedora(query);
		throwRecordNotFoundExceptionIfNoPersonsFound(id, list);
	}

	private NodeList readPersonListFromFedora(String query) {
		String personListXML = getPersonListXMLFromFedora(query);
		return extractNodeListWithPidsFromXML(personListXML);
	}

	private void throwRecordNotFoundExceptionIfNoPersonsFound(String id, NodeList list) {
		if (0 == list.getLength()) {
			throw new RecordNotFoundException("Record not found for type: person and id: " + id);
		}
	}

	private DataGroup readAndConvertPersonFromFedora(String id) {
		String responseText = tryToReadPersonFromFedora(id);
		return convertPerson(responseText);
	}

	private DataGroup convertPerson(String responseText) {
		DivaFedoraToCoraConverter toCoraConverter = converterFactory.factorToCoraConverter(PERSON);
		return toCoraConverter.fromXML(responseText);
	}

	private String tryToReadPersonFromFedora(String id) {
		HttpHandler httpHandler = createHttpHandlerForPerson(id);
		int responseCode = httpHandler.getResponseCode();
		throwErrorIfRecordNotFound(id, responseCode);
		return httpHandler.getResponseText();
	}

	private void throwErrorIfRecordNotFound(String id, int responseCode) {
		if (NOT_FOUND == responseCode) {
			throw new RecordNotFoundException("Record not found for type: person and id: " + id);
		}
	}

	private HttpHandler createHttpHandlerForPerson(String id) {
		String url = baseURL + "objects/" + id + "/datastreams/METADATA/content";
		HttpHandler httpHandler = httpHandlerFactory.factor(url);
		httpHandler.setRequestMethod("GET");
		return httpHandler;
	}

	private DataGroup readAndConvertPersonDomainPartFromFedora(String id) {
		String responseText = readPersonDomainPart(id);
		return convertPersonDomainPart(id, responseText);
	}

	private String readPersonDomainPart(String id) {
		String personIdPart = extractPersonIdFromId(id);
		HttpHandler httpHandler = createHttpHandlerForPerson(personIdPart);
		return httpHandler.getResponseText();
	}

	private DataGroup convertPersonDomainPart(String id, String responseText) {
		DivaFedoraToCoraConverter toCoraConverter = converterFactory
				.factorToCoraConverter(PERSON_DOMAIN_PART);
		Map<String, Object> parameters = createParameters(id);
		return toCoraConverter.fromXMLWithParameters(responseText, parameters);
	}

	private String extractPersonIdFromId(String id) {
		int domainStartIndex = id.lastIndexOf(':');
		return id.substring(0, domainStartIndex);
	}

	private Map<String, Object> createParameters(String id) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("domainFilter", extractDomainFromId(id));
		return parameters;
	}

	private String extractDomainFromId(String id) {
		int domainStartIndex = id.lastIndexOf(':');
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
		if (PERSON_DOMAIN_PART.equals(type)) {
			return readAndConvertDomainPartListFromFedora();
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
		String query = "state=A pid~authority-person:*";
		NodeList list = readPersonListFromFedora(query);
		return constructCollectionOfPersonFromFedora(list);
	}

	private String getPersonListXMLFromFedora(String query) {
		HttpHandler httpHandler = createHttpHandlerForPersonList(query);
		return httpHandler.getResponseText();
	}

	private HttpHandler createHttpHandlerForPersonList(String query) {
		String url = createUrlForPersonList(query);
		HttpHandler httpHandler = httpHandlerFactory.factor(url);
		httpHandler.setRequestMethod("GET");
		return httpHandler;
	}

	private String createUrlForPersonList(String query) {
		String urlEncodedQuery = URLEncoder.encode(query);
		return baseURL + "objects?pid=true&maxResults=100&resultFormat=xml&query="
				+ urlEncodedQuery;
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

	private StorageReadResult readAndConvertDomainPartListFromFedora() {
		String query = "state=A pid~authority-person:*";
		NodeList list = readPersonListFromFedora(query);
		List<DataGroup> domainPartList = new ArrayList<>(list.getLength());
		for (int i = 0; i < list.getLength(); i++) {
			readPersonAndPossiblyConvertDomainParts(list, i, domainPartList);
		}
		return createStorageResult(domainPartList);
	}

	private StorageReadResult createStorageResult(List<DataGroup> domainPartList) {
		StorageReadResult storageReadResult = new StorageReadResult();
		storageReadResult.listOfDataGroups = domainPartList;
		return storageReadResult;
	}

	private void readPersonAndPossiblyConvertDomainParts(NodeList list, int index,
			List<DataGroup> domainPartList) {
		Node node = list.item(index);
		String pid = node.getTextContent();
		String responseText = tryToReadPersonFromFedora(pid);
		DataGroup convertedPerson = convertPerson(responseText);
		possiblyAddDomainPartsToList(responseText, convertedPerson, domainPartList);
	}

	private void possiblyAddDomainPartsToList(String responseText, DataGroup convertedPerson,
			List<DataGroup> domainPartList) {
		for (DataGroup dataGroup : convertedPerson.getAllGroupsWithNameInData(PERSON_DOMAIN_PART)) {
			addDomainPartToList(responseText, dataGroup, domainPartList);
		}
	}

	private void addDomainPartToList(String responseText, DataGroup dataGroup,
			List<DataGroup> domainPartList) {
		String id = dataGroup.getFirstAtomicValueWithNameInData("linkedRecordId");
		DataGroup convertedPersonDomainPart = convertPersonDomainPart(id, responseText);
		domainPartList.add(convertedPersonDomainPart);
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

	@Override
	public long getTotalNumberOfRecordsForType(String type, DataGroup filter) {
		throw NotImplementedException
				.withMessage("getTotalNumberOfRecordsForType is not implemented");
	}

	@Override
	public long getTotalNumberOfRecordsForAbstractType(String abstractType,
			List<String> implementingTypes, DataGroup filter) {
		throw NotImplementedException
				.withMessage("getTotalNumberOfRecordsForAbstractType is not implemented");
	}

}
