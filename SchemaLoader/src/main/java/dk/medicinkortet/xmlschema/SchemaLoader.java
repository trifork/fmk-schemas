package dk.medicinkortet.xmlschema;

import dk.medicinkortet.xmlschema.FindReferencedSchemaFiles.SchemaFile;
import dk.sosi.seal.xml.XmlUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class goes through the schemas in etc/schemas and places them in the correct structure in target/gensrc/META-INF.
 *
 * @author recht
 */
public class SchemaLoader {

    private static final String SCHEMA_NS = "http://www.w3.org/2001/XMLSchema";
    private static final String NS_NS = "http://www.w3.org/2000/xmlns/";
    private static final String XML_NS = "http://www.w3.org/XML/1998/namespace";
    private static final String SOAP_NS = "http://schemas.xmlsoap.org/soap/envelope/";

    private static HashMap<String, Integer> uniquePrefixIdx = new HashMap<String, Integer>();

    private static HashMap<String, String> ns2Prefix = new HashMap<String, String>();
    private static HashMap<String, String> prefix2NS = new HashMap<String, String>();

    private static HashSet<String> globalPrefixes = new HashSet<>();
    
    private static String targetDir() {
        String targetDir = System.getProperty("target.dir");
        if (targetDir != null) return targetDir;
        return "target";
    }

    private static String etcDir() {
        String etcDir = System.getProperty("etc.dir");
        if (etcDir != null) return etcDir;
        return "../etc";
    }


    public static void main(String[] args) throws ParserConfigurationException, IOException, URISyntaxException, SAXException, TransformerFactoryConfigurationError, TransformerException {
        File outputDir = new File(targetDir() + "/gensrc/META-INF/schemas/");
        FileUtils.forceMkdir(outputDir);

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();

        Map<String, Set<File>> namespaces = new HashMap<String, Set<File>>();
        Set<URI> externals = new HashSet<URI>();

        copySchemaFiles(outputDir, builder, namespaces, externals);
        resolveExternals(outputDir, externals);
        generateIndexFiles(outputDir, namespaces);
        Map<String, Set<SchemaFile>> ns2schemaFilesMap = generateInlineWsdl(builder);
        makeWsdlXsdCollectionZip(ns2schemaFilesMap.values());
    }

    private static void makeWsdlXsdCollectionZip(Collection<Set<SchemaFile>> schemaFilesCollection) throws IOException {

        File outputDir;
        String filename;

        if (System.getProperty("VersionDate") != null) {
            filename = targetDir() + "/wsdl/" + System.getProperty("WsdlName") + "_" + System.getProperty("VersionDate");
        } else {
            filename = targetDir() + "/wsdl/" + System.getProperty("WsdlName");
        }

        System.out.println("Make WSDL + XSD collection zip-file: " + filename);
        outputDir = new File(filename);
        outputDir.mkdirs();

        File schemadir = new File(outputDir.getAbsolutePath() + "/schemas");

        copyFileAndRemoveBOM(new File(getWsdlFilename()), outputDir);

        for (Set<SchemaFile> schemaFilesSet : schemaFilesCollection) {
            copySchemaFileSet(schemaFilesSet, schemadir);
        }
    }

    private static void copySchemaFileSet(Set<SchemaFile> schemaFilesSet, File outputDir) throws IOException {
        for (SchemaFile schemaFile : schemaFilesSet) {
            if (schemaFile.hasBeenCopied) {
                continue;
            }
            schemaFile.hasBeenCopied = true;

            if (schemaFile.location != null) {
                String destPath = schemaFile.location.substring(schemaFile.location.indexOf(File.separatorChar + "schemas" + File.separatorChar) + 9);
                File destFile = new File(outputDir, destPath);
                File destDir = destFile.getParentFile();
                destDir.mkdirs();
                copyFileAndRemoveBOM(new File(schemaFile.location), destDir);
            }

            if (schemaFile.references != null) {
                copySchemaFileSet(schemaFile.references, outputDir);
            }
        }

    }


    private static void resolveExternals(File outputDir, Set<URI> externals) {
        System.out.println("Resolving externals: " + externals);
        Set<URI> missing = new HashSet<URI>();
        for (URI external : externals) {
            File schema = new File(outputDir, external.getPath());
            if (!schema.exists()) {
                //TODO: Download schema if valid URL?
                downloadSchema(external, schema);
                missing.add(external);
            }
        }
        if (missing.size() > 0) {
            throw new RuntimeException("Some schemas are missing: " + missing);
        }
    }

    private static void downloadSchema(URI external, File target) {

    }

