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

import se.uu.ub.cora.sqldatabase.DbQueryInfo;
import se.uu.ub.cora.sqldatabase.SortOrder;

public class DbQueryInfoSpy implements DbQueryInfo {

	public String orderBy;

	@Override
	public Integer getOffset() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getLimit() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDelimiter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getFromNo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getToNo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean delimiterIsPresent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;

	}

	@Override
	public String getOrderByPartOfQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSortOrder(SortOrder sortOrder) {
		// TODO Auto-generated method stub

	}

}
