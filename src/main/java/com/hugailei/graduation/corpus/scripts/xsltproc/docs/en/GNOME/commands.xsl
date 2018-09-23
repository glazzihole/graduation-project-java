<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:template name="command_section">
    <sect1 id="command_section">
      <title>Command summary</title>
        <xsl:apply-templates select="cmd">
          <xsl:sort name="@name"/>
        </xsl:apply-templates>
    </sect1>
  </xsl:template>

  <xsl:template match="cmd">
    <xsl:element name="sect2">
      <xsl:attribute name="id" >
        <xsl:value-of select="concat(@name, '_cmd')"/>
      </xsl:attribute>
      <title id="{@name}_cmd.title"><xsl:value-of select="@title"/></title>
      <xsl:apply-templates />
    </xsl:element>
  </xsl:template>

  <xsl:template match="link">
    <link linkend="{@href}_cmd" endterm="{@href}_cmd.title">
      <xsl:value-of select="@href"/>
    </link>
  </xsl:template>

</xsl:stylesheet>
