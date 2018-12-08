// Copyright (C) 2012 by Maxim Iorsh <iorsh@users.sourceforge.net>.
// Distributed under the term of the GNU General Public License version 2.

package org.culmus.nakdan;

import java.lang.String;
import org.ivrix.hspell.*;

class ShinHaZika implements NikudRule
{
    public String apply(String base, HLingData ld)
    {
	String result = "";

	if (base.length() < 2)
	{
	    return H.SHIN+H.SHINDOT+H.SEGOL + base; // add shin with segol
	}

	// Remove dagesh (probably kal), it will be brought back unconditionally as hazak.
	base = NikudRuleFactory.removeDagesh(base);

	// Before gronit - with segol
	if (base.charAt(0) == H.aleph || base.charAt(0) == H.he || base.charAt(0) == H.het ||
		 base.charAt(0) == H.ayin || base.charAt(0) == H.resh)
	{
	    result = H.SHIN+H.SHINDOT+H.SEGOL + base;
	}
	// Default - shin with segol and dagesh hazaq in first base letter
	else
	{
	    result = H.SHIN+H.SHINDOT+H.SEGOL + base.substring(0,1) + H.DAGESH + base.substring(1);
	}

	return result;
    }
}
