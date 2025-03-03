<?xml version="1.0" encoding="UTF-8"?>
<!--  This XSLT will remove duplicate elements and simpleTypes/complexTypes from a wsdl -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:str="http://exslt.org/strings">

  <xsl:output indent="yes" method="xml"/>
  <xsl:key name="complextypes-by-name" match="xs:complexType | xs:simpleType" use="@name"/>

  <xsl:template match="/">
  	<wsdl:definitions targetNamespace="http://www.dkma.dk/medicinecard/xml.schema/2015/06/01" xmlns:sdsd201206="http://www.sdsd.dk/dgws/2012/06" xmlns:medicinecard20150601="http://www.dkma.dk/medicinecard/xml.schema/2015/06/01" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/" xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" xmlns:str="http://exslt.org/strings" xmlns:medcom="http://www.medcom.dk/dgws/2006/04/dgws-1.0.xsd" xmlns:tns="http://www.dkma.dk/medicinecard/xml.schema/2015/06/01" xmlns="http://schemas.xmlsoap.org/wsdl/">
		<wsdl:types>
		<xs:schema attributeFormDefault="unqualified" elementFormDefault="qualified" targetNamespace="http://www.dkma.dk/medicinecard/xml.schema/2015/06/01" xmlns:xkom="http://rep.oio.dk/xkom.dk/xml/schemas/2006/01/06/" xmlns:ean="http://rep.oio.dk/ean/xml/schemas/2005/01/10/" xmlns:cpr="http://rep.oio.dk/cpr.dk/xml/schemas/core/2005/03/18/">
    		<xsl:apply-templates select="/wsdl:definitions/wsdl:types/xs:schema/xs:complexType | /wsdl:definitions/wsdl:types/xs:schema/xs:simpleType"/>
    	</xs:schema>
		</wsdl:types>
	</wsdl:definitions>
  </xsl:template>
  
   <xsl:template match="xs:complexType | xs:simpleType">
    <xsl:if test="generate-id() = generate-id(key('complextypes-by-name', @name))">
    	<xsl:copy-of select="preceding::xs:element[1]"/>
	<xsl:text>&#xA;</xsl:text>
 	<xsl:copy-of select="."/>
 	<xsl:text>&#xA;</xsl:text>
      <xsl:text>
</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="text()">
    <xsl:apply-templates/>
  </xsl:template>
</xsl:stylesheet>