    @SuppressWarnings("unchecked")
    private static void copySchemaFiles(File outputDir,
                                        DocumentBuilder builder, Map<String, Set<File>> namespaces,
                                        Set<URI> externals) throws URISyntaxException,
            FileNotFoundException, IOException {
        System.out.println("Copying schema files to " + outputDir);
        Pattern locationPattern = Pattern.compile("schemaLocation\\s*=\\s*[\"'](\\S+)[\"']");
        Collection<File> files = FileUtils.listFiles(new File(etcDir() + "/schemas"), FileFilterUtils.suffixFileFilter("xsd"), FileFilterUtils.directoryFileFilter());
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

                //Copy to ns based location
                URI p = new URI(ns);
                String path = p.getPath();
                if (path == null) {
                    path = ns.replaceAll(":", "_");
                }
                File sub = new File(outputDir, path);
                FileUtils.forceMkdir(sub);
                copyFileAndRemoveBOM(file, sub);

/*
                //Copy to source based location
                String path2 = file.isFile() ? file.getParentFile().getPath() : file.getPath();
                path2 = path2.substring(path2.indexOf("/schemas/") + 9);
                File sub2 = new File(outputDir, path2);
                FileUtils.forceMkdir(sub2);
                copyFileAndRemoveBOM(file, sub2);
*/
            } catch (SAXException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            StringBuilder sb = new StringBuilder((int) file.length());
            BufferedReader r = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }

            String schema = sb.toString();
            r.close();

