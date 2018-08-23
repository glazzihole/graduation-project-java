<?xml version="1.0" ?>
<!-- 
  File : xsldoc2html.xsl 
  Purpose :Convert xsldoc.xml to kde style xmlDocbook
  Author : Keith Isdale <k_isdale@tpg.com.au>
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:output method="xml" doctype-public="-//KDE//DTD DocBook XML V4.1.2-Based Variant V1.1//EN" 
    doctype-system="dtd/kdex.dtd"/>
  <xsl:param name="xsldbg_version" select="'0.5.9'"/>
  <xsl:param name="use_cs2" select="0"/>
  <xsl:variable name="doc_version" select="'0.4'"/>
  <xsl:strip-space elements="title list li para entry"/>
  <xsl:include href="bookinfo.xsl"/>
  <xsl:include href="overview.xsl"/>
  <xsl:include href="credits.xsl"/>
  <xsl:include href="commands.xsl"/>

  <xsl:template match="/xsldoc">
  <book>    
  <xsl:call-template name="book_info"/> 
  <xsl:call-template name="overview_chapter"/>
  <xsl:call-template name="command_chapter"/>
  <xsl:call-template name="credit_chapter"/> 
  </book>
  </xsl:template>

  <xsl:template match="para">
    <xsl:choose>
      <xsl:when test="@title">
        <section>
           <title><xsl:value-of select="@title"/></title>
           <xsl:apply-templates />
        </section>
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
