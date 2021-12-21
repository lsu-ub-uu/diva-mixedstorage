/*
 * Copyright 2021 Uppsala University Library
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

import static org.testng.Assert.assertTrue;

import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.db.DbStorageSpy;

public class UserLinkCollectorTest {

	private DbStorageSpy classicDbStorage;
	private UserLinkCollector collector;
	private DataGroupSpy recordInfo;

	@BeforeMethod
	public void setUp() {
		classicDbStorage = new DbStorageSpy();
		recordInfo = new DataGroupSpy("recordInfo");
		collector = new UserLinkCollector(classicDbStorage);
	}

	@Test
	public void testCollectNoUserLinks() {
		Map<String, Map<String, DataGroup>> collectedLinks = collector.collectLinks(recordInfo);
		assertTrue(collectedLinks.isEmpty());
	}

	@Test
	public void testCreatedBy() {
		DataGroupSpy createdBy = new DataGroupSpy("createdBy");
		createdBy.addChild(new DataAtomicSpy("linkedRecordType", "user"));
		createdBy.addChild(new DataAtomicSpy("linkedRecordId", "ert678"));

		Map<String, Map<String, DataGroup>> collectedLinks = collector.collectLinks(recordInfo);
		Map<String, DataGroup> map = collectedLinks.get("user");
	}

}
