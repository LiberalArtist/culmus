// Copyright 2011 Maxim Iorsh
package org.ivrix.hspell;

import java.util.List;

// Singleton class
public class HSpellWrapper
{
    private long dict = 0; // address of native handler

    // Private constructor prevents instantiation from other classes
    private HSpellWrapper()
    {
	long[] addr = {0};
	int flags = LibHSpellJ.HSPELL_OPT_DEFAULT;

	if (LibHSpellJ.hspell_init(addr, flags) == 0)
	    dict = addr[0];
	else
	    dict = 0;
    }
 
    private static class HSpellWrapperHolder
    { 
	public static final HSpellWrapper INSTANCE = new HSpellWrapper();
    }
 
    public static HSpellWrapper getInstance()
    {
	return HSpellWrapperHolder.INSTANCE;
    }

    // Call this at the very end, it will invalidate the underlying native resource
    public void close()
    {
	LibHSpellJ.hspell_uninit(dict);
	dict = 0;
    }

    protected void finalize() throws Throwable
    {
	if (dict != 0)
	    close();

	super.finalize(); //not necessary if extending Object.
    } 

    public boolean CheckWord(String word)
    {
	int[] preflen = {0};

	int res = LibHSpellJ.hspell_check_word(dict, word, preflen);

	return (res == 1);
    }

    public String[] TryCorrect(String word)
    {
	String[][] cl = {{""}};

	LibHSpellJ.hspell_trycorrect(dict, word, cl);

	return cl[0];
    }

    public static int IsCanonicGimatria(String word)
    {
	return LibHSpellJ.hspell_is_canonic_gimatria(word);
    }

    private class SplitCollector implements LibHSpellJ.hspell_word_split_callback_func
    {
	public List<String> splits;

	public int HspellWordSplit_CB(String word, String baseword,
				      int preflen, int prefspec)
	{
	    splits.add(baseword);
	    return 0;
	}
    }

    public List<String> EnumSplits(String word)
    {
	SplitCollector scoll = new SplitCollector();

	LibHSpellJ.hspell_enum_splits(dict, word, scoll);

	return scoll.splits;
    }
}
