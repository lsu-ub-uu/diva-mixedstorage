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

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.sqldatabase.ResultDelimiter;

public class FilterToResultDelimiterConverterImp implements FilterToResultDelimiterConverter {

	@Override
	public ResultDelimiter convert(DataGroup filter) {
		return createResultDelimiter(filter);
	}

	private ResultDelimiter createResultDelimiter(DataGroup filter) {
		Integer fromNum = calculateFromNum(filter);
		Integer toNum = calculateToNum(filter);

		Integer limit = calculateLimit(fromNum, toNum);
		Integer offset = fromNum != 0 ? fromNum - 1 : null;
		return new ResultDelimiter(limit, offset);
	}

	private Integer calculateFromNum(DataGroup filter) {
		if (filter.containsChildWithNameInData("fromNo")) {
			return getAtomicValueAsInteger(filter, "fromNo");
		}
		return 0;
	}

	private Integer getAtomicValueAsInteger(DataGroup filter, String nameInData) {
		String atomicValue = filter.getFirstAtomicValueWithNameInData(nameInData);
		return Integer.valueOf(atomicValue);
	}

	private Integer calculateToNum(DataGroup filter) {
		if (filter.containsChildWithNameInData("toNo")) {
			return getAtomicValueAsInteger(filter, "toNo");
		}
		return null;
	}

	private Integer calculateLimit(Integer fromNum, Integer toNum) {
		if (toNum != null) {
			return fromNum != 0 ? fromToDifferencePlusOne(fromNum, toNum) : toNum;
		}
		return null;
	}

	private int fromToDifferencePlusOne(Integer fromNum, Integer toNum) {
		return (toNum - fromNum) + 1;
	}

}
