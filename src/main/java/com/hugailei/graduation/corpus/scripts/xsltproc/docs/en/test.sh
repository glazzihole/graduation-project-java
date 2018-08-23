#! sh
	if test "x`grep xsldbg/catalog.xml /etc/xml/catalog`" = "x" 
	then	
	  xmlcatalog -v --noout --add nextCatalog "file://$(HTML_DIR)/xsldbg/catalog.xml" "" /etc/xml/catalog 
	else	
	  echo "skipping adition of xsldbg catalog as it is already present" 
	fi;