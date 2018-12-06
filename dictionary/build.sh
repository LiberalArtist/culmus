#! /bin/sh
# This program has been created by Maxim Iorsh (iorsh@users.sourceforge.net).
# It is a PUBLIC DOMAIN - you may do with it anything you wish.

# 22-Aug-08 | iorsh@users.sourceforge.net | Created
# 17-Sep-08 | iorsh@users.sourceforge.net | Added Hebrew WordNet

cp FOLDOC_head_wikt.txt hewiktionary.foldoc
cp FOLDOC_head_mwn.txt  hebwordnet.foldoc

echo 'Building Culmus abstract XML files...'
./HeWiktionary_2_CulmusDic.pl hewiktionary-20080616-pages-articles.xml > hewiktionary-culmus.xml
./HebrewWordNet_2_CulmusDic.pl hebrew_synonyms.xml hebrew_synsets.xml > hebwordnet-culmus.xml

echo 'Building FOLDOC files...'
./CulmusDic_2_FOLDOC.pl hewiktionary-culmus.xml >> hewiktionary.foldoc
./CulmusDic_2_FOLDOC.pl hebwordnet-culmus.xml   >> hebwordnet.foldoc

echo 'Building DICT files...'
cat hewiktionary.foldoc | dictfmt -f -s 'Hebrew Wiktionary' -u 'http://culmus.sourceforge.net/dictionary/' --utf8 --without-headword  --headword-separator \| --columns 0 wiktionary-he-eng
cat hebwordnet.foldoc | dictfmt -f -s 'Hebrew WordNet' -u 'http://culmus.sourceforge.net/dictionary/' --utf8 --without-headword  --headword-separator \| --columns 0 wordnet-he-eng

dictzip wiktionary-he-eng.dict
dictzip wordnet-he-eng.dict
