/**************************************************************************
                          debugXSL.h  -  describes the core xsldbg shell functions
                             -------------------
    begin                : Sun Sep 16 2001
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
 * Orinal file : debugXML.h : This is a set of routines used for
 *    debugging the tree   produced by the XML parser.
 *
 * New file : debugXSL.h : Debug support version
 *
 * See Copyright for the status of this software.
 *
 * Daniel Veillard <daniel@veillard.com>
 *
 * Permission abtained to modify the LGPL'd code and extend to include 
 *   break points, inspections of stylesheet source, xml data, stylesheet
 *    variables   Keith Isdale <k_isdale@tpg.com.au>
 */
/* We want skip most of these includes when building documentation*/
/* how may items have been printed */
    extern int printCount;

  /* used to indicated that xsldbg should stop tracing/walking 
     value : 1 stop tracing at start of next cycle
     value : 0 normal operation 
  */

  extern int xsldbgStop;
  extern int xsldbgValidateBreakpoints;

/****************************************************************
 *								*
 *	 The XSL shell related structures and functions		*
 *								*
 ****************************************************************/

/*
   Note that functions that have a prefix of xslDbgShell are NOT implemented
      in debugXSL.c unless stated

   All functions with the prefix of debygXSL are implemented in debugXSL.c

 */
/**
 * A break point has been found so pass control to user
 *
 * @param templ The source node being executed
 * @param node The data node being processed
 * @param root The template being applied to "node"
 * @param ctxt transform context for stylesheet being processed
 */


    void debugXSLBreak(xmlNodePtr templ, xmlNodePtr node,
                       xsltTemplatePtr root, xsltTransformContextPtr ctxt);
/** 
 * Get the last template node found, if any
 *
 * @returns the last template node found, if any
 */


    xsltTemplatePtr debugXSLGetTemplate(void);



/* -----------------------------------------
   Break Point related commands

   They are implemented in breakpoint_cmds.c
  ------------------------------------------- */
/**
 * Set a "frame" break point either up or down from here
 *
 * @returns 1 on success,
 *          0 otherwise
 *
 * @param arg Is valid  and in UTF-8
 * @param stepup If != 1 then we step up, otherwise step down
 */


    int xslDbgShellFrameBreak(xmlChar * arg, int stepup);
/**
 * Add break point specified by arg
 *
 * @returns 1 on success,
 *          0 otherwise
 *
 * @param arg Is valid and in UTF-8
 * @param style Is valid
 * @param ctxt Is valid
 */


    int xslDbgShellBreak(xmlChar * arg, xsltStylesheetPtr style,
                         xsltTransformContextPtr ctxt);
/**
 * Delete break point specified by arg
 *
 * @returns 1 on success,
 *          0 otherwise
 *
 * @param arg Is valid and in UTF-8
 */


    int xslDbgShellDelete(xmlChar * arg);
/**
 * Enable/disable break points via use of scan of break points
 *
 * @param payload Is valid xslBreakPointPtr
 * @param data Enable type, a pointer to an integer 
 *         for a value of 
 *              @li   1 enable break point
 *              @li   0 disable break point
 *              @li   -1 toggle enabling of break point 
 * @param name Not used
*/


    void xslDbgShellEnableBreakPoint(void *payload, void *data,
                                     xmlChar * name);
  /**
   * Enable/disable break point specified by arg using enable
   *
   * @param arg: is valid enable "commmand text" and in UTF-8
   * @param enableType : enable break point if 1, disable if 0, toggle if -1
   *
   * @returns 1 if successful,
   *          0 otherwise
   */


    int xslDbgShellEnable(xmlChar * arg, int enableType);
/**
 * Print data given by scan of break points 
 *
 * @param payload Is valid xslBreakPointPtr
 * @param data Not used
 * @param name Not used
*/


    void xslDbgShellPrintBreakPoint(void *payload, void *data,
                                    xmlChar * name);
/**
 * Print an warning if a breakpoint is invalid
 *
 * @param payload Is valid xslBreakPointPtr
 * @param data Not used
 * @param name Not used
*/


    void xslDbgShellValidateBreakPoint(void *payload, void *data,
                                    xmlChar * name);


/* -----------------------------------------
   Template related commands

   They are implemented in template_cmds.c
  ------------------------------------------- */
