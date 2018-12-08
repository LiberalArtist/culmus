// Copyright (C) 2012 by Maxim Iorsh <iorsh@users.sourceforge.net>.
// Distributed under the term of the GNU General Public License version 2.

package org.culmus.nakdan;

import java.lang.String;
import org.ivrix.hspell.*;

class VavHaHibur implements NikudRule
{
    public String apply(String base, HLingData ld)
    {
	String result = "";

	if (base.length() < 2)
	{
	    return H.VAV+H.SHVA + base; // add vav with shwa
	}

	// Remove dagesh kal in beged-kefet.
	base = NikudRuleFactory.removeDagesh(base);

	// Before "hetsi" (half) and "reva" (quarter) - with qamats
	if (base.charAt(0) == '\u05d7' && ld.stem.equals(H.HET+H.TSADI+H.YOD) ||
	    base.charAt(0) == '\u05e8' && ld.stem.equals(H.RESH+H.BETH+H.AYIN))
	{
	    result = H.VAV+H.QAMATS + base;
	}
	// TODO: Before stressed syllable in bible hefsek form - with qamats
	// Before hataf - with corresponding full vowel
	else if (base.charAt(1) == '\u05b1') // hataf-segol
	{
	    result = H.VAV+H.SEGOL + base;
	}
	else if (base.charAt(1) == '\u05b2') // hataf-patah
	{
	    result = H.VAV+H.PATAH + base;
	}
	else if (base.charAt(1) == '\u05b3') // hataf-qamats
	{
	    result = H.VAV+H.QAMATS + base;
	}
	// Before yod+shwa - with hiriq, shwa falls off
	else if (base.startsWith(H.YOD+H.SHVA))
	{
	    result = H.VAV+H.HIRIQ+H.YOD + base.substring(2);
	}
	// Before bet/waw/mem/pe - shuruq
	else if (base.charAt(0) == H.beth ||
		 base.charAt(0) == H.vav ||
		 base.charAt(0) == H.mem ||
		 base.charAt(0) == H.pe)
	{
	    result = H.VAV+H.SHURUQ + base;
	}
	// Before letter with shwa - shuruq
	else if (base.charAt(1) == '\u05b0')
	{
	    result = H.VAV+H.DAGESH + base;
	}
	// Otherwise add vav with shwa
	else
	{
	    result = H.VAV+H.SHVA + base;
	}

	return result;
    }
}
