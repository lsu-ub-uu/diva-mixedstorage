/*
 * Copyright 2020 Uppsala University Library
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
package se.uu.ub.cora.diva.mixedstorage.db.user;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.RecordReaderFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.RecordReaderSpy;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.DivaMultipleRowDbToDataReaderImp;

public class MultipleRowDbToDataUserGroupReaderTest {

	private RecordReaderFactorySpy readerFactory;
	private DivaDbToCoraConverterFactorySpy converterFactory;

	@BeforeMethod
	public void setUp() {
		readerFactory = new RecordReaderFactorySpy();
		converterFactory = new DivaDbToCoraConverterFactorySpy();

	}

	@Test
	public void testInit() {
		DivaMultipleRowDbToDataReaderImp userGroupReader = new MultipleRowDbToDataUserGroupReader(
				readerFactory, converterFactory);
		assertSame(userGroupReader.getRecordReaderFactory(), readerFactory);
	}

	@Test
	public void testRead() {
		DivaMultipleRowDbToDataReaderImp userGroupReader = new MultipleRowDbToDataUserGroupReader(
				readerFactory, converterFactory);
		userGroupReader.read("", "67");
		RecordReaderSpy factoredReader = readerFactory.factored;
		assertEquals(factoredReader.usedTableName, "groupsforuser");

		Map<String, Object> conditions = new HashMap<>();
		conditions.put("db_id", 67);

		assertEquals(factoredReader.usedConditions, conditions);
	}

}
