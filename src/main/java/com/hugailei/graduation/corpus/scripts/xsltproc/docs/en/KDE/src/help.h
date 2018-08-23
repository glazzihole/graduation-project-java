/***************************************************************************
                          help.h  -  describe the help support functions
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
 * Provide a help system for user 
 *
 * @short help system support
 *
 * @author Keith Isdale <k_isdale@tpg.com.au> 
 */
/**
 * Display help about the command in @p arg
 *
 *
 *  This is a platform specific interface
 *
 * @param args Is valid or empty string
 * @returns 1 on success,
 *          0 otherwise
 */


    int helpTop(const xmlChar * args);
