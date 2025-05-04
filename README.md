# fmk-schemas

This project holds all xml schemas used in the FMK-backend, and can build wsdl's and publish them to internal nexus. 

Please note:
From FMK 1.6.0 it has been rewritten to use maven, whereas the older versions uses ant. 
Use mvn to build 1.6.0 wsdl and zip-files.
Use ant to build all 1.4.x wsdl and zip-files. See bottom of README.md for more information.

## Project structure
The project is made up for several maven modules. They are executed in the following order:

* SchemaLoader: builds a java application, that reads all .xsd-files for a certain version of the interface, and builds an inline wsdl as well as a wsdl together with a collection of all the required xsd schema files. 
* mc160_wsdl: call SchemaLoader to generate wsdl for 1.6.0 DGWS
* mc160idws_wsdl: call SchemaLoader to generate wsdl for 1.6.0 IDWS
* fmk-160-schema-classes: call axis to generate java classes from the inline wsdl
* fmk-160-idws-schema-classes: call axis to generate java classes from the inline wsdl for idws

During the package phase of the maven build, the inline'd and the collection version of the wsdl will be zip'ped into redistributable files.
* 
## How to build MedicineCard_2022_01_01 wsdl files and zip files for upload:

```
mvn clean package
```
In the mc160_wsdl/target folder MedicineCard_2022_01_01-collection.zip and MedicineCard_2022_01_01-inline.zip will be built.
In the mc160idws_wsdl/target folder, MedicineCard_2022_01_01_Idws-collection.zip and MedicineCard_2022_01_01_Idws-inline.zip can be found.

## How to build wsdl's and generate client stub code fmk-160-(idws-)-schema-classes jar for use by clients:

```
mvn clean install
```



# Building 1.4.x wsdl and zip-files

Build with [ant](https://ant.apache.org/bindownload.cgi).

## Ant targets

```
all
all-standard
all-idws
all-idws-xua
publish
```

## Publish wsdl- and schema-files

Temporary (?) solution, until jenkins job FMK_schema_upload is fixed:

- Build https://ci.fmk.netic.dk/job/FMK_schema/
- Open link to artefacts: https://ci.fmk.netic.dk/job/FMK_schema/lastSuccessfulBuild/artifact/
- Navigate to target/wsdl
- Download for example OrdinationsAI_2022_03_01-collection-dist.zip and OrdinationsAI-inline_2022_03_01.zip to local pc
- Rename OrdinationsAI-inline_2022_03_01.zip to OrdinationsAI-inline_2022_03_01.wsdl.zip
- Upload the two files to https://github.com/trifork/FMKResources/tree/master/wsdl
