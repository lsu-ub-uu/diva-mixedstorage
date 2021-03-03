package se.uu.ub.cora.diva.mixedstorage.fedora;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import se.uu.ub.cora.diva.mixedstorage.ParseException;

public final class XMLXPathParser {
	private static final String XPATH_STRING_ERROR_MESSAGE = "Unable to use xpathString: ";
	private Document document;
	private XPath xpath;

	private XMLXPathParser(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		document = createDocumentFromXML(xml);
		setupXPath();
	}

	public static XMLXPathParser forXML(String xml) {
		try {
			return new XMLXPathParser(xml);
		} catch (Exception e) {
			throw ParseException.withMessageAndException("Can not read xml: " + e.getMessage(), e);
		}
	}

	private void setupXPath() {
		XPathFactory xpathFactory = XPathFactory.newInstance();
		xpath = xpathFactory.newXPath();
	}

	public Document createDocumentFromXML(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder dBuilder = createDocumentBuilder();
		return readXMLUsingBuilderAndXML(dBuilder, xml);
	}

	private DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		ErrorHandler errorHandlerWithoutSystemOutPrinting = new DefaultHandler();
		dBuilder.setErrorHandler(errorHandlerWithoutSystemOutPrinting);
		return dBuilder;
	}

	private Document readXMLUsingBuilderAndXML(DocumentBuilder dBuilder, String xml)
			throws SAXException, IOException {
		StringReader reader = new StringReader(xml);
		InputSource in = new InputSource(reader);
		Document doc = dBuilder.parse(in);
		doc.getDocumentElement().normalize();
		return doc;
	}

	public NodeList getNodeListFromDocumentUsingXPath(String xpathString) {
		try {
			XPathExpression expr = xpath.compile(xpathString);
			return (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw ParseException
					.withMessageAndException(XPATH_STRING_ERROR_MESSAGE + e.getMessage(), e);
		}
	}

	public void setStringInDocumentUsingXPath(String xpathString, String newValue) {
		try {
			XPathExpression expr = xpath.compile(xpathString);
			Node nodeToSet = (Node) expr.evaluate(document, XPathConstants.NODE);
			nodeToSet.setTextContent(newValue);
		} catch (XPathExpressionException e) {
			throw new RuntimeException("Error setting string value on node", e);
		}
	}

	public String getDocumentAsString(String xpathString) {
		try {
			XPathExpression expr = xpath.compile(xpathString);
			Node nodeToExport = (Node) expr.evaluate(document, XPathConstants.NODE);
			StringWriter sw = new StringWriter();
			TransformerFactory tf = TransformerFactory.newInstance();
			tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "no");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			transformer.transform(new DOMSource(nodeToExport), new StreamResult(sw));
			return sw.toString();
		} catch (Exception e) {
			throw new RuntimeException("Error converting node to String", e);
		}
	}

}