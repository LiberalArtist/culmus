#! /bin/sh
# This program has been created by Maxim Iorsh (iorsh@users.sourceforge.net).
# It is a PUBLIC DOMAIN - you may do with it anything you wish.

# 22-Aug-08 | iorsh@users.sourceforge.net | Created

cp FOLDOC_head.txt hewiktionary.foldoc

echo 'Building Culmus abstract XML file...'
./HeWiktionary_2_CulmusDic.pl hewiktionary-20080616-pages-articles.xml > hewiktionary-culmus.xml

echo 'Building FOLDOC file...'
./CulmusDic_2_FOLDOC.pl hewiktionary-culmus.xml >> hewiktionary.foldoc

echo 'Building DICT file...'
cat hewiktionary.foldoc | dictfmt -f -s 'Hebrew Wiktionary' -u 'http://culmus.sourceforge.net/dictionary/' --utf8 --without-headword  --headword-separator \| --columns 0 wiktionary-he-eng

dictzip wiktionary-he-eng.dict
