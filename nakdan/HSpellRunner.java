// Copyright (C) 2012 by Maxim Iorsh <iorsh@users.sourceforge.net>.
// Distributed under the term of the GNU General Public License version 2.

package org.ivrix.hspell;

import java.util.List;
import java.io.*;
import java.util.ArrayList;

public class HSpellRunner
{
    public List<HSplit> EnumSplits(String word)
    {
	String inp;

	try
	{
	    Process hspell = Runtime.getRuntime().exec(new String[]{"hspell", "-l"});
	    OutputStream hout = hspell.getOutputStream();
	    InputStream hin = hspell.getInputStream();

	    hout.write(word.getBytes("ISO-8859-8"));
	    hout.close();
	    hspell.waitFor();

	    byte[] inbuffer = new byte[hin.available()];
	    hin.read(inbuffer);
	    inp = new String(inbuffer, "ISO-8859-8");
	}
	catch (java.lang.Throwable e)
	{
	    e.printStackTrace();
	    return null;
	}

	String[] lines = inp.split("\n");
	HSplit curSplit = null;
	List<HSplit> splits = new ArrayList();

	for (String line : lines)
	{
	    if (line.length() == 0)
	    {
		continue;
	    }
	    else if (line.charAt(0) == H.shin)
	    {
		break; // Shgia (erroneous word)
	    }
	    else if (line.charAt(0) == H.mem)
	    {
		String baseword = line.split(": ", 2)[1];
		curSplit = new HSplit(word, baseword, 0, 0);
		splits.add(curSplit);
	    }
	    else if (line.charAt(0) == H.tsadi)
	    {
		String word_w_prefix = line.split(": ", 2)[1];
		String prefix = word_w_prefix.split("\\+", 2)[0];
		String baseword = word_w_prefix.split("\\+", 2)[1];

		curSplit = new HSplit(word, baseword, prefix.length(), 0);
		splits.add(curSplit);
	    }
	    else if (line.charAt(0) == '\t')
	    {
		line = line.trim();
		String stem = line.split("[()]", 3)[0];
		String desc = line.split("[()]", 3)[1];

		curSplit.lingdata.add(new HLingData(stem, desc, 0));
	    }
	}

	return splits;
    }
}
