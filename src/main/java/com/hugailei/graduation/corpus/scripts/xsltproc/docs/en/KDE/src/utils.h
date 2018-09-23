/**************************************************************************
                          utils.c  -  declaration for misc utils this is
                                       mixed bag of functions so it is
                                       not realy a module hense no need
                                       for a utils prefix its functions 

                             -------------------
    begin                : Thur Jan 31 2002
    copyright            : (C) 2001 by Keith Isdale
    email                : k_isdale@tpg.com.au
 **************************************************************************/

/**************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 **************************************************************************/
/* 
  Make things simpler when working between char* and xmlChar*  .
   By definition a char is the same size as an xmlChar(unsigned char). 
*/
/* what char is use to separate directories in an URI*/


    /* Handle the differences in path and quote character between
     * win32 and *nix systems */
/* JRF: Although RISC OS native paths use . as a separator, the arguments
        to xsldbg are passed in unix or URI form, and thus the above
        specification is correct. */



/**
 * IS_BLANK:
 * @c:  an UNICODE value (int)
 *
 * Macro to check the following production in the XML spec
 *
 * [3] S ::= (#x20 | #x9 | #xD | #xA)+
 */
/**
 * Remove leading and trailing spaces off @p text
 *         stores result back into @p text
 *
 * @returns 1 on success,
 *          0 otherwise
 *
 * @param text A valid string with leading or trailing spaces
 */


    int trimString(xmlChar * text);
/**
 * Spit string by white space and put into @p out
 * 
 * @returns 1 on success,
 *          0 otherwise
 *
 * @param textIn The string to split
 * @param maxStrings The max number of strings to put into @p out
 * @param out Is valid and at least the size of @p maxStrings
 */


    int splitString(xmlChar * textIn, int maxStrings, xmlChar ** out);
/** 
 * Lookup and name in a list
 *
 *
 * @returns The id of name found in @p matchList
 *         0 otherwise
 *
 * @param name Is valid
 * @param matchList A NULL terminated list of names to use as lookup table
 *
*/


    int lookupName(xmlChar * name, xmlChar ** matchList);
/**
 * Join nameURI to name
 *
 * @returns a copy of "nameURI:name"

 * fullQName:
 * @param nameURI : QName part of name
 * @param name : Local part of name 
 *
 *
 */



  xmlChar * fullQName(const xmlChar* nameURI, const xmlChar * name);
