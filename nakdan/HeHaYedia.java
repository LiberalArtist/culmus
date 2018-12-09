// Copyright (C) 2012 by Maxim Iorsh <iorsh@users.sourceforge.net>.
// Distributed under the term of the GNU General Public License version 2.

package org.culmus.nakdan;

import java.lang.String;
import org.ivrix.hspell.*;

class HeHaYedia implements NikudRule
{
    public String apply(String base, HLingData ld, LexicalItem lex)
    {
	String result = "";

	if (base.length() < 2)
	{
	    return H.HE+H.PATAH + base; // add he with patah
	}

	// Remove dagesh (probably kal), it will be brought back unconditionally as hazak.
	base = NikudRuleFactory.removeDagesh(base);

	// Before het with qamats, hataf qamats - he with segol
	if (base.charAt(0) == H.het &&
	    (base.charAt(1) == H.qamats || base.charAt(1) == H.hatafqamats))
	{
	    result = H.HE+H.SEGOL + base;
	}
	// Before he, ayin with qamats - check stress
	else if ((base.charAt(0) == H.he || base.charAt(0) == H.ayin) &&
	    base.charAt(1) == H.qamats)
	{
	    if (lex.stress == LexicalItem.Stress.FirstSyllable)
		result = H.HE+H.QAMATS + base;
	    else
		result = H.HE+H.SEGOL + base;
	}
	// Before he or het without qamats - he with patah
	else if ((base.charAt(0) == H.he || base.charAt(0) == H.het) &&
	    base.charAt(1) != H.qamats)
	{
	    result = H.HE+H.PATAH + base;
	}
	// Before aleph, ayin, resh - he with qamats
	else if (base.charAt(0) == H.aleph || base.charAt(0) == H.ayin || base.charAt(0) == H.resh)
	{
	    result = H.HE+H.QAMATS + base;
	}
	// Default - he with patah and dagesh hazaq in first base letter
	else
	{
	    result = H.HE+H.PATAH + base.substring(0,1) + H.DAGESH + base.substring(1);
	}

	return result;
    }
}
