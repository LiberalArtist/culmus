// Copyright 2011 Maxim Iorsh
package org.ivrix.hspell;

public class LibHSpellJ
{
    public static final int HSPELL_OPT_DEFAULT = 0;
    public static final int HSPELL_OPT_HE_SHEELA = 1;
    public static final int HSPELL_OPT_LINGUISTICS = 2;

    // int hspell_init([out] struct dict_radix **dictp, [in] int flags);
    public static native int hspell_init(long dictp[], int flags);

    // int hspell_check_word([in] struct dict_radix *dict,
    //			     [in] const char *word, [out] int *preflen);
    public static native int hspell_check_word(long dict, String word, int preflen[]);

    // int hspell_trycorrect([in] struct dict_radix *dict,
    //			     [in] const char *word, [out] struct corlist *cl);
    public static native void hspell_trycorrect(long dict, String word, String cl[][]);

    // int hspell_is_canonic_gimatria([in] const char *w);
    public static native int hspell_is_canonic_gimatria(String w);

    // void hspell_uninit([in] struct dict_radix *dict);
    public static native void hspell_uninit(long dict);

    // const char *hspell_get_dictionary_path(void);
    public static native String hspell_get_dictionary_path();

    // void hspell_set_dictionary_path(const char *path);
    public static native void hspell_set_dictionary_path(String path);

    // typedef int hspell_word_split_callback_func(const char *word,
    //			const char *baseword, int preflen, int prefspec);
    public static interface hspell_word_split_callback_func
    {
	public abstract int HspellWordSplit_CB(String word, String baseword,
					       int preflen, int prefspec);
    }

    // int hspell_enum_splits([in] struct dict_radix *dict, [in] const char *word, 
    //			      [in] hspell_word_split_callback_func *enumf);
    // !!! NOT THREAD SAFE !!!
    public static native int hspell_enum_splits(long dict, String word,
						hspell_word_split_callback_func enumf);

    static
    {
	System.loadLibrary("hspellj");
    }
}
