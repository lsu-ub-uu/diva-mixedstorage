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
package se.uu.ub.cora.diva.mixedstorage.fedora;

import se.uu.ub.cora.xmlconverter.converter.XsltTransformer;

public class DivaFedoraToCoraPersonConverter implements DivaFedoraToCoraConverter {

	private static final String XSLT_PATH = "xslt/DivaFedoraToCoraPerson.xsl";
	private XsltTransformer transformer;

	public DivaFedoraToCoraPersonConverter(XsltTransformer transformer) {
		this.transformer = transformer;
	}

	@Override
	public void fromXML(String xmlToTransform) {
		transformer.transform(XSLT_PATH);

	}

	// @Override
	// public DataGroup fromXML(String xmlToTransform) {
	// XsltTransformation xsltTransformation = new XsltTransformation(XSLT_PATH);
	// String coraXml = xsltTransformation.transform(xmlToTransform);
	// return convertXMLToDataElement(coraXml);
	// }
	//
	// private DataGroup convertXMLToDataElement(String xmlString) {
	// Converter converter = ConverterProvider.getConverter("xml");
	// return (DataGroup) converter.convert(xmlString);
	// }
}
