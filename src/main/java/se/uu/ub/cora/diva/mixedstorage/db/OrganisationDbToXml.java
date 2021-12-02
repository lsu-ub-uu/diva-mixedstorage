package se.uu.ub.cora.diva.mixedstorage.db;

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.converter.Converter;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.sqldatabase.SqlDatabaseFactory;
import se.uu.ub.cora.xmlconverter.XmlConverterFactory;
import se.uu.ub.cora.xmlutils.transformer.XsltTransformationFactory;

public class OrganisationDbToXml implements DbToXml {

	private SqlDatabaseFactory sqlDatabaseFactory;
	private DivaDbRecordStorage dbStorage;
	private XmlConverterFactory xmlConverterFactory;
	private XsltTransformationFactory transformationFactory;

	public OrganisationDbToXml(DivaDbRecordStorage dbStorage,
			XmlConverterFactory xmlConverterFactory,
			XsltTransformationFactory transformationFactory) {
		this.dbStorage = dbStorage;
		this.xmlConverterFactory = xmlConverterFactory;
		this.transformationFactory = transformationFactory;
	}

	@Override
	public String read(String recordType, String recordId) {
		// test this
		Converter converter = xmlConverterFactory.factorConverter();
		DataGroup dataGroup = dbStorage.read("organisation", recordId);
		List<String> parentXMLs = new ArrayList<>();
		StringBuilder combinedXML = new StringBuilder();
		String coraXml = converter.convert(dataGroup);
		combinedXML.append(coraXml);

		if (dataGroup.containsChildWithNameInData("parentOrganisation")) {
			convertParents(converter, dataGroup, combinedXML);
		}

		// läs alla länkar och konvertera dem? hur få till trädet?

		// dataGroup to coraXml
		// XmlConverter.convert(readDataGroup)

		return combinedXML.toString();
	}

	private void convertParents(Converter converter, DataGroup dataGroup,
			StringBuilder combinedXML) {
		List<DataGroup> parents = dataGroup.getAllGroupsWithNameInData("parentOrganisation");
		for (DataGroup parent : parents) {
			DataGroup orgLink = parent.getFirstGroupWithNameInData("organisationLink");
			String parentId = orgLink.getFirstAtomicValueWithNameInData("linkedRecordId");
			// String parentId = recordInfo.getFirstAtomicValueWithNameInData("id");
			DataGroup parentDataGroup = dbStorage.read("organisation", parentId);
			String parentXML = converter.convert(parentDataGroup);
			combinedXML.append(parentXML);
			// parentXMLs.add(parentXML);
			if (parentDataGroup.containsChildWithNameInData("parentOrganisation")) {
				convertParents(converter, parentDataGroup, combinedXML);
			}
			// List<DataGroup> parentParents =
			// parent.getAllGroupsWithNameInData("parentOrganisation");
		}
	}

}
