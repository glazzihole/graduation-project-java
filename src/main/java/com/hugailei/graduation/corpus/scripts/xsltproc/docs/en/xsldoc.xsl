<?xml version="1.0" ?>
<!-- 
     File : xsldoc.xsl     
     Author: Keith Isdale <k_isdale@tpg.com.au>
     Description: Stylesheet to process xsldoc.xml and generate help text
     Copyright Reserved Under GPL     
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">
  <xsl:output method="text"/>
  <xsl:strip-space elements="text"/>
  <!-- The selected nodes to be printed for overview -->
  <xsl:variable name="overview_node" select="/xsldoc/overview"/>
  <!-- The list of valid xsldbg commands -->
  <xsl:variable name="command_nodes" select="/xsldoc/cmd"/>
  <!-- What version is this document-->
  <xsl:variable name="doc_version" select="'0.6'"/>
  <!-- The default version of xsldbg -->
  <xsl:param name="xsldbg_version" select="'0.5.9'"/>
  <!-- We want 'help' to point to a invalid command if stylesheet
       user has not provided a value for 'help' param-->
  <xsl:param name="help" select="'_#_'"/>
  <!-- Do we printout all documentation '1' if so '0' otherwise -->
  <xsl:param name="alldocs" select="0"/>
  <!-- The documentation we can find for 'help' user requires -->
  <xsl:variable name="help_cmd" select="$command_nodes[@name=$help or @shortcut=$help]"/>



  <!-- Main template-->
  <xsl:template match="/">
         xsldbg version <xsl:value-of select="$xsldbg_version"/>
         ====================

    <xsl:choose>
      <xsl:when test="$alldocs=0">
        <xsl:call-template name="helpOnCommand"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="$overview_node"/>
        <xsl:for-each select="/xsldoc/cmd">
          <xsl:sort select="@name"/>
          <xsl:text>- - - - - - - - - - - - - - - - - - - -

</xsl:text>
          <xsl:apply-templates select="."/>
        </xsl:for-each>
      </xsl:otherwise>
    </xsl:choose>
<xsl:text>
</xsl:text>
  Help document version <xsl:value-of select="$doc_version"/><xsl:text>
</xsl:text>
  </xsl:template>


  <!-- Display help on a particular command -->
  <xsl:template name="helpOnCommand">
    <xsl:choose>
    <xsl:when test="count($help_cmd) > 0" >
      <xsl:apply-templates select="$help_cmd" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:if test="$help !='_#_'">
          <xsl:text>Help about </xsl:text>
          <xsl:value-of select="$help"/>
          <xsl:text> was not found.
        </xsl:text>
      </xsl:if>
      <xsl:apply-templates select="$overview_node"/>
    </xsl:otherwise>
  </xsl:choose>
  </xsl:template>

   <!-- Convert header into something useful -->
  <xsl:template match="header">
<xsl:value-of select="."/>
<xsl:text>
    </xsl:text>
<xsl:value-of
  select="substring('____________________________________________________________',
  1, string-length())" />
<xsl:text>
</xsl:text>
  </xsl:template>

   <!-- -->
  <xsl:template match="text()">
    <xsl:value-of select="normalize-space()"/>
  </xsl:template>

  <!-- -->
  <xsl:template match="para">
    <xsl:text>
</xsl:text>
    <xsl:apply-templates/>
  </xsl:template>

  <!-- -->
  <xsl:template match="list | usage">
    <xsl:text>
</xsl:text>
    <xsl:for-each select="li">
      <xsl:for-each select="ancestor::node()"><xsl:text>  </xsl:text></xsl:for-each><xsl:apply-templates />
      <xsl:text>
</xsl:text>
    </xsl:for-each>
  </xsl:template>

  <!-- -->
  <xsl:template match="comment">
    <xsl:text>   </xsl:text><xsl:value-of select="."/>
  </xsl:template>

  <!-- -->
  <xsl:template name="cmd-summary" match="cmd-summary">
    <xsl:text >                   Command summary
</xsl:text>
    <xsl:apply-templates select="cmd"/>
  </xsl:template>

  <!-- Format command documentation -->
  <xsl:template match="cmd">
Command : <xsl:value-of select="@title"/><xsl:text >
</xsl:text>Summary : <xsl:value-of select="summary"/><xsl:text>
</xsl:text>
    <xsl:if test="body">
        <xsl:apply-templates select="body"/><xsl:text >
</xsl:text>
    </xsl:if>
    <xsl:text>Usage:
</xsl:text>
    <xsl:apply-templates select="usage"/>
  </xsl:template>


</xsl:stylesheet>


<!-- initialization code for xemacs -->
<!--
Local Variables:
mode: xsl
sgml-minimize-attributes:nil
sgml-general-insert-case:lower
sgml-indent-step:2
sgml-indent-data:nil
End:
-->
