<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:exslt="http://exslt.org/common" xmlns:set="http://exslt.org/sets" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:str="http://exslt.org/strings">
	<xsl:output method="html"/>
	<xsl:variable name="nsfFilter" select="'nsf=&quot;ignore&quot;'"/>
	<xsl:key name="complexTypeName" match="/wsdl:definitions/wsdl:types/xs:schema/xs:complexType" use="."/>

    <xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyzæøå'" />
    <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZÆØÅ'" />

    <!--  Templates for expandable schema boxes -->
	<xsl:template name="SchemaDiv">
		<xsl:param name="id"/>
		<xsl:param name="contents"/>
		<xsl:variable name="buttonID" select="concat($id, '_button')"/>
		<xsl:variable name="buttonUsageID" select="concat($id, '_usage_button')"/>

		<input type="button" id="{$buttonID}" value="Vis skema" class="showHideXSD" onclick="toggleVisibility('{$id}'); return false;" />
		<input type="button" id="{$buttonUsageID}" value="Vis anvendelse" class="showHideXSD" onclick="toggleUsageVisibility('{$id}'); return false;" />

		<div id="{concat($id, '_xsddiv')}" class="contents" style="display: none">
			<xsl:copy-of select="$contents"/>
		</div>

		<div id="{concat($id, '_xsddiv_usage')}" class="usage" style="display: none">
			<!-- Placeholder - filled by toggleUsageVisibility() -->
		</div>

	</xsl:template>

	<xsl:template name="SchemaTemplate">
		<xsl:param name="component"/>
		<xsl:param name="name"/>

		<xsl:variable name="componentID" select="@name"/>

		<xsl:call-template name="SchemaDiv">
			<xsl:with-param name="id" select="$componentID"/>
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


	<!-- SEQUENCE -->

	<xsl:template name="sequence">
		<xsl:param name="isNSF"/>
		<ul>
		Sekvens af:
			<xsl:for-each select="node()">
				<xsl:if test="name() and  (not(isNSF) or (not(name()='filter') and not (contains(preceding-sibling::node()[not(self::text())][1], $nsfFilter))))">
					<li>
						<xsl:if test="isNSF">
							<xsl:apply-templates select="current()" mode="nsf"/>
						</xsl:if>
						<xsl:if test="not(isNSF)">
							<xsl:apply-templates select="current()" mode="all"/>
						</xsl:if>
					</li>
				</xsl:if>
			</xsl:for-each>
		</ul>
	</xsl:template>

	<xsl:template match="xs:sequence" mode="nsf">
		<xsl:call-template name="sequence">
			<xsl:with-param name="isNSF" select="true"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="xs:sequence" mode="all">
		<xsl:call-template name="sequence">
			<xsl:with-param name="isNSF" select="false"/>
		</xsl:call-template>
	</xsl:template>



	<!-- CHOICE -->


	<xsl:template name="choice">
		<xsl:param name="isNSF"/>
		<xsl:if test="not(isNSF) or (not(contains(preceding-sibling::node()[not(self::text())][1], $nsfFilter)))">
			<ul>Et af følgende elementer:
				<xsl:for-each select="node()">
					<xsl:if test="not(name()='')">
						<xsl:if test="position() > 2">
					ELLER
						</xsl:if>
						<li>
							<xsl:apply-templates select="current()" mode="nsf"/>
						</li>
					</xsl:if>
				</xsl:for-each>
			</ul>
		</xsl:if>
	</xsl:template>

	<xsl:template match="xs:choice" mode="nsf">
		<xsl:call-template name="choice">
			<xsl:with-param name="isNSF" select="true"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="xs:choice" mode="all">
		<xsl:call-template name="choice">
			<xsl:with-param name="isNSF" select="false"/>
		</xsl:call-template>
	</xsl:template>

	<!-- ELEMENT -->

	<xsl:template match="xs:element" mode="nsf">
		<xsl:call-template name="element">
			<xsl:with-param name="isNSF" select="true"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="xs:element" mode="all">
		<xsl:call-template name="element">
			<xsl:with-param name="isNSF" select="false"/>
		</xsl:call-template>
	</xsl:template>


	<xsl:template name="element">
		<xsl:param name="isNSF"/>

		<xsl:if test="not(isNSF) or (not(contains(preceding-sibling::node()[not(self::text())][1], $nsfFilter)))">
			<dl>
				<xsl:choose>

					<!-- Hvis der er en annotation på selve elementet, anvendes denne -->
					<xsl:when test="xs:annotation/xs:documentation[@xml:lang='da-DK'] != ''">
						<dt>Navn:</dt>
						<dd>
							<xsl:value-of select="@name"/>
						</dd>
						<dt>Beskrivelse:</dt>
						<dd>
							<xsl:value-of select="xs:annotation/xs:documentation[@xml:lang='da-DK']"/>
						</dd>
						<dt>Type:</dt>
						<dd>
							<a href="#{translate(substring-after(@type, ':'),$uppercase,$lowercase)}">
								<xsl:value-of select="substring-after(@type, ':')"/>
							</a>
						</dd>
					</xsl:when>

					<!-- Hvis det er en complexType med en annotation på, da anvendes denne annotation -->
					<xsl:when test="/wsdl:definitions/wsdl:types/xs:schema/xs:complexType[@name=concat(current()/@name, 'Type')]/xs:annotation/xs:documentation[@xml:lang='da-DK'] != ''">
						<dt>Navn:</dt>
						<dd>
							<xsl:value-of select="@name"/>
						</dd>
						<dt>Beskrivelse:</dt>
						<dd>
							<xsl:value-of select="/wsdl:definitions/wsdl:types/xs:schema/xs:complexType[@name=concat(current()/@name, 'Type')]/xs:annotation/xs:documentation[@xml:lang='da-DK']"/>
						</dd>
						<dt>Type:</dt>
						<dd>
							<a href="#{translate(substring-after(@type, ':'),$uppercase,$lowercase)}">
								<xsl:value-of select="substring-after(@type, ':')"/>
							</a>
						</dd>
					</xsl:when>
					<!-- Variant#2 hvis det er en complexType med en annotation på, da anvendes denne annotation -->
					<xsl:when test="/wsdl:definitions/wsdl:types/xs:schema/xs:complexType[@name=substring-after(current()/@type, ':')]/xs:annotation/xs:documentation[@xml:lang='da-DK'] != ''">
						<dt>Navn:</dt>
						<dd>
							<xsl:value-of select="@name"/>
						</dd>
						<dt>Beskrivelse:</dt>
						<dd>
							<xsl:value-of select="/wsdl:definitions/wsdl:types/xs:schema/xs:complexType[@name=substring-after(current()/@type, ':')]/xs:annotation/xs:documentation[@xml:lang='da-DK']"/>
						</dd>
						<dt>Type:</dt>
						<dd>
							<a href="#{translate(substring-after(@type, ':'),$uppercase,$lowercase)}">
								<xsl:value-of select="substring-after(@type, ':')"/>
							</a>
						</dd>
					</xsl:when>

					<!-- Hvis det er en simpleType med en annotation på, da anvendes denne annotation -->
					<xsl:when test="/wsdl:definitions/wsdl:types/xs:schema/xs:simpleType[@name=concat(current()/@name, 'Type')]/xs:annotation/xs:documentation[@xml:lang='da-DK'] != ''">
						<dt>Navn:</dt>
						<dd>
							<xsl:value-of select="@name"/>
						</dd>
						<dt>Beskrivelse:</dt>
						<dd>
							<xsl:value-of select="/wsdl:definitions/wsdl:types/xs:schema/xs:simpleType[@name=concat(current()/@name, 'Type')]/xs:annotation/xs:documentation[@xml:lang='da-DK']"/>
						</dd>
						<dt>Type:</dt>
						<dd>
							<a href="#{translate(substring-after(@type, ':'),$uppercase,$lowercase)}">
							<xsl:value-of select="substring-after(@type, ':')"/>
							</a>
						</dd>
					</xsl:when>

					<!-- Variant#2 Hvis det er en simpleType med en annotation på, da anvendes denne annotation -->
					<xsl:when test="/wsdl:definitions/wsdl:types/xs:schema/xs:simpleType[@name=substring-after(current()/@type, ':')]/xs:annotation/xs:documentation[@xml:lang='da-DK'] != ''">
						<dt>Navn:</dt>
						<dd>
							<xsl:value-of select="@name"/>
						</dd>
						<dt>Beskrivelse:</dt>
						<dd>
							<xsl:value-of select="/wsdl:definitions/wsdl:types/xs:schema/xs:simpleType[@name=substring-after(current()/@type, ':')]/xs:annotation/xs:documentation[@xml:lang='da-DK']"/>
						</dd>
						<dt>Type:</dt>
						<dd>
							<a href="#{translate(substring-after(@type, ':'),$uppercase,$lowercase)}">
								<xsl:value-of select="substring-after(@type, ':')"/>
							</a>
						</dd>
					</xsl:when>

					<!-- Ellers: anvend en evt. annotation fra et type-element -->
					<xsl:otherwise>
						<dt>Navn:</dt>
						<dd>
							<xsl:value-of select="@name"/>
						</dd>
						<dt>Beskrivelse:</dt>
						<dd>
							<xsl:value-of select="/wsdl:definitions/wsdl:types/xs:schema/xs:element[@type=current()/@type]/xs:annotation/xs:documentation[@xml:lang='da-DK']"/>
						</dd>
						<dt>Type:</dt>
						<dd>
							<a href="#{translate(substring-after(@type, ':'),$uppercase,$lowercase)}">
								<xsl:value-of select="substring-after(@type, ':')"/>
							</a>
						</dd>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:choose>
					<xsl:when test="not(@minOccurs) and not(@maxOccurs)">
						<dt>Antal</dt>
						<dd>Præcis én</dd>
					</xsl:when>
					<xsl:when test="@minOccurs=0 and not(@maxOccurs)">
						<dt>Antal</dt>
						<dd>0 eller 1</dd>
					</xsl:when>
					<xsl:when test="@minOccurs=0 and @maxOccurs='unbounded'">
						<dt>Antal</dt>
						<dd>0, 1 eller flere</dd>
					</xsl:when>
					<xsl:when test="(@minOccurs='1' or not(@minOccurs)) and @maxOccurs='unbounded'">
						<dt>Antal</dt>
						<dd>1 eller flere</dd>
					</xsl:when>
					<xsl:otherwise>

						<dt>Min. antal:</dt>
						<dd>
							<xsl:choose>
								<xsl:when test="not(@minOccurs)">
					1
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="@minOccurs"/>
								</xsl:otherwise>
							</xsl:choose>
						</dd>
						<dt>Max antal:</dt>
						<dd>
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

	<!-- COMPLEXTYPE -->


	<xsl:template match="xs:complexType" mode="nsf">
		<xsl:call-template name="complexType">
			<xsl:with-param name="isNSF" select="true"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="xs:complexType" mode="all">
		<xsl:call-template name="complexType">
			<xsl:with-param name="isNSF" select="false"/>
		</xsl:call-template>
	</xsl:template>



	<xsl:template name="complexType">
		<xsl:param name="isNSF"/>

		<!-- Ignore all ...RequestType and ...ResponseType types since they are not relevant for stats - except ...ForResponseType that some dosagetypes are named -->
		<xsl:if test="not(isNSF) or (('ForResponseType' = substring(current()/@name, string-length(current()/@name) - string-length('ForResponseType') +1))
		or (not('RequestType' = substring(current()/@name, string-length(current()/@name) - string-length('RequestType') +1))
		and not('ResponseType' = substring(current()/@name, string-length(current()/@name) - string-length('ResponseType') +1)))
		and not(contains(preceding-sibling::node()[not(self::text())][1], $nsfFilter)))
		">

			<h2 id="{translate(@name,$uppercase,$lowercase)}">Type: <xsl:value-of select="@name"/>
			</h2>

			<xsl:choose>
				<xsl:when test="/wsdl:definitions/wsdl:types/xs:schema/xs:complexType[@name=current()/@name]/xs:annotation/xs:documentation[@xml:lang='da-DK']">
				Beskrivelse: <xsl:value-of select="/wsdl:definitions/wsdl:types/xs:schema/xs:complexType[@name=current()/@name]/xs:annotation/xs:documentation[@xml:lang='da-DK']"/>
				</xsl:when>
				<xsl:when test="/wsdl:definitions/wsdl:types/xs:schema/xs:element[substring-after(@type, ':')=current()/@name]/xs:annotation/xs:documentation[@xml:lang='da-DK']">
				Beskrivelse: <xsl:value-of select="/wsdl:definitions/wsdl:types/xs:schema/xs:element[substring-after(@type, ':') = current()/@name]/xs:annotation/xs:documentation[@xml:lang='da-DK']"/>
				</xsl:when>
			</xsl:choose>
			<xsl:call-template name="SchemaTemplate">
				<xsl:with-param name="component" select="current()"/>
				<xsl:with-param name="name" select="current()/@name"/>
				<xsl:with-param name="namespace" select="substring-before(current()/@name, ':')"/>
			</xsl:call-template>

			<xsl:if test="isNSF">
				<xsl:apply-templates select="xs:sequence" mode="nsf"/>
			</xsl:if>
			<xsl:if test="not(isNSF)">
				<xsl:apply-templates select="xs:sequence" mode="all"/>
			</xsl:if>


			<xsl:if test="isNSF">
				<xsl:apply-templates select="xs:choice" mode="nsf"/>
			</xsl:if>
			<xsl:if test="not(isNSF)">
				<xsl:apply-templates select="xs:choice" mode="all"/>
			</xsl:if>

			<xsl:apply-templates select="xs:simpleContent"/>
			<xsl:apply-templates select="xs:complexContent"/>
		</xsl:if>
	</xsl:template>

	<xsl:template match="xs:attribute">
		<li>
			<dl>
				<dt>Tilføjet attributnavn:</dt>
				<dd>
					<xsl:value-of select="@name"/>
				</dd>
				<dt>Tilføjet attributtype:</dt>
				<dd>
					<a href="#{translate(substring-after(@type, ':'),$uppercase,$lowercase)}">
						<xsl:value-of select="substring-after(@type, ':')"/>
					</a>
				</dd>
				<dt>Tilføjet attribut, anvendelse: </dt>
				<dd>
					<xsl:value-of select="@use"/>
				</dd>
			</dl>
		</li>
	</xsl:template>


	<xsl:template match="xs:complexContent">

		<ul>
			Typeudvidelse:
			<li>
				<dl>
					<dt>Basistype:</dt>
					<dd>
						<a href="#{substring-after(xs:extension/@base, ':')}">
							<xsl:value-of select="substring-after(xs:extension/@base, ':')"/>
						</a>
					</dd>
				</dl>
				<ul>
					<xsl:apply-templates select="xs:extension/xs:attribute"/>
				</ul>
			</li>
		</ul>
	</xsl:template>

	<xsl:template match="xs:simpleContent">

		<ul>
			Typeudvidelse:
			<li>
				<dl>
					<dt>Basistype:</dt>
					<dd>
						<a href="#{translate(substring-after(xs:extension/@base, ':'),$uppercase,$lowercase)}">
							<xsl:value-of select="substring-after(xs:extension/@base, ':')"/>
						</a>
					</dd>
				</dl>
				<ul>
					<xsl:apply-templates select="xs:extension/xs:attribute"/>
				</ul>
			</li>
		</ul>
	</xsl:template>

	<xsl:template match="xs:simpleType">

		<h2 id="{translate(@name,$uppercase,$lowercase)}">Type: <xsl:value-of select="@name"/>
		</h2>
		<xsl:if test="/wsdl:definitions/wsdl:types/xs:schema/xs:element[substring-after(@type, ':')=current()/@name]/xs:annotation/xs:documentation[@xml:lang='da-DK']">
		Beskrivelse: <xsl:value-of select="/wsdl:definitions/wsdl:types/xs:schema/xs:element[substring-after(@type, ':')=current()/@name]/xs:annotation/xs:documentation[@xml:lang='da-DK']"/>
		</xsl:if>

		<xsl:call-template name="SchemaTemplate">
			<xsl:with-param name="component" select="current()"/>
			<xsl:with-param name="name" select="current()/@name"/>
		</xsl:call-template>

		<xsl:if test="xs:union">

			<ul>Foreningsmængde af flg. typer:
				<xsl:for-each select="str:tokenize(xs:union/@memberTypes, ' ')">
					<li>
						<a href="#{translate(substring-after(., ':'),$uppercase,$lowercase)}">
							<xsl:value-of select="substring-after(., ':')"/>
						</a>
					</li>
				</xsl:for-each>
			</ul>
		</xsl:if>
		<xsl:if test="xs:restriction">
			<dl>
				<dt>Basistype:</dt>
				<dd>
					<xsl:choose>
						<xsl:when test="substring-after(xs:restriction/@base, ':')">
							<xsl:value-of select="substring-after(xs:restriction/@base, ':')"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="xs:restriction/@base"/>
						</xsl:otherwise>
					</xsl:choose>
				</dd>
				<xsl:if test="xs:restriction/xs:minInclusive/@value">
					<dt>Min værdi:</dt>
					<dd>
						<xsl:value-of select="xs:restriction/xs:minInclusive/@value"/>
					</dd>
				</xsl:if>
				<xsl:if test="xs:restriction/xs:maxInclusive/@value">
					<dt>Max værdi:</dt>
					<dd>
						<xsl:value-of select="xs:restriction/xs:maxInclusive/@value"/>
					</dd>
				</xsl:if>
				<xsl:if test="xs:restriction/xs:minLength/@value">
					<dt>Min længde:</dt>
					<dd>
						<xsl:value-of select="xs:restriction/xs:minLength/@value"/>
					</dd>
				</xsl:if>
				<xsl:if test="xs:restriction/xs:maxLength/@value">
					<dt>Max længde:</dt>
					<dd>
						<xsl:value-of select="xs:restriction/xs:maxLength/@value"/>
					</dd>
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

	<xsl:template name="CSS">
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

  .usage {
  	border-width: 1px;
  	border-style: solid;
  	margin: 10px;
  	padding: 10px;
  }

  .showHideXSD {
  	margin-left: 10px;
 }

		</style>
	</xsl:template>

	<xsl:template name="js">
		<xsl:for-each select="/wsdl:definitions/wsdl:types/xs:schema/xs:complexType | /wsdl:definitions/wsdl:types/xs:schema/xs:simpleType">
			<xsl:variable name="typename" select="@name"/>

			usages["<xsl:value-of select="@name"/>"] = [
				<xsl:for-each select="//xs:element[concat(':',$typename) = substring(@type, string-length(@type) - string-length($typename))] | //xs:union[concat(':',$typename) = substring(@memberTypes, string-length(@memberTypes) - string-length($typename))]">
					<xsl:if test="ancestor::xs:complexType">
						"<xsl:value-of select="ancestor::xs:complexType[1]/@name"/>",
					</xsl:if>
					<xsl:if test="ancestor::xs:simpleType">
						"<xsl:value-of select="ancestor::xs:simpleType[1]/@name"/>",
					</xsl:if></xsl:for-each>
			];
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="Javascript">

	<script type="text/javascript">

	var usages = [];

	<xsl:call-template name="js"/>

