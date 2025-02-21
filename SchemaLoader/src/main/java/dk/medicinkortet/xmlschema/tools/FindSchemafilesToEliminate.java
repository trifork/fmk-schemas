package dk.medicinkortet.xmlschema.tools;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.trifork.xmlquery.XMLObject;
import com.trifork.xmlquery.XMLObjectCreator;

/**
 * 
 * This tool can be used to remove all schemafiles relating to one or more services from a wsdl file.
 * The wsdl file is examined, and for each operation the corresponding request/response schemas are traversed
 * recursively to determine the total set of schemas which are referenced by these services.
 * Finally the schema directory itself is traversed to ensure that no schema file is deleted which is referenced
 * from an unrelated schema.
 * 
 * Note: There is currently no direct way to run this (no ant task or otherwise). Suggest you run this interactively in your IDE.
 *
 * Note: Error handling is sketchy at best.
 * 
 * @author mbe
 *
 */
public class FindSchemafilesToEliminate {

	// Which wsdl file are we looking for services in - these are the services we want to remove all schema files for.
	private static final String WSDL_FILE_WITH_SERVICES_TO_ELIMINATE = "etc/wsdl/EffectuationOrdering_2015_01_01.wsdl";
	
	// Where are the schemafiles located (that we want to remove). Note that in this case these were in a different namespace directory from the wsdl file.
	private static final String SCHEMADIRECTORY = "etc/schemas/2015/06/01/";
	
	// Perform the actual delete of schema files, or just output a list of files to delete?
	private final boolean PERFORM_DELETE = false;
	
	public static void main(String[] args) throws Exception {
		System.out.println("There is currently no direct way to run this (no ant task or otherwise). Suggest you run this interactively in your IDE.");
		// You could simply do this: new FindSchemafilesToEliminate().findSchemaFiles();
	}
	
	public void findSchemaFiles() throws Exception {
		
		File wsdlFile = new File(WSDL_FILE_WITH_SERVICES_TO_ELIMINATE);
		assert wsdlFile.exists();
		
        XMLObject wsdlXMLObject = XMLObjectCreator.getXMLObjectFromFile(WSDL_FILE_WITH_SERVICES_TO_ELIMINATE);
        
        List<XMLObject> operations = wsdlXMLObject.getObjects("binding/operation");
        
        Set<String> candidateSchemasToDelete = new HashSet<>();
        operations.forEach(op -> candidateSchemasToDelete.add(op.getString("input@name").replace("_2015_01_01", ".xsd")));
        operations.forEach(op -> candidateSchemasToDelete.add(op.getString("output@name").replace("_2015_01_01", ".xsd")));
        System.out.println(candidateSchemasToDelete);
        
        Set<String> schemaRefs = new HashSet<String>();
        candidateSchemasToDelete.forEach(candidateSchemaToDelete -> {
        	try {
				XMLObject fileToDeleteXMLObject = XMLObjectCreator.getXMLObjectFromFile(SCHEMADIRECTORY + candidateSchemaToDelete);
				fileToDeleteXMLObject.getObjects("include").forEach(sl -> {
					try {
						String ref = sl.getString("@schemaLocation");
						schemaRefs.add(ref);
						schemaRefs.addAll(collectRefs(ref));
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
        });
        
        candidateSchemasToDelete.addAll(schemaRefs);
        System.out.println(candidateSchemasToDelete);
        
        // For each of the candidates, make sure they are not referenced in any other schema file that is not also a candidate
        File baseDir = new File("/home/michael/Trifork/Projects/fmk-schemas/etc/schemas/2015/06/01");
        Arrays.stream(baseDir.listFiles())
        	.filter(f -> !candidateSchemasToDelete.contains(f.getName()))
        	.forEach(schemaFile -> {
	    		try {
					XMLObject schemaXMLObject = XMLObjectCreator.getXMLObjectFromFile(schemaFile.getAbsolutePath());
					Set<String> includedSchemas = schemaXMLObject.getObjects("include").stream().map(x -> x.getString("@schemaLocation")).collect(Collectors.toSet());
					for (String s: includedSchemas) {
						if (candidateSchemasToDelete.contains(s)) {
							System.out.println("Will not delete " + s + " since it is referenced from " + schemaFile);
							// Everything that schemaFile references (and recursively down from there) is no longer a candidate
							candidateSchemasToDelete.remove(s);
							candidateSchemasToDelete.removeAll(collectRefs(s));
						}
					}
				} catch (Exception e) {
				}
        	});

        candidateSchemasToDelete.forEach(cs -> {
        	File schemaFileToDelete = new File(SCHEMADIRECTORY + cs);
        	System.err.println("Going to delete " + schemaFileToDelete);
        	if (PERFORM_DELETE) {
        		schemaFileToDelete.delete();
        	}
        });
	}
	
	// Recursively collect referenced schemas from a single starting point
	public Set<String> collectRefs(String schemaName) throws Exception {
		Set<String> refs = new HashSet<String>();

		XMLObject fileToDeleteXMLObject = XMLObjectCreator.getXMLObjectFromFile(SCHEMADIRECTORY + schemaName);
		fileToDeleteXMLObject.getObjects("include").forEach(sl -> {
			String refSchema = sl.getString("@schemaLocation");
			refs.add(refSchema);
			try {
				refs.addAll(collectRefs(refSchema));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		return refs;
	}
}
