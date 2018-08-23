<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:variable name="legalNotice" select="document('legal.xml')" />

  <xsl:template name="article_header">
    <xsl:comment>      
  This is a GNOME documentation template, designed by the GNOME
  Documentation Project Team. Please use it for writing GNOME
  documentation, making obvious changes. In particular, all the words
  written in UPPERCASE (with the exception of GNOME) should be
  replaced. As for "legalnotice", please leave the reference
  unchanged.

  Remember that this is a guide, rather than a perfect model to follow
  slavishly. Make your manual logical and readable.  And don't forget
  to remove these comments in your final documentation!  ;-)
  </xsl:comment><xsl:text>

</xsl:text><xsl:comment>
      (Do not remove this comment block.)
  Maintained by the GNOME Documentation Project
  http://developer.gnome.org/projects/gdp
  Template version: 1.0.4
  Template last modified April 20, 2001
  </xsl:comment>

<xsl:text>

</xsl:text><xsl:comment> =============Document Header ============================= </xsl:comment><xsl:text>

</xsl:text>
  </xsl:template>


  <xsl:template name="article_info">
  <articleinfo>
    <title>xsldbg Manual <xsl:value-of select="$manrevision"/></title>
    <copyright>
      <year>2002</year>
      <holder>Keith Isdale</holder>
    </copyright>

   <xsl:comment> translators: uncomment this:

  <copyright>
   <year>2000</year>
   <holder>ME-THE-TRANSLATOR (Latin translation)</holder>
  </copyright>

</xsl:comment><xsl:text>

</xsl:text>

    <publisher> 
      <publishername> Keith Isdale</publishername> 
    </publisher> 

    <xsl:copy-of select="$legalNotice"/>
  	
    <authorgroup> 
      <author> 
		<firstname>Keith</firstname> 
		<surname>Isdale</surname> 
		<affiliation> 
	  		<orgname></orgname> 
	  		<address> <email>k_isdale@tpg.com.au</email> </address> 
		</affiliation> 
      </author> 

<!-- This is appropriate place for other contributors: translators,
     maintainers,  etc. Commented out by default.
     
      <othercredit role="translator">
		<firstname>Latin</firstname> 
		<surname>Translator 1</surname> 
		<affiliation> 
	  		<orgname>Latin Translation Team</orgname> 
	  		<address> <email>translator@gnome.org</email> </address> 
		</affiliation>
		<contrib>Latin translation</contrib>
      </othercredit>
-->
    </authorgroup>

    <revhistory>
      <revision> 
		<revnumber>xsldbg Manual V<xsl:value-of select="$manrevision"/></revnumber> 
		<date><xsl:value-of select="$date"/></date> 
		<revdescription> 
	  		<para role="author">Keith Isdale
	    	<email>k_isdale@tpg.com.au</email>
	  		</para>
	  		<para role="publisher"></para>
		</revdescription> 
      </revision>
    </revhistory> 
    <releaseinfo>
      This manual describes version <xsl:value-of select="$xsldbg_version"/> of xsldbg. It was
       last updated on <xsl:value-of select="$xsldbg_date"/>.
    </releaseinfo>

    <legalnotice> 
      <title>Feedback</title> 
      <para>To report a bug or make a suggestion regarding xsldbg application or this manual, follow the directions in the <ulink url="http://xsldbg.sourceforge.net"> xsldbg Feedback Page</ulink>. 
      </para>
<!-- Translators may also add here feedback address for translations -->
    </legalnotice> 

  </articleinfo>

  <indexterm zone="index">
    <primary>xsldbg</primary>
  </indexterm>

  </xsl:template>


</xsl:stylesheet>
