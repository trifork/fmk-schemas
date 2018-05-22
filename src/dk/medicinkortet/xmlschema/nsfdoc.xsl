<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:str="http://exslt.org/strings">
 <xsl:output method="html" cdata-section-elements="cdata"/>
 <xsl:variable name="nsfFilter" select="'nsf=&quot;ignore&quot;'"></xsl:variable>

  
<xsl:template match="xs:sequence">
	<ul>
		Sekvens af:
		<xsl:for-each select="node()">
			<xsl:if test="name() and not(name()='filter') and not (contains(preceding-sibling::node()[not(self::text())][1], $nsfFilter))">
				<li>
					<xsl:apply-templates select="current()"/>
				</li>
			</xsl:if>
		</xsl:for-each>
	</ul>
</xsl:template>


<xsl:template match="xs:choice">
	<xsl:if test="not(contains(preceding-sibling::node()[not(self::text())][1], $nsfFilter))">	
		<ul>Et af følgende elementer:
		<xsl:for-each select="node()">
			<xsl:if test="not(name()='')">
				<xsl:if test="position() > 2">
					ELLER
				</xsl:if>
				<li>
					<xsl:apply-templates select="current()"/>
				</li>
			</xsl:if>
		</xsl:for-each>
		</ul>
	</xsl:if>
</xsl:template>

<xsl:template match="xs:element">
	<xsl:if test="not(contains(preceding-sibling::node()[not(self::text())][1], $nsfFilter))">	
	 	<dl> 
 		<xsl:choose>
 			
 			<!-- Hvis der er en annotation på selve elementet, anvendes denne -->
	 		<xsl:when test="xs:annotation/xs:documentation[@xml:lang='da-DK'] != ''">
	 			<dt>Navn:</dt><dd><xsl:value-of select="@name"/></dd>
	 			<dt>Beskrivelse:</dt><dd><xsl:value-of select="xs:annotation/xs:documentation[@xml:lang='da-DK']"/></dd>
	 			<dt>Type:</dt><dd><a href="#{substring-after(@type, 'medicinecard20150601:')}"><xsl:value-of select="substring-after(@type, 'medicinecard20150601:')"/></a></dd>
	 		</xsl:when>
	 		
	 		<!-- Hvis det er en complexType med en annotation på, da anvendes denne annotation -->
	 		<xsl:when test="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:complexType[@name=concat(current()/@name, 'Type')]/xs:annotation/xs:documentation[@xml:lang='da-DK'] != ''">
	 			<dt>Navn:</dt><dd><xsl:value-of select="@name"/></dd>
	 			<dt>Beskrivelse:</dt><dd><xsl:value-of select="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:complexType[@name=concat(current()/@name, 'Type')]/xs:annotation/xs:documentation[@xml:lang='da-DK']"/></dd>
				<dt>Type:</dt><dd><a href="#{substring-after(@type, 'medicinecard20150601:')}"><xsl:value-of select="substring-after(@type, 'medicinecard20150601:')"/></a></dd>
	 		</xsl:when>
	 		
	 		<!-- Hvis det er en simpleType med en annotation på, da anvendes denne annotation -->
	 		<xsl:when test="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:simpleType[@name=concat(current()/@name, 'Type')]/xs:annotation/xs:documentation[@xml:lang='da-DK'] != ''">
	 			<dt>Navn:</dt><dd><xsl:value-of select="@name"/></dd>
	 			<dt>Beskrivelse:</dt><dd><xsl:value-of select="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:simpleType[@name=concat(current()/@name, 'Type')]/xs:annotation/xs:documentation[@xml:lang='da-DK']"/></dd>
		 		<dt>Type:</dt><dd><xsl:value-of select="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:simpleType[@name=concat(current()/@name, 'Type')]/xs:restriction/@base"/></dd>
	 		</xsl:when>
	 		
	 		<!-- Ellers: anvend en evt. annotation fra et type-element -->
	 		<xsl:otherwise>
	 			<dt>Navn:</dt><dd><xsl:value-of select="@name"/></dd>
	 			<dt>Beskrivelse:</dt><dd><xsl:value-of select="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:element[@type=current()/@type]/xs:annotation/xs:documentation[@xml:lang='da-DK']"/></dd>
		 		<dt>Type:</dt><dd><a href="#{substring-after(@type, 'medicinecard20150601:')}"><xsl:value-of select="substring-after(@type, 'medicinecard20150601:')"/></a></dd>
	 		</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="not(@minOccurs) and not(@maxOccurs)">
				<dt>Antal</dt><dd>Præcis én</dd>
			</xsl:when>
			<xsl:when test="@minOccurs=0 and not(@maxOccurs)">
				<dt>Antal</dt><dd>0 eller 1</dd>
			</xsl:when>
			<xsl:when test="@minOccurs=0 and @maxOccurs='unbounded'">
				<dt>Antal</dt><dd>0, 1 eller flere</dd>
			</xsl:when>
			<xsl:when test="(@minOccurs='1' or not(@minOccurs)) and @maxOccurs='unbounded'">
				<dt>Antal</dt><dd>1 eller flere</dd>
			</xsl:when>
		<xsl:otherwise>
		
			<dt>Min. antal:</dt><dd> 
			<xsl:choose>
				<xsl:when test="not(@minOccurs)">
					1
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@minOccurs"/>
				</xsl:otherwise>
			</xsl:choose>
		</dd>
		<dt>Max antal:</dt><dd>
			<xsl:choose>
				<xsl:when test="not(@maxOccurs)">
					1
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@maxOccurs"/>
				</xsl:otherwise>
			</xsl:choose>
		</dd>
		</xsl:otherwise>
		</xsl:choose>	
		</dl>
	</xsl:if>
