<?xml version="1.0" ?>
<!-- 
  File : xsldoc2html.xsl 
  Purpose :Convert xsldoc.xml to html 
  Author : Keith Isdale <k_isdale@tpg.com.au>
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:output method="html" version="4.01" encoding="ISO-8859-1"/>
  <xsl:strip-space elements="li text comment"/>
  <!-- The default version of xsldbg -->
  <xsl:param name="xsldbg_version" select="'0.5.9'"/>
  <!-- What version is this document-->
  <xsl:variable name="doc_version" select="'0.6'"/>

  <!-- -->
  <xsl:template match="/">
    <html>
      <head>
         <xsl:call-template name="style"/>
         <title>
           xsldbg <xsl:value-of select="$xsldbg_version"/> documentation
         </title>
   </head>
      <body bgcolor="#8b7765" text="#000000" link="#000000" vlink="#000000">
        <table class="listbody">
          <tr><td>
          <h1 class="cmdth">xsldbg <xsl:value-of select="$xsldbg_version"/></h1>
          <h2 class="cmdth">Overview</h2>
          <xsl:apply-templates select="xsldoc/overview"/>
          <h2 class="cmdth">Commands</h2>
          <table width="100%" class="cmd">
            <tr><td>
              <xsl:apply-templates select="//cmd">
               <xsl:sort select="@name"/>
             </xsl:apply-templates>
           </td></tr>
         </table>
       </td></tr>
      </table>
      </body>
    </html>
  </xsl:template>

  <!-- Format command documentation -->
  <xsl:template match="cmd">
    <xsl:element name="a">
      <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
    </xsl:element>
    <table width="100%" class="cmd">
      <th class="cmdth"><xsl:value-of select="@title"/></th>
      <tr class="cmdsummary"><xsl:apply-templates select="summary"/></tr>
      <xsl:if test="body">
      <tr class="cmdtr"><td class="cmdtd"><xsl:apply-templates select="body"/></td></tr>
      </xsl:if>
      <xsl:if test="usage">
        <tr class="cmdtr"><xsl:apply-templates select="usage"/></tr>
      </xsl:if>
  </table>
  <br />
  <br />  
  </xsl:template>


 <!-- -->
  <xsl:template select="body">
         <xsl:apply-templates select="*"/>
  </xsl:template>

  <xsl:template match="para">
    <xsl:choose>
       <xsl:when test="@title">
         <h3><xsl:value-of select="@title"/></h3>
           <xsl:apply-templates/>
       </xsl:when>
       <xsl:otherwise>
         <p>
           <xsl:apply-templates/>
         </p>
       </xsl:otherwise>
     </xsl:choose>
  </xsl:template>

  <!-- -->
  <xsl:template match="title | header" >
    <h3><xsl:value-of select="."/></h3>
  </xsl:template>

  <!-- -->
  <xsl:template match="list | usage">
    <xsl:element name="table">
    <xsl:choose>
      <xsl:when test="name()='usage'">
        <xsl:attribute name="class">usage</xsl:attribute>
        <xsl:attribute name="width">100%</xsl:attribute>
         <th class="cmdth">Usage</th>
      </xsl:when>
      <xsl:otherwise>
        <xsl:attribute name="class">list</xsl:attribute>
      </xsl:otherwise>
      </xsl:choose>

       <tr>
          <td width="10"></td>
          <td>
            <table class="listbody">
            <xsl:for-each select="li">
              <xsl:element name="tr">
                <td>
                <xsl:for-each select=".">
                  <xsl:apply-templates />
                    <xsl:text>    </xsl:text>
                  </xsl:for-each>
                </td>
              </xsl:element>
            </xsl:for-each>
            </table>
          </td>
        </tr>
      </xsl:element>
  </xsl:template>

  <!-- -->
  <xsl:template match="comment">
    <xsl:text disable-output-escaping="yes">&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;</xsl:text>
    <i><xsl:value-of select="."/></i>
  </xsl:template>

  <!-- -->
  <xsl:template match="summary">
    <td>
      <xsl:value-of select="."/>
    </td>
  </xsl:template>

  <!-- -->
  <xsl:template match="link">
    <xsl:element name="a">
      <xsl:attribute name="href">#<xsl:value-of select="@href"/></xsl:attribute>
      <xsl:value-of select="."/>
    </xsl:element>
  </xsl:template>

  <!-- Ouput the stylesheet data -->
  <xsl:template name="style">
    <style type="text/css"><xsl:text disable-output-escaping="yes">&lt;!--</xsl:text>
TD {font-size: 10pt; font-family: Verdana,Arial,Helvetica}
BODY {
    font-size: 10pt;
    font-family: Verdana,Arial,Helvetica;
    margin-top: 5pt;
    margin-left: 0pt;
    margin-right: 0pt;}

H1 {font-size: 16pt; font-family: Verdana,Arial,Helvetica}
H2 {font-size: 14pt; font-family: Verdana,Arial,Helvetica}
H3 {font-size: 12pt; font-family: Verdana,Arial,Helvetica}
A:link, A:visited, A:active { text-decoration: underline }

.cmd { background-color  :#8b7765}
.cmdth {background-color :#fffacd}
.cmdtd {background-color :#FFFFFF}
.cmdtr {background-color :#FFFFFF}
.cmdsummary {
    font-weight : bold;
    font-size : larger;
    background-color :#FFFFFF
    }
.listbody {
    font-weight : normal;
    font-size : medium;
    background-color :#FFFFFF
}

.comment {
    space-treatment : preserve
}

<!-- -->
<xsl:text disable-output-escaping="yes">--&gt;</xsl:text></style>
  </xsl:template>

</xsl:stylesheet>
