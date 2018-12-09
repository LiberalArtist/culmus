#! /bin/sh

# make directories
mkdir -p org/ivrix/hspell
mkdir -p org/culmus/nakdan
mkdir -p org/culmus/ooo/nakdan
mkdir -p org/culmus/ooo/xmeaningimpltd
mkdir -p META-INF

# copy source files 
cp NikudRuleFactory.java VavHaHibur.java HeHaYedia.java BakalHaShimush.java MemHaShimush.java ShinHaZika.java org/culmus/nakdan/
cp DictionaryDataReader.java LexicalItem.java TestInstall.java TestNakdan.java CulmusNakdan.java NakdanException.java org/culmus/nakdan/
cp CulmusOOoNakdan.java org/culmus/ooo/nakdan/
cp H.java HLingData.java HSpellRunner.java HSplit.java org/ivrix/hspell/
cp nakdan.txt org/culmus/nakdan/
cp XMeaningImpl.java JavaExceptionMeaning.java org/culmus/ooo/xmeaningimpltd/
cp manifest.xml META-INF/

#find HSpellWrapper.jar
#if [ -f /usr/local/share/hspell/HSpellWrapper.jar ]
#then
#    CLASSPATH=$CLASSPATH:/usr/local/share/hspell/HSpellWrapper.jar
#elif [ -f /usr/share/hspell/HSpellWrapper.jar ]
#then
#    CLASSPATH=$CLASSPATH:/usr/share/hspell/HSpellWrapper.jar
#fi

#compile thesaurus
javac -source 1.5 -cp .:$CLASSPATH org/culmus/nakdan/TestNakdan.java
javac -source 1.5 -cp .:$CLASSPATH org/culmus/ooo/nakdan/CulmusOOoNakdan.java

#delete source files from package directory structure
rm org/ivrix/hspell/*.java
rm org/culmus/nakdan/*.java
rm org/culmus/ooo/nakdan/*.java
rm org/culmus/ooo/xmeaningimpltd/*.java

#clean
rm CulmusOOoNakdan.uno.jar CulmusOOoNakdan.20141029.oxt

#create CulmusOOoNakdan.uno.jar
jar -cvmf MANIFEST.MF CulmusOOoNakdan.uno.jar org/

#create uno package which is nothing but a zip file with the extension .oxt
zip CulmusOOoNakdan.20141029.oxt META-INF/manifest.xml CulmusOOoNakdan.uno.jar description.xml culmus-logo.png CHANGES GNU-GPL LICENSE

rm -rf org/ META-INF/

echo 'Testing...'
java -jar CulmusOOoNakdan.uno.jar