</xsl:template>


<xsl:template match="xs:complexType"> 
	<!-- Ignore all ...RequestType and ...ResponseType types since they are not relevant for stats - except ...ForResponseType that some dosagetypes are named -->
	<xsl:if test="('ForResponseType' = substring(current()/@name, string-length(current()/@name) - string-length('ForResponseType') +1)) 
		or (not('RequestType' = substring(current()/@name, string-length(current()/@name) - string-length('RequestType') +1)) 
		and not('ResponseType' = substring(current()/@name, string-length(current()/@name) - string-length('ResponseType') +1)))
		and not(contains(preceding-sibling::node()[not(self::text())][1], $nsfFilter))
		">

		<h2 id="{@name}">Type: <xsl:value-of select="@name"/></h2>
		<xsl:choose>
			<xsl:when test="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:complexType[@name=current()/@name]/xs:annotation/xs:documentation[@xml:lang='da-DK']">
				Beskrivelse: <xsl:value-of select="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:complexType[@name=current()/@name]/xs:annotation/xs:documentation[@xml:lang='da-DK']"/>
			</xsl:when>
			<xsl:when test="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:element[@type=concat('medicinecard20150601:', current()/@name)]/xs:annotation/xs:documentation[@xml:lang='da-DK']">
				Beskrivelse: <xsl:value-of select="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:element[@type=concat('medicinecard20150601:', current()/@name)]/xs:annotation/xs:documentation[@xml:lang='da-DK']"/>
			</xsl:when>			
		</xsl:choose>
		<xsl:call-template name="SchemaTemplate">
			<xsl:with-param name="component" select="current()"/>
      	</xsl:call-template>
	 	<xsl:apply-templates select="xs:sequence"/>
	 	<xsl:apply-templates select="xs:simpleContent"/>
	 </xsl:if>
</xsl:template>

<xsl:template name="SchemaDiv">
	  <xsl:param name="id"/>
      <xsl:param name="contents"/>
	  <xsl:variable name="buttonID" select="concat($id, '_button')"/>
	
		<input type="button" id="{$buttonID}" value="Vis skema" class="showHideXSD" onclick="toggleVisibility('{$id}'); return false;" />
        <div id="{$id}" class="contents" style="display: none">
	   	     <xsl:copy-of select="$contents"/>
      </div>
 
</xsl:template>
   
