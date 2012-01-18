package dk.medicinkortet.xmlschema;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import dk.sosi.seal.xml.XmlUtil;

/**
 * This class goes through the schemas in etc/schemas and places them in the correct structure in target/gensrc/META-INF.
 * 
 * {@link SchemaResolver} and {@link SchemaInput} requires this structure to work.
 * 
 * @author recht
 *
 */
public class SchemaLoader {
	
	private static final String SCHEMA_NS = "http://www.w3.org/2001/XMLSchema";
	private static final String NS_NS = "http://www.w3.org/2000/xmlns/";
	private static final String XML_NS = "http://www.w3.org/XML/1998/namespace";
	private static final String SOAP_NS = "http://schemas.xmlsoap.org/soap/envelope/";

	private static HashMap<String,Integer> uniquePrefixIdx = new HashMap<String,Integer>();
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws ParserConfigurationException, IOException, URISyntaxException, SAXException, TransformerFactoryConfigurationError, TransformerException {
		File outputDir = new File("target/gensrc/META-INF/schemas/");
		FileUtils.forceMkdir(outputDir);

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		DocumentBuilder builder = builderFactory.newDocumentBuilder();

		Document wsdlDoc = builder.parse(new File("etc/wsdl/"+System.getProperty("WsdlName")+"_"+System.getProperty("VersionDate")+".wsdl"));
		Element wsdl = wsdlDoc.getDocumentElement();
		NodeList typeElements = wsdl.getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/", "types");
		Element types = null;
		if (typeElements.getLength() == 1) {
			types = (Element) typeElements.item(0);
		} else {
			throw new IllegalStateException("WSDL file must have a <types> element");
		}
		NodeList children = types.getChildNodes();
		for (int i = children.getLength() - 1; i >= 0; i--) {
			Node item = children.item(i);
			item.getParentNode().removeChild(item);
		}
		
		Map<String, Set<File>> namespaces = new HashMap<String, Set<File>>();
		Set<URI> externals = new HashSet<URI>();
		
		Pattern locationPattern = Pattern.compile("schemaLocation\\s*=\\s*[\"'](\\S+)[\"']");


		System.out.println("Copying schema files to " + outputDir);
		Collection<File> files = FileUtils.listFiles(new File("etc/schemas"), FileFilterUtils.suffixFileFilter("xsd"), FileFilterUtils.directoryFileFilter());
		for (File file : files) {
			try {
				Document doc = builder.parse(file);
				String ns = doc.getDocumentElement().getAttribute("targetNamespace");
				
				Set<File> schemas = namespaces.get(ns);
				if (schemas == null) {
					schemas = new HashSet<File>();
					namespaces.put(ns, schemas);
				}
				schemas.add(file);
				
				URI p = new URI(ns);
				String path = p.getPath();
				if (path == null) {
					path = ns.replaceAll(":", "_");
				}
				File sub = new File(outputDir, path);
				FileUtils.forceMkdir(sub);
				
				OutputStream fos = new BufferedOutputStream(new FileOutputStream(new File(sub, file.getName())));
				byte[] contents = FileUtils.readFileToByteArray(file);
				if (contents.length > 3 && contents[0] == (byte)0xef && contents[1] == (byte)0xbb && contents[2] == (byte)0xbf) {
					System.out.println("Removing BOM from " + file);
					fos.write(contents, 3, contents.length - 3);
				} else {
					fos.write(contents);
				}
				fos.close();
			} catch (SAXException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			StringBuilder sb = new StringBuilder((int)file.length());
			BufferedReader r = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = r.readLine()) != null) {
				sb.append(line);
			}
			
			String schema = sb.toString();
			r.close();

			Matcher m = locationPattern.matcher(schema);
			while (m.find()) {
				URI location = new URI(m.group(1));
				if (location.getScheme() != null) {
					externals.add(location);
				}
			}
		}
		
