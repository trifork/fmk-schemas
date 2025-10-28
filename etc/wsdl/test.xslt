<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
        version="1.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
>
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/">
        <operations>
            <xsl:for-each select="//wsdl:operation">
                <operation>
                    <xsl:value-of select="@name"/>
                </operation>
            </xsl:for-each>
        </operations>
    </xsl:template>
</xsl:stylesheet>