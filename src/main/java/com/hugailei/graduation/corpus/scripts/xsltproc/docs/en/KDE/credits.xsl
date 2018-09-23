<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:template match="credit_chapter" name="credit_chapter">
    <chapter id="credits_chapter">
      <title>Credits and Licence</title> 
	<section>
	<title>Licence</title>
      <xsl:text disable-output-escaping="yes">&amp;underFDL;</xsl:text> <!-- FDL: do not remove -->
      <xsl:text disable-output-escaping="yes">&amp;underGPL;</xsl:text> <!-- FDL: do not remove -->
	</section>

      <section id="thanks">
        <title>Thanks to </title>
        <para> The writers the libxml and libxsl.</para>
        <para> <ulink url="http://members.nextra.at/johsixt/">Johannes Sixt</ulink> for helping with adding xsldbg support to KDbg</para>
      </section>

    </chapter>
  </xsl:template>

</xsl:stylesheet>
