// Copyright (C) 2012 by Maxim Iorsh <iorsh@users.sourceforge.net>.
// Distributed under the term of the GNU General Public License version 2.

package org.ivrix.hspell;

import java.lang.String;

public class HLingData
{
    public enum Category
    {
	Undefined, Noun, Adjective, Verb, Uncategorized
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

    public String stem;
    private final String desc;
    public int ps;

    public Category category = Category.Undefined;
    public Gender gender = Gender.Undefined;
    public Number number = Number.Undefined;
    public PossessivePlus possessive = PossessivePlus.Indefinite;

    public HLingData(String stem_, String desc_, int ps_)
    {
	stem = stem_;
	desc = desc_;
	ps = ps_;
	ParseDesc(desc);
    }

    public HLingData(HLingData ld, boolean definite)
    {
        this(ld.stem, ld.desc, ld.ps);

        if (definite)
            possessive = PossessivePlus.Definite;
    }

    public String getDesc()
    {
        if (possessive == PossessivePlus.Definite)
            return desc + ","+H.MEM+H.YOD+H.VAV+H.DALET+H.AYIN;
        else
            return desc;
    }

    private void ParseDesc(String desc_)
    {
	String kinui = H.KAF+H.YOD+H.NUN+H.VAV+H.YOD+"/";
	int kinuiIdx = desc_.indexOf(kinui);

	if (kinuiIdx == -1)
	{
	    possessive = PossessivePlus.Indefinite;
	    ParseBaseDesc(desc_);
	}
	else
	{
	    possessive = PossessivePronoun(desc_.substring(kinuiIdx + kinui.length()));
	    ParseBaseDesc(desc_.substring(0,kinuiIdx-1));
	}
    }

    private void ParseBaseDesc(String baseDesc)
    {
	switch (baseDesc.charAt(0))
	{
	    case H.ayin : category = Category.Noun; break;
	    case H.tav  : category = Category.Adjective; break;
	    case H.pe   : category = Category.Verb; break;
	    case 'x'    : category = Category.Uncategorized; return;
	    default: category = Category.Undefined; return;
	}

	switch (baseDesc.charAt(2))
	{
	    case H.zayin : gender = Gender.Masculine; break;
	    case H.nun   : gender = Gender.Feminine; break;
	    default: gender = Gender.Undefined; break;
	}

	number = Number.Undefined;
	if (baseDesc.contains(H.YOD+H.HET+H.YOD+H.DALET))
	    number = Number.Singular;
	else if (baseDesc.contains(H.RESH+H.BETH+H.YOD+H.FINALMEM))
	    number = Number.Plural;

	if (baseDesc.contains(H.SAMECH+H.MEM+H.YOD+H.KAF+H.VAV+H.TAV))
	    possessive = PossessivePlus.ConstructState;
    }

    private PossessivePlus PossessivePronoun(String kinuiGuf)
    {
	PossessivePlus pronoun = PossessivePlus.Indefinite;

	if (kinuiGuf.startsWith(",1,"+H.YOD))
	    pronoun = PossessivePlus.FirstSingular;
	else if (kinuiGuf.startsWith(",1,"+H.RESH))
	    pronoun = PossessivePlus.FirstPlural;
	else if (kinuiGuf.startsWith(H.ZAYIN+",2,"+H.YOD))
	    pronoun = PossessivePlus.SecondSingularMasculine;
	else if (kinuiGuf.startsWith(H.ZAYIN+",2,"+H.RESH))
	    pronoun = PossessivePlus.SecondPluralMasculine;
	else if (kinuiGuf.startsWith(H.NUN+",2,"+H.YOD))
	    pronoun = PossessivePlus.SecondSingularFeminine;
	else if (kinuiGuf.startsWith(H.NUN+",2,"+H.RESH))
	    pronoun = PossessivePlus.SecondPluralFeminine;
	else if (kinuiGuf.startsWith(H.ZAYIN+",3,"+H.YOD))
	    pronoun = PossessivePlus.ThirdSingularMasculine;
	else if (kinuiGuf.startsWith(H.ZAYIN+",3,"+H.RESH))
	    pronoun = PossessivePlus.ThirdPluralMasculine;
	else if (kinuiGuf.startsWith(H.NUN+",3,"+H.YOD))
	    pronoun = PossessivePlus.ThirdSingularFeminine;
	else if (kinuiGuf.startsWith(H.NUN+",3,"+H.RESH))
	    pronoun = PossessivePlus.ThirdPluralFeminine;

	return pronoun;
    }
}
