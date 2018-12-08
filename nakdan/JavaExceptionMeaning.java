// Copyright (C) 2012 by Maxim Iorsh <iorsh@users.sourceforge.net>.
// Distributed under the term of the GNU General Public License version 2.

package org.culmus.ooo.xmeaningimpltd;
 
import com.sun.star.uno.RuntimeException;
import com.sun.star.linguistic2.XMeaning;
import java.lang.*;
import java.io.*;
 
public class JavaExceptionMeaning implements
    com.sun.star.linguistic2.XMeaning
{
    Throwable   exception;
 
    public JavaExceptionMeaning (Throwable e)
    {
	exception = e;
    }

    public String getMeaning()
	throws com.sun.star.uno.RuntimeException
    {
	return exception.toString();
    }

    public String[] querySynonyms()
	throws com.sun.star.uno.RuntimeException
    {
	StringWriter writer = new StringWriter();
	exception.printStackTrace(new PrintWriter(writer));

	String stackbuffer = writer.toString();

	return stackbuffer.split("\n");
    }
};

