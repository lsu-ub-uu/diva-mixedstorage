/*
 * Copyright 2018, 2021 Uppsala University Library
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.HttpHandlerFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.storage.RecordNotFoundException;
import se.uu.ub.cora.storage.RecordStorage;

public class DivaFedoraRecordStorageTest {
	private DivaFedoraRecordStorage divaToCoraRecordStorage;
	private HttpHandlerFactorySpy httpHandlerFactory;
	private DivaFedoraConverterFactorySpy converterFactory;
	private String baseURL = "http://diva-cora-fedora:8088/fedora/";
	private String fedoraUsername = "fedoraUser";
	private String fedoraPassword = "fedoraPassword";

	@BeforeMethod
	public void BeforeMethod() {
		httpHandlerFactory = new HttpHandlerFactorySpy();
		converterFactory = new DivaFedoraConverterFactorySpy();
		divaToCoraRecordStorage = DivaFedoraRecordStorage
				.usingHttpHandlerFactoryAndConverterFactoryAndBaseURLAndUsernameAndPassword(
						httpHandlerFactory, converterFactory, baseURL, fedoraUsername,
						fedoraPassword);
	}

	@Test
	public void testInit() throws Exception {
		assertNotNull(divaToCoraRecordStorage);
		assertSame(divaToCoraRecordStorage.getHttpHandlerFactory(), httpHandlerFactory);
		assertSame(divaToCoraRecordStorage.getDivaFedoraConverterFactory(), converterFactory);
		assertEquals(divaToCoraRecordStorage.getBaseURL(), baseURL);
		assertEquals(divaToCoraRecordStorage.getBaseURL(), baseURL);
		assertEquals(divaToCoraRecordStorage.getFedoraUsername(), fedoraUsername);
		assertEquals(divaToCoraRecordStorage.getFedoraPassword(), fedoraPassword);

	}

	@Test
	public void divaToCoraRecordStorageImplementsRecordStorage() throws Exception {
		assertTrue(divaToCoraRecordStorage instanceof RecordStorage);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "read is not implemented for type: null")
	public void readThrowsNotImplementedExceptionForTypeNull() throws Exception {
		divaToCoraRecordStorage.read(null, null);
	}

	@Test
	public void readPersonCallsFedoraAndReturnsConvertedResult() throws Exception {
		simulateResponseWithAListOfPersonsFromFedora();
		simulateResponseWithAPersonFromFedora();

		DataGroup readPerson = divaToCoraRecordStorage.read("person", "authority-person:11685");

		assertEquals(httpHandlerFactory.factoredHttpHandlers.size(), 2);

		checkHttpCalls();

		assertEquals(converterFactory.factoredConverters.size(), 1);
		assertEquals(converterFactory.factoredTypes.get(0), "person");
		DivaFedoraToCoraConverterSpy divaToCoraConverter = (DivaFedoraToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		assertEquals(divaToCoraConverter.xml, "Some response text");
		assertEquals(readPerson, divaToCoraConverter.convertedDataGroup);
	}

	private void simulateResponseWithAListOfPersonsFromFedora() {
		httpHandlerFactory.responseCodes.add(200);
		httpHandlerFactory.responseTexts.add(createXMLForPersonList());
	}

	private void simulateResponseWithAPersonFromFedora() {
		httpHandlerFactory.responseTexts.add("Some response text");
		httpHandlerFactory.responseCodes.add(200);
	}

	private void checkHttpCalls() {
		assertEquals(httpHandlerFactory.urls.get(0),
				baseURL + "objects?pid=true&maxResults=100&resultFormat=xml&"
						+ "query=state%3DA+pid%3Dauthority-person%3A11685");
		HttpHandlerSpy httpHandler = httpHandlerFactory.factoredHttpHandlers.get(0);
		assertEquals(httpHandler.requestMethod, "GET");

		assertEquals(httpHandlerFactory.urls.get(1),
				baseURL + "objects/authority-person:11685/datastreams/METADATA/content");
		HttpHandlerSpy httpHandler2 = httpHandlerFactory.factoredHttpHandlers.get(1);
		assertEquals(httpHandler2.requestMethod, "GET");
	}

	@Test(expectedExceptions = RecordNotFoundException.class, expectedExceptionsMessageRegExp = ""
			+ "Record not found for type: person and id: authority-person:22")
	public void testRecordNotFoundOrDeletedInStorage() throws Exception {
		httpHandlerFactory.responseCodes.add(200);
		httpHandlerFactory.responseTexts.add(createXMLForPlaceListNoRecordsFound());
		divaToCoraRecordStorage.read("person", "authority-person:22");
	}

	@Test(expectedExceptions = RecordNotFoundException.class, expectedExceptionsMessageRegExp = ""
			+ "Record not found for type: person and id: authority-person:11685")
	public void testRecordFoundWhenLookingForNonDeletedButThenDeletedBeforeWeCanReadIt()
			throws Exception {
		httpHandlerFactory.responseTexts.add(createXMLForPersonList());
		httpHandlerFactory.responseCodes.add(200);
		httpHandlerFactory.responseTexts.add("Dummy response text");
		httpHandlerFactory.responseCodes.add(404);
		divaToCoraRecordStorage.read("person", "authority-person:11685");
	}

	private String createXMLForPlaceListNoRecordsFound() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<result xmlns=\"http://www.fedora.info/definitions/1/0/types/\" "
				+ "xmlns:types=\"http://www.fedora.info/definitions/1/0/types/\" "
				+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xsi:schemaLocation=\"http://www.fedora.info/definitions/1/0/types/ "
				+ "http://localhost:8088/fedora/schema/findObjects.xsd\">\n" + "  <resultList>\n"
				+ "  </resultList>\n" + "</result>";
	}

	@Test
	public void readPersonDomainPartCallsFedoraAndReturnsConvertedResult() throws Exception {
		simulateResponseWithAListOfPersonsFromFedora();
		simulateResponseWithAPersonFromFedora();

		DataGroup readPersonDomainPart = divaToCoraRecordStorage.read("personDomainPart",
				"authority-person:11685:uu");

		checkHttpCalls();

		assertEquals(converterFactory.factoredConverters.size(), 1);
		assertEquals(converterFactory.factoredTypes.get(0), "personDomainPart");

		DivaFedoraToCoraConverterSpy divaToCoraConverter = (DivaFedoraToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		assertTrue(divaToCoraConverter.fromXMLWithParametersWasCalled);

		assertEquals(divaToCoraConverter.xml, "Some response text");
		assertEquals(readPersonDomainPart, divaToCoraConverter.convertedDataGroup);
		assertEquals(divaToCoraConverter.parameters.get("domainFilter"), "uu");

	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "create is not implemented")
	public void createThrowsNotImplementedException() throws Exception {
		divaToCoraRecordStorage.create(null, null, null, null, null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "deleteByTypeAndId is not implemented")
	public void deleteByTypeAndIdThrowsNotImplementedException() throws Exception {
		divaToCoraRecordStorage.deleteByTypeAndId(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "linksExistForRecord is not implemented")
	public void linksExistForRecordThrowsNotImplementedException() throws Exception {
		divaToCoraRecordStorage.linksExistForRecord(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "update is not implemented for type: null")
	public void updateThrowsNotImplementedException() throws Exception {
		divaToCoraRecordStorage.update(null, null, null, null, null, null);
	}

	@Test
	public void updateUpdatesNameInRecordStorage() throws Exception {
		simulateResponseWithAPersonFromFedora();
		DataGroup record = new DataGroupSpy("authority");

		DataGroup collectedTerms = createCollectTermsWithRecordLabel();

		DataGroup linkList = null;
		String dataDivider = null;

		divaToCoraRecordStorage.update("person", "diva-person:2233", record, collectedTerms,
				linkList, dataDivider);

		assertEquals(httpHandlerFactory.factoredHttpHandlers.size(), 1);
		assertEquals(httpHandlerFactory.urls.get(0),
				baseURL + "objects/diva-person:2233/datastreams/METADATA?format=?xml&controlGroup=M"
						+ "&logMessage=coraWritten&checksumType=SHA-512");

		HttpHandlerSpy httpHandler = httpHandlerFactory.factoredHttpHandlers.get(0);
		assertEquals(httpHandler.requestMethod, "PUT");
		String encoded = Base64.getEncoder().encodeToString(
				(fedoraUsername + ":" + fedoraPassword).getBytes(StandardCharsets.UTF_8));
		assertEquals(httpHandler.requestProperties.get("Authorization"), "Basic " + encoded);

		assertEquals(converterFactory.factoredToFedoraConverters.size(), 1);
		assertEquals(converterFactory.factoredToFedoraTypes.get(0), "person");
		DivaCoraToFedoraConverterSpy converterSpy = (DivaCoraToFedoraConverterSpy) converterFactory.factoredToFedoraConverters
				.get(0);
		assertSame(converterSpy.dataRecord, record);
		assertEquals(converterSpy.returnedXML, httpHandler.outputStrings.get(0));
	}

	private DataGroup createCollectTermsWithRecordLabel() {
		DataGroup collectedTerms = new DataGroupSpy("collectedData");
		collectedTerms.addChild(new DataAtomicSpy("type", "person"));
		collectedTerms.addChild(new DataAtomicSpy("id", "diva-person:2233"));

		DataGroup storageTerms = new DataGroupSpy("storage");
		collectedTerms.addChild(storageTerms);

		DataGroup collectedRecordLabel = new DataGroupSpy("collectedDataTerm");
		storageTerms.addChild(collectedRecordLabel);
		collectedRecordLabel.setRepeatId("someRepeatId");
		collectedRecordLabel.addChild(new DataAtomicSpy("collectTermId", "recordLabelStorageTerm"));
		collectedRecordLabel
				.addChild(new DataAtomicSpy("collectTermValue", "Some Person Collected Name åäö"));
		return collectedTerms;
	}

	@Test(expectedExceptions = FedoraException.class, expectedExceptionsMessageRegExp = ""
			+ "update to fedora failed for record: diva-person:77")
	public void updateIfNotOkFromFedoraThrowException() throws Exception {
		httpHandlerFactory.responseCodes.add(505);
		httpHandlerFactory.responseTexts.add("Some response text");

		DataGroup record = new DataGroupSpy("authority");
		DataGroup collectedTerms = createCollectTermsWithRecordLabel();

		divaToCoraRecordStorage.update("person", "diva-person:77", record, collectedTerms, null,
				null);
	}

	@Test(expectedExceptions = FedoraException.class, expectedExceptionsMessageRegExp = ""
			+ "update to fedora failed for record: diva-person:23")
	public void updateIfNotOkFromFedoraThrowExceptionOtherRecord() throws Exception {
		httpHandlerFactory.responseCodes.add(505);
		httpHandlerFactory.responseTexts.add("Some response text");

		DataGroup record = new DataGroupSpy("authority");
		DataGroup collectedTerms = createCollectTermsWithRecordLabel();

		divaToCoraRecordStorage.update("person", "diva-person:23", record, collectedTerms, null,
				null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "readList is not implemented for type: null")
	public void readListThrowsNotImplementedExceptionForTypeNull() throws Exception {
		divaToCoraRecordStorage.readList(null, null);
	}

	@Test(expectedExceptions = FedoraException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to read list of persons: Can not read xml: "
			+ "The element type \"someTag\" must be terminated by the matching end-tag \"</someTag>\".")
	public void readListThrowsParseExceptionOnBrokenXML() throws Exception {
		httpHandlerFactory.responseCodes.add(200);
		httpHandlerFactory.responseTexts.add("<someTag></notSameTag>");
		divaToCoraRecordStorage.readList("person", new DataGroupSpy("filter"));
	}

	@Test
	public void readPersonListCallsFedoraAndReturnsConvertedResult() throws Exception {
		simulateResponseWithAListOfPersonsFromFedora();
		simulateResponseWithAPersonFromFedora();
		simulateResponseWithAPersonFromFedora();
		simulateResponseWithAPersonFromFedora();

		Collection<DataGroup> readPersonList = divaToCoraRecordStorage.readList("person",
				new DataGroupSpy("filter")).listOfDataGroups;
		assertEquals(httpHandlerFactory.urls.get(0), baseURL
				+ "objects?pid=true&maxResults=100&resultFormat=xml&query=state%3DA+pid%7Eauthority-person%3A*");
		assertEquals(httpHandlerFactory.factoredHttpHandlers.size(), 4);
		HttpHandlerSpy httpHandler = httpHandlerFactory.factoredHttpHandlers.get(0);
		assertEquals(httpHandler.requestMethod, "GET");

		assertEquals(httpHandlerFactory.urls.get(1),
				baseURL + "objects/authority-person:11685/datastreams/METADATA/content");
		assertEquals(httpHandlerFactory.urls.get(2),
				baseURL + "objects/authority-person:12685/datastreams/METADATA/content");
		assertEquals(httpHandlerFactory.urls.get(3),
				baseURL + "objects/authority-person:13685/datastreams/METADATA/content");

		assertEquals(converterFactory.factoredConverters.size(), 3);
		assertEquals(converterFactory.factoredTypes.get(0), "person");
		DivaFedoraToCoraConverterSpy divaToCoraConverter = (DivaFedoraToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		assertEquals(divaToCoraConverter.xml, "Some response text");
		assertEquals(readPersonList.size(), 3);
		Iterator<DataGroup> readPersonIterator = readPersonList.iterator();
		assertEquals(readPersonIterator.next(), divaToCoraConverter.convertedDataGroup);
	}

	private String createXMLForPersonList() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<result xmlns=\"http://www.fedora.info/definitions/1/0/types/\" xmlns:types=\"http://www.fedora.info/definitions/1/0/types/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.fedora.info/definitions/1/0/types/ http://localhost:8088/fedora/schema/findObjects.xsd\">\n"
				+ "  <resultList>\n" + "  <objectFields>\n"
				+ "      <pid>authority-person:11685</pid>\n" + "  </objectFields>\n"
				+ "  <objectFields>\n" + "      <pid>authority-person:12685</pid>\n"
				+ "  </objectFields>\n" + "  <objectFields>\n"
				+ "      <pid>authority-person:13685</pid>\n" + "  </objectFields>\n"
				+ "  </resultList>\n" + "</result>";
	}

	@Test
	public void readPersonDomainPartListCallsFedoraAndReturnsConvertedResult() throws Exception {
		setUpForListDomainPart();

		Collection<DataGroup> readPersonDomainPartList = divaToCoraRecordStorage
				.readList("personDomainPart", new DataGroupSpy("filter")).listOfDataGroups;
		assertEquals(httpHandlerFactory.urls.get(0), baseURL
				+ "objects?pid=true&maxResults=100&resultFormat=xml&query=state%3DA+pid%7Eauthority-person%3A*");
		assertEquals(httpHandlerFactory.factoredHttpHandlers.size(), 4);
		HttpHandlerSpy httpHandler = httpHandlerFactory.factoredHttpHandlers.get(0);
		assertEquals(httpHandler.requestMethod, "GET");

		assertEquals(httpHandlerFactory.urls.get(1),
				baseURL + "objects/authority-person:11685/datastreams/METADATA/content");
		assertEquals(httpHandlerFactory.urls.get(2),
				baseURL + "objects/authority-person:12685/datastreams/METADATA/content");
		assertEquals(httpHandlerFactory.urls.get(3),
				baseURL + "objects/authority-person:13685/datastreams/METADATA/content");

		assertCorrectFactoredTypesForListDomainPart();
		DivaFedoraToCoraConverterSpy domainPartConverter = (DivaFedoraToCoraConverterSpy) converterFactory.factoredConverters
				.get(1);
		assertEquals(domainPartConverter.parameters.get("domainFilter"), "uu");
		DivaFedoraToCoraConverterSpy domainPartConverter2 = (DivaFedoraToCoraConverterSpy) converterFactory.factoredConverters
				.get(3);
		assertEquals(domainPartConverter2.parameters.get("domainFilter"), "test");
		assertEquals(readPersonDomainPartList.size(), 2);
	}

	private void setUpForListDomainPart() {
		simulateResponseWithAListOfPersonsFromFedora();
		simulateResponseWithAPersonFromFedora();
		simulateResponseWithAPersonFromFedora();
		simulateResponseWithAPersonFromFedora();

		setUpConvertToReturnDataGroup();
	}

	private void setUpConvertToReturnDataGroup() {
		List<DataGroup> dataGroups = new ArrayList<>();
		createAndAddGroupWithDomainPart(dataGroups, "authority-person:11685:uu");
		createAndAddGroupWithDomainPart(dataGroups, "authority-person:12685:test");
		dataGroups.add(new DataGroupSpy("person"));
		converterFactory.dataGroupsToReturnFromConverter = dataGroups;
	}

	private void assertCorrectFactoredTypesForListDomainPart() {
		assertEquals(converterFactory.factoredConverters.size(), 5);
		assertEquals(converterFactory.factoredTypes.get(0), "person");
		assertEquals(converterFactory.factoredTypes.get(1), "personDomainPart");
		assertEquals(converterFactory.factoredTypes.get(2), "person");
		assertEquals(converterFactory.factoredTypes.get(3), "personDomainPart");
		assertEquals(converterFactory.factoredTypes.get(4), "person");
	}

	private void createAndAddGroupWithDomainPart(List<DataGroup> dataGroups,
			String linkedRecordId) {
		DataGroupSpy personGroup = new DataGroupSpy("person");
		DataGroupSpy domainPart = new DataGroupSpy("personDomainPart");
		domainPart.addChild(new DataAtomicSpy("linkedRecordId", linkedRecordId));
		personGroup.addChild(domainPart);

		dataGroups.add(personGroup);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "readAbstractList is not implemented")
	public void readAbstractListThrowsNotImplementedException() throws Exception {
		divaToCoraRecordStorage.readAbstractList(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "readLinkList is not implemented")
	public void readLinkListThrowsNotImplementedException() throws Exception {
		divaToCoraRecordStorage.readLinkList(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "generateLinkCollectionPointingToRecord is not implemented")
	public void generateLinkCollectionPointingToRecordThrowsNotImplementedException()
			throws Exception {
		divaToCoraRecordStorage.generateLinkCollectionPointingToRecord(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "recordExistsForAbstractOrImplementingRecordTypeAndRecordId is not implemented")
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdThrowsNotImplementedException()
			throws Exception {
		divaToCoraRecordStorage.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(null,
				null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "getTotalNumberOfRecordsForType is not implemented")
	public void getTotalNumberOfRecordsForTypeThrowsNotImplementedException() throws Exception {
		divaToCoraRecordStorage.getTotalNumberOfRecordsForType("anyType",
				new DataGroupSpy("filter"));
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "getTotalNumberOfRecordsForAbstractType is not implemented")
	public void getTotalNumberOfRecordsForAbstractTypeThrowsNotImplementedException()
			throws Exception {
		divaToCoraRecordStorage.getTotalNumberOfRecordsForAbstractType("anyType",
				Collections.emptyList(), new DataGroupSpy("filter"));
	}
}
