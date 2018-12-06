#! /bin/sh
# This program has been created by Maxim Iorsh (iorsh@users.sourceforge.net).
# It is a PUBLIC DOMAIN - you may do with it anything you wish.

# 22-Aug-08 | iorsh@users.sourceforge.net | Created
# 17-Sep-08 | iorsh@users.sourceforge.net | Added Hebrew WordNet
# 28-Nov-08 | iorsh@users.sourceforge.net | Added Waldstein scans

cp FOLDOC_head_wikt.txt hewiktionary.foldoc
cp FOLDOC_head_mwn.txt  hebwordnet.foldoc
cp FOLDOC_head_wald.txt waldstein-he-en.foldoc

echo 'Building Culmus abstract XML files...'
./HeWiktionary_2_CulmusDic.pl hewiktionary-20080616-pages-articles.xml > hewiktionary-culmus.xml
./HebrewWordNet_2_CulmusDic.pl hebrew_synonyms.xml hebrew_synsets.xml > hebwordnet-culmus.xml

echo 'Building FOLDOC files...'
./CulmusDic_2_FOLDOC.pl hewiktionary-culmus.xml >> hewiktionary.foldoc
./CulmusDic_2_FOLDOC.pl hebwordnet-culmus.xml   >> hebwordnet.foldoc

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

