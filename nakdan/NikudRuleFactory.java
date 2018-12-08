// Copyright (C) 2012 by Maxim Iorsh <iorsh@users.sourceforge.net>.
// Distributed under the term of the GNU General Public License version 2.

package org.culmus.nakdan;

import java.lang.String;
import java.lang.StringBuffer;
import org.ivrix.hspell.*;

public class NikudRuleFactory
{
    private static NikudRule prefixRule(String base /*in*/, StringBuffer prefixes /*in-out*/)
    {
	String prefix = prefixes.substring(prefixes.length() - 1);

	// Cut off the last prefix, which we are going to process now.
	prefixes.delete(prefixes.length() - 1, prefixes.length());

	if (prefix.equals(H.VAV))
	    return new VavHaHibur();
	else if (prefix.equals(H.HE))
	    return new HeHaYedia();
	else if (prefix.equals(H.BETH) || prefix.equals(H.KAF) || prefix.equals(H.LAMED))
	    return new BakalHaShimush(prefix);
	else if (prefix.equals(H.MEM))
	    return new MemHaShimush();
	else if (prefix.equals(H.SHIN))
	    return new ShinHaZika();

	return new EmptyRule();
    }

    public static String addPrefix(String base, String prefix, HLingData ld)
    {
	String result = base;
	StringBuffer curPrefix = new StringBuffer(prefix);

	while (curPrefix.length() > 0)
	{
	    NikudRule rule = prefixRule(result, curPrefix);
	    result = rule.apply(result, ld);
	}

	return result;
    }

    public static String removeDagesh(String base)
    {
	return removeDagesh(base, false);
    }

    public static String removeDagesh(String base, boolean remove_kal_only)
    {
	if (remove_kal_only &&
	    base.charAt(0) != H.beth && base.charAt(0) != H.gimel && base.charAt(0) != H.dalet &&
	    base.charAt(0) != H.kaf  && base.charAt(0) != H.pe    && base.charAt(0) != H.tav)
	    return base;

	// Dagesh after consonant.
	if (base.charAt(1) == H.dagesh)
	    return base.substring(0,1) + base.substring(2);
	// Dagesh after consonant and vowel. 
	else if (base.length() >= 3 && base.charAt(1) < H.aleph && base.charAt(2) == H.dagesh)
	    return base.substring(0,2) + base.substring(3);
	// Dagesh after shin+dot and vowel. 
	else if (base.length() >= 4 && base.charAt(0) == H.shin && base.charAt(1) < H.aleph &&
		 base.charAt(2) < H.aleph && base.charAt(3) == H.dagesh)
	    return base.substring(0,3) + base.substring(4);
	else
	    return base;
    }
}

interface NikudRule
{
    String apply(String base, HLingData ld);
}

class EmptyRule implements NikudRule
{
    public String apply(String base, HLingData ld)
    {
	return base;
    }
}
