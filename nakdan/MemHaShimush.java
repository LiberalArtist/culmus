// Copyright (C) 2012 by Maxim Iorsh <iorsh@users.sourceforge.net>.
// Distributed under the term of the GNU General Public License version 2.

package org.culmus.nakdan;

import java.lang.String;
import org.ivrix.hspell.*;

class MemHaShimush implements NikudRule
{
    public String apply(String base, HLingData ld, LexicalItem lex)
    {
	String result = "";

	if (base.length() < 2)
	{
	    return H.MEM+H.HIRIQ + base; // add mem with hiriq
	}

	// Remove dagesh in the first letter. It will be replaced by dagesh hazak.
	base = NikudRuleFactory.removeDagesh(base);

	// Exceptions: Before "hut" (thread), "huts" (outside) and "heyot" (being) - with hiriq
	if (base.charAt(0) == H.het && ld.stem.equals(H.HET+H.VAV+H.TET) ||
	    base.charAt(0) == H.het && ld.stem.equals(H.HET+H.VAV+H.FINALTSADI) ||
	    base.charAt(0) == H.he && ld.stem.equals(H.HE+H.YOD+H.VAV+H.TAV))
	{
	    result = H.MEM+H.HIRIQ + base;
	}
	// Before gronit - with tsere
	else if (base.charAt(0) == H.aleph || base.charAt(0) == H.he || base.charAt(0) == H.het ||
		 base.charAt(0) == H.ayin || base.charAt(0) == H.resh)
	{
	    result = H.MEM+H.TSERE + base;
	}
	// Before yod+shwa - with hiriq, shwa falls off
	else if (base.startsWith(H.YOD+H.SHVA))
	{
	    result = H.MEM+H.HIRIQ+H.YOD + base.substring(2);
	}
	// Otherwise add mem with hiriq and dagesh hazak afterwards
	else
	{
	    result = H.MEM+H.HIRIQ + base.substring(0,1) + H.DAGESH + base.substring(1);
	}

	return result;
    }
}
