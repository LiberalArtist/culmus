// Copyright (C) 2012 by Maxim Iorsh <iorsh@users.sourceforge.net>.
// Distributed under the term of the GNU General Public License version 2.

package org.culmus.nakdan;

import java.util.*;
import org.ivrix.hspell.*;

public class TestNakdan
{
    public static void main(String [] args)
    {
	// shalom (en. peace)
	check(H.SHIN+H.LAMED+H.VAV+H.FINALMEM, H.SHIN+H.QAMATS+H.SHINDOT+H.LAMED+H.VAV+H.HOLAM+H.FINALMEM);
	// bitsa (en. swamp)
	check(H.BETH+H.YOD+H.TSADI+H.HE, H.BETH+H.HIRIQ+H.DAGESH+H.TSADI+H.QAMATS+H.DAGESH+H.HE);
	// ledavar (en. for a thing)
	check(H.LAMED+H.DALET+H.BETH+H.RESH, H.LAMED+H.SHVA+H.DALET+H.QAMATS+H.BETH+H.QAMATS+H.RESH);
	// ladavar (en. for the thing)
	check(H.LAMED+H.DALET+H.BETH+H.RESH, H.LAMED+H.PATAH+H.DALET+H.DAGESH+H.QAMATS+H.BETH+H.QAMATS+H.RESH);
	// sipim (en. for pl. edges)
	check(H.SAMECH+H.PE+H.YOD+H.FINALMEM, H.SAMECH+H.HIRIQ+H.PE+H.HIRIQ+H.DAGESH+H.YOD+H.FINALMEM);
        // einha (en. not you, m.)
        check(H.ALEPH+H.YOD+H.NUN+H.FINALKAF, H.ALEPH+H.TSERE+H.YOD+H.NUN+H.SHVA+H.FINALKAF+H.QAMATS);
        // eineh (en. not you, f.)
        check(H.ALEPH+H.YOD+H.NUN+H.FINALKAF, H.ALEPH+H.TSERE+H.YOD+H.NUN+H.TSERE+H.FINALKAF+H.SHVA);
        // shtaim (en. two, f.)
        check(H.SHIN+H.TAV+H.YOD+H.YOD+H.FINALMEM, H.SHIN+H.SHINDOT+H.SHVA+H.TAV+H.DAGESH+H.PATAH+H.YOD+H.HIRIQ+H.FINALMEM);
    }

    private static void check(String aTerm, String menukad)
    {
	Vector<CulmusNakdan.Entry> entries = CulmusNakdan.queryMeanings(aTerm);

	for (CulmusNakdan.Entry entry : entries)
        {
	    if (entry.values != null)
	    {
		for (CulmusNakdan.Value value : entry.values)
		{
		    if (value.menukad.equals(menukad))
		    {
			System.out.println("PASS: " + aTerm + " -> " + menukad);
			return;
		    }
		}
	    }
	}

	System.out.println("FAIL: " + aTerm + " -> " + menukad);

	for (CulmusNakdan.Entry entry : entries)
        {
	    if (entry.values != null)
	    {
		for (CulmusNakdan.Value value : entry.values)
		{
		    System.out.println("  Value: " + value.menukad + " " + stringToHex(value.menukad));
		}
	    }
	}
    }

    static String stringToHex(String str)
    { 
	char[] chars = str.toCharArray();
	StringBuffer strBuffer = new StringBuffer();

	for (int i = 0; i < chars.length; i++)
	{
	    strBuffer.append(Integer.toHexString((int) chars[i]));
	    strBuffer.append(" ");
	}
	return strBuffer.toString();
    }
}
