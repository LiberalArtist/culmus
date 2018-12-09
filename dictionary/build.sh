#! /bin/sh
# This program has been created by Maxim Iorsh (iorsh@users.sourceforge.net).
# It is a PUBLIC DOMAIN - you may do with it anything you wish.

# 22-Aug-08 | iorsh@users.sourceforge.net | Created
# 17-Sep-08 | iorsh@users.sourceforge.net | Added Hebrew WordNet
# 28-Nov-08 | iorsh@users.sourceforge.net | Added Waldstein scans
# 15-Apr-09 | iorsh@users.sourceforge.net | Updated for 20090423 wiktionary.
# 17-Jun-09 | iorsh@users.sourceforge.net | Updated for 20090608 wiktionary.
# 08-Jan-11 | iorsh@users.sourceforge.net | Updated for 20101102 wiktionary.
# 29-Jan-11 | iorsh@users.sourceforge.net | Updated for 20110129 wiktionary.
# 21-Jan-12 | iorsh@users.sourceforge.net | Updated for 20120114 wiktionary.
# 18-Feb-12 | iorsh@users.sourceforge.net | Updated for 20120215 wiktionary, build nakdan.txt.
# 04-Mar-12 | iorsh@users.sourceforge.net | Updated for 20120304 wiktionary, build nakdan.txt.
# 02-Jun-12 | iorsh@users.sourceforge.net | Updated for 20120528 wiktionary.
# 03-Sep-12 | iorsh@users.sourceforge.net | Updated for 20120902 wiktionary.
# 12-Jan-13 | iorsh@users.sourceforge.net | Updated for 20130110 wiktionary.
# 25-Sep-14 | iorsh@users.sourceforge.net | Fixed wiktionary wildcard.

cp FOLDOC_head_wikt.txt hewiktionary.foldoc
cp FOLDOC_head_mwn.txt  hebwordnet.foldoc
cp FOLDOC_head_wald.txt waldstein-he-en.foldoc

echo 'Building Culmus abstract XML files...'
./HeWiktionary_2_CulmusDic.pl hewiktionary-????????-pages-articles.xml > hewiktionary-culmus.xml
./HebrewWordNet_2_CulmusDic.pl hebrew_synonyms.xml hebrew_synsets.xml > hebwordnet-culmus.xml

echo 'Building FOLDOC files...'
./CulmusDic_2_FOLDOC.pl hewiktionary-culmus.xml >> hewiktionary.foldoc
./CulmusDic_2_FOLDOC.pl hebwordnet-culmus.xml   >> hebwordnet.foldoc

echo 'Building nakdan.txt...'
./CulmusDic_2_nakdan.pl hewiktionary-culmus.xml > nakdan.txt

echo 'Building Waldstein FOLDOC file...'
cat waldstein/*.txt > waldstein_he_en.txt
./WaldsteinHeEn_2_FOLDOC.pl waldstein_he_en.txt >> waldstein-he-en.foldoc

echo 'Building DICT files...'
cat hewiktionary.foldoc | dictfmt -f -s "Hebrew Wiktionary" -u 'http://culmus.sourceforge.net/dictionary/' --utf8 --without-headword  --headword-separator \| --columns 0 wiktionary-he-eng
cat hebwordnet.foldoc | dictfmt -f -s "Hebrew WordNet" -u 'http://culmus.sourceforge.net/dictionary/' --utf8 --without-headword  --headword-separator \| --columns 0 wordnet-he-eng
cat waldstein-he-en.foldoc | dictfmt -f -s "Waldstein's Hebrew-English Dictionary" -u 'http://culmus.sourceforge.net/dictionary/' --utf8 --without-headword  --headword-separator \| --columns 0 waldstein-he-eng

dictzip wiktionary-he-eng.dict
dictzip wordnet-he-eng.dict
dictzip waldstein-he-eng.dict

