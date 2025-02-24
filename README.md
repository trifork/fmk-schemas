# fmk-schemas

This project holds all xml schemas used in the FMK-backend, and can build wsdl's and publish them to internal nexus. 

Please note:
From FMK 1.6.0 it has been rewritten to use maven, whereas the older versions used ant. Only the 1.6.0 wsdl and zip-files can be built using this project. If you need to build 1.4.x version, you will have to switch to a branch where the ant-files for 1.4.x are still included.

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

## Publish til trifork-internal nexus
```
mvn -f fmk-160-schema-classes deploy
mvn -f fmk-160-idws-schema-classes deploy
```
