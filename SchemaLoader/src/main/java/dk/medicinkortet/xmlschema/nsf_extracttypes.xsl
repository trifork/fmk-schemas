<?xml version="1.0" encoding="UTF-8"?>
<!--  This XSLT will traverse the type graph and extract all types needed for the NSF documentation, that is, all types related to MedicineCardType, including recursive subtypes.
	It will cause a lot of duplicates, which will be removed in another xslt 
 -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:str="http://exslt.org/strings">
 <xsl:output indent="yes" method="xml"/>
 <xsl:variable name="nsfFilter" select="'nsf=&quot;ignore&quot;'"></xsl:variable>

<xsl:template match="xs:sequence">
	<xsl:for-each select="node()">
		<xsl:if test="name() and not(name()='filter') and not (contains(preceding-sibling::node()[not(self::text())][1], $nsfFilter))">
			<xsl:apply-templates select="current()"/> 
			<xsl:text>&#xA;</xsl:text>
		</xsl:if>
	</xsl:for-each>
</xsl:template>


<xsl:template match="xs:choice">
	<xsl:if test="not(contains(preceding-sibling::node()[not(self::text())][1], $nsfFilter))">	
		<xsl:for-each select="node()">
			<xsl:if test="name() and not(name()='filter') and not (contains(preceding-sibling::node()[not(self::text())][1], $nsfFilter))">
				<xsl:apply-templates select="current()"/>
				<xsl:text>&#xA;</xsl:text>
			</xsl:if>
		</xsl:for-each>
	</xsl:if>
</xsl:template>



<xsl:template match="xs:element">
	<xsl:if test="not(contains(preceding-sibling::node()[not(self::text())][1], $nsfFilter))">
		<xsl:apply-templates select="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:complexType[@name=substring-after(current()/@type, 'medicinecard20150601:')]"/>
		<xsl:apply-templates select="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:simpleType[@name=substring-after(current()/@type, 'medicinecard20150601:')]"/>
	</xsl:if>
</xsl:template>


<xsl:template match="xs:complexType">

	<xsl:apply-templates select="child::xs:sequence | child::xs:simpleContent | child::xs:choice"/>
	<xsl:copy-of select="preceding::xs:element[1]"/>
	<xsl:text>&#xA;</xsl:text>
 	<xsl:copy-of select="current()"/>
	<xsl:text>&#xA;</xsl:text>
</xsl:template>


<xsl:template match="xs:simpleContent">

	<xsl:apply-templates select="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:complexType[@name=substring-after(current()/xs:extension/@base, 'medicinecard20150601:')]"/>
	<xsl:apply-templates select="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:simpleType[@name=substring-after(current()/xs:extension/@base, 'medicinecard20150601:')]"/>

	<xsl:apply-templates select="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:complexType[@name=substring-after(current()/xs:extension/xs:attribute/@type, 'medicinecard20150601:')]"/>
	<xsl:apply-templates select="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:simpleType[@name=substring-after(current()/xs:extension/xs:attribute/@type, 'medicinecard20150601:')]"/>
	
</xsl:template>

<xsl:template match="xs:simpleType">

	<xsl:if test="xs:union">
	
		<xsl:for-each select="str:tokenize(xs:union/@memberTypes, ' ')">
			<xsl:apply-templates select="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:complexType[@name=substring-after(current(), 'medicinecard20150601:')]"/>
			<xsl:apply-templates select="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:simpleType[@name=substring-after(current(), 'medicinecard20150601:')]"/>
		</xsl:for-each>
	</xsl:if>
	<xsl:if test="xs:restriction">
		<xsl:if test="starts-with(xs:restriction/@base, 'medicinecard20150601:')">
			<xsl:apply-templates select="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:complexType[@name=substring-after(current()/xs:restriction/@base, 'medicinecard20150601:')]"/>
			<xsl:apply-templates select="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:simpleType[@name=substring-after(current()/xs:restriction/@base, 'medicinecard20150601:')]"/>
		</xsl:if>
		<xsl:if test="not(starts-with(xs:restriction/@base, 'medicinecard20150601:'))">
			<xsl:apply-templates select="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:complexType[@name=current()/xs:restriction/@base]"/>
			<xsl:apply-templates select="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:simpleType[@name=current()/xs:restriction/@base]"/>
		</xsl:if>
	</xsl:if>
	
	<xsl:copy-of select="preceding::xs:element[1]"/>
	<xsl:text>&#xA;</xsl:text>
 	<xsl:copy-of select="current()"/>
 	<xsl:text>&#xA;</xsl:text>
</xsl:template>

<xsl:template match="/">

	<wsdl:definitions xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" xmlns="http://schemas.xmlsoap.org/wsdl/" xmlns:medcom="http://www.medcom.dk/dgws/2006/04/dgws-1.0.xsd" xmlns:medicinecard20150601="http://www.dkma.dk/medicinecard/xml.schema/2015/06/01" xmlns:sdsd201206="http://www.sdsd.dk/dgws/2012/06" xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/" xmlns:tns="http://www.dkma.dk/medicinecard/xml.schema/2015/06/01" xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" xmlns:xs="http://www.w3.org/2001/XMLSchema" targetNamespace="http://www.dkma.dk/medicinecard/xml.schema/2015/06/01">
    <wsdl:types>
    <xs:schema xmlns:cpr="http://rep.oio.dk/cpr.dk/xml/schemas/core/2005/03/18/" xmlns:ean="http://rep.oio.dk/ean/xml/schemas/2005/01/10/" xmlns:xkom="http://rep.oio.dk/xkom.dk/xml/schemas/2006/01/06/" attributeFormDefault="unqualified" elementFormDefault="qualified" targetNamespace="http://www.dkma.dk/medicinecard/xml.schema/2015/06/01" xmlns:xs="http://www.w3.org/2001/XMLSchema">
     	<xsl:text>&#xA;</xsl:text>
		<xsl:apply-templates select="wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:complexType[@name='MedicineCardType']"/>
	</xs:schema>
	</wsdl:types>
	</wsdl:definitions>

</xsl:template>
</xsl:stylesheet>