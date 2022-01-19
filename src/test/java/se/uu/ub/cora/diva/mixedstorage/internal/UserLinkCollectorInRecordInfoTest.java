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
package se.uu.ub.cora.diva.mixedstorage.internal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.db.DbStorageSpy;

public class UserLinkCollectorInRecordInfoTest {

	private DbStorageSpy classicDbStorage;
	private UserLinkInRecordInfoCollector collector;
	private DataGroupSpy recordInfo;

	@BeforeMethod
	public void setUp() {
		classicDbStorage = new DbStorageSpy();
		recordInfo = new DataGroupSpy("recordInfo");
		collector = new UserLinkInRecordInfoCollector(classicDbStorage);
	}

	@Test
	public void testCollectNoUserLinks() {
		Map<String, Map<String, DataGroup>> collectedLinks = collector.collectLinks(recordInfo);
		assertTrue(collectedLinks.isEmpty());
	}

	@Test
	public void testCreatedBy() {
		DataGroupSpy createdBy = createUserLink("createdBy", "ert678");
		recordInfo.addChild(createdBy);

		Map<String, Map<String, DataGroup>> collectedLinks = collector.collectLinks(recordInfo);
		Map<String, DataGroup> map = collectedLinks.get("user");

		assertEquals(classicDbStorage.types.get(0), "public.user");
		assertEquals(classicDbStorage.ids.get(0), "ert678");

		assertEquals(map.size(), 1);
		DataGroup dataGroup = map.get("ert678");
		assertSame(dataGroup, classicDbStorage.returnedDataGroups.get(0));
	}

	private DataGroupSpy createUserLink(String nameInData, String recordId) {
		DataGroupSpy createdBy = new DataGroupSpy(nameInData);
		createdBy.addChild(new DataAtomicSpy("linkedRecordType", "user"));
		createdBy.addChild(new DataAtomicSpy("linkedRecordId", recordId));
		return createdBy;
	}

	@Test
	public void testMultipleUpdatedBy() {
		setUpRecordInfoWithCreatedByAndMultipleUpdatedBy();

		Map<String, Map<String, DataGroup>> collectedLinks = collector.collectLinks(recordInfo);
		Map<String, DataGroup> map = collectedLinks.get("user");

		assertCorrectCallToStorage("ert678", "bnm234", "qwe123", "yui789");

		assertEquals(map.size(), 4);
		assertSame(map.get("ert678"), classicDbStorage.returnedDataGroups.get(0));
		assertSame(map.get("bnm234"), classicDbStorage.returnedDataGroups.get(1));
		assertSame(map.get("qwe123"), classicDbStorage.returnedDataGroups.get(2));
		assertSame(map.get("yui789"), classicDbStorage.returnedDataGroups.get(3));
	}

	private void assertCorrectCallToStorage(String... ids) {
		for (int i = 0; i < ids.length; i++) {
			assertEquals(classicDbStorage.types.get(i), "public.user");
			assertEquals(classicDbStorage.ids.get(i), ids[i]);
		}
		assertEquals(classicDbStorage.ids.size(), ids.length);
	}

	private void setUpRecordInfoWithCreatedByAndMultipleUpdatedBy() {
		DataGroupSpy createdBy = createUserLink("createdBy", "ert678");
		recordInfo.addChild(createdBy);
		recordInfo.addChild(createUpdatedGroup("bnm234"));
		recordInfo.addChild(createUpdatedGroup("qwe123"));
		recordInfo.addChild(createUpdatedGroup("yui789"));
	}

	private DataGroupSpy createUpdatedGroup(String recordId) {
		DataGroupSpy updated = new DataGroupSpy("updated");
		updated.addChild(createUserLink("updatedBy", recordId));
		return updated;
	}

	@Test
	public void testSameLinkOnlyAddedOnce() {
		DataGroupSpy createdBy = createUserLink("createdBy", "ert678");
		recordInfo.addChild(createdBy);
		recordInfo.addChild(createUpdatedGroup("ert678"));

		Map<String, Map<String, DataGroup>> collectedLinks = collector.collectLinks(recordInfo);
		Map<String, DataGroup> map = collectedLinks.get("user");

		assertEquals(classicDbStorage.types.get(0), "public.user");
		assertEquals(classicDbStorage.ids.get(0), "ert678");

		assertEquals(map.size(), 1);
		assertCorrectCallToStorage("ert678", "ert678");

	}

}
