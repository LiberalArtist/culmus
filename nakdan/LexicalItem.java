// Copyright (C) 2012 by Maxim Iorsh <iorsh@users.sourceforge.net>.
// Distributed under the term of the GNU General Public License version 2.

package org.culmus.nakdan;

import java.lang.*;
import org.ivrix.hspell.*;

// A single line in a nakdan.txt file, parsed into meaningful fields.
public class LexicalItem
{
    public enum Category
    {
	Undefined, Noun, Adjective, Verb, Adposition, Numeral
    }

    public enum Gender
    {
	Undefined, Masculine, Feminine
    }

    public enum Number
    {
	Undefined, Singular, Plural
    }

    public enum PossessivePlus
    {
	Indefinite, Definite, ConstructState,
	FirstSingular, FirstPlural,
	SecondSingularMasculine, SecondPluralMasculine,
	SecondSingularFeminine, SecondPluralFeminine,
	ThirdSingularMasculine, ThirdPluralMasculine,
	ThirdSingularFeminine, ThirdPluralFeminine
    }

    public enum Stress
    {
	Unknown, FirstSyllable, SecondSyllable, ThirdSyllable, LastSyllable
    }

    public final Category category;
    public Gender gender = Gender.Undefined;
    public PossessivePlus possessive = PossessivePlus.Indefinite;
    public Stress stress = Stress.Unknown;
    public String description;
    private String[] forms = new String[4]; // singular, plural, singular conjugate, plural conjugate

    // TODO: throw exception for bad entry
    public LexicalItem(String entry) // TODO: need an Object Factory
    {
        String[] raw = entry.split("\\|", 8);

	// NOTE: A meaningful entry should have at least the key, the category and some data.
	if (raw.length < 3 || raw[1].length() < 3)
	{
            category = Category.Undefined;
	    return;
	}

	// Category
        switch (raw[1].charAt(0))
	{
	    case 'n' : category = Category.Noun; break;
	    case 'a' : category = Category.Adjective; break;
	    case 'A' : category = Category.Adposition; break;
	    case 'N' : category = Category.Numeral; break;
            default  : category = Category.Undefined; break;
	}

	// Gender
        switch (raw[1].charAt(1))
	{
	    case 'm' : gender = Gender.Masculine; break;
	    case 'f' : gender = Gender.Feminine; break;
            default  : gender = Gender.Undefined; break;
	}

	// Stress
        switch (raw[1].charAt(2))
	{
	    case 's' :
	    case '0' : stress = Stress.FirstSyllable; break;
	}

        // Posessive
        if (category == Category.Adposition ||
            category == Category.Numeral)
        {
            switch (raw[1].charAt(3))
            {
    	        case 'i' : possessive = PossessivePlus.Indefinite; break;
    	        case 'd' : possessive = PossessivePlus.Definite; break;
    	        case 'c' : possessive = PossessivePlus.ConstructState; break;
                default  : possessive = PossessivePlus.Indefinite; break;
            }
	}
        else
            possessive = PossessivePlus.Indefinite;

        description = raw[2];
	if (category == Category.Adposition ||
            category == Category.Numeral)
	{
            forms[0] = raw[3];
	}
	else
	{
            System.arraycopy(raw, 3, forms, 0, Math.min(raw.length - 3, 4));
	}
    }

    public String resolve(HLingData ld)
	throws NakdanException
    {
	// Only nouns are supported
	if (ld.category != HLingData.Category.Noun &&
            ld.category != HLingData.Category.Uncategorized)
	    throw new NakdanException(NakdanException.ErrorCode.UnsupportedPartOfSpeech);

	// Unsupported kinui guf
	if (ld.category == HLingData.Category.Noun &&
            ld.possessive != HLingData.PossessivePlus.Indefinite &&
            ld.possessive != HLingData.PossessivePlus.Definite &&
	    ld.possessive != HLingData.PossessivePlus.ConstructState)
	    throw new NakdanException(NakdanException.ErrorCode.UnsupportedKinuiGuf);

	// Check data matching
	if (!dataMatch(ld))
	    throw new NakdanException(NakdanException.ErrorCode.UnmatchedLingData);

	int index = -1;

        if (ld.category == HLingData.Category.Uncategorized)
            index = 0;
	else if (ld.number == HLingData.Number.Singular)
	    index = (ld.possessive == HLingData.PossessivePlus.ConstructState) ? 2 : 0;
	else if (ld.number == HLingData.Number.Plural)
	    index = (ld.possessive == HLingData.PossessivePlus.ConstructState) ? 3 : 1;

	if (index == -1 || forms[index].equals(""))
	    throw new NakdanException(NakdanException.ErrorCode.WiktionaryMissingForm);
	else
	    return forms[index];
    }

    public boolean dataMatch(HLingData ld)
    {
        return ((category.name().equals(ld.category.name())) ||
                ((category == Category.Adposition || category == Category.Numeral) &&
                 ld.category == HLingData.Category.Uncategorized));
    }

    public String describe()
    {
        String desc = "";

        switch (gender)
	{
	    case Masculine : desc += H.ZAYIN; break;
	    case Feminine  : desc += H.NUN; break;
	}

        switch (possessive)
	{
	    case Indefinite     : break;
	    case Definite       : desc += "," + H.MEM+H.YOD+H.VAV+H.DALET+H.AYIN; break;
	    case ConstructState : desc += "," + H.SAMECH+H.MEM+H.YOD+H.KAF+H.VAV+H.TAV; break;
	}

        return desc;
    }

    static final String errUnsuppPart = H.HET+H.LAMED+H.QUF+" "+H.DALET+H.YOD+H.BETH+H.RESH+" "+H.LAMED+H.ALEPH+" "+H.NUN+H.TAV+H.MEM+H.FINALKAF; // helek diber lo nitmach (unsupported part of speech)
    public static final LexicalItem UnsupportedPartOfSpeech = new LexicalItem("-|---|" + errUnsuppPart);
    public static final LexicalItem Empty = new LexicalItem("-|---|");
}
