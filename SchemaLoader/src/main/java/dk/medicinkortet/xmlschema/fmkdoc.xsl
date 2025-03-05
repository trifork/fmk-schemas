<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:str="http://exslt.org/strings">
	<xsl:output method="html" />
	<xsl:include href="common_htmldoc.xsl" />
	
	<xsl:template match="/">
		<html>
			<body>
				<head>
					<xsl:call-template name="CSS" />
					<xsl:call-template name="Javascript" />
				</head>
				
				<xsl:apply-templates
					select="wsdl:definitions/wsdl:types/xs:schema[starts-with(@targetNamespace, 'http://www.dkma.dk/medicinecard/xml.schema/')]/xs:complexType"
					mode="all" />
				<xsl:apply-templates
					select="wsdl:definitions/wsdl:types/xs:schema[starts-with(@targetNamespace, 'http://www.dkma.dk/medicinecard/xml.schema/')]/xs:simpleType" />
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>