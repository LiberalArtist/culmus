// Copyright (C) 2012 by Maxim Iorsh <iorsh@users.sourceforge.net>.
// Distributed under the term of the GNU General Public License version 2.

package org.culmus.nakdan;

import java.lang.String;
import org.ivrix.hspell.*;

class BakalHaShimush implements NikudRule
{
    private final String prefix;

    public BakalHaShimush(String prefix_)
    {
	if (prefix_.equals(H.LAMED))
	    prefix = prefix_;
	else
	    prefix = prefix_ + H.DAGESH; // beth, kaf
    }

    public String apply(String base, HLingData ld, LexicalItem lex)
    {
	String result = "";

	if (base.length() < 2)
	{
	    return prefix + H.SHVA + base; // add prefix with shva
	}

	base = NikudRuleFactory.removeDagesh(base);

	// Prefix replaces he ha-yedia with the same diacritic
	if (ld.possessive == HLingData.PossessivePlus.Definite)
	{
	    result = prefix + base.substring(1);
	}
	// Before hataf - with corresponding full vowel
	else if (base.charAt(1) == H.hatafsegol)
	{
	    result = prefix + H.SEGOL + base;
	}
	else if (base.charAt(1) == H.hatafpatah)
	{
	    result = prefix + H.PATAH + base;
	}
	else if (base.charAt(1) == H.hatafqamats)
	{
	    result = prefix + H.QAMATS + base;
	}
	// Before yod+shva - with hiriq; yod rests
	else if (base.charAt(0) == H.yod && base.charAt(1) == H.shva)
	{
	    result = prefix + H.HIRIQ+H.YOD + base.substring(2);
	}
	// Before shva - with hiriq
	else if (base.charAt(1) == H.shva)
	{
	    result = prefix + H.HIRIQ + base;
	}
	// Default - with shva
	else
	{
	    result = prefix + H.SHVA + base;
	}

	return result;
    }
}