            Matcher m = locationPattern.matcher(schema); //TODO: Replace with xpath expression - the regexp will also find schemaLocation inside xs:documentation tags, and this will lead to problems...
            while (m.find()) {
                URI location = new URI(m.group(1));
                if (location.getScheme() != null) {
                    externals.add(location);
                }
            }
        }
    }

    private static void copyFileAndRemoveBOM(File srcFile, File destDir) throws IOException {
        // Copy file
        File targetFile = new File(destDir, srcFile.getName());
        if (targetFile.exists() && srcFile.isFile() && targetFile.isFile() && FileUtils.contentEquals(srcFile, targetFile)) {
//            System.out.println("--> Source '" + srcFile.getPath() + "' and targetFile '" + targetFile.getPath() + "' contents are identical...skipping copy");
            return;
        }
        OutputStream fos = new BufferedOutputStream(new FileOutputStream(targetFile));
        byte[] contents = FileUtils.readFileToByteArray(srcFile);
        if (contents.length > 3 && contents[0] == (byte) 0xef && contents[1] == (byte) 0xbb && contents[2] == (byte) 0xbf) {
            System.out.println("Removing BOM from " + srcFile);
            fos.write(contents, 3, contents.length - 3);
        } else {
            fos.write(contents);
        }
        fos.close();
    }

    private static void generateIndexFiles(File outputDir,
                                           Map<String, Set<File>> namespaces) throws URISyntaxException,
            FileNotFoundException, IOException {
        System.out.println("Generating index files");
        for (Map.Entry<String, Set<File>> e : namespaces.entrySet()) {
            String targetNamespace = e.getKey();
            URI p = new URI(targetNamespace);
            String path = p.getPath();
            if (path == null) {
                path = targetNamespace.replaceAll(":", "_");
            }
            File sub = new File(outputDir, path);

            File out = new File(sub, "index.xsd");
            BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out)));

            try {
                w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                w.write("<schema targetNamespace=\"");
                w.write(targetNamespace);
                w.write("\" xmlns=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\">\n");

                for (File schema : e.getValue()) {
                    w.write("<include schemaLocation=\"");
                    w.write(schema.getName());
                    w.write("\"/>\n");
                }
                w.write("</schema>");
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            } finally {
                w.close();
            }
        }
    }

    private static Map<String, Set<SchemaFile>> generateInlineWsdl(DocumentBuilder builder) throws IOException, SAXException {
        String inputFilename = getWsdlFilename();
        System.out.println("Generating Inline WSDL file: " + inputFilename);

        File wsdlFile = new File(inputFilename);
        File basedir = wsdlFile.getParentFile();
        Document wsdlDoc = builder.parse(wsdlFile);
        Element wsdl = wsdlDoc.getDocumentElement();

        NodeList typeElements = wsdl.getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/", "types");
        Element types = null;
        if (typeElements.getLength() == 1) {
            types = (Element) typeElements.item(0);
        } else {
            throw new IllegalStateException("WSDL file must have a <types> element");
        }

        // Find referenced schema files from the WSDL
        Map<String, Set<SchemaFile>> ns2schemaFilesMap = FindReferencedSchemaFiles.run(wsdlDoc, basedir);

        // Empty the <types> element of the WSDL file
        while (types.getFirstChild() != null) {
            types.removeChild(types.getFirstChild());
        }

        collectGlobalPrefixes(wsdlDoc);

        for (QName qn: FmkNamespaces.getNamespaces()) {
        	prefix2NS.put(qn.getPrefix(), qn.getNamespaceURI());
        	ns2Prefix.put(qn.getNamespaceURI(), qn.getPrefix());
        }
        
        // Insert new schema sections to the <types> element of the WSDL file
        for (Map.Entry<String, Set<SchemaFile>> entry : ns2schemaFilesMap.entrySet()) {
            String targetNamespace = entry.getKey();

            System.out.println("Add <schema targetNamespace='" + targetNamespace + "'>");

            Element ns = addSchemaToWsdl(types, wsdlDoc, targetNamespace);
            for (SchemaFile schemaFile : entry.getValue()) {
//                System.out.println("  - including " + schemaFile.location);
                insertSchema(builder, wsdlDoc, ns, schemaFile);
            }
        }

        organizeIncludesAndImports(wsdl);

        wsdl.normalize();
        File wsdlOut;
        if (System.getProperty("VersionDate") != null) {
            wsdlOut = new File(targetDir() + "/wsdl/" + System.getProperty("WsdlName") + "-inline_" + System.getProperty("VersionDate") + ".wsdl");
        } else {
            wsdlOut = new File(targetDir() + "/wsdl/" + System.getProperty("WsdlName") + "-inline.wsdl");
        }

        FileOutputStream fw = new FileOutputStream(wsdlOut);
        IOUtils.write(XmlUtil.node2String(wsdl, true, true), fw, "UTF-8");
        fw.close();
        System.out.println("Written new wsdl to " + wsdlOut);

        return ns2schemaFilesMap;
    }

    private static void collectGlobalPrefixes(Document wsdlDoc) {
        NamedNodeMap attributes = wsdlDoc.getDocumentElement().getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);

            if (NS_NS.equals(attr.getNamespaceURI())) { 

                String expectedPrefix = ns2Prefix.get(attr.getNodeValue());
                String prefix = attr.getLocalName();
                
                if (!"xmlns".equals(prefix)) {
                	if (expectedPrefix != null && !prefix.equals(ns2Prefix.get(attr.getNodeValue()))) {
                		System.err.println("Unexpected global prefix binding: " + prefix + "=\"" + attr.getNodeValue() + "\" (expected prefix: " + expectedPrefix);
                	}
                	globalPrefixes.add(attr.getLocalName());
                }
            }
        }
	}

	private static String getWsdlFilename() {
        if (System.getProperty("VersionDate") != null) {
            return etcDir() + "/wsdl/" + System.getProperty("WsdlName") + "_" + System.getProperty("VersionDate") + ".wsdl";
        }
        return etcDir() + "/wsdl/" + System.getProperty("WsdlName") + ".wsdl";
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
                ((Element) item).removeAttribute("schemaLocation");

                Set<String> importedNamespaces = imported.get(item.getParentNode());
                if (importedNamespaces == null) {
                    importedNamespaces = new HashSet<String>();
                    imported.put(item.getParentNode(), importedNamespaces);
                }
                if (!importedNamespaces.contains(((Element) item).getAttribute("namespace"))) {
                    item.getParentNode().insertBefore(item, item.getParentNode().getFirstChild());

                    importedNamespaces.add(((Element) item).getAttribute("namespace"));
                } else {
                    item.getParentNode().removeChild(item);
                    i--;
                }
            }

        }
    }

    private static void insertSchema(DocumentBuilder builder, Document wsdlDoc, Element ns, SchemaFile schemaFile) throws SAXException, IOException {
        if (ns == null) {
            System.err.println("Ignoring schemaFile because given schema section is null");
            return;
        }
        Element childDoc = schemaFile.domSchemaNode;

        NamedNodeMap attrNodeMap = childDoc.getAttributes();
        Node[] attributes = new Node[attrNodeMap.getLength()];
        
        for (int i = 0; i < attrNodeMap.getLength(); i++) {
        	attributes[i] = attrNodeMap.item(i);
        }
        for (Node attr: attributes) {

            if (NS_NS.equals(attr.getNamespaceURI())) { 

                String oldPrefix;
                
                String prefix = ns2Prefix.get(attr.getNodeValue());
                
                if ("xmlns".equals(attr.getLocalName())) {
                	// attr.getNodeValue() is defined as the default namespace
                	oldPrefix = null;
                	
                } else {
                    oldPrefix = attr.getLocalName();

                    if (prefix == null) {
                    	// First time we see this namespace - just accept the prefix
                    	prefix = oldPrefix;
                        prefix2NS.put(prefix, attr.getNodeValue());
                        ns2Prefix.put(attr.getNodeValue(), prefix);
                    }
                }

                // only make up a prefix if namespace hasn't previously been bound to another prefix
                if (prefix == null) {
                    prefix = makeUniquePrefix(attr.getLocalName());
                    prefix2NS.put(prefix, attr.getNodeValue());
                    ns2Prefix.put(attr.getNodeValue(), prefix);
                }
                
                if (!prefix.equals(oldPrefix)) {
//                	System.out.println("Rewrite prefix, old=" + oldPrefix + ", new=" + prefix + ", schema=" + schemaFile.location + ", ns=" + attr.getNodeValue());
                	rewritePrefix(oldPrefix, prefix, ns, attr, childDoc);
                }

                if (!globalPrefixes.contains(prefix)) {
                	ns.setAttribute("xmlns:" + prefix, attr.getNodeValue());
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
    	childDoc.removeAttribute("xmlns" + (oldPrefix != null ? ":" + oldPrefix : ""));

        replacePrefixUsages(oldPrefix, newPrefix, childDoc, attr.getNodeValue());
    }

    private static void replacePrefixUsages(String oldPrefix, String newPrefix, Element element, String namespace) {

        NodeList elements = element.getChildNodes();
        for (int i = 0; i < elements.getLength(); i++) {
            Node node = elements.item(i);

    		if (node instanceof Element) {
            	Element el = (Element) node;

            	if (namespace.equals(el.getNamespaceURI())) {
            		el.setPrefix(newPrefix);
        		}
                
            	NamedNodeMap attributes = el.getAttributes();
            	if (oldPrefix != null) {
            		for (int j = 0; j < attributes.getLength(); j++) {
            			Node attribute = attributes.item(j);
            			String name = attribute.getLocalName();
            			String value = attribute.getNodeValue();
            			if (value != null && value.contains(oldPrefix + ":")) {
            				
            				// An attribute value may refer multiple times to the same prefix (e.g., 
            				// <union memberTypes="sdsd201008:PredefinedRequestedRole sdsd201008:UndefinedRequestedRole"/>)
            				// Below, we replace prefix, but only if its at start of string or is following a space character
            				String newValue = value.replaceAll("(^| )" + oldPrefix + ":", "$1" + newPrefix + ":");
            				el.setAttribute(name, newValue);
            			}
            			
            		}
            	} else {
            		if (SCHEMA_NS.equals(element.getNamespaceURI())) {
            			// TODO: This should be generalized
            			String typeAttr = el.getAttribute("type");
                        if (typeAttr != null && typeAttr.length() > 0 && typeAttr.indexOf(':') == -1) {
                            el.setAttribute("type", newPrefix + ":" + typeAttr);
                        }
                        String baseAttr = el.getAttribute("base");
                        if (baseAttr != null && baseAttr.length() > 0 && baseAttr.indexOf(':') == -1) {
                            el.setAttribute("base", newPrefix + ":" + baseAttr);
                        }
            		}
            	}

            	replacePrefixUsages(oldPrefix, newPrefix, el, namespace);
            }
        }
	}

	private static String findDeclaredPrefix(Element top, String namespace) {
        NamedNodeMap attributes = top.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            if (attr.getNodeName().startsWith("xmlns:") && attr.getNodeValue().equals(namespace)) {
                return attr.getNodeName().substring(6);
            }
        }
        return ns2Prefix.get(namespace);
    }

	private static String declarePrefix(Element top, String namespace) {
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

    private static Element createSchemaElement(Document wsdlDoc, String targetNamespace) {
        if (targetNamespace != null && !targetNamespace.equals("")) {
            if (SCHEMA_NS.equals(targetNamespace) || XML_NS.equals(targetNamespace) || SOAP_NS.equals(targetNamespace)) {
                return null;
            }
        } else {
            return null;
        }

        Element ns = wsdlDoc.createElementNS(SCHEMA_NS, "schema");
        ns.setPrefix("xs");
        ns.setAttribute("targetNamespace", targetNamespace);
        ns.setAttribute("elementFormDefault", "qualified");
        ns.setAttribute("attributeFormDefault", "unqualified");

        return ns;
    }

    private static Element addSchemaToWsdl(Element types, Document wsdlDoc, String targetNamespace) {
        Element ns = createSchemaElement(wsdlDoc, targetNamespace);
        if (ns != null) {
            types.appendChild(ns);
        }
        return ns;
    }
}
