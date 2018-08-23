<?xml version="1.0" ?>
<!-- 
  File : xsldoc2gnome.xsl 
  Purpose :Convert xsldoc.xml to GNOME style xmlDocbook
  Author : Keith Isdale <k_isdale@tpg.com.au>
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:output method="xml" doctype-public="-//OASIS//DTD DocBook XML V4.1.2//EN"
    doctype-system="http://www.oasis-open.org/docbook/xml/4.1.2/docbookx.dtd" indent="yes"/>

  <!--
GNOME docs template state this as its preable of 
<!DOCTYPE article PUBLIC "-//OASIS//DTD DocBook XML V4.1.2//EN"
    "http://www.oasis-open.org/docbook/xml/4.1.2/docbookx.dtd" [
<!ENTITY version "1.4.0"> 
<!ENTITY date "4/20/2001">
]>

This is achieved useing $xsldbg_version and $xsldbg_date
 -->

  <xsl:param name="xsldbg_version" select="'0.5.9'"/>
  <xsl:param name="xsldbg_date" select="'12/13/2001'"/>
  <xsl:variable name="manrevision" select="'1.0'"/>
  <xsl:variable name="date" select="$xsldbg_date"/>
  <xsl:param name="use_cs2" select="0"/>
  <xsl:variable name="doc_version" select="'0.3'"/>
  <xsl:strip-space elements="title list li para entry"/>
  <xsl:include href="articleinfo.xsl"/>
  <xsl:include href="overview.xsl"/>
  <xsl:include href="commands.xsl"/>
  <xsl:include href="about.xsl"/>

  <xsl:template match="/xsldoc">
    <xsl:call-template name="article_header"/>
  <article id="index" lang="en"> 
  <xsl:call-template name="article_info"/>
  <xsl:comment>please do not change the id; for translations, change lang to </xsl:comment>
  <xsl:comment>appropriate code </xsl:comment>
  <xsl:call-template name="overview_section"/>
  <xsl:call-template name="command_section"/>
  <xsl:call-template name="about_section"/>
  </article>
  </xsl:template>

  <xsl:template match="para">
    <xsl:choose>
      <xsl:when test="@title">
        <sect2>
           <title><xsl:value-of select="@title"/></title>
           <xsl:apply-templates />
        </sect2>
      </xsl:when>
      <xsl:otherwise>
        <para>
          <xsl:apply-templates/>
        </para>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
 

 <xsl:template match="body">
    <xsl:apply-templates select="*"/>
  </xsl:template>
  

  <xsl:template match="title">
    <xsl:value-of select="''"/>
    <xsl:element name="title">
      <xsl:value-of select="."/>
    </xsl:element>
  </xsl:template>


  <xsl:template match="list">
    <informaltable>
      <tgroup cols="1">
      <tbody>  
      <xsl:for-each select ="li">
       <row>
         <entry><xsl:apply-templates /></entry>
       </row>
       </xsl:for-each>
      </tbody>
      </tgroup>
    </informaltable>
  </xsl:template>



  <xsl:template match="usage">
    <table label="of usage">      
      <tgroup cols="1">
      <tbody>  
      <xsl:for-each select ="li">
       <row>         
       <entry><xsl:apply-templates /></entry>
       </row>
       </xsl:for-each>
      </tbody>
      </tgroup>
    </table>
  </xsl:template>



  <xsl:template match="comment">
    <markup>      
      <xsl:text disable-output-escaping="yes">&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;</xsl:text>
      <emphasis>
        <xsl:value-of select="."/>
      </emphasis>
    </markup>

  </xsl:template>

  <xsl:template match="summary">
    <xsl:value-of select="."/>
  </xsl:template>

</xsl:stylesheet>
