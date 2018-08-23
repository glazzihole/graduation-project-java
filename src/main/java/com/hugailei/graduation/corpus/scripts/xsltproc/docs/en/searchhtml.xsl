<?xml version="1.0" ?>
<!-- 
     File : searchhtml.xsl     
     Author: Keith Isdale <k_isdale@tpg.com.au>
     Description: Stylesheet to process searchreslt.xml and generate list of 
                 breakPoints, templates, callStack items etc.
                 This uses html format for its output in contrast to
                    search.xsl which is text output. Needs some work to be 
                    for the output to be formatted better.
     Copyright Reserved Under GPL     
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:param name="query" select="/search/*"/>
  <xsl:param name="dosort" select="0"/>

  <xsl:strip-space elements="text() text"/>
  <xsl:output method="html"/>


  <xsl:template match="/">
    <html>
      <head><title>Search output</title></head>
      <body>
        <pre>
    <xsl:if test="count*($query)=0">
      <xsl:variable name="query" select="/search/*"/>
    </xsl:if>
    <xsl:choose>
      <xsl:when test="boolean($dosort)">
        <xsl:apply-templates select="$query">
          <xsl:sort select="//breakpoint/@id" data-type="number" order="ascending"/>
          <xsl:sort select="//template/@name" order="ascending"/>
          <xsl:sort select="//template/@match" order="ascending"/>
          <xsl:sort select="//source/@href" order="ascending"/>
        </xsl:apply-templates>
       </xsl:when>
       <xsl:otherwise>
         <!-- disabled sorting -->
         <xsl:apply-templates select="$query">
         </xsl:apply-templates>
       </xsl:otherwise>
  </xsl:choose>
  <xsl:if test="count($query)=0"><xsl:text>No nodes match query
</xsl:text>
   </xsl:if>
      </pre>
   </body>
   </html>
  </xsl:template>

  <!-- Display the details of where the item where included in XSL source -->
  <xsl:template name="fileDetails">
    <xsl:text> file </xsl:text>
    <xsl:value-of select="@url"/>
     <xsl:text> : line </xsl:text>   
    <xsl:value-of select="@line"/>
     <xsl:text>
</xsl:text>  
  </xsl:template>

  <xsl:template match="breakpoint">
    <xsl:text> Breakpoint </xsl:text>
    <xsl:value-of select="@id"/>
    <xsl:choose>
      <xsl:when test="@enabled='1'">        
        <xsl:text> enabled</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text> disabled</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="@template">
       <xsl:text> for template </xsl:text>
         <xsl:text>&quot;</xsl:text>
         <xsl:value-of select="@template"/>
         <xsl:text>&quot;</xsl:text>
    </xsl:if>
    <xsl:call-template name="fileDetails" />
  </xsl:template>

  <xsl:template match="template">
    <xsl:text> Template </xsl:text>
    <xsl:choose>
    <xsl:when test="@name">
       <xsl:text>&quot;</xsl:text>
       <xsl:value-of select="@name"/>
       <xsl:text>&quot;</xsl:text>
    </xsl:when>
    <xsl:otherwise>
       <xsl:text>&quot;</xsl:text>
       <xsl:value-of select="@match"/>
       <xsl:text>&quot;</xsl:text>
    </xsl:otherwise>
    </xsl:choose>
    <xsl:call-template name="fileDetails" />
  </xsl:template>

  <xsl:template match="source|import|include">
      <xsl:text> Source </xsl:text>
      <xsl:value-of select="@href"/>
      <xsl:if test="@url"> 
        <!-- Ok we know we're no the top document so print out 
             our type and where the xsl:included or xsl:import was placed
             in parent -->
        <xsl:choose>
          <xsl:when test="name()='import'">
            <xsl:text> imported from </xsl:text>
          </xsl:when>
          <xsl:otherwise>
          <xsl:text> included from </xsl:text>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:call-template name="fileDetails" />
      </xsl:if>
      <xsl:text>
</xsl:text>
  </xsl:template>

  <xsl:template match="callstack">
      <xsl:text> Callstack entry</xsl:text>
      <xsl:value-of select="@href"/>
      <xsl:call-template name="fileDetails" />
      <xsl:text> from template </xsl:text>
      <xsl:text>&quot;</xsl:text>
      <xsl:value-of select="@template"/>
      <xsl:text>&quot;</xsl:text>
      <xsl:text>
</xsl:text>
  </xsl:template>

  <!-- This is a global variable -->
 <xsl:template match="variable[@templname = '' and @templmatch = '']">
      <xsl:text> Global variable </xsl:text>
      <xsl:text>name =&quot;</xsl:text>
      <xsl:value-of select="@name"/>
      <xsl:text>&quot;</xsl:text>
      <xsl:text>select=&quot;</xsl:text>
      <xsl:value-of select="@name"/>
      <xsl:text>&quot;</xsl:text>
      <xsl:call-template name="fileDetails" />
  </xsl:template>

  <!-- This is a global variable -->
 <xsl:template match="variable[@templname or @templmatch]">
      <xsl:text> Local variable </xsl:text>
      <xsl:text>name =&quot;</xsl:text>
      <xsl:value-of select="@name"/>
      <xsl:text>&quot;</xsl:text>
      <xsl:text> select=&quot;</xsl:text>
      <xsl:value-of select="@name"/>
      <xsl:text>&quot;</xsl:text>
      <xsl:if test="@templname">
      <xsl:text> templateName =&quot;</xsl:text>
      <xsl:value-of select="@templname"/>
      <xsl:text>&quot;</xsl:text>
      </xsl:if>
     <xsl:if test="@templmatch">
      <xsl:text> templateMatch =&quot;</xsl:text>
      <xsl:value-of select="@templmatch"/>
      <xsl:text>&quot;</xsl:text>
      </xsl:if>
      <xsl:call-template name="fileDetails" />
  </xsl:template>

</xsl:stylesheet>
