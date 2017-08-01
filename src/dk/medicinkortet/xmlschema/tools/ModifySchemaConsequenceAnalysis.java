package dk.medicinkortet.xmlschema.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import dk.sosi.seal.model.SystemInfo;

/**
 * This tool accepts a list of schema files (*.xsd) that you for one reason or another want to modify, and
 * tries to identify which other schema files must also be modified as a consequence. For example, if you
 * modify CreateDrugMedication.xsd, then UpdateMedicineCardRequest.xsd also requires modification because it
 * references that element.
 * 
 * Note: There is currently no direct way to run this (no ant task or otherwise). Suggest you run this interactively in your IDE.
 * Note: Sketchy error handling, expect crashes.
 * 
 * @author mbe
 *
 */
public class ModifySchemaConsequenceAnalysis {

	// This constant will print out little checkmarks next to the actionable items,
	// which allows you to use the output as a sort of todo list in a text editor. 
	private static final String CHECKBOX = "[ ] ";

	// Which schemafiles are we looking for references to
	private static final String[] SCHEMAFILES_TO_MODIFY = new String[] {
			"OrganisationIdentifier.xsd",
	};
	
	// Where are the schemafiles located (that we want to analyse).
	private static final File SCHEMADIRECTORY = new File("etc/schemas/2015/06/01/");
	private static final File EXTENSIONDIRECTORY = new File("etc/schemas/2015/06/01/E2");
	
	private Map<String, Set<String>> matchRefs = new HashMap<>();
	
	public static void main(String[] args) throws Exception {
		
		// System.out.println("There is currently no direct way to run this (no ant task or otherwise). Suggest you run this interactively in your IDE.");
		// You could simply do this:
		new ModifySchemaConsequenceAnalysis().findSchemaFiles();
	}
	
	private static final Pattern TARGETNAMESPACEPATTERN = Pattern.compile(".*targetNamespace=\"([^\"]*)\".*", Pattern.MULTILINE | Pattern.DOTALL);
	
	public String getLocalNameForTargetNamespace(String fileName, String s) {
		Matcher matcher = TARGETNAMESPACEPATTERN.matcher(s);
		if (matcher.matches()) {
			String targetNamespace = matcher.group(1);
			Pattern targetNamespaceLocalNamePattern = Pattern.compile(".*xmlns:([^:]*)=\""+targetNamespace+"\".*", Pattern.MULTILINE | Pattern.DOTALL);
			Matcher matcher2 = targetNamespaceLocalNamePattern.matcher(s);
			if (matcher2.matches()) {
				return matcher2.group(1);
			}
		}

		return "NO_TARGETNAMESPACE";
	}
	
	public void findSchemaFiles() throws Exception {
		
		Set<String> matches = Arrays.asList(SCHEMAFILES_TO_MODIFY).stream().collect(Collectors.toSet());

		boolean keepOn = true;
		while (keepOn) {
			keepOn = scanDirectory(matches, SCHEMADIRECTORY);
		}
	
//		matches.stream().sorted().distinct().forEach(file -> {
//			if (!new File(EXTENSIONDIRECTORY, file).exists()) {
//				System.out.print("[NEW] ");
//			} else {
//				String content = getFileContents(file);
//				if (matchRefs.get(file).stream().filter(fileName -> !content.contains(inThisNamespace(content, fileName))).findAny().isPresent()) {
//					System.out.print("      ");
//				} else {
//					System.out.print("[OK]  ");
//				}
//			}
//			System.out.print(file);
//			if (matchRefs.get(file) != null) {
//				System.out.print(" - references: " + matchRefs.get(file).stream().collect(Collectors.joining(", ")));	
//			}
//			System.out.println();
//		});
		
		System.out.println("\nFiles that must be introduced to the extension");
		System.out.println("----------------------------------------------");
		matches.stream().sorted().distinct().forEach(file -> {
			if (!new File(EXTENSIONDIRECTORY, file).exists()) {
				System.out.print(CHECKBOX + file);
				if (matchRefs.get(file) != null) {
					System.out.print(" - references: " + matchRefs.get(file).stream().collect(Collectors.joining(", ")));	
				}
				System.out.println();
			}
		});
		
		System.out.println("\nFiles that already exist in the extension and need additional changes");
		System.out.println("-----------------------------------------------------------------------");
		matches.stream().sorted().distinct().forEach(file -> {
			if (!new File(EXTENSIONDIRECTORY, file).exists()) {
			} else {
				String content = getFileContents(file);
				if (matchRefs.get(file).stream().filter(fileName -> !content.contains(inThisNamespace(content, fileName))).findAny().isPresent()) {
					System.out.print(CHECKBOX + file);
					if (matchRefs.get(file) != null) {
						System.out.print(" - references: " + matchRefs.get(file).stream().collect(Collectors.joining(", ")));	
					}
					System.out.println();
				}
			}
		});

		System.out.println("\nFiles that are affected by the modification but need no additional changes");
		System.out.println("--------------------------------------------------------------------------");
		matches.stream().sorted().distinct().forEach(file -> {
			if (!new File(EXTENSIONDIRECTORY, file).exists()) {
			} else {
				String content = getFileContents(file);
				if (matchRefs.get(file).stream().filter(fileName -> !content.contains(inThisNamespace(content, fileName))).findAny().isPresent()) {
				} else {
					System.out.print(file);
					if (matchRefs.get(file) != null) {
						System.out.print(" - already references: " + matchRefs.get(file).stream().collect(Collectors.joining(", ")));	
					}
					System.out.println();
				}
			}
		});

		System.out.println("\nRequest/response schemas that requires modification of the extension WSDL file");
		System.out.println("--------------------------------------------------------------------------------");
		matches.stream().sorted().distinct().forEach(file -> {
			if ((file.endsWith("Request.xsd") || file.endsWith("Response.xsd")) && !new File(EXTENSIONDIRECTORY, file).exists()) {
				System.out.print(CHECKBOX + file);
				if (matchRefs.get(file) != null) {
					System.out.print(" - references: " + matchRefs.get(file).stream().collect(Collectors.joining(", ")));	
				}
				System.out.println();
			}
		});
	}

	private String getFileContents(String file) {
		try {
			return new String(Files.readAllBytes(Paths.get(new File(EXTENSIONDIRECTORY, file).toURI())));
		} catch (IOException e) {
			return "";
		}
	}

	private boolean scanDirectory(Set<String> matches, File dir) throws IOException {
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				if (scanDirectory(matches, f)) {
					return true;
				}
			} if (f.getName().endsWith(".xsd")) {
				String content = new String(Files.readAllBytes(Paths.get(f.toURI())));
				if (!matches.contains(f.getName()) && matches.stream().filter(fileName -> content.contains(whatToScanFor(fileName))).findAny().isPresent()) {
					matchRefs.put(f.getName(), matches.stream().filter(fileName -> content.contains(whatToScanFor(fileName))).collect(Collectors.toSet()));
					// Output som raw progress info which is useful for debugging purposes.
					System.err.println(f + " references the following:\n\t\t" + matches.stream().filter(fileName -> content.contains(whatToScanFor(fileName))).collect(Collectors.joining("\n\t\t")));
					matches.add(f.getName());
					return true;
				}
			}
		}
		return false;
	}

	private String inThisNamespace(String content, String fileName) {
		return getLocalNameForTargetNamespace(fileName, content) + whatToScanFor(fileName);
	}

	private String whatToScanFor(String fileName) {
		return ":"+fileName.substring(0, fileName.lastIndexOf('.')) + "Type\"";
	}
}
