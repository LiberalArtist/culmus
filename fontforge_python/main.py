import fontforge
import sys
from os.path import expanduser

home = expanduser("~")
sys.path.append(home + "/.FontForge/python")

import CreatePrecomposedGlyphs
import Kern2Comments
import GuessMarkToMarkGaps
import AddHebrewGSUB
import AddHebrewGPOS

fontforge.registerMenuItem(CreatePrecomposedGlyphs.CreatePrecomposedGlyphs,None,None,"Font",None,"Create Precomposed Glyphs");
fontforge.registerMenuItem(Kern2Comments.Kern2Comments,None,None,"Font",None,"Convert Base-Mark Kernings to Position");
fontforge.registerMenuItem(GuessMarkToMarkGaps.GuessMarkToMarkGaps,None,None,"Font",None,"Guess Initial Mark To Mark Gaps");
fontforge.registerMenuItem(lambda a, b: False, lambda a, b: False,None,"Font",None, "-----------------------");
fontforge.registerMenuItem(AddHebrewGSUB.AddHebrewGSUB,None,None,"Font",None,"Add Hebrew GSUB");
fontforge.registerMenuItem(AddHebrewGPOS.AddHebrewGPOS,None,None,"Font",None,"Add Hebrew GPOS");

