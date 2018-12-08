// Copyright (C) 2012 by Maxim Iorsh <iorsh@users.sourceforge.net>.
// Distributed under the term of the GNU General Public License version 2.

package org.culmus.nakdan;

import java.util.*;
import org.ivrix.hspell.*;

public class CulmusNakdan
{
    public static class Key
    {
	public HSplit split;
	public HLingData ld;

	public Key(HSplit split_, HLingData ld_)
	{
	    split = split_;
	    ld = ld_;
	}
    }

    public static class Value
    {
	public LexicalItem item;
	public String menukad;

	public Value(LexicalItem item_, String menukad_)
	{
	    item = item_;
	    menukad = menukad_;
	}
    }

    public static class Entry
    {
	public Key key;
	public Value[] values;

	public Entry(Key key_, Value[] values_)
	{
	    key = key_;
	    values = values_;
	}
    }

    static final String errUnsuppForm = H.TSADI+H.VAV+H.RESH+H.HE+" "+H.LAMED+H.ALEPH+" "+H.NUN+H.TAV+H.MEM+H.KAF+H.TAV; // tsura lo nitmechet (unsupported form)
    static final String errMissing = H.MEM+H.YOD+H.LAMED+H.HE+" "+H.HET+H.SAMECH+H.RESH+H.HE; // mila hasera (missing word)
    static final String errMissingWiki = H.AYIN+H.RESH+H.FINALKAF+" "+H.HET+H.SAMECH+H.RESH+" "+H.BETH+H.VAV+H.VAV+H.YOD+H.QUF+H.YOD+H.MEM+H.YOD+H.LAMED+H.VAV+H.FINALNUN; // erech haser bavikimilon (missing wiktionary entry)

    private static Entry queryOneMeaning(Key key, String prefix)
    {
        DictionaryDataReader reader = DictionaryDataReader.getInstance();
	Set<LexicalItem> lexset = reader.get(key.ld.stem);

	Vector<Value> values = new Vector();

	if (lexset != null)
	{
            for (LexicalItem item : lexset)
	    {
		try
		{
		    String menukad = item.resolve(key.ld);

		    menukad = NikudRuleFactory.addPrefix(menukad, prefix, key.ld);
		    values.add(new Value(item, menukad));
		}
		catch (NakdanException e)
		{
		    switch (e.getErrorCode())
		    {
		    case UnsupportedPartOfSpeech:
			values.add(new Value(LexicalItem.UnsupportedPartOfSpeech, ""));
			break;
		    case UnsupportedKinuiGuf:
			values.add(new Value(item, errUnsuppForm));
			break;
		    case WiktionaryMissingForm:
			values.add(new Value(item, errMissing));
			break;
		    case UnmatchedLingData:
			break; // skip
		    default:
			throw new Error("Unimplemented", e);
		    }
		}
	    }
	}
	else
	{
	    // No entry found, determine reason
	    try
	    {
		String menukad = LexicalItem.UnsupportedPartOfSpeech.resolve(key.ld);
	    }
	    catch (NakdanException e)
	    {
		switch (e.getErrorCode())
		{
		case UnsupportedPartOfSpeech:
		    values.add(new Value(LexicalItem.UnsupportedPartOfSpeech, ""));
		    break;
		case UnsupportedKinuiGuf:
		    values.add(new Value(LexicalItem.Empty, errUnsuppForm));
		    break;
		case UnmatchedLingData:
		    values.add(new Value(LexicalItem.Empty, errMissingWiki));
		    break;
		default:
			throw new Error("Unexpected", e);
		}
	    }
	}

	if (values == null)
	    return new Entry(key, null);
	else
	    return new Entry(key, values.toArray(new Value[]{}));
    }

    public static Vector<Entry> queryMeanings(String aTerm)
    {
	// HSpellWrapper hspell = HSpellWrapper.getInstance();
	HSpellRunner hspell = new HSpellRunner();

	Vector<Entry> result = new Vector<Entry>();

        // get lexical info from hspell
	List<HSplit> cl = hspell.EnumSplits(aTerm);
        
        for (HSplit split : cl)
        {
	    for (HLingData ld : split.lingdata)
	    {
		Key key = new Key(split, ld);
		String prefix = split.prefix;
		result.add(queryOneMeaning(key, prefix));

		// BakalHaShimush could have swallowed HeHaYedia.
		// Check it and retrieve an additional variant.
		if (ld.possessive == HLingData.PosessivePlus.Indefinite &&
		    prefix.length() > 0 && (prefix.charAt(0) == H.beth ||
					    prefix.charAt(0) == H.kaf ||
					    prefix.charAt(0) == H.lamed))
		{
		    // Clone HLingData
		    HLingData ld2 = new HLingData(ld.stem, ld.desc, ld.ps);

		    // Modify possessive property
		    ld2.possessive = HLingData.PosessivePlus.Definite;
		    ld2.desc = ld2.desc + ","+H.MEM+H.YOD+H.VAV+H.DALET+H.AYIN;

		    Key key2 = new Key(split, ld2);
		    String prefix2 = split.prefix + H.HE;
		    result.add(queryOneMeaning(key2, prefix2));
		}
	    }
        }

	return result;
    }
}