function minimizeXSDDiv(xsdDivObj, buttonObj, name) {
  if (xsdDivObj == null || buttonObj == null) {
  } else {
     xsdDivObj.style.display="none";

     // Change text of button
     if (xsdDivObj.style.display=="none") {
        buttonObj.value="Vis " + name;
     }
  }
}

function expandXSDDiv(xsdDivObj, buttonObj, name) {
  if (xsdDivObj == null || buttonObj == null) {
  } else {
     xsdDivObj.style.display="block";
     if (xsdDivObj.style.display == "block") {
        buttonObj.value="Skjul " + name;
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

function toggleVisibility(id, name) {
  var divId = id + "_xsddiv";
  var xsdDivObj = document.getElementById(divId);
  var buttonObj = document.getElementById(id + "_button");
  if (xsdDivObj.style.display=="none") {
     expandXSDDiv(xsdDivObj, buttonObj, 'skema');
  } else if (xsdDivObj.style.display=="block") {
     minimizeXSDDiv(xsdDivObj, buttonObj, 'skema');
  }
}

function toggleUsageVisibility(id) {
	var divId = id + '_xsddiv_usage';
	var xsdDivObj = document.getElementById(divId);
	var buttonObj = document.getElementById(id + "_usage_button");
	if (xsdDivObj.style.display=="none") {
	  expandXSDDiv(xsdDivObj, buttonObj, 'anvendelse');
	} else if (xsdDivObj.style.display=="block") {
	  minimizeXSDDiv(xsdDivObj, buttonObj, 'anvendelse');
	}

    if(xsdDivObj.innerHTML.length == 0) {

		xsdDivObj.innerHTML += "Anvendes i flg. typer:&lt;br/&gt;";

		var uniqueArray = usages[id].filter(function(item, pos, self) {
			return self.indexOf(item) == pos;
		});
		uniqueArray.sort();
		uniqueArray.forEach(function(value) {
			generateUniqueUsageLinks(xsdDivObj, value);
		});
	}
}

function generateUniqueUsageLinks(xsdDivObj, value) {
	var link = document.createElement("a");
	link.href = "#" + value.toLowerCase();
	link.innerHTML = value;
	var p = document.createElement("p");
	p.appendChild(link);
	xsdDivObj.appendChild(p);
}


		</script>
	</xsl:template>
</xsl:stylesheet>