/***************************************************************************
                          xsldbg.h  - describe the application level functions
                             -------------------
    begin                : Sun Sep 16 2001
    copyright            : (C) 2001 by Keith Isdale
    email                : k_isdale@tpg.com.au
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/







/**
 * Provide provide application level services and useful bits and pieces
 *
 * @short application functions and useful bits and pieces
 *
 * @author Keith Isdale <k_isdale@tpg.com.au> 
 */



/*
 * xsltconfig.h: compile-time version informations for the XSLT engine
 *
 * See Copyright for the status of this software.
 *
 * daniel@veillard.com
 */
/**
 * LIBXSLT_DOTTED_VERSION:
 *
 * the version string like "1.2.3"
 */


/**
 * LIBXSLT_VERSION:
 *
 * the version number: 1.2.3 value is 1002003
 */


/**
 * LIBXSLT_VERSION_STRING:
 *
 * the version number string, 1.2.3 value is "1002003"
 */


/**
 * WITH_XSLT_DEBUG:
 *
 * Activate the compilation of the debug reporting. Speed penalty
 * is insignifiant and being able to run xsltpoc -v is useful. On
 * by default unless --without-debug is passed to configure
 */
/**
 * WITH_XSLT_DEBUGGER:
 *
 * Activate the compilation of the debugger support. Speed penalty
 * is insignifiant.
 * On by default unless --without-debugger is passed to configure
 */






/**
 * ATTRIBUTE_UNUSED:
 *
 * This macro is used to flag unused function parameters to GCC
 */
/**
 * LIBXSLT_PUBLIC:
 *
 * This macro is used to declare PUBLIC variables for Cygwin and for MSC on Windows
 */
/**
 * ATTRIBUTE_UNUSED:
 *
 * This macro is used to flag unused function parameters to GCC
 */
/* We want skip most of these includes when building documentation */
/**
 * Load the stylesheet and return it 
 *
 * @returns The stylesheet after reloading it if successful
 *         NULL otherwise
 */


    xsltStylesheetPtr xsldbgLoadStylesheet(void);
/**
 * Load the xml data file and return it  
 *
 * @returns The stylesheet after reloading it if successful
 *         NULL otherwise
 */


    xmlDocPtr xsldbgLoadXmlData(void);
/**
 * Load the temporary data file and return it 
 *
 * @returns The temporary file after reloading it if successful,
 *         NULL otherwise
 * @param path The name of temporary file to loa
 */


    xmlDocPtr xsldbgLoadXmlTemporary(const xmlChar * path);