		System.out.println("Resolving externals: " + externals);
		Set<URI> missing = new HashSet<URI>();
		for (URI external : externals) {
			File schema = new File(outputDir, external.getPath());
			if (!schema.exists()) {
				missing.add(external);
			}
		}
		if (missing.size() > 0) {
			throw new RuntimeException("Some schemas are missing: " + missing);
		}
		
		
		System.out.println("Generating index files");
		for (Map.Entry<String, Set<File>> e : namespaces.entrySet()) {
			URI p = new URI(e.getKey());
			String path = p.getPath();
			if (path == null) {
				path = e.getKey().replaceAll(":", "_");
			}
			File sub = new File(outputDir, path);
			
			File out = new File(sub, "index.xsd");
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out)));
			
			try {
				w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				w.write("<schema targetNamespace=\"");
				w.write(e.getKey());
				w.write("\" xmlns=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\">\n");
				
				Element ns = addSchemaToWsdl(types, wsdlDoc, wsdl, e.getKey());
				
				for (File schema : e.getValue()) {
					w.write("<include schemaLocation=\"");
					w.write(schema.getName());
					w.write("\"/>\n");

					insertSchema(builder, wsdlDoc, ns, schema);
				}
				w.write("</schema>");
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			} finally {
				w.close();
			}
		}

		organizeIncludesAndImports(wsdl);
		
		wsdl.normalize();
		File wsdlOut = new File("target/wsdl/"+System.getProperty("WsdlName")+"-inline_"+System.getProperty("VersionDate")+".wsdl");
		
		FileOutputStream fw = new FileOutputStream(wsdlOut);
		IOUtils.write(XmlUtil.node2String(wsdl, true, true), fw, "UTF-8");
		fw.close();
		System.out.println("Written new wsdl to " + wsdlOut);
	}
	
	private static void organizeIncludesAndImports(Element wsdl) {
		System.out.println("Removing includes from wsdl");
		
		NodeList includes = wsdl.getElementsByTagNameNS(SCHEMA_NS, "include");
		for (int i = includes.getLength() - 1; i >= 0; i--) {
			Node node = includes.item(i);
			node.getParentNode().removeChild(node);
		}
		
		Map<Node, Set<String>> imported = new HashMap<Node, Set<String>>();
		
		System.out.println("Organizing imports in wsdl");
		NodeList imports = wsdl.getElementsByTagNameNS(SCHEMA_NS, "import");
		for (int i = 0; i < imports.getLength(); i++) {
			Node item = imports.item(i);
			if (item instanceof Element) {
				((Element)item).removeAttribute("schemaLocation");
				
				Set<String> importedNamespaces = imported.get(item.getParentNode());
				if (importedNamespaces == null) {
					importedNamespaces = new HashSet<String>();
					imported.put(item.getParentNode(), importedNamespaces);
				}
				if (!importedNamespaces.contains(((Element)item).getAttribute("namespace"))) {
					item.getParentNode().insertBefore(item, item.getParentNode().getFirstChild());
					
					importedNamespaces.add(((Element)item).getAttribute("namespace"));
				} else {
					item.getParentNode().removeChild(item);
					i--;
				}
			}
			
		}
	}

	private static HashMap<String, String> ns2Prefix = new HashMap<String, String>();
	private static HashMap<String, String> prefix2NS = new HashMap<String, String>();
	
	private static void insertSchema(DocumentBuilder builder, Document wsdlDoc, Element ns, File schema) throws SAXException, IOException {
		if (ns == null) {
			System.out.println("Ignoring " + schema);
			return;
		}
		Element childDoc = builder.parse(schema).getDocumentElement();
		
		NamedNodeMap attributes = childDoc.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node attr = attributes.item(i);
			
			if (NS_NS.equals(attr.getNamespaceURI()) && "xmlns".equals(attr.getLocalName()) && !ns.hasAttributeNS(NS_NS, "xmlns")) {
				ns.setAttribute("xmlns", attr.getNodeValue());
			} else if (NS_NS.equals(attr.getNamespaceURI())) { //&& !ns.hasAttributeNS(NS_NS, attr.getLocalName())) {

				String prefix = ns2Prefix.get(attr.getNodeValue());
				String ns1 = prefix2NS.get(attr.getLocalName());
				
				if (ns1 != null && !ns1.equals(attr.getNodeValue())) {
					// same prefix, different namespace. Rewrite.

					// only make up a prefix if namespace hasn't previously been bound to another prefix
					if (prefix == null) {
						prefix = makeUniquePrefix(attr.getLocalName());
					}
				}

				if (prefix == null) {
					prefix = attr.getLocalName();
				}

				if (!prefix.equals(attr.getLocalName())) {
					rewritePrefix(attr.getLocalName(), prefix, ns, attr, childDoc);
				}
				ns.setAttribute("xmlns:" + prefix, attr.getNodeValue());

				ns2Prefix.put(attr.getNodeValue(), prefix);
				prefix2NS.put(prefix, attr.getNodeValue());
			}
		}
		
		if (childDoc.getAttribute("xmlns") != null && childDoc.getAttribute("xmlns").length() > 0) {
			String defaultPrefix = findOrDeclarePrefix(ns, childDoc.getAttribute("xmlns"));
			ns.setAttribute("xmlns:" + defaultPrefix, childDoc.getAttribute("xmlns"));
			NodeList childElements = childDoc.getElementsByTagNameNS(SCHEMA_NS, "element");
			for (int j = 0; j < childElements.getLength(); j++) {
				Element el = (Element) childElements.item(j);
				String refAttr = el.getAttribute("type");
				if (refAttr != null && refAttr.length() > 0 && refAttr.indexOf(':') == -1) {
					el.setAttribute("type", defaultPrefix + ":" + refAttr);
				}
			}
		}
		
							
		NodeList elements = childDoc.getChildNodes();
		for (int i = 0; i < elements.getLength(); i++) {
			ns.appendChild(wsdlDoc.importNode(elements.item(i), true));
		}
	}
	
	private static void rewritePrefix(String oldPrefix, String newPrefix,
			Element ns, Node attr, Element childDoc) {

		ns.setAttribute("xmlns:" + newPrefix, attr.getNodeValue());
		childDoc.removeAttribute(attr.getNodeValue());
		
		NodeList elements = childDoc.getElementsByTagNameNS(SCHEMA_NS, "element");
		for (int j = 0; j < elements.getLength(); j++) {
			Element el = (Element) elements.item(j);
			
			String refAttr = el.getAttribute("ref");
			if (refAttr != null && refAttr.startsWith(oldPrefix + ":")) {
				el.setAttribute("ref", newPrefix + ":" + refAttr.substring((oldPrefix + ":").length()));
			}
			String typeAttr = el.getAttribute("type");
			if (typeAttr != null && typeAttr.startsWith(oldPrefix + ":")) {
				el.setAttribute("type", newPrefix + ":" + typeAttr.substring((oldPrefix + ":").length()));
			}
		}
	}

	private static String findOrDeclarePrefix(Element top, String namespace) {
		NamedNodeMap attributes = top.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node attr = attributes.item(i);
			if (attr.getNodeName().startsWith("xmlns:") && attr.getNodeValue().equals(namespace)) {
				return attr.getNodeName().substring(6);
			}
		}
		String defaultPrefix = makeUniquePrefix("def");
		top.setAttribute("xmlns:" + defaultPrefix, namespace);
		return defaultPrefix;
	}

	private static String makeUniquePrefix(String s) {
		Integer idx = uniquePrefixIdx.get(s);
		if (idx == null) {
			idx = 1;
		} else {
			idx += 1;
		}
		uniquePrefixIdx.put(s, idx);

		return s + idx;
	}

	private static Element addSchemaToWsdl(Element types, Document wsdlDoc, Element wsdl, String namespace) {
		if (namespace != null && !namespace.equals("")) {
			if (SCHEMA_NS.equals(namespace) || XML_NS.equals(namespace) || SOAP_NS.equals(namespace)) {
				return null;
			}
		} else {
			return null;
		}
		Element ns = wsdlDoc.createElementNS(SCHEMA_NS, "schema");
		ns.setPrefix("xs");
		ns.setAttribute("targetNamespace", namespace);
		ns.setAttribute("elementFormDefault", "qualified");
		ns.setAttribute("attributeFormDefault", "unqualified");
		types.appendChild(ns);

		return ns;
	}
}
