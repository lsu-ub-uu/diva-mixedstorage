/// *
// * Copyright 2020 Uppsala University Library
// *
// * This file is part of Cora.
// *
// * Cora is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * Cora is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with Cora. If not, see <http://www.gnu.org/licenses/>.
// */
// package se.uu.ub.cora.diva.mixedstorage.db.user;
//
// import se.uu.ub.cora.data.DataGroup;
// import se.uu.ub.cora.data.DataGroupProvider;
// import se.uu.ub.cora.diva.mixedstorage.db.ConversionException;
// import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverter;
// import se.uu.ub.cora.sqldatabase.Row;
//
// public class DivaDbToCoraUserGroupConverter implements DivaDbToCoraConverter {
//
// @Override
// public DataGroup fromMap(Row dbRow) {
//
// if (!map.containsKey("domain") || !map.containsKey("group_type")) {
// throw ConversionException.withMessageAndException(
// "Error converting user group: Map does not contain mandatory "
// + "values domain and groupType",
// null);
// }
//
// DataGroup dataGroup = DataGroupProvider.getDataGroupUsingNameInData("userRole");
// dataGroup.setRepeatId("0");
//
// DataGroup userRoleLink = DataGroupProvider.getDataGroupAsLinkUsingNameInDataTypeAndId(
// "userRole", "permissionRole", "divaDomainAdminRole");
//
// dataGroup.addChild(userRoleLink);
//
// return dataGroup;
// }
//
// }
