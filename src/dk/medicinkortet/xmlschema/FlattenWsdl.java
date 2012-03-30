package dk.medicinkortet.xmlschema;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class FlattenWsdl {
	
	private Node typesNode;

	private static class SchemaFile {
		private String targetNamespace;
		private String location;
		private Element domSchemaNode;
		
		private boolean analyzed = false;
		private boolean output = false;
		
		private Set<SchemaFile> references = new HashSet<SchemaFile>();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		new FlattenWsdl().parseXmlFile(args[0], args[1]);
		new FlattenWsdl().run("etc/wsdl/MedicineCard_2011_01_01.wsdl", "target/MedicineCard-inline_2011_01_01.wsdl");
	}
	
	private void run(String input, String output) {
		try {
			File inputFile = new File(input);
			File basedir = inputFile.getParentFile();
			Document wsdlDocument = parseXmlFile(inputFile);
			
			Map<String, SchemaFile> map = new HashMap<String, SchemaFile>();
			Set<SchemaFile> referredSchemaFiles = findReferencedSchemaFilesInWsdl(wsdlDocument, map, basedir);
			
			for (SchemaFile schemaFile : referredSchemaFiles) {
				findReferencedSchemaFilesInSchemaFile(schemaFile, map);
			}
			
			for (SchemaFile schemaFile : referredSchemaFiles) {
				insertSchemaSectionIntoWsdl(schemaFile, wsdlDocument);
			}
			
//			Writer stream = new FileWriter(output);
//			XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(stream);
//			xmlStreamWriter.
			
			wsdlDocument.normalizeDocument();
			Transformer trans = TransformerFactory.newInstance().newTransformer();
			StreamResult streamResult = new StreamResult(new File(output));
			trans.transform(new DOMSource(wsdlDocument.getDocumentElement()), streamResult);
			
			printSchemaFiles(referredSchemaFiles);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Document parseXmlFile(File file) throws ParserConfigurationException, SAXException, IOException  {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document dom = db.parse(file);

		System.out.println("Parsed file '" + file.getName() + "'");
		return dom;
	}
	
	private void printSchemaFiles(Set<SchemaFile> schemaFiles) {
		for (SchemaFile schemaFile : schemaFiles) {
			System.out.println(schemaFile.location);
		}
	}
	
	private Set<SchemaFile> findReferencedSchemaFilesInWsdl(Document dom, Map<String, SchemaFile> map, File basedir) throws IOException {
		Element rootElm = dom.getDocumentElement();

		// Find definitions/types/schema/ section of wsdl
		if (!"definitions".equals(rootElm.getLocalName())) {
			throw new RuntimeException("Expected 'definitions' element, found '" + rootElm.getLocalName() + "'");
		}
		
		typesNode = rootElm.getFirstChild();
		while (typesNode != null && !"types".equals(typesNode.getLocalName())) {
			typesNode = typesNode.getNextSibling();
		}
		if (typesNode == null) {
			throw new RuntimeException("Expected 'types' element");
		}
		
		// Traverse child nodes to 'types' and proces 'schema' elements
		Set<SchemaFile> referredSchemaFiles = new HashSet<SchemaFile>();
		Node node = typesNode.getFirstChild();
		while (node != null) {
			if ("schema".equals(node.getLocalName())) {
				findReferencedSchemaFilesInSchemaNode(node, map, referredSchemaFiles, basedir);
			}
			node = node.getNextSibling();
		}
		
		return referredSchemaFiles;
	}

	private void findReferencedSchemaFilesInSchemaFile(SchemaFile schemaFile, Map<String, SchemaFile> map) 
			throws ParserConfigurationException, SAXException, IOException {
		
		// Schema files may be referred to from multiple paths, so check if this one has
		// already been parsed and analyzed
		if (schemaFile.analyzed) {
			return;
		}
		schemaFile.analyzed = true;
		
		// schemaLocation has been converted to an absolute path in schemaFile.location
		File file = new File(schemaFile.location); 
		File basedir = file.getParentFile();
		Document dom = parseXmlFile(file);
		Element e = dom.getDocumentElement();
		 
		if (!"schema".equals(e.getLocalName())) {
			throw new RuntimeException("Expected root element 'schema' in schema file");
		}
		
		schemaFile.domSchemaNode = e;
		findReferencedSchemaFilesInSchemaNode(e, map, schemaFile.references, basedir);
		System.out.println("Found " + schemaFile.references.size() + " references in " + schemaFile.location);
		
		// Recurse into the schema files referenced by the current schema file
		for (SchemaFile ref : schemaFile.references) {
			findReferencedSchemaFilesInSchemaFile(ref, map);
		}
	}

	/**
	 * Helper method that is used both when finding references directly in the wsdl file
	 * and when finding references in the schema files.
	 * 
	 * @param node The 'schema' node (from WSDL or XSD file) to be processed.
	 * @param map Map from canonical schemaLocation string to SchemaFile representations, to insure they are 
	 * 		processed and included only once.
	 * @param referredSchemaFiles A Set instance to be filled with a SchemaFile instance for each import/include 
	 * 		found in this file.
	 * @param removeReferenceNodes If true this method will remove the import/include nodes as they are found.
	 * @throws IOException 
	 */
	private void findReferencedSchemaFilesInSchemaNode(Node node, Map<String, SchemaFile> map,
			Set<SchemaFile> referredSchemaFiles, File basedir) throws IOException {
		// Traverse child nodes to 'schema' and proces 'import' and 'include' elements
		Node child = node.getFirstChild();
		while (child != null) {
			// Now check if child is an include or import element
			if ("import".equals(child.getLocalName()) || "include".equals(child.getLocalName())) {
				String schemaLocation = getAttribute(child, "schemaLocation");
				if (schemaLocation == null) {
					throw new RuntimeException("'" + child.getLocalName() + "' element is missing schemaLocation");
				}
				//System.out.println("Found " + child.getLocalName() + " schemaLocation=" + schemaLocation);
				
				SchemaFile schemaFile = getSchemaFile(schemaLocation, map, basedir);
				if (schemaFile != null) {
					String namespaceAttribute = getAttribute(child, "namespace");
					if (schemaFile.targetNamespace == null) {
						schemaFile.targetNamespace = namespaceAttribute;
					} else if (namespaceAttribute != null && !schemaFile.targetNamespace.equals(namespaceAttribute)) {
						throw new RuntimeException("Different namespaces expected from import of schema file: " + schemaFile.location);
					}
					referredSchemaFiles.add(schemaFile);
					
					if ("include".equals(child.getLocalName())) {
						// Remove the import/include element from the DOM (because this will be copied into the WSDL)
						Node oldChild = child;
						child = child.getNextSibling();
						oldChild.getParentNode().removeChild(oldChild);
						continue;
					} else {
						child.getAttributes().removeNamedItem("schemaLocation");
					}
				}
			}
			child = child.getNextSibling();
		}
	}
	
	private SchemaFile getSchemaFile(String schemaLocation, Map<String, SchemaFile> map, File basedir) throws IOException {
		try {
			// If schemaLocation is a valid URL, then don't inline!?!
			new URL(schemaLocation);
			System.err.println("*** Skipping URL schema location: " + schemaLocation);
			return null;
		} catch (MalformedURLException e) {
			// ok, continue
		}
		
		String canonicalSchemaLocation = new File(basedir, schemaLocation).getCanonicalPath();
		
		SchemaFile schemaFile = map.get(canonicalSchemaLocation);
		if (schemaFile == null) {
			schemaFile = new SchemaFile();
			schemaFile.location = canonicalSchemaLocation;
			
			map.put(canonicalSchemaLocation, schemaFile);
		}
		
		return schemaFile;
	}

	private String getAttribute(Node node, String attributeName) {
		Node attribute = node.getAttributes().getNamedItem(attributeName);
		return attribute == null ? null : attribute.getNodeValue();
	}

	/**
	 * This makes a depth-first traversal of the SchemaFile "graph" and inserts the "leafs" into the WSDL first.
	 * That ensures that types are defined before being used in the output WSDL.
	 *  
	 * @param schemaFile
	 * @param wsdlDocument
	 * @throws TransformerException 
	 * @throws TransformerFactoryConfigurationError 
	 */
	private void insertSchemaSectionIntoWsdl(SchemaFile schemaFile, Document wsdlDocument) throws TransformerFactoryConfigurationError, TransformerException {
		// Test and set a flag to prevent the same file being copied more than once
		if (schemaFile.output) {
			return;
		}
		schemaFile.output = true;
		
		if (schemaFile.domSchemaNode == null) {
			throw new RuntimeException("dom == null for schemaLocation=" + schemaFile.location);
		}
		
//		schemaFile.domSchemaNode.normalize();
		Transformer trans = TransformerFactory.newInstance().newTransformer();
		trans.transform(new DOMSource(schemaFile.domSchemaNode), new DOMResult(typesNode));
		
		
		for (SchemaFile ref : schemaFile.references) {
			insertSchemaSectionIntoWsdl(ref, wsdlDocument);
		}
		
	}
}
