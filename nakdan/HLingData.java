// Copyright (C) 2012 by Maxim Iorsh <iorsh@users.sourceforge.net>.
// Distributed under the term of the GNU General Public License version 2.

package org.ivrix.hspell;

import java.lang.String;

public class HLingData
{
    public enum PosessivePlus
    {
	Indefinite, Definite, ConstructState,
	FirstSingular, FirstPlural,
	SecondSingularMasculine, SecondPluralMasculine,
	SecondSingularFeminine, SecondPluralFeminine,
	ThirdSingularMasculine, ThirdPluralMasculine,
	ThirdSingularFeminine, ThirdPluralFeminine
    }

    public enum Category
    {
	Undefined, Noun, Adjective, Verb
    }

    public enum Gender
    {
	Undefined, Masculine, Feminine
    }

    public enum Number
    {
	Undefined, Singular, Plural
    }

    public enum Stress
    {
	Unknown, FirstSyllable, SecondSyllable, ThirdSyllable, LastSyllable
    }

    public String stem;
    public String desc;
    public int ps;

    public Category category = Category.Undefined;
    public Gender gender = Gender.Undefined;
    public Number number = Number.Undefined;
    public PosessivePlus possessive = PosessivePlus.Indefinite;
    public Stress stress = Stress.Unknown;

    public HLingData(String stem_, String desc_, int ps_)
    {
	stem = stem_;
	desc = desc_;
	ps = ps_;
	ParseDesc(desc);
    }

    private void ParseDesc(String desc_)
    {
	String kinui = H.KAF+H.YOD+H.NUN+H.VAV+H.YOD+"/";
	int kinuiIdx = desc_.indexOf(kinui);

	if (kinuiIdx == -1)
	{
	    possessive = PosessivePlus.Indefinite;
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
	    possessive = PosessivePlus.ConstructState;
    }

    private PosessivePlus PossessivePronoun(String kinuiGuf)
    {
	PosessivePlus pronoun = PosessivePlus.Indefinite;

	if (kinuiGuf.startsWith(",1,"+H.YOD))
	    pronoun = PosessivePlus.FirstSingular;
	else if (kinuiGuf.startsWith(",1,"+H.RESH))
	    pronoun = PosessivePlus.FirstPlural;
	else if (kinuiGuf.startsWith(H.ZAYIN+",2,"+H.YOD))
	    pronoun = PosessivePlus.SecondSingularMasculine;
	else if (kinuiGuf.startsWith(H.ZAYIN+",2,"+H.RESH))
	    pronoun = PosessivePlus.SecondPluralMasculine;
	else if (kinuiGuf.startsWith(H.NUN+",2,"+H.YOD))
	    pronoun = PosessivePlus.SecondSingularFeminine;
	else if (kinuiGuf.startsWith(H.NUN+",2,"+H.RESH))
	    pronoun = PosessivePlus.SecondPluralFeminine;
	else if (kinuiGuf.startsWith(H.ZAYIN+",3,"+H.YOD))
	    pronoun = PosessivePlus.ThirdSingularMasculine;
	else if (kinuiGuf.startsWith(H.ZAYIN+",3,"+H.RESH))
	    pronoun = PosessivePlus.ThirdPluralMasculine;
	else if (kinuiGuf.startsWith(H.NUN+",3,"+H.YOD))
	    pronoun = PosessivePlus.ThirdSingularFeminine;
	else if (kinuiGuf.startsWith(H.NUN+",3,"+H.RESH))
	    pronoun = PosessivePlus.ThirdPluralFeminine;

	return pronoun;
    }
}
