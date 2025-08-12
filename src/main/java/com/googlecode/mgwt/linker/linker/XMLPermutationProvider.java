package com.googlecode.mgwt.linker.linker;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.googlecode.mgwt.linker.server.BindingProperty;

public class XMLPermutationProvider implements Serializable {

	private static final long serialVersionUID = -8892369911664489332L;
	private static final String PERMUTATION_NODE = "permutation";
	private static final String PERMUTATION_NAME = "name";
	private static final String PERMUTATIONS = "permutations";

	protected static final Logger logger = Logger.getLogger(XMLPermutationProvider.class.getName());

	public Map<String, List<BindingProperty>> getBindingProperties(final InputStream stream) throws XMLPermutationProviderException {

		final Map<String, List<BindingProperty>> map = new HashMap<>();

		final Document document = createDocumentFromInputStream(stream);

		final Element permutationsNode = document.getDocumentElement();

		final String tagName = permutationsNode.getTagName();
		if (!PERMUTATIONS.equals(tagName)) {
			logger.severe("unexpected xml structure: Expected node : '" + PERMUTATIONS + "' got: '" + tagName + "'");
			throw new XMLPermutationProviderException();
		}

		final NodeList permutationsChildren = permutationsNode.getChildNodes();

		for (int i = 0 ; i < permutationsChildren.getLength() ; i++) {
			final Node node = permutationsChildren.item(i);

			if (node.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			final Element permutationNode = (Element) node;
			handlePermutation(map, permutationNode);
		}

		return map;
	}

	public Set<String> getPermutationFiles(final InputStream inputStream) throws XMLPermutationProviderException {

		final Document document = createDocumentFromInputStream(inputStream);

		final Element documentNode = document.getDocumentElement();

		final Set<String> set = new HashSet<>();
		final NodeList mainNodes = documentNode.getChildNodes();
		for (int i = 0 ; i < mainNodes.getLength() ; i++) {
			final Node item = mainNodes.item(i);
			if (item.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			final Element variables = (Element) item;
			final String varKey = variables.getTagName();

			if ("files".equals(varKey)) {
				final NodeList fileNodes = variables.getChildNodes();
				handleFileNodes(set, fileNodes);
			}
		}

		return set;
	}

	public String serializeMap(final Map<String, Set<BindingProperty>> map) throws XMLPermutationProviderException {

		final Document document = createDocument();
		final Element permutationsNode = document.createElement(PERMUTATIONS);
		document.appendChild(permutationsNode);

		for (final Entry<String, Set<BindingProperty>> entry : map.entrySet()) {
			final Element node = document.createElement(PERMUTATION_NODE);
			node.setAttribute(PERMUTATION_NAME, entry.getKey());
			permutationsNode.appendChild(node);

			for (final BindingProperty b : entry.getValue()) {
				final Element variable = document.createElement(b.getName());
				variable.appendChild(document.createTextNode(b.getValue()));
				node.appendChild(variable);
			}
		}
		return transformDocumentToString(document);
	}

	public String writePermutationInformation(final String strongName, final Set<BindingProperty> bindingProperties, final Set<String> files) throws XMLPermutationProviderException {

		final Document document = createDocument();

		final Element permutationNode = document.createElement(PERMUTATION_NODE);
		document.appendChild(permutationNode);

		permutationNode.setAttribute(PERMUTATION_NAME, strongName);

		// create and append variables node
		final Element variablesNode = document.createElement("variables");
		permutationNode.appendChild(variablesNode);

		// write out all variables
		for (final BindingProperty prop : bindingProperties) {
			final Element varNode = document.createElement(prop.getName());
			varNode.appendChild(document.createTextNode(prop.getValue()));
			variablesNode.appendChild(varNode);
		}

		// create file node
		final Element filesNode = document.createElement("files");
		permutationNode.appendChild(filesNode);

		// write out all files
		for (final String string : files) {
			final Element fileNode = document.createElement("file");
			fileNode.appendChild(document.createTextNode(string));
			filesNode.appendChild(fileNode);
		}

		return transformDocumentToString(document);

	}

	protected Document createDocument() throws XMLPermutationProviderException {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (final ParserConfigurationException e) {
			logger.log(Level.SEVERE, "can not create new document", e);
			throw new XMLPermutationProviderException("can not create new document", e);

		}
	}

	protected Document createDocumentFromInputStream(final InputStream inputStream) throws XMLPermutationProviderException {

		try {
			final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return builder.parse(inputStream);
		} catch (final SAXException e) {
			logger.log(Level.SEVERE, "can not parse input stream", e);
			throw new XMLPermutationProviderException("can not parse input stream", e);
		} catch (final IOException e) {
			logger.log(Level.SEVERE, "can not parse input stream", e);
			throw new XMLPermutationProviderException("can not parse input stream", e);
		} catch (final ParserConfigurationException e) {
			logger.log(Level.SEVERE, "can not parse input stream", e);
			throw new XMLPermutationProviderException("can not parse input stream", e);
		}
	}

	protected void handleFileNodes(final Set<String> set, final NodeList fileNodes) throws XMLPermutationProviderException {
		for (int i = 0 ; i < fileNodes.getLength() ; i++) {
			final Node item = fileNodes.item(i);
			if (item.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			final Element fileNode = (Element) item;

			final NodeList childNodes = fileNode.getChildNodes();

			if (childNodes.getLength() != 1) {
				logger.severe("Unexpected XML Structure: Expected property value");
				throw new XMLPermutationProviderException();
			}

			final String varValue = childNodes.item(0).getNodeValue();
			set.add(varValue);
		}
	}

	protected void handlePermutation(final Map<String, List<BindingProperty>> map, final Element permutationNode) throws XMLPermutationProviderException {

		final String strongName = permutationNode.getAttribute(PERMUTATION_NAME);

		final ArrayList<BindingProperty> list = new ArrayList<>();
		map.put(strongName, list);

		final NodeList variableNodes = permutationNode.getChildNodes();
		for (int i = 0 ; i < variableNodes.getLength() ; i++) {
			final Node item = variableNodes.item(i);
			if (item.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			final Element variables = (Element) item;
			final String varKey = variables.getTagName();
			final NodeList childNodes = variables.getChildNodes();
			if (childNodes.getLength() != 1) {
				logger.severe("Unexpected XML Structure: Expected property value");
				throw new XMLPermutationProviderException();
			}

			final String varValue = childNodes.item(0).getNodeValue();
			final BindingProperty bindingProperty = new BindingProperty(varKey, varValue);
			list.add(bindingProperty);
		}
	}

	protected String transformDocumentToString(final Document document) throws XMLPermutationProviderException {
		try {
			final StringWriter xml = new StringWriter();
			final Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(new DOMSource(document), new StreamResult(xml));

			final String permMapString = xml.toString();
			return permMapString;
		} catch (final TransformerConfigurationException e) {
			logger.log(Level.SEVERE, "can not transform document to String");
			throw new XMLPermutationProviderException("can not transform document to String", e);
		} catch (final TransformerFactoryConfigurationError e) {
			logger.log(Level.SEVERE, "can not transform document to String");
			throw new XMLPermutationProviderException("can not transform document to String", e);
		} catch (final TransformerException e) {
			logger.log(Level.SEVERE, "can not transform document to String");
			throw new XMLPermutationProviderException("can not transform document to String", e);
		}
	}
}
