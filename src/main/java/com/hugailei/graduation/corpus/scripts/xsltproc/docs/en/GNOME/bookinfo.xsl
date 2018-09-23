<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:template name="book_info">
    <bookinfo>
      <title>Xsldbg <xsl:value-of select="$xsldbg_version"/></title>
      <subtitle>Command and usage documentation</subtitle>
      <authorgroup>
        <author>   
          <firstname>Keith</firstname>
          <surname>Isdale</surname>
          <affiliation>
            <address><email>k_isdale@tpg.com.au</email></address>
          </affiliation>
        </author>
      </authorgroup>

      <!-- TRANS:ROLES_OF_TRANSLATORS -->

      <copyright>
        <year>2001</year>
        <holder>Keith Isdale</holder>
      </copyright>

    <!-- Translators: put here the copyright notice of the translation -->
    <!-- Put here the FDL notice.  Read the explanation in fdl-notice.docbook 
         and in the FDL itself on how to use it. -->

    <xsl:element name="legalnotice"><xsl:text disable-output-escaping="yes">&amp;FDLNotice;</xsl:text>
    </xsl:element>

    <!-- Date and version information of the documentation
         Don't forget to include this last date and this last revision number, we
         need them for translation coordination !
         Please respect the format of the date (DD/MM/YYYY) and of the version
         (V.MM.LL), it could be used by automation scripts.
         Do NOT change these in the translation. -->
    
      <date>13/12/2001</date>
      <releaseinfo><xsl:value-of select="$xsldbg_version"/></releaseinfo>
      <abstract>
        <para>
xsldbg is a tool intended to help understand stylesheets. 
What makes it different to other stylesheet debuggers is the ability 
to search for items of interest and trace stylesheet execution. You can
use the test framework in the tests directory to describe automatic tests
to be done on the output of stylesheet execution/debugging. This will be 
useful for quality assurance purposes. Also a good deal of effort has 
been put into creating a xemacs and KDbg front ends to xsldbg.
      </para>
      </abstract>
    </bookinfo>
  </xsl:template>


</xsl:stylesheet>
