// This script must be run from the schemas directory.
//
// It will find all schemas that reference the schema you enter.
// Optionally you can copy the referencing schemas to a new namespace
// in order to edit them for a new interface version.

baseDir = new File(".")
console = false

// Classes and functions
//

class Schema {
    String name
    String namespace
    File location
    
    String toString() {
        return "${namespace}/${name}"
    }
    
    String fileName() {
        return location.getName()
    }
    
    boolean equals(other) {
        if (other.class != this.class) return false;
        return other.name == this.name && ((this.namespace == ".+" || other.namespace == ".+") || this.namespace == other.namespace)
    }
    
    static Schema schemaFromFile(file) {
        def matcher = file =~ "/([0-9]{4}/[0-9]{2}/[0-9]{2})"
        if (!matcher) return null
        def namespace = matcher[0][1]
        
        matcher = file =~ "/(DKMA_)*([a-z_A-Z0-9-]+).xsd"
        if (!matcher) return null
        def name = matcher[0][2]
        return new Schema(name: name, namespace:namespace, location:file)
    }
}

def isSchemaFile(file) {
    name = file.getAbsolutePath()
    return (name.indexOf(".xsd") == (name.length()-4))
}

// Lists all schemas recursive
def listSchemas(dir) {
    def result = []
    dir.eachFileRecurse {
        if (isSchemaFile(it)) {
            def schema = Schema.schemaFromFile(it)
            if (schema == null) {
//                println "Schema was null for ${it}"
            } else {
//                println "Schema: ${schema}"
                result.add(schema)
            }
        }
    }
    return result
}

def listNamespaces(dir) {
    dirs = []
    dir.eachFileRecurse(groovy.io.FileType.DIRECTORIES) {
        if ( it =~ "/[0-9]{4}/[0-9]{2}/[0-9]{2}\$" && !(it =~ ".svn")) {
            dirs.add(it)
        }
    }
    return dirs.sort()
}

def innerFindSchemasReferencing(schema, schemas, result) {
//    println "Searching for references to ${schema.name}..."
    schemas.each {
        fileContent = it.getLocation().getText("UTF-8")
//        if (it.namespace == schema.namespace || schema.namespace == ".+") {
            // Look for include
            regex = "<include schemaLocation=\"[DKMA_]*${schema.name}.xsd\"/>"
            if (fileContent =~regex) {
                result.add(it)
                innerFindSchemasReferencing(it, schemas, result)
            }
//        } else {
            // Look for import
            regex = "<import namespace=\"http://www\\.dkma\\.dk/medicinecard/xml\\.schema/${schema.namespace}\" schemaLocation=\".+/[DKMA_]*${schema.name}.xsd\"/>"
            if (fileContent =~regex) {
                result.add(it)
                innerFindSchemasReferencing(it, schemas, result)
            }
//        }
    }
}

def findSchemas(schema, schemas) {
    result = []
    schemas.each {
        if (schema == it) {
            result.add(it)
        }
    }
    return result
}

// Returns list of schemas that reference given schema
def findSchemasReferencing(schema, schemas) {
    def result = findSchemas(schema, schemas)
    innerFindSchemasReferencing(schema, schemas, result)
    return result
}

// Make every schema distinct. If more schemas of the same type, select the one in the newest namespace
def distinctSchemas(schemas) {
    def result = [:]
    schemas.each {
        if (!result.containsKey(it.name)) {
            result.put(it.name, it)
        } else {
            // Keep the newest
            existingSchema = result.get(it.name)
            if (existingSchema.namespace < it.namespace) {
                result.put(it.name, it)
            }
        }
    }
    return result.values().sort();
}

// Copies the given schema to the new location specified by namespace and dir
// Existing files won't be overwritten
def copySchemaToNamespace(schema, namespace, dir) {
    def destFolder = new File(dir,namespace)
    def destFile = new File(destFolder,schema.fileName())
    if (destFile.exists()) {
        println "Schema ${schema.name} already exists in namespace ${namespace}"
    } else {
        println "Copying ${schema.location} to ${destFile}"
        destFile.write(schema.location.getText("UTF-8"), "UTF-8")
    }
}

def findNamespaceFromFileName(fileName, allSchemas) {
    result = null
    allSchemas.findAll { fileName == it.fileName() }. each {
        if (!result) {
            result = it
        } else {
            if (it.namespace > result.namespace) {
                result = it
            }
        }
    }
    return result?.namespace
}

