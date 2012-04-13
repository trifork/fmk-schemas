package dk.medicinkortet.xmlschema;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class FlattenWsdl {
	
	private static final String NS_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	private DocumentBuilderFactory documentBuilderFactory;
	private Node typesNode;
	
	/**
	 * Map from schemaLocation to SchemaFile, so that multiple references to the same
	 * schema file can be resolved to the same SchemaFile instance.
	 */
	private Map<String, SchemaFile> schemaFileMap = new HashMap<String, SchemaFile>();
	
	/** 
	 * Map from targetNamespace to SchemaSection, so that there can be a single SchemaSection 
	 * for each targetNamespace. 
	 */
	private Map<String, SchemaSection> schemaSectionMap = new HashMap<String, SchemaSection>();

	/**
	 * This class is used to collect all SchemaFile instances that have the same targetNamespace,
	 * so that they can be moved into the same 'schema' section of the output WSDL file.
	 */
	private static class SchemaSection {
		private String targetNamespace;
		private Map<String, String> prefixToNamespaceMap = new HashMap<String,String>();
		private Map<String, Set<String>> namespaceToSetOfPrefixesMap = new HashMap<String,Set<String>>();
		private Set<SchemaFile> schemaFilesToInclude = new HashSet<SchemaFile>();
	}
	
	/**
	 * This class is used to build a simple graph of the referenced schema files.
	 * The "root" of the graph is a set of schema files (instance of Set<SchemaFile>) referenced from the WSDL file.
	 * 
	 * When the graph is built, it is insured that references to the same file will
	 * be represented as a reference to a shared SchemaFile instance. 
	 */
	private static class SchemaFile {
		private String targetNamespace;
		private String location;
		private Element domSchemaNode;
		
		private boolean hasBeenAnalyzed = false;
		
		private Set<SchemaFile> references = new HashSet<SchemaFile>();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		new FlattenWsdl().parseXmlFile(args[0], args[1]);
		new FlattenWsdl().run("etc/wsdl/MedicineCard_2011_01_01.wsdl", "target/MedicineCard-inline_2011_01_01.wsdl");
	}
	
	private FlattenWsdl() {
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
	}
	
	private void run(String input, String output) {
		try {
			File inputFile = new File(input);
			File basedir = inputFile.getParentFile();
			Document wsdlDocument = parseXmlFile(inputFile);
			
			Set<SchemaFile> referredSchemaFiles = findReferencedSchemaFilesInWsdl(wsdlDocument, basedir);
			
			for (SchemaFile schemaFile : referredSchemaFiles) {
				findReferencedSchemaFilesInSchemaFile(schemaFile);
			}
			
			for (SchemaSection schemaSection : schemaSectionMap.values()) {
				insertSchemaSectionIntoWsdl(schemaSection, wsdlDocument);
			}
			
			wsdlDocument.normalizeDocument();
			Transformer trans = TransformerFactory.newInstance().newTransformer();
			StreamResult streamResult = new StreamResult(new File(output));
			trans.transform(new DOMSource(wsdlDocument.getDocumentElement()), streamResult);
			
//			printSchemaFiles(referredSchemaFiles);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Document parseXmlFile(File file) throws ParserConfigurationException, SAXException, IOException  {
		//System.out.println("Parsing file '" + file.getName() + "'");
		return documentBuilderFactory.newDocumentBuilder().parse(file);
	}
	
//	private void printSchemaFiles(Set<SchemaFile> schemaFiles) {
//		for (SchemaFile schemaFile : schemaFiles) {
//			System.out.println(schemaFile.location);
//		}
//	}
	
	private Set<SchemaFile> findReferencedSchemaFilesInWsdl(Document dom, File basedir) throws IOException {
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
			throw new RuntimeException("Expected a 'types' element in WSDL file");
		}
		
		// Traverse child nodes to 'types' and proces 'schema' elements
		Set<SchemaFile> referredSchemaFiles = new HashSet<SchemaFile>();
		Node node = typesNode.getFirstChild();
		while (node != null) {
			if ("schema".equals(node.getLocalName())) {
				findReferencedSchemaFilesInSchemaNode(node, referredSchemaFiles, basedir);
			}
			node = node.getNextSibling();
		}
		
		return referredSchemaFiles;
	}

	private void findReferencedSchemaFilesInSchemaFile(SchemaFile schemaFile) 
			throws ParserConfigurationException, SAXException, IOException {
		
		// Schema files may be referred to from multiple paths, so check if this one has
		// already been parsed and analyzed
		if (schemaFile.hasBeenAnalyzed) {
			return;
		}
		schemaFile.hasBeenAnalyzed = true;
		
		// schemaLocation has been converted to an absolute path in schemaFile.location
		File file = new File(schemaFile.location); 
		File basedir = file.getParentFile();
		Document dom = parseXmlFile(file);
		schemaFile.domSchemaNode = dom.getDocumentElement();
		 
		if (!"schema".equals(schemaFile.domSchemaNode.getLocalName())) {
			throw new RuntimeException("Expected root element 'schema' in schema file");
		}
		
		registerSchemaFile(schemaFile);
		findReferencedSchemaFilesInSchemaNode(schemaFile.domSchemaNode, schemaFile.references, basedir);
		//System.out.println("Found " + schemaFile.references.size() + " references in " + schemaFile.location);
		
		// Recurse into the schema files referenced by the current schema file
		for (SchemaFile ref : schemaFile.references) {
			findReferencedSchemaFilesInSchemaFile(ref);
		}
	}

	
	/**
	 * Register the given schemaFile to be included in a given SchemaSection, and also register the namespaces/prefixes used. 
	 * 
	 * NOTE: This tool does not rewrite namespace prefixes, so a given prefix can be used for one namespace only in a given
	 * schema section. Conflicts will generate a RuntimeException.
	 */
	private void registerSchemaFile(SchemaFile schemaFile) {
		String targetNamespace = getAttribute(schemaFile.domSchemaNode, "targetNamespace");
		if (targetNamespace == null) {
			throw new RuntimeException("Expected attribute 'targetNamespace' of 'schema' element in " + schemaFile.location);
		}
		
		if (schemaFile.targetNamespace != null && !schemaFile.targetNamespace.equals(targetNamespace)) {
			throw new RuntimeException("An import of schema file '" + schemaFile.location + "' specified namespace '"
					+ schemaFile.targetNamespace + "' but the targetNamespace inside the schema file is '"
					+ targetNamespace + "'");
		}
		
		schemaFile.targetNamespace = targetNamespace;

		//System.out.println("Registering SchemaFile: " + schemaFile.location + ", targetNamespace=" + targetNamespace);
		SchemaSection schemaSection = getSchemaSectionForTargetNamespace(targetNamespace);
		schemaSection.schemaFilesToInclude.add(schemaFile);

		// Find and register namespace prefixes from attributes in the 'schema' node
		// Example: <schema xmlns:example="http://www.example.com/service">
		NamedNodeMap attributes = schemaFile.domSchemaNode.getAttributes();
		for (int i=0; i<attributes.getLength(); i++) {
			Node node = attributes.item(i);
			
			if (node.getPrefix() != null && node.getPrefix().equals("xmlns")) {
				String attrNsPrefix = node.getLocalName();
				String attrNamespace = node.getNodeValue();
				
				String registeredNamespace = schemaSection.prefixToNamespaceMap.get(attrNsPrefix);
				if (registeredNamespace != null) {
					if (!registeredNamespace.equals(attrNamespace)) {
						System.err.println("prefixToNamespaceMap:" + schemaSection.prefixToNamespaceMap);
						System.err.println("namespaceToSetOfPrefixesMap:" + schemaSection.namespaceToSetOfPrefixesMap);
						throw new RuntimeException("This tool cannot currently handle that multiple namespaces that use the same prefix. " +
								"prefix=" + attrNsPrefix + ". new namespace: " + attrNamespace + 
								", registered namespace: " + registeredNamespace);
					}
				} else {
					schemaSection.prefixToNamespaceMap.put(attrNsPrefix, attrNamespace);
					
					Set<String> setOfPrefixes = schemaSection.namespaceToSetOfPrefixesMap.get(attrNamespace);
					if (setOfPrefixes == null) {
						setOfPrefixes = new HashSet<String>();
						schemaSection.namespaceToSetOfPrefixesMap.put(attrNamespace, setOfPrefixes);
					}
					setOfPrefixes.add(attrNsPrefix);
				}
			}
		}

	}

	private SchemaSection getSchemaSectionForTargetNamespace(String targetNamespace) {
		SchemaSection schemaSection = schemaSectionMap.get(targetNamespace);
		if (schemaSection == null) {
			schemaSection = new SchemaSection();
			schemaSection.targetNamespace = targetNamespace;
			schemaSectionMap.put(targetNamespace, schemaSection);
		}
		return schemaSection;
	}
	
	/**
	 * Helper method that is used both when finding references directly in the wsdl file
	 * and when finding references in the schema files.
	 * 
	 * @param node The 'schema' node (from WSDL or XSD file) to be processed.
	 * @param schemaFileMap Map from canonical schemaLocation string to SchemaFile representations, to insure they are 
	 * 		processed and included only once.
	 * @param referredSchemaFiles A Set instance to be filled with a SchemaFile instance for each import/include 
	 * 		found in this file.
	 * @param removeReferenceNodes If true this method will remove the import/include nodes as they are found.
	 * @throws IOException 
	 */
	private void findReferencedSchemaFilesInSchemaNode(Node node, Set<SchemaFile> referredSchemaFiles, File basedir) 
			throws IOException {
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
				
				SchemaFile schemaFile = getSchemaFile(schemaLocation, basedir);
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
	
	private SchemaFile getSchemaFile(String schemaLocation, File basedir) throws IOException {
		try {
			// If schemaLocation is a valid URL, then don't inline!?!
			new URL(schemaLocation);
			System.err.println("*** Skipping URL schema location: " + schemaLocation);
			return null;
		} catch (MalformedURLException e) {
			// ok, continue
		}
		
		String canonicalSchemaLocation = new File(basedir, schemaLocation).getCanonicalPath();
		
		SchemaFile schemaFile = schemaFileMap.get(canonicalSchemaLocation);
		if (schemaFile == null) {
			schemaFile = new SchemaFile();
			schemaFile.location = canonicalSchemaLocation;
			
			schemaFileMap.put(canonicalSchemaLocation, schemaFile);
		}
		
		return schemaFile;
	}

	private String getAttribute(Node node, String attributeName) {
		Node attribute = node.getAttributes().getNamedItem(attributeName);
		return attribute == null ? null : attribute.getNodeValue();
	}

	private void insertSchemaSectionIntoWsdl(SchemaSection schemaSection, Document wsdlDocument) throws TransformerFactoryConfigurationError, TransformerException {
		
		System.out.println("Generating <schema targetNamespace='" + schemaSection.targetNamespace + "'>");
		
		// Create <schema> element defining all the relevant namespace prefixes
		Element schemaElement = wsdlDocument.createElementNS(NS_SCHEMA, "schema");
		schemaElement.setAttribute("xmlns", "http://www.w3.org/2001/XMLSchema");
		schemaElement.setAttribute("targetNamespace", schemaSection.targetNamespace);
		for (Map.Entry<String, String> entry : schemaSection.prefixToNamespaceMap.entrySet()) {
			schemaElement.setAttribute("xmlns:"+entry.getKey(), entry.getValue());
		}
		typesNode.appendChild(schemaElement);
		
		// This set is used to collect the namespace which already have an import element in the schema section
		Set<String> importedNamespaces = new HashSet<String>();
		DOMResult schemaElementResult = new DOMResult(schemaElement);

		// Insert contents of files into schemaElement
		// First all imports
		for (SchemaFile schemaFile : schemaSection.schemaFilesToInclude) {
			insertSchemaFileIntoSchemaElement(schemaFile, schemaElementResult, importedNamespaces, true);
		}
		// Then everything except imports
		for (SchemaFile schemaFile : schemaSection.schemaFilesToInclude) {
			insertSchemaFileIntoSchemaElement(schemaFile, schemaElementResult, importedNamespaces, false);
		}
	}
	
	private void insertSchemaFileIntoSchemaElement(SchemaFile schemaFile, DOMResult schemaElementResult, Set<String> importedNamespaces, boolean imports) 
			throws TransformerFactoryConfigurationError, TransformerException {
		
		if (schemaFile.domSchemaNode == null) {
			throw new RuntimeException("dom == null for schemaLocation=" + schemaFile.location);
		}
		
		Transformer trans = TransformerFactory.newInstance().newTransformer();
		NodeList childNodeList = schemaFile.domSchemaNode.getChildNodes();
		
		for (int i=0; i<childNodeList.getLength(); i++) {
			Node node = childNodeList.item(i);
			
			if ("import".equals(node.getLocalName()) && NS_SCHEMA.equals(node.getNamespaceURI())) {
				if (imports) {
					Node namespaceNode = node.getAttributes().getNamedItem("namespace");
					if (namespaceNode == null) {
						throw new RuntimeException("Expected a 'namespace' attribute in import statement in file: " + schemaFile.location);
					}
					if (importedNamespaces.contains(namespaceNode.getNodeValue())) {
						// This is an import that is already included, so don't copy it again!
						continue;
					}
					System.out.println("    <import namespace='" + namespaceNode.getNodeValue() + "'/>");
					importedNamespaces.add(namespaceNode.getNodeValue());
					trans.transform(new DOMSource(node), schemaElementResult);
				}
			} else if (!imports) {
				trans.transform(new DOMSource(node), schemaElementResult);
			}
		}
		
//		for (SchemaFile ref : schemaFile.references) {
//			insertSchemaSectionIntoWsdl(ref, wsdlDocument);
//		}
		
	}
}