<xsl:template name="SchemaTemplate">
    <xsl:param name="component"/>
   	<xsl:variable name="componentID" select="@name"/>
   		
        <xsl:call-template name="SchemaDiv">
         <xsl:with-param name="id" select="concat($componentID, '_xsddiv')"/>
        <xsl:with-param name="contents">
               <xsl:apply-templates select="$component" mode="serialize"/>
            </xsl:with-param>
	</xsl:call-template>
   
</xsl:template>

<!--  Serialize nodes to text -->
<xsl:template match="*" mode="serialize">
	<div style="margin-left: 10px">
    <xsl:text>&lt;</xsl:text>
    <xsl:value-of select="name()"/>
    <xsl:apply-templates select="@*" mode="serialize" />
    <xsl:choose>
        <xsl:when test="node()">
            <xsl:text>&gt;</xsl:text>
            <xsl:apply-templates mode="serialize" />
            <xsl:text>&lt;/</xsl:text>
            <xsl:value-of select="name()"/>
            <xsl:text>&gt;</xsl:text>
        </xsl:when>
        <xsl:otherwise>
            <xsl:text> /&gt;</xsl:text>
        </xsl:otherwise>
    </xsl:choose>
    </div>
</xsl:template>

<xsl:template match="@*" mode="serialize">
    <xsl:text> </xsl:text>
    <xsl:value-of select="name()"/>
    <xsl:text>="</xsl:text>
    <xsl:value-of select="."/>
    <xsl:text>"</xsl:text>
</xsl:template>

<xsl:template match="text()" mode="serialize">
    <xsl:value-of select="."/>
</xsl:template>

<!--  END: Serialize nodes to text -->

<xsl:template match="xs:simpleContent">
	
	<ul>
		Typeudvidelse:
	<li>
	<dl>
		<dt>Basistype:</dt>
		<dd><a href="#{substring-after(xs:extension/@base, 'medicinecard20150601:')}"><xsl:value-of select="substring-after(xs:extension/@base, 'medicinecard20150601:')"/></a></dd>
		<xsl:if test="xs:extension/xs:attribute">
			<dt>Tilføjet attributnavn:</dt>
			<dd><xsl:value-of select="xs:extension/xs:attribute/@name"/></dd>
			<dt>Tilføjet attributtype:</dt>
			<dd><a href="#{substring-after(xs:extension/xs:attribute/@type, 'medicinecard20150601:')}"><xsl:value-of select="substring-after(xs:extension/xs:attribute/@type, 'medicinecard20150601:')"/></a></dd>
			<dt>Tilføjet attribut, anvendelse: </dt>
			<dd><xsl:value-of select="xs:extension/xs:attribute/@use"/></dd>
		</xsl:if>
	</dl>
	</li>
	</ul>
</xsl:template>

<xsl:template match="xs:simpleType">

	<h2 id="{@name}">Type: <xsl:value-of select="@name"/></h2>
	<xsl:if test="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:element[@type=concat('medicinecard20150601:', current()/@name)]/xs:annotation/xs:documentation[@xml:lang='da-DK']">
		Beskrivelse: <xsl:value-of select="/wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:element[@type=concat('medicinecard20150601:', current()/@name)]/xs:annotation/xs:documentation[@xml:lang='da-DK']"/>
	</xsl:if>			

	<xsl:if test="xs:union">
	
		<ul>Foreningsmængde af flg. typer:
		<xsl:for-each select="str:tokenize(xs:union/@memberTypes, ' ')">
			<li>
				<a href="#{substring-after(., 'medicinecard20150601:')}"><xsl:value-of select="substring-after(., 'medicinecard20150601:')"/></a>
			</li>
		</xsl:for-each>
		</ul>
	</xsl:if>
	<xsl:if test="xs:restriction">
		<dl>
			<dt>Basistype:</dt>
			<dd>
			<xsl:if test="starts-with(xs:restriction/@base, 'medicinecard20150601:')">
				<xsl:value-of select="substring-after(xs:restriction/@base, 'medicinecard20150601:')"/>
			</xsl:if>
			<xsl:if test="not(starts-with(xs:restriction/@base, 'medicinecard20150601:'))">
				<xsl:value-of select="xs:restriction/@base"/>
			</xsl:if>
			</dd>
			<xsl:if test="xs:restriction/xs:minInclusive/@value">
				<dt>Min værdi:</dt>
				<dd><xsl:value-of select="xs:restriction/xs:minInclusive/@value"/></dd>
			</xsl:if>
			<xsl:if test="xs:restriction/xs:maxInclusive/@value">
				<dt>Max værdi:</dt>
				<dd><xsl:value-of select="xs:restriction/xs:maxInclusive/@value"/></dd>
			</xsl:if>
			<xsl:if test="xs:restriction/xs:minLength/@value">
				<dt>Min længde:</dt>
				<dd><xsl:value-of select="xs:restriction/xs:minLength/@value"/></dd>
			</xsl:if>
			<xsl:if test="xs:restriction/xs:maxLength/@value">
				<dt>Max længde:</dt>
				<dd><xsl:value-of select="xs:restriction/xs:maxLength/@value"/></dd>
			</xsl:if>
			<xsl:if test="xs:restriction/xs:enumeration">
				<ul>Enumeration af flg. værdier: 
					<xsl:for-each select="xs:restriction/xs:enumeration">
						<li>
							<xsl:value-of select="@value"/>
						</li>
					</xsl:for-each>
				</ul>
			</xsl:if>
		</dl>
	</xsl:if>
	
