package se.uu.ub.cora.diva.mixedstorage.fedora;

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.converter.Converter;
import se.uu.ub.cora.converter.ConverterProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.xmlutils.transformer.CoraTransformation;
import se.uu.ub.cora.xmlutils.transformer.CoraTransformationFactory;

public class DivaCoraToFedoraPersonConverter implements DivaCoraToFedoraConverter {

	private CoraTransformationFactory transformationFactory;
	private static final String PERSON_XSLT_PATH = "person/toCoraPerson.xsl";
	private static final String PERSON_DOMAIN_PART_XSLT_PATH = "person/toCoraPersonDomainPart.xsl";

	public DivaCoraToFedoraPersonConverter(CoraTransformationFactory transformationFactory) {
		this.transformationFactory = transformationFactory;
	}

	@Override
	public String toXML(DataGroup record) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toXML(DataGroup dataRecord, List<DataGroup> relatedRecords) {
		Converter converter = ConverterProvider.getConverter("xml");
		String mainXML = converter.convert(dataRecord);
		List<String> relatedXmlStrings = convertRelatedRecords(converter, relatedRecords);
		return transformToFedoraXml(mainXML, relatedXmlStrings);
	}

	private List<String> convertRelatedRecords(Converter converter,
			List<DataGroup> relatedRecords) {
		List<String> relatedXmlStrings = new ArrayList<>();
		for (DataGroup relatedRecord : relatedRecords) {
			convertAndAddRelatedRecord(converter, relatedXmlStrings, relatedRecord);
		}
		return relatedXmlStrings;
	}

	private void convertAndAddRelatedRecord(Converter converter, List<String> relatedXmlStrings,
			DataGroup relatedRecord) {
		String relatedXml = converter.convert(relatedRecord);
		relatedXmlStrings.add(relatedXml);
	}

	private String transformToFedoraXml(String mainXML, List<String> relatedXmlStrings) {
		CoraTransformation transformation = transformationFactory
				.factorWithRelatedRecords(PERSON_XSLT_PATH, PERSON_DOMAIN_PART_XSLT_PATH);
		return transformation.transform(mainXML, relatedXmlStrings);
	}

}
