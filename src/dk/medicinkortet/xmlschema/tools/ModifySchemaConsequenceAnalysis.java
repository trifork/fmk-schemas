package dk.medicinkortet.xmlschema.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

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

	// Which schemafiles are we looking for references to
	private static final String[] SCHEMAFILES_TO_MODIFY = new String[] {
			"DosageQuantityUnitTexts.xsd",
			"DosageQuantityUnitText.xsd",
	};
	
	// Where are the schemafiles located (that we want to analyse).
	private static final File SCHEMADIRECTORY = new File("etc/schemas/2015/01/01/");
	private static final File EXTENSIONDIRECTORY = new File("etc/schemas/2015/01/01/E2");
	
	public static void main(String[] args) throws Exception {
		// System.out.println("There is currently no direct way to run this (no ant task or otherwise). Suggest you run this interactively in your IDE.");
		// You could simply do this:
		new ModifySchemaConsequenceAnalysis().findSchemaFiles();
	}
	
	public void findSchemaFiles() throws Exception {
		
		Set<String> matches = Arrays.asList(SCHEMAFILES_TO_MODIFY).stream().collect(Collectors.toSet());

		boolean keepOn = true;
		while (keepOn) {
			keepOn = scanDirectory(matches, SCHEMADIRECTORY);
		}
	
		matches.stream().sorted().distinct().forEach(file -> {
			System.out.print(file);
			if (!new File(EXTENSIONDIRECTORY, file).exists()) {
				System.out.print(" [NEW]");
			}
			System.out.println();
		});
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
					System.err.println(f + " references the following:\n\t\t" + matches.stream().filter(fileName -> content.contains(whatToScanFor(fileName))).collect(Collectors.joining("\n\t\t")));
					matches.add(f.getName());
					return true;
				}
			}
		}
		return false;
	}

	private String whatToScanFor(String fileName) {
		return ":"+fileName.substring(0, fileName.lastIndexOf('.')) + "Type\"";
	}
}
