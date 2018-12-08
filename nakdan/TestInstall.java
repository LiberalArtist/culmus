// Copyright (C) 2012 by Maxim Iorsh <iorsh@users.sourceforge.net>.
// Distributed under the term of the GNU General Public License version 2.

package org.culmus.nakdan;

import java.io.*;
import org.ivrix.hspell.*;

public class TestInstall
{
    // Return error description for specific recognized errors or null when everything is ok.
    public static String TestEnvironment()
    {
	// Test hspell executable
	try
	{
	    Process hspell = Runtime.getRuntime().exec(new String[]{"hspell", "-vv"});
	    InputStream hin = hspell.getInputStream();
	    hspell.waitFor();

	    byte[] inbuffer = new byte[hin.available()];
	    hin.read(inbuffer);
	    String verInfo = new String(inbuffer, "ISO-8859-8");

	    if (!verInfo.contains("LINGINFO"))
	    {
		return "Error: 'hspell' compiled without --enable-linginfo option";
	    }
	}
	catch (java.io.IOException e)
	{
	    return "Error: 'hspell' missing";
	}
	catch (java.lang.InterruptedException e)
	{
	    return "Error: java.lang.InterruptedException throwed";
	}
/*
	// Test for presence of "libhspellj.so" library
	try
	{
	    System.loadLibrary("hspellj");
	}
	catch (java.lang.UnsatisfiedLinkError e)
	{
	    return "Error: Native library \"libhspellj.so\" is missing";
	}

	// Test for presence of "HSpellWrapper.jar" library
	try
	{
	    HSpellWrapper hspell = HSpellWrapper.getInstance();
            if (!hspell.isValid())
            {
		return "Error: Failed to initialize hspell. Please check hspell installation";
            }
	}
	catch (java.lang.NoClassDefFoundError e)
	{
	    return "Error: Java library \"HSpellWrapper.jar\" is missing";
	}
*/

	// Read dictionary
	if (DictionaryDataReader.getInstance() == null)
	{
	    return "Error: Data file \"nakdan.txt\" is missing";
	}

	return null;
    }
}
