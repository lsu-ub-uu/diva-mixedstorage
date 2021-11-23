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
package se.uu.ub.cora.diva.mixedstorage.fedora;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.resource.ResourceReader;

public class OldDivaCoraToFedoraPersonConverterTest {
	@Test
	public void testConvertToFedoraXML() throws Exception {

		HttpHandlerFactorySpy httpHandlerFactory = new HttpHandlerFactorySpy();
		httpHandlerFactory.responseCodes.add(200);
		httpHandlerFactory.responseTexts
				.add(ResourceReader.readResourceAsString("person/11685.xml"));

		String fedoraURL = "someFedoraURL";
		DivaCoraToFedoraConverter converter = OldDivaCoraToFedoraPersonConverter
				.usingHttpHandlerFactoryAndFedoraUrl(httpHandlerFactory, fedoraURL);
		DataGroup record = createPerson11685DataGroup();

		String xml = converter.toXML(record);
		assertEquals(httpHandlerFactory.factoredHttpHandlers.size(), 1);
		assertEquals(httpHandlerFactory.urls.get(0),
				fedoraURL + "objects/authority-person:11685/datastreams/METADATA/content");

		HttpHandlerSpy httpHandler = httpHandlerFactory.factoredHttpHandlers.get(0);
		assertEquals(httpHandler.requestMethod, "GET");

		assertEquals(xml, ResourceReader.readResourceAsString("person/expectedUpdated11685.xml"));

	}

	private DataGroup createPerson11685DataGroup() {
		DataGroup record = new DataGroupSpy("authorityPerson");
		DataGroup recordInfo = new DataGroupSpy("recordInfo");
		record.addChild(recordInfo);
		recordInfo.addChild(new DataAtomicSpy("id", "authority-person:11685"));

		DataGroup authorizedNameGroup = new DataGroupSpy("authorizedName");
		record.addChild(authorizedNameGroup);

		DataAtomic familyName = new DataAtomicSpy("familyName", "Andersson");
		authorizedNameGroup.addChild(familyName);

		DataAtomic givenName = new DataAtomicSpy("givenName", "Karl");
		authorizedNameGroup.addChild(givenName);

		return record;
	}

}