</xsl:template>

<xsl:template match="/">
<html> 
	<body>
	<head>
	<style>
dl {

    padding: 0.5em;
    border-bottom-style: dotted;
    border-width: 1px;
  }
  
  dt {
    float: left;
    clear: left;
    width: 200px;
    text-align: right;
    font-weight: bold;
    color: black;
    padding-right: 10px;
  }
  dd {
    margin: 0px 0 10px 200px;
    padding: 0 0 0.5em 0;
    color: grey;
  }  
  ul {
    padding-top: 10px;
    
    color: black;
  }
  h2 {
  border-top-style: solid;
  padding-top: 10px;
  }
  
  .contents { 
  	border-width: 1px;
  	border-style: solid;
  	margin: 10px;
  	padding: 10px;
  	font-family: monospace;
  }
  
  .showHideXSD {
  	margin-left: 10px;
 }
 
</style>

<xsl:text>
      
<script type="text/javascript">

function minimizeXSDDiv(xsdDivObj, buttonObj) {
  if (xsdDivObj == null || buttonObj == null) {
  } else {
     xsdDivObj.style.display="none";

     // Change text of button 
     if (xsdDivObj.style.display=="none") {
        buttonObj.value="Vis skema";
     }
  }
}

function expandXSDDiv(xsdDivObj, buttonObj) {
  if (xsdDivObj == null || buttonObj == null) {
  } else {
     xsdDivObj.style.display="block";
     if (xsdDivObj.style.display == "block") {
        buttonObj.value="Skjul skema";
     }
  }
}

function setState(divId, show) {
  var xsdDivObj = document.getElementById(divId);
  var buttonObj = document.getElementById(divId + "_button");
  if (show) {
     expandXSDDiv(xsdDivObj, buttonObj);
  } else {
     minimizeXSDDiv(xsdDivObj, buttonObj);
  }
}

function toggleVisibility(divId) {
  var xsdDivObj = document.getElementById(divId);
  var buttonObj = document.getElementById(divId + "_button");
  if (xsdDivObj.style.display=="none") {
     expandXSDDiv(xsdDivObj, buttonObj);
  } else if (xsdDivObj.style.display=="block") {
     minimizeXSDDiv(xsdDivObj, buttonObj);
  }
}

</script>
</xsl:text>
	
	</head>
		<xsl:apply-templates select="wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:complexType[@name='MedicineCardType']"/>
		<xsl:apply-templates select="wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:complexType[@name!='MedicineCardType' and @name != 'CreateDrugMedicationType' and @name != 'UpdateDrugMedicationType' and @name != 'DrugMedicationHistoryPeriodType']"/>
		<xsl:apply-templates select="wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='http://www.dkma.dk/medicinecard/xml.schema/2015/06/01']/xs:simpleType"/>
	 
</body>
</html>
</xsl:template>
</xsl:stylesheet>