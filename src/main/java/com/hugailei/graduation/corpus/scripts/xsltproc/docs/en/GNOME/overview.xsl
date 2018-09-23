<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:template name="overview_section">
    <sect1 id="overview_section">
      <title>Overview</title>
      <xsl:for-each select="overview/body">
            <xsl:element name="sect2">
              <xsl:attribute name="id">
                <xsl:value-of select="concat('overview', string(position()))"/>
              </xsl:attribute>
              <xsl:apply-templates />
             </xsl:element>
          </xsl:for-each>
    </sect1>
  </xsl:template>

</xsl:stylesheet>
