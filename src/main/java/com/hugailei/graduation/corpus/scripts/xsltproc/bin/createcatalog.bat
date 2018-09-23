Rem Create xml catalog for xsldbg
@echo create xml catalog for xsldbg
md c:\etc
md c:\etc\xml
set XSLDBG_CATALOG_FILES="C:\Program Files\xsldbg\docs\en\catalog_win32"
set XSLDBG_CATALOG_FILES_URI="file:///C%%3A/Program%%20Files/xsldbg/docs/en/catalog_win32"

pause Do you wish to create new catalog %XSLDBG_CATALOG_FILES%
xmlcatalog -v --noout --create %XSLDBG_CATALOG_FILES%

xmlcatalog -v --noout --add public "-//xsldbg//DTD search XML V1.0//EN" "search.dtd" %XSLDBG_CATALOG_FILES%
xmlcatalog -v --noout --add public "-//xsldbg//DTD search XML V1.1//EN" "search_v1_1.dtd" %XSLDBG_CATALOG_FILES%
xmlcatalog -v --noout --add public "-//xsldbg//DTD xsldoc XML V1.0//EN" "xsldoc.dtd" %XSLDBG_CATALOG_FILES%
xmlcatalog -v --noout --add public "-//xsldbg//DTD config XML V1.0//EN" "config.dtd" %XSLDBG_CATALOG_FILES%

set XML_CATALOG_FILES="C:\etc\xml\catalog"
pause Do you wish to create new catalog %XML_CATALOG_FILES% 
xmlcatalog -v --noout --create %XML_CATALOG_FILES%

xmlcatalog -v --noout --add nextCatalog %XSLDBG_CATALOG_FILES_URI% "" %XML_CATALOG_FILES%

set XML_CATALOG_FILES=file:///C%%3A/etc/xml/catalog


