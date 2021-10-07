// package se.uu.ub.cora.diva.mixedstorage.db.user;
//
// import static org.testng.Assert.assertEquals;
//
// import java.util.Collections;
// import java.util.HashMap;
// import java.util.Map;
//
// import org.testng.annotations.BeforeMethod;
// import org.testng.annotations.Test;
//
// import se.uu.ub.cora.data.DataGroup;
// import se.uu.ub.cora.data.DataGroupFactory;
// import se.uu.ub.cora.data.DataGroupProvider;
// import se.uu.ub.cora.diva.mixedstorage.DataGroupFactorySpy;
// import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
// import se.uu.ub.cora.diva.mixedstorage.db.ConversionException;
//
// public class DivaDbToCoraUserGroupConverterTest {
//
// private DivaDbToCoraUserGroupConverter groupConverter;
// private DataGroupFactory dataGroupFactory;
// // private DataGroupProvider dataGroupProvider;
//
// @BeforeMethod
// public void setUp() {
// dataGroupFactory = new DataGroupFactorySpy();
// DataGroupProvider.setDataGroupFactory(dataGroupFactory);
// groupConverter = new DivaDbToCoraUserGroupConverter();
// }
// // struktur på dataGruppen som ska komma tillbaka från convertern
// // OM group_type är domainAdmin så ska denna roll komma tillbaka, annars
// // {
// // "name": "userRole",
// // "children": [
// // {
// // "name": "userRole",
// // "children": [
// // {
// // "name": "linkedRecordType",
// // "value": "permissionRole"
// // },
// // {
// // "name": "linkedRecordId",
// // "value": "divaDomainAdminRole"
// // }
// // ]
// // }
// // ],
// // "repeatId": "1"
// // }
//
// @Test
// public void testFromMap() {
// Map<String, Object> conditions = new HashMap<>();
// conditions.put("domain", "someDomain");
// conditions.put("group_type", "someType");
//
// DataGroup roleGroup = groupConverter.fromMap(conditions);
// assertEquals(roleGroup.getNameInData(), "userRole");
// DataGroupSpy userRoleLink = (DataGroupSpy) roleGroup
// .getFirstGroupWithNameInData("userRole");
// assertEquals(userRoleLink.recordType, "permissionRole");
// assertEquals(userRoleLink.recordId, "divaDomainAdminRole");
//
// assertEquals(roleGroup.getRepeatId(), "0");
//
// }
//
// @Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = "Error
// converting "
// + "user group: Map does not contain mandatory values domain and groupType")
// public void testNoDomain() {
// Map<String, Object> conditions = new HashMap<>();
// conditions.put("group_type", "someType");
// groupConverter.fromMap(conditions);
// }
//
// @Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = "Error
// converting "
// + "user group: Map does not contain mandatory values domain and groupType")
// public void testNoGroupType() {
// Map<String, Object> conditions = new HashMap<>();
// conditions.put("domain", "someDomain");
// groupConverter.fromMap(conditions);
// }
//
// @Test(expectedExceptions = ConversionException.class)
// public void testFromMapEmptyMap() {
// groupConverter.fromMap(Collections.emptyMap());
// }
// }