/**
 * Print stylesheets that can be found in loaded stylsheet
 *
 * @returns 1 on success,
 *          0 otherwise
 *
 * @param arg The stylesheets of interests and in UTF-8, is NULL for all stylesheets
 *
 */


    int xslDbgShellPrintStyleSheets(xmlChar * arg);
/** 
 * Print out the list of template names found that match critieria   
 *
 * @param styleCtxt Is valid 
 * @param ctxt Not used
 * @param arg Not used
 * @param verbose If 1 then print extra messages about templates found,
 *            otherwise print normal messages only 
 * @param allFiles If 1 then look for all templates in stylsheets found in 
 *                 @p styleCtxt
 *             otherwise look in the stylesheet found by 
 *                 debugXSLBreak function
 * @returns 1 on success,
 *          0 otherwise
 */


    int xslDbgShellPrintTemplateNames(xsltTransformContextPtr styleCtxt,
                                      xmlShellCtxtPtr ctxt,
                                      xmlChar * arg, int verbose,
                                      int allFiles);




/* -----------------------------------------

   Node viewing related commands

  ------------------------------------------- */
/**
 * Print list of nodes in either ls or dir format
 *
 * @returns 1 on success,
 *          0 otherwise
 *
 * @param ctxt The current shell context
 * @param arg What xpath to display  and in UTF-8
 * @param dir If 1 print in dir mode, 
 *        otherwise ls mode
 */


    int xslDbgShellPrintList(xmlShellCtxtPtr ctxt, xmlChar * arg, int dir);
/** 
 * Print the result of an xpath expression. This can include variables
 *        if styleCtxt is not NULL
 *
 * @returns 1 on success,
 *          0 otherwise
 *
 * @param styleCtxt Current stylesheet context
 * @param ctxt Current shell context
 * @param arg The xpath to print and in UTF-8
 */


    int xslDbgShellCat(xsltTransformContextPtr styleCtxt,
                       xmlShellCtxtPtr ctxt, xmlChar * arg);
/**
 *  Print the value variable specified by args.
 *
 * @returns 1 on success,
 *          0 otherwise

 *
 * @param styleCtxt The current stylesheet context 
 * @param arg The name of variable to look for '$' prefix is optional and in UTF-8
 * @param type Is valid VariableTypeEnum
 */


    int xslDbgShellPrintVariable(xsltTransformContextPtr styleCtxt,
                                 xmlChar * arg, VariableTypeEnum type);


/* -----------------------------------------

   File related command

   Implemented in file_cmds.c
  ------------------------------------------- */
/**
 * xslDbgShellOutput:
 * @arg : Is valid, either a local file name which will be expanded 
 *        if needed, or a "file://" protocol URI
 *
 * Set the output file name to use
 *
 * Returns 1 on success, 
 *         0 otherwise
 */


  int xslDbgShellOutput(xmlChar *arg);
    int xslDbgEntities(void);
  /**
   * Print what a system file @p arg maps to via the current xml catalog
   *
   * @param arg Is valid in UTF-8
   * 
   * @returns 1 on sucess,
   *          0 otherwise
   */


    int xslDbgSystem(const xmlChar * arg);
  /**
   * Print what a public ID @p arg maps to via the current xml catalog
   *
   * @param arg Is valid PublicID in UTF-8
   * 
   * @returns 1 on sucess,
   *          0 otherwise
   */


    int xslDbgPublic(const xmlChar * arg);
  /**
   * Set current encoding to use for output to standard output
   *
   * @param arg Is valid encoding supported by libxml2
   *
   *
   * Returns 1 on sucess,
   */


    int xslDbgEncoding(xmlChar * arg);

/* -----------------------------------------

   Operating system related commands

   Implemented in os_cmds.c
  ------------------------------------------- */
/**
 * @returns 1 if able to change xsldbg working direcorty to @p path
 *          0 otherwise
 *
 * @param path Operating system path(directory) to change to and in UTF-8 
 */


    int xslDbgShellChangeWd(xmlChar * path);
/**
 * @returns 1 if able to execute command @p name,
 *          0 otherwise
 *
 * @param name The name of command string to be executed 
 *            by operating system shell
 * @param verbose If 1 then print extra debugging messages,
 *            normal messages otherwise
 */


    int xslDbgShellExecute(xmlChar * name, int verbose);




