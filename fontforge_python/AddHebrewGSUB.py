import fontforge;
import os.path

# This script for FontForge was created by Maxim Iorsh. It is in a public
# domain. You can use it in any way and for any purpose.

# This script automatically adds Advanced Typography GSUB features to a 
# font file. It deletes all existing GSUB features, so use carefully!

# Currently supported: 
# 1. letter + dagesh ligatures
# 2. sin-shin ligatures
# 3. yiddish ligatures
# 4. wider letters (justification alternatives)
# 5. miscellaneous features
# 5.1 final-kaf with shva/qamats
# 5.2 holam male
# 5.3 aleph-lamed
# 5.4 alternative plus
# 6. chain substitution features
# 6.1 In the presence of meteg, replace tsere/segol/patah/qamats with
#     narrow versions under narrow letters only
# 6.2 In the presence of meteg, replace hatafim/qubuts with narrow versions

script_dir = os.path.dirname(os.path.abspath(__file__))

def AddHebrewGSUB(unused, font):

    lookups = font.gsub_lookups

    for l in lookups:
        font.removeLookup(l)

    font.mergeFeature(script_dir + "/AAYN.fea")
    font.mergeFeature(script_dir + "/VHOL.fea")
    font.mergeFeature(script_dir + "/CustomPrecomp.fea")
    font.mergeFeature(script_dir + "/Dagesh.fea")
    font.mergeFeature(script_dir + "/SinShin.fea")
    font.mergeFeature(script_dir + "/WideLetters.fea")
    font.mergeFeature(script_dir + "/Yiddish.fea")
    font.mergeFeature(script_dir + "/Ladino.fea")
    font.mergeFeature(script_dir + "/MiscSALT.fea")
    font.mergeFeature(script_dir + "/NarrowVowels.fea")