def fixIncludesInSchema(schema, allSchemas) {
    content = schema.location.getText("UTF-8")

    print "Fixing ${schema}... "
    boolean changed = false
    
    // Look at all includes
    matcher = content =~ "<include schemaLocation=\"(.+\\.xsd)\"/>"
    matcher.each {
        refFileName = it[1]
        namespace = findNamespaceFromFileName(refFileName, allSchemas)
        if (!namespace) {
            println "Namespace for reference ${refFileName} in schema ${schema}"
            return
        }
        if (namespace != schema.namespace) {
            // include won't cut it - use import instead
//            println "Changing include to import of: ${refFileName}"
            content = content.replace(it[0], "<import namespace=\"http://www.dkma.dk/medicinecard/xml.schema/${namespace}\" schemaLocation=\"../../../${namespace}/${refFileName}\"/>")
            changed = true
        }
    }   

    // Look at all imports
    matcher = content =~ "<import namespace=\"http://www.dkma.dk/medicinecard/xml.schema/([0-9]{4}/[0-9]{2}/[0-9]{2})\" schemaLocation=\"../../../([0-9]{4}/[0-9]{2}/[0-9]{2})/(.+\\.xsd)\"/>"
    matcher.each {
        refFileName = it[3]
        refNamespace = it[1]
        namespace = findNamespaceFromFileName(refFileName, allSchemas)
        if (!namespace) {
            println "Namespace for schema not found: ${schema}"
            return
        }
        if (namespace == schema.namespace) {
            // use include instead of import
//            println "Changing import to include of: ${it[3]}"
            content = content.replace(it[0], "<include schemaLocation=\"${it[3]}\"/>")
            changed = true
        } else if (namespace != refNamespace) { // Meaning that there is actually a newer 
            println "A newer schema exists in ${refNamespace} for reference to ${refFileName} in file: ${schema.fileName()}"
        }
    }   

    // Write to file
    if (changed) {
        println "Saving to ${schema.fileName()}"
        schema.location.write(content, "UTF-8")
    } else {
        println "No changes"
    }
}

def fixIncludesInNamespace(namespace, allSchemas, indir) {
    dir = new File(indir, namespace)
    schemas = listSchemas(dir)
    schemas.each {
        fixIncludesInSchema(it, allSchemas)
    }
}

def ask(question, defaultVal) {
    if (defaultVal) {
        print "${question} (${defaultVal}): "
    } else {
        print "${question}: "
    }

    if (console) {
        return defaultVal
    }
    
    def answer = new InputStreamReader(System.in).readLine()
    if (answer == null || answer.length() == 0) return defaultVal
    return answer
}

// Script part
//

println()
println "Select what to do:"
println "\t1. Lists schemas that references schema X"
println "\t2. Copy schemas that references schema X to namespace Y"
println "\t3. Fix import/include references in namespace Y (make a copy of namespace directory before running!!!)"
println()
selection = ask("Choose function", "1")

println()

schemas = listSchemas(baseDir)

if (selection == "1") {
    schemaName = ask("Enter name of schema", "PrescriptionMedicationOverviewStructure")
    namespace = ask("From namespace", "any")
    if (namespace == "any") namespace = ".+"
    refSchemas = findSchemasReferencing(new Schema(name: schemaName, namespace: namespace), schemas)
    println()
    disSchemas = distinctSchemas(refSchemas)
    disSchemas.each { println it }
    doCopy = ask("Copy these files to namespace", "n") == "y"
    if (doCopy) {
        namespace = ask("Destination namespace", "2012/01/01")
        disSchemas = distinctSchemas(refSchemas)
        disSchemas.each {
            copySchemaToNamespace(it, namespace, baseDir)
        }
    }
}
if (selection == "2") {
    schemaName = ask("Enter name of schema", "PrescriptionMedicationOverviewStructure")
    namespace = ask("From namespace", "any")
    if (namespace == "any") namespace = ".+"
    namespace = ask("Destination namespace", "2012/01/01")
    refSchemas = findSchemasReferencing(new Schema(name: schemaName, namespace: namespace), schemas)
    distinctSchemas(refSchemas).each {
        copySchemaToNamespace(it, namespace, baseDir)
    }
}
if (selection == "3") {
    namespace = ask("Namespace", "2012/01/01")
    fixIncludesInNamespace(namespace, schemas, baseDir)
}



null
