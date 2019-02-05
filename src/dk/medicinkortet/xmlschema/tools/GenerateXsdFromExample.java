package dk.medicinkortet.xmlschema.tools;

import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.util.*;

public class GenerateXsdFromExample {
	public static void main (String[] args) throws Exception
	{
		if (args.length != 2) {
			System.out.println("Wrong input. Should be:");
			System.out.println("<inputFile> <outputFile>");
			return;
		}
		
		File inputFile = new File(args[0]); 
		File outputFile = new File(args[1]); 
		File namespaceFolder = outputFile.getParentFile();

		FilenameFilter importFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.startsWith("importsFrom");
			}
		};

		Comparator<File> sortByName = new Comparator<File>() {
			public int compare(File a, File b) {
				return b.getName().compareTo(a.getName());
			}
		};

		File[] importFiles = namespaceFolder.listFiles(importFilter);
		Arrays.sort(importFiles, sortByName);

		Map<String, String> imports = extractImports(importFiles);

		String namespace = importFiles[0].getName().substring(11, importFiles[0].getName().indexOf("."));

		Set<String> usedImports = new HashSet();

		StringBuilder result = new StringBuilder();

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(inputFile);
		doc.getDocumentElement().normalize();

		Node root = doc.getFirstChild();
		while (root != null) {
			String rootName = root.getNodeName();

			result.append("\t<element name=\"" + rootName + "\" type=\"medicinecard" + namespace + ":" + rootName + "Type\">\n")
				.append("\t\t<annotation>\n")
				.append("\t\t\t<documentation xml:lang=\"en-GB\"> </documentation>\n")
				.append("\t\t\t<documentation xml:lang=\"da-DK\"> </documentation>\n")
				.append("\t\t</annotation>\n")
				.append("\t</element>\n\n");

			result.append("\t<complexType name=\"" + rootName + "Type\">\n")
				.append("\t\t<sequence>\n");

			Node node = root.getFirstChild();
			while (node != null) {
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					String nodeName = node.getNodeName();
					String nodeNamespace = getNamespaceForType(nodeName, imports, usedImports);
					result.append("\t\t\t<element name=\"" + nodeName + "\" type=\"medicinecard" + nodeNamespace + ":" + nodeName + "Type\"/>\n");
				}
				node = node.getNextSibling();
			}

			result.append("\t\t</sequence>\n")
				.append("\t</complexType>\n");

			root = root.getNextSibling();
		}
		result.append("</schema>");

		result.insert(0, "\t<include schemaLocation=\"importsFrom" + namespace + ".xsd\"/>\n\n");

		for (String s : usedImports) {
			if (s == null || s.equals(namespace)) {
				continue;
			}
			result.insert(0, "\t<import schemaLocation=\"importsFrom" + s + ".xsd\" namespace=\"" + namespaceToURL(s) + "\"/>\n");
		}

		result.insert(0, "\t\tattributeFormDefault=\"unqualified\">\n\n");
		result.insert(0, "\t\telementFormDefault=\"qualified\"\n");
		result.insert(0, "\t\ttargetNamespace=\"" + namespaceToURL(namespace) + "\"\n");
		
		for (String s : usedImports) {
			if (s == null || s.equals(namespace)) {
				continue;
			}
			result.insert(0, "\t\txmlns:medicinecard" + s + "=\"" + namespaceToURL(s) + "\"\n");
		}
		result.insert(0, "\t\txmlns:medicinecard" + namespace + "=\"" + namespaceToURL(namespace) + "\"\n");

		result.insert(0, "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\"\n");
		result.insert(0, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

		FileWriter writer = new FileWriter(outputFile);

		writer.write(result.toString());
		writer.close();

		System.out.println("HUSK:");
		System.out.println("\tTjek at xml blev formet korrekt");
		System.out.println("\tTilføj til importsFrom fil");
		System.out.println("\tRet i WSDL filer");
	}

	public static Map<String, String> extractImports(File[] importFiles) throws Exception {
		Map<String, String> imports = new HashMap();
		for (File f : importFiles) {
			BufferedReader r = new BufferedReader(new FileReader(f));
			String namespace = f.getName().substring(11, f.getName().indexOf("."));
			String line;
			while ((line = r.readLine()) != null) {
				if (line.contains("schemaLocation")) {
					String name = line.substring(0, line.lastIndexOf("."));
					name = name.substring(Math.max(name.lastIndexOf("/") + 1, name.lastIndexOf("\"")));
					if (!imports.containsKey(name)) {
						imports.put(name, namespace);
					}
				}
			}
		}

		return imports;
	}

	public static String getNamespaceForType(String type, Map<String, String> imports, Set<String> usedImports) {
		String result = imports.get(type);
		usedImports.add(result);
		return result;
	}
	
	public static String namespaceToURL(String namespace) {
		return "http://www.dkma.dk/medicinecard/xml.schema/" + namespace.substring(0, 4) + "/" + namespace.substring(4, 6) + "/" + namespace.substring(6, 8) + (namespace.length() > 8 ? "/" + namespace.substring(8) : "");
	}

}

