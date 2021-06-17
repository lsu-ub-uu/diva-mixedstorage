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
package se.uu.ub.cora.diva.mixedstorage.db;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.sqldatabase.ResultDelimiter;

public class FilterToResultDelimiterConverterTest {

	@Test
	public void testDefaultFilterNoFromNoTo() {
		FilterToResultDelimiterConverter converter = new FilterToResultDelimiterConverterImp();
		DataGroup filter = new DataGroupSpy("filter");
		ResultDelimiter resultDelimiter = converter.convert(filter);
		assertNull(resultDelimiter.limit);
		assertNull(resultDelimiter.offset);
	}

	@Test
	public void testDefaultFilterWithFromNoTo() {
		FilterToResultDelimiterConverter converter = new FilterToResultDelimiterConverterImp();
		DataGroup filter = new DataGroupSpy("filter");
		filter.addChild(new DataAtomicSpy("fromNo", "0"));
		ResultDelimiter resultDelimiter = converter.convert(filter);
		assertNull(resultDelimiter.limit);
		assertNull(resultDelimiter.offset);
	}

	@Test
	public void testDefaultFilterNoFromWithTo() {
		FilterToResultDelimiterConverter converter = new FilterToResultDelimiterConverterImp();
		DataGroup filter = new DataGroupSpy("filter");
		filter.addChild(new DataAtomicSpy("toNo", "19"));
		ResultDelimiter resultDelimiter = converter.convert(filter);
		assertEquals(resultDelimiter.limit, Integer.valueOf(19));
		assertNull(resultDelimiter.offset);
	}

	@Test
	public void testDefaultFilterWithFromAndWithTo() {
		FilterToResultDelimiterConverter converter = new FilterToResultDelimiterConverterImp();
		DataGroup filter = new DataGroupSpy("filter");
		filter.addChild(new DataAtomicSpy("fromNo", "8"));
		filter.addChild(new DataAtomicSpy("toNo", "19"));
		ResultDelimiter resultDelimiter = converter.convert(filter);
		assertEquals(resultDelimiter.limit, Integer.valueOf(12));
		assertEquals(resultDelimiter.offset, Integer.valueOf(7));
	}
}
