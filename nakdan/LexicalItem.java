// Copyright (C) 2012 by Maxim Iorsh <iorsh@users.sourceforge.net>.
// Distributed under the term of the GNU General Public License version 2.

package org.culmus.nakdan;

import java.lang.*;
import org.ivrix.hspell.*;

// Currently stores a single noun. TODO: convert to interface
public class LexicalItem
{
    // TODO: throw exception for bad entry
    public LexicalItem(String entry) // TODO: need an Object Factory
    {
        String[] raw = entry.split("\\|", 8);

	// A meaningful entry should have at least the key, the category and some data.
	if (raw.length < 3)
	    return;

	if (raw[1].length() < 3)
	{
	    return;
	}

	// Category
        switch (raw[1].charAt(0))
	{
	    case 'n' : category = HLingData.Category.Noun; break;
	    case 'a' : category = HLingData.Category.Adjective; break;
	}

	// Gender
        switch (raw[1].charAt(1))
	{
	    case 'm' : gender = HLingData.Gender.Masculine; break;
	    case 'f' : gender = HLingData.Gender.Feminine; break;
	}

	// Stress
        switch (raw[1].charAt(2))
	{
	    case 's' :
	    case '0' : stress = HLingData.Stress.FirstSyllable; break;
	}

        description = raw[2];
        System.arraycopy(raw, 3, forms, 0, Math.min(raw.length - 3, 4));
    }

    public String resolve(HLingData ld)
	throws NakdanException
    {
	// Only nouns are supported
	if (ld.category != HLingData.Category.Noun)
	    throw new NakdanException(NakdanException.ErrorCode.UnsupportedPartOfSpeech);

	// Unsupported kinui guf
	if (ld.possessive != HLingData.PosessivePlus.Indefinite &&
	    ld.possessive != HLingData.PosessivePlus.Definite &&
	    ld.possessive != HLingData.PosessivePlus.ConstructState)
	    throw new NakdanException(NakdanException.ErrorCode.UnsupportedKinuiGuf);

	// Check data matching
	if (ld.category != category)
	    throw new NakdanException(NakdanException.ErrorCode.UnmatchedLingData);

	int index = -1;

	if (ld.number == HLingData.Number.Singular)
	    index = (ld.possessive == HLingData.PosessivePlus.ConstructState) ? 2 : 0;
	if (ld.number == HLingData.Number.Plural)
	    index = (ld.possessive == HLingData.PosessivePlus.ConstructState) ? 3 : 1;

	if (index == -1 || forms[index].equals(""))
	    throw new NakdanException(NakdanException.ErrorCode.WiktionaryMissingForm);
	else
	    return forms[index];
    }

    private HLingData.Category category = HLingData.Category.Undefined;
    private HLingData.Gender gender = HLingData.Gender.Undefined;
    private HLingData.Stress stress = HLingData.Stress.Unknown;
    public String description;
    private String[] forms = new String[4]; // singular, plural, singular conjugate, plural conjugate

    static final String errUnsuppPart = H.HET+H.LAMED+H.QUF+" "+H.DALET+H.YOD+H.BETH+H.RESH+" "+H.LAMED+H.ALEPH+" "+H.NUN+H.TAV+H.MEM+H.FINALKAF; // helek diber lo nitmach (unsupported part of speech)
    public static final LexicalItem UnsupportedPartOfSpeech = new LexicalItem("-|---|" + errUnsuppPart);
    public static final LexicalItem Empty = new LexicalItem("-|---|");
}