/* -----------------------------------------

   libxslt parameter related commands

   Implemented in param_cmds.c
  ------------------------------------------- */
/* 
 * Add a parameter to be sent to libxslt later on
 *
 * @returns 1 on success,
 *          0 otherwise
 *
 * @param arg A string comprised of two words separated by
 *          one or more spaces which are in UTF-8
 */


    int xslDbgShellAddParam(xmlChar * arg);
/**
 * Delete a libxslt parameter that was to be sent to libxslt later on
 *
 * @returns 1 if able to delete parameter @p name,
 *          0 otherwise
 *
 * @param arg A single white space trimmed libxslt parameter number to look for
 */


    int xslDbgShellDelParam(xmlChar * arg);
/**
 * Print list of current paramters
 *
 * @returns 1 on success,
 *         0 otherwise
 *
 * @param arg Not used
 */


    int xslDbgShellShowParam(xmlChar * arg);


    /* -----------------------------------------
     * 
     * Option related commands
     * 
     * Implemented in option_cmds.c
     * 
     * ------------------------------------------- */
/**
 * Set the value of an option 
 *
 * @returns 1 on success,
 *          0 otherwise
 *
 * @param arg is valid, and in format   <NAME> <VALUE>
 * 
 */


    int xslDbgShellSetOption(xmlChar * arg);
/**
 * Prints out values for user options
 *
 * @returns 1 on success,
 *          0 otherwise
 */


    int xslDbgShellOptions(void);


  /**
   * xslDbgShellShowWatches:
   * @styleCtxt: the current stylesheet context
   * @ctxt: The current shell context
   * @showWarnings : If 1 then showWarning messages,
   *                 otherwise do not show warning messages
   *
   * Print the current watches and their values
   *
   * Returns 1 on success,
   *         0 otherwise
   */
  int xslDbgShellShowWatches(xsltTransformContextPtr styleCtxt,
                               xmlShellCtxtPtr ctx,int showWarnings);

  /**
   * xslDbgShellAddWatch:
   * @arg : A valid xPath of expression to watch the value of
   *
   * Add expression to list of expressions to watch value of
   *
   * Returns 1 on success,
   *         0 otherwise   
   */
  int xslDbgShellAddWatch(xmlChar* arg);

  /**
   * xslDbgShellDeleteWatch:
   * @arg : A watch ID to remove
   *
   * Delete a given watch ID from our list of expressions to watch
   *
   * Returns 1 on success,
   *         0 otherwise
   */
  int xslDbgShellDeleteWatch(xmlChar* arg);


    /* -----------------------------------------
     * 
     * Tracing, walking related commands
     * 
     * Implemented in shell.c
     * 
     * ------------------------------------------- */
/**
 * Start the tracing of the stylesheet. First need to restart it.
 *
 * @returns 1 on success,
 *          0 otherwise
 *
 * @param arg Not used
 */


    int xslDbgShellTrace(xmlChar * arg);
/**
 * Start walking through the stylesheet.
 *
 * @returns 1 on success,
 *          0 otherwise
 *
 * @param arg An interger between 0 and 9 indicate the speed of walk
 */


    int xslDbgShellWalk(xmlChar * arg);



    /* -----------------------------------------
     * 
     * Seach related commands
     * 
     * Implemented in search_cmds.c
     * ------------------------------------------- */
/**
 * @returns 1 if able to run query with @p arg, 
 *          0 otherwise
 *
 * @param styleCtxt Is valid
 * @param style Is valid
 * @param arg The xpath query to use for searching dataBase
 */


    int xslDbgShellSearch(xsltTransformContextPtr styleCtxt,
                          xsltStylesheetPtr style, xmlChar * arg);


/* -----------------------------------------
   
   Seach related commands

   Implemented in variable_cmds.c
  ------------------------------------------- */
/**
 * Change the value of a global or local variable
 *
 * @param styleCtxt Is valid
 * @param arg Is valid must be in the format of 
 *         <VARIABLE_NAME> <XPATH>
 * 
 * @returns 1 on success,
 *          0 otherwise
 */



    int xslDbgShellSetVariable(xsltTransformContextPtr styleCtxt,
                               xmlChar * arg);





 /* __DEBUG_XSL__ */
