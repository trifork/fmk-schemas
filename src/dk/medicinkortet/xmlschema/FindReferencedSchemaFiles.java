package dk.medicinkortet.xmlschema;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class FindReferencedSchemaFiles {
	private DocumentBuilderFactory documentBuilderFactory;
	private Set<SchemaFile> schemaFiles = new HashSet<SchemaFile>();

	/**
	 * Map from schemaLocation to SchemaFile, so that multiple references to the same
	 * schema file can be resolved to the same SchemaFile instance.
	 */
	private Map<String, SchemaFile> schemaFileMap = new HashMap<String, SchemaFile>();
	
	/**
	 * This class is used to build a simple graph of the referenced schema files.
	 * The "root" of the graph is a set of schema files (instance of Set<SchemaFile>) referenced from the WSDL file.
	 * 
	 * When the graph is built, it is insured that references to the same file will
	 * be represented as a reference to a shared SchemaFile instance. 
	 */
	public static class SchemaFile {
		public String targetNamespace;
		public String location;
		public URL url;
		public Element domSchemaNode;
		
		private boolean hasBeenAnalyzed = false;
		
		public Set<SchemaFile> references = new HashSet<SchemaFile>();
	}

	public static Map<String,Set<SchemaFile>> run(Document wsdlDocument, File basedir) {
		FindReferencedSchemaFiles findReferencedSchemaFiles = new FindReferencedSchemaFiles();
		findReferencedSchemaFiles.doRun(wsdlDocument, basedir);
		
		Map<String, Set<SchemaFile>> ns2schemaFilesMap = new HashMap<String, Set<SchemaFile>>();
		for (SchemaFile schemaFile : findReferencedSchemaFiles.schemaFiles) {
			Set<SchemaFile> set = ns2schemaFilesMap.get(schemaFile.targetNamespace);
			if (set == null) {
				set = new HashSet<SchemaFile>();
				ns2schemaFilesMap.put(schemaFile.targetNamespace, set);
			}
			set.add(schemaFile);
		}
		return ns2schemaFilesMap;
	}
	
	private void doRun(Document wsdlDocument, File basedir) {
		try {
			Set<SchemaFile> referredSchemaFiles = findReferencedSchemaFilesInWsdl(wsdlDocument, basedir);
			
			for (SchemaFile schemaFile : referredSchemaFiles) {
				findReferencedSchemaFilesInSchemaFile(schemaFile);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private DocumentBuilderFactory getDocumentBuilderFactory() {
		if (documentBuilderFactory == null) {
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
		}
		return documentBuilderFactory;
	}
	
	private Document parseXmlFile(File file) throws ParserConfigurationException, SAXException, IOException  {
		//System.out.println("Parsing file '" + file.getName() + "'");
		return getDocumentBuilderFactory().newDocumentBuilder().parse(file);
	}
	
	private Document parseXmlFile(URL url) throws ParserConfigurationException, SAXException, IOException  {
		System.out.println("Parsing file from URL '" + url.toExternalForm() + "'");
		InputStream stream = null;
		try {
			stream = url.openStream();
			return getDocumentBuilderFactory().newDocumentBuilder().parse(new InputSource(stream));
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}
	
	private Set<SchemaFile> findReferencedSchemaFilesInWsdl(Document dom, File basedir) throws IOException {
		Element rootElm = dom.getDocumentElement();

		// Find definitions/types/schema/ section of wsdl
		if (!"definitions".equals(rootElm.getLocalName())) {
			throw new RuntimeException("Expected 'definitions' element, found '" + rootElm.getLocalName() + "'");
		}
		String wsdlTargetNamespace = getAttribute(rootElm, "targetNamespace");
		
		Node typesNode = rootElm.getFirstChild();
		while (typesNode != null && !"types".equals(typesNode.getLocalName())) {
			typesNode = typesNode.getNextSibling();
		}
		if (typesNode == null) {
			throw new RuntimeException("Expected a 'types' element in WSDL file");
		}
		
		SchemaFile dummyWsdlSchemaFile = new SchemaFile();
		dummyWsdlSchemaFile.location = new File(basedir, "dummy.wsdl").getCanonicalPath();
		
		// Traverse child nodes to 'types' and proces 'schema' elements
		Set<SchemaFile> referredSchemaFiles = new HashSet<SchemaFile>();
		Node node = typesNode.getFirstChild();
		while (node != null) {
			if ("schema".equals(node.getLocalName())) {
				String targetNamespace = getAttribute(node, "targetNamespace");
				if (targetNamespace == null) {
					targetNamespace = wsdlTargetNamespace;
				}
					
				findReferencedSchemaFilesInSchemaNode(node, dummyWsdlSchemaFile, targetNamespace);
			}
			node = node.getNextSibling();
		}
		
		return dummyWsdlSchemaFile.references;
	}

	private void findReferencedSchemaFilesInSchemaFile(SchemaFile schemaFile) 
			throws ParserConfigurationException, SAXException, IOException {
		
		// Schema files may be referred to from multiple paths, so check if this one has
		// already been parsed and analyzed
		if (schemaFile.hasBeenAnalyzed) {
			return;
		}
		schemaFile.hasBeenAnalyzed = true;
		
		File basedir = null;
		if (schemaFile.location != null) {
			// schemaLocation has been converted to an absolute path in schemaFile.location
			File file = new File(schemaFile.location); 
			basedir = file.getParentFile();
			Document dom = parseXmlFile(file);
			schemaFile.domSchemaNode = dom.getDocumentElement();
		} else {
			Document dom = parseXmlFile(schemaFile.url);
			schemaFile.domSchemaNode = dom.getDocumentElement();
		}
		 
		if (!"schema".equals(schemaFile.domSchemaNode.getLocalName())) {
			throw new RuntimeException("Expected root element 'schema' in schema file");
		}
		
		//registerSchemaFile(schemaFile);
		findReferencedSchemaFilesInSchemaNode(schemaFile.domSchemaNode, schemaFile, schemaFile.targetNamespace);
		//System.out.println("Found " + schemaFile.references.size() + " references in " + schemaFile.location);
		
		// Recurse into the schema files referenced by the current schema file
		for (SchemaFile ref : schemaFile.references) {
			findReferencedSchemaFilesInSchemaFile(ref);
		}
	}

	/**
	 * Helper method that is used both when finding references directly in the wsdl file
	 * and when finding references in the schema files.
	 * 
	 * @param node The 'schema' node (from WSDL or XSD file) to be processed.
	 * @param referredSchemaFiles A Set instance to be filled with a SchemaFile instance for each import/include 
	 * 		found in this file.
	 * @param removeReferenceNodes If true this method will remove the import/include nodes as they are found.
	 * @throws IOException 
	 */
	private void findReferencedSchemaFilesInSchemaNode(Node node, SchemaFile referringSchemaFile, String referringFileTargetNamespace) 
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
				SchemaFile schemaFile = getSchemaFile(schemaLocation, referringSchemaFile);
				String targetNamespace;
				if ("include".equals(child.getLocalName())) {
					targetNamespace = referringFileTargetNamespace;
				} else {
					targetNamespace = getAttribute(child, "namespace");
				}
				
				if (schemaFile.targetNamespace == null) {
					schemaFile.targetNamespace = targetNamespace;
					//schemaFileMap.put(targetNamespace, schemaFile);
				} else if (targetNamespace != null && !schemaFile.targetNamespace.equals(targetNamespace)) {
					throw new RuntimeException("Different namespaces expected from import of schema file: " + schemaFile.location);
				}
				referringSchemaFile.references.add(schemaFile);
				
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
			child = child.getNextSibling();
		}
	}
	
	private String getAttribute(Node node, String attributeName) {
		Node attribute = node.getAttributes().getNamedItem(attributeName);
		return attribute == null ? null : attribute.getNodeValue();
	}

	private SchemaFile getSchemaFile(String schemaLocation, SchemaFile referringSchemaFile) throws IOException {
		String canonicalSchemaLocation = null;
		URL url = null;
		String key;
		try {
			if (referringSchemaFile.url != null) {
				// If the referring schema file was referenced by an URL, then interprete the schemaLocation
				// in that context, ie. as a possible relative URL.
				url = new URL(referringSchemaFile.url, schemaLocation);
			} else {
				// Otherwise, just try to interprete the schemaLocation as an URL
				url = new URL(schemaLocation);
			}
			key = schemaLocation;
			//System.err.println("*** Skipping URL schema location: " + schemaLocation);
		} catch (MalformedURLException e) {
			File basedir = new File(referringSchemaFile.location).getParentFile();
			canonicalSchemaLocation = new File(basedir, schemaLocation).getCanonicalPath();
			key = canonicalSchemaLocation;
		}
		
		SchemaFile schemaFile = schemaFileMap.get(key);
		if (schemaFile == null) {
			schemaFile = new SchemaFile();
			schemaFile.location = canonicalSchemaLocation;
			schemaFile.url = url;

			schemaFiles.add(schemaFile);
			schemaFileMap.put(key, schemaFile);
		}
		
		return schemaFile;
	}

}
