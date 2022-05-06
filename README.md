# fmk-schemas

This project holds all schemas used in the FMK-backend

## Dependency

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
- Download for example DosageAI_2022_03_01-collection-dist.zip and DosageAI-inline_2022_03_01.zip to local pc
- Rename DosageAI-inline_2022_03_01.zip to DosageAI-inline_2022_03_01.wsdl.zip
- Upload the two files to https://github.com/trifork/FMKResources/tree/master/wsdl