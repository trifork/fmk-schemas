<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:str="http://exslt.org/strings">
	<xsl:output method="html" />
	<xsl:variable name="fmkns" select="'http://www.dkma.dk/medicinecard/xml.schema/2015/06/01'"/>
	<xsl:include href="common_htmldoc.xsl" />

	<xsl:template match="/">

		<html>
			<body>
				<head>
					<xsl:call-template name="CSS" />
					<xsl:call-template name="Javascript" />
				</head>
				<xsl:apply-templates
					select="wsdl:definitions/wsdl:types/xs:schema[@targetNamespace=$fmkns]/xs:complexType[@name='MedicineCardType']"
					mode="nsf" />
				<xsl:apply-templates
					select="wsdl:definitions/wsdl:types/xs:schema[@targetNamespace=$fmkns]/xs:complexType[@name!='MedicineCardType' and @name != 'CreateDrugMedicationType' and @name != 'UpdateDrugMedicationType' and @name != 'DrugMedicationHistoryPeriodType']"
					mode="nsf" />
				<xsl:apply-templates
					select="wsdl:definitions/wsdl:types/xs:schema[@targetNamespace=$fmkns]/xs:simpleType"
					mode="nsf" />
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>