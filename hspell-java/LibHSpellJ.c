/* Copyright 2011 Maxim Iorsh */
#include <jni.h>
#include "LibHSpellJ.h"
#include "hspell.h"
#include <iconv.h>
#include <string.h>

#define MAX_BYTES_IN_UTF8_CHARACTER 4

/* Covert the contents of Java string 'word' to ISO8859-8
 * and put the result into 'isoBuf'.*/
static void getIsoBuffer(JNIEnv *env, jstring word, char *isoBuf, size_t isoBufLength)
{
    iconv_t ic = iconv_open("ISO8859-8", "UTF8");

    char *utfBuf = malloc(sizeof(char)*isoBufLength);
    char *utf = utfBuf, *iso = isoBuf;
    int len = (*env)->GetStringLength(env, word);
    int inBytes = (*env)->GetStringUTFLength(env, word);
    size_t in = inBytes, out = isoBufLength;

    (*env)->GetStringUTFRegion(env, word, 0, len, utfBuf);
    iconv(ic, &utf, &in, &iso, &out);
    iconv_close(ic);
}

/* Convert the contents of ISO8859-8 buffer 'isoBuf'
 * and put them into the Java string 'word'.*/
static jstring putIsoBuffer(JNIEnv *env, const char *isoBuf)
{
    iconv_t ic = iconv_open("UTF8", "ISO8859-8");

    size_t isoBufLength = strlen(isoBuf);
    size_t utfBufLength = MAX_BYTES_IN_UTF8_CHARACTER * isoBufLength + 1;
    char *isoDynBuf = malloc(sizeof(char) * (isoBufLength + 1));
    char *utfDynBuf = malloc(sizeof(char) * utfBufLength);
    char *utf = utfDynBuf, *iso = isoDynBuf;
    size_t in = isoBufLength, out = utfBufLength;
    jstring result = 0;

    strcpy(isoDynBuf, isoBuf);
    iconv(ic, &iso, &in, &utf, &out);
    iconv_close(ic);

    /* Seal the utfDynBuf */
    *utf = '\0';

    result = (*env)->NewStringUTF(env, utfDynBuf);
    free(isoDynBuf);
    free(utfDynBuf);

    return result;
}

/* int hspell_init(struct dict_radix **dictp, int flags); */
JNIEXPORT jint JNICALL Java_org_ivrix_hspell_LibHSpellJ_hspell_1init
  (JNIEnv *env, jclass jcl, jlongArray dictp, jint flags)
{
    jlong dict; // holds pointer
    jint result = hspell_init((struct dict_radix **)&dict, flags);

    (*env)->SetLongArrayRegion(env, dictp, 0, 1, &dict);

    return result;
}

/* int hspell_check_word(struct dict_radix *dict, const char *word, int *preflen); */
JNIEXPORT jint JNICALL Java_org_ivrix_hspell_LibHSpellJ_hspell_1check_1word
  (JNIEnv *env, jclass jcl, jlong dict, jstring word, jintArray preflen)
{
    char iso8_buf[N_CORLIST_LEN] = {0};
    jint preflen_ = 0;
    jint result = 0;

    getIsoBuffer(env, word, iso8_buf, N_CORLIST_LEN);

    result = hspell_check_word((struct dict_radix *)dict, iso8_buf, &preflen_);

    (*env)->SetIntArrayRegion(env, preflen, 0, 1, &preflen_);

    return result;
}

/* void hspell_trycorrect(struct dict_radix *dict, const char *w, struct corlist *cl); */
JNIEXPORT void JNICALL Java_org_ivrix_hspell_LibHSpellJ_hspell_1trycorrect
  (JNIEnv *env, jclass jcl, jlong dict, jstring word, jobjectArray cl)
{
    char iso8_buf[N_CORLIST_LEN] = {0};
    struct corlist cl_;
    jint preflen_ = 0;
    jobjectArray clOut = 0;
    int i;

    getIsoBuffer(env, word, iso8_buf, N_CORLIST_LEN);

    corlist_init(&cl_);
    hspell_trycorrect((struct dict_radix *)dict, iso8_buf, &cl_);

    /* Create JNI array of strings */
    clOut = (jobjectArray)(*env)->NewObjectArray(env, corlist_n(&cl_),
		(*env)->FindClass(env, "java/lang/String"),
		(*env)->NewStringUTF(env, ""));

    /* Fill JNI array of strings */
    for (i=0; i<corlist_n(&cl_); i++)
    {
	(*env)->SetObjectArrayElement(env, clOut, i,
		putIsoBuffer(env, corlist_str(&cl_, i)));
    }

    /* Put JNI clOut into cl */
    (*env)->SetObjectArrayElement(env, cl, 0, clOut);

    corlist_free(&cl_);

    return;
}

