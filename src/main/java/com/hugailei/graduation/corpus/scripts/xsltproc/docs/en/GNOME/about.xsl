<?xml version="1.0" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:template name="about_section">
    <xsl:comment>============= About ================================ </xsl:comment><xsl:text>
</xsl:text>
    <sect1 id="about_section">
       <title>About xsldbg</title>
  <para>
   <application>xsldbg</application> was written by Keith Isdale
   (<email>k_isdale@tpg.com.au</email>). To find more information about
   <application>xsldbg</application>, please visit the <ulink
   url="http://xsldbg.sourceforge.net" type="http">xsldbg Web
   page</ulink>.  
  </para>
  <para>
    If you have some comments or suggestions regarding this
   application, need help, or want to report a bug, plese contact us!
   via <ulink type="http"
   url="http://sourceforge.net/tracker/?group_id=35673&amp;atid=415039"><citetitle>Reporting xsldbg bugs and other
   feedback</citetitle></ulink>
  </para>

    <para> This program is distributed under the terms of the GNU
      General Public license as published by the Free Software
      Foundation; either version 2 of the License, or (at your option)
      any later version. A copy of this license can be found at this
      <ulink url="ghelp:gpl" type="help">link</ulink>, or in the file
      COPYING included with the source code of this program. </para>


  <xsl:comment>For translations: uncomment this:

  <para>
   Latin translation was done by ME
   (<email>MYNAME@MYADDRESS</email>). Please send all  comments  and
   suggestions regarding this translation to SOMEWHERE.
  </para>

   </xsl:comment>

      <sect2>
        <title>Thanks to </title>
        <para> The writers the libxml and libxsl.</para>
      </sect2>

    </sect1>


  </xsl:template>

</xsl:stylesheet>