/* int hspell_is_canonic_gimatria(const char *w); */
JNIEXPORT jint JNICALL Java_org_ivrix_hspell_LibHSpellJ_hspell_1is_1canonic_1gimatria
  (JNIEnv *env, jclass jcl, jstring w)
{
    char iso8_buf[N_CORLIST_LEN] = {0};
    jint result = 0;

    getIsoBuffer(env, w, iso8_buf, N_CORLIST_LEN);

    result = hspell_is_canonic_gimatria(iso8_buf);

    return result;
}

/* void hspell_uninit(struct dict_radix *dict); */
JNIEXPORT void JNICALL Java_org_ivrix_hspell_LibHSpellJ_hspell_1uninit
  (JNIEnv *env, jclass jcl, jlong dict)
{
    hspell_uninit((struct dict_radix *)dict);
}

/* const char *hspell_get_dictionary_path(void); */
JNIEXPORT jstring JNICALL Java_org_ivrix_hspell_LibHSpellJ_hspell_1get_1dictionary_1path
  (JNIEnv *env, jclass jcl)
{
    const char *path = hspell_get_dictionary_path();
    jstring result = 0;

    return putIsoBuffer(env, path);
}

/* void hspell_set_dictionary_path(const char *path); */
JNIEXPORT void JNICALL Java_org_ivrix_hspell_LibHSpellJ_hspell_1set_1dictionary_1path
  (JNIEnv *env, jclass jcl, jstring path)
{
    char iso8_buf[N_CORLIST_LEN] = {0};

    getIsoBuffer(env, path, iso8_buf, N_CORLIST_LEN);

    hspell_set_dictionary_path(iso8_buf);
}

/* Unfortunately hspell_enum_splits doesn't provide arguments for arbitrary context,
 * so we need to pass the context by means of static variables. This, of course,
 * ruins the thread safety. */
static JNIEnv *static_env = 0;
static jobject static_enumf = 0;

/* typedef int hspell_word_split_callback_func(const char *word,
	    const char *baseword, int preflen, int prefspec); */
static int hspell_word_split_CB(const char *word, const char *baseword,
				int preflen, int prefspec)
{
    JNIEnv *env = static_env;
    jobject enumf = static_enumf;

    /* Locate the Java callback method "hspell_word_split_CB", which must
       be implemented in enumf object. */
    jclass cls = (*env)->GetObjectClass(env, enumf);
    jmethodID mid = (*env)->GetMethodID(env, cls, "HspellWordSplit_CB",
					"(Ljava/lang/String;Ljava/lang/String;II)I");
    if (mid == NULL)
    {
	return 0; /* method not found */
    }

    /* Call Java method with created Java callback arguments. */
    return (*env)->CallIntMethod(env, enumf, mid, putIsoBuffer(env, word),
				 putIsoBuffer(env, baseword), preflen, prefspec);
}

/* int hspell_enum_splits(struct dict_radix *dict, const char *word, 
			  hspell_word_split_callback_func *enumf); */
/* !!! NOT THREAD SAFE !!! */
JNIEXPORT jint JNICALL Java_org_ivrix_hspell_LibHSpellJ_hspell_1enum_1splits
  (JNIEnv *env, jclass jcl, jlong dict, jstring word, jobject enumf)
{
    char iso8_buf[N_CORLIST_LEN] = {0};
    jint result = 0;

    getIsoBuffer(env, word, iso8_buf, N_CORLIST_LEN);

    static_env = env;
    static_enumf = enumf;

    result = hspell_enum_splits((struct dict_radix *)dict, iso8_buf,
				hspell_word_split_CB);
    static_env = 0;
    static_enumf = 0;

    return result;
}

