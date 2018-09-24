import fontforge

import utils

# This script for FontForge was created by Maxim Iorsh in 2017. It is public
# domain. You can use it in any way and for any purpose.
#
# We guess the desired gap sizes between adjacent marks and store them in
# glyph comments.
# 
# The comments stored in a glyph are in the following format:
# %<anchor-class>=<offset>
# 
# This script was developed and tested with build 20120731.
# Script version: 01 Dec 2017.

def GuessGapsFromHataf(hataf_glyph):

    # Gap between segol/patah (left) and 1dot/2dots (right)

    # Sort hataf components by position from left to right. Thus the two shwa contours
    # will be at the end of the sorted list.
    source_contours = hataf_glyph.foreground.dup()
    source_contours = sorted(source_contours, key=lambda c: c.boundingBox()[2], reverse=False)

    # The upper shwa dot can be the last or the one before, depending on the slanting.
    # Find it by comparing the contour height.
    upper_shwa_idx = -1 if source_contours[-1].boundingBox()[1] > source_contours[-2].boundingBox()[1] else -2

    # Get distance between the left component and upper shwa dot
    gap_dot = source_contours[upper_shwa_idx].boundingBox()[0] - source_contours[-3].boundingBox()[2]

    # Get distance between the left component and shwa
    gap_shwa = min(source_contours[-1].boundingBox()[0], source_contours[-2].boundingBox()[0]) - source_contours[-3].boundingBox()[2]

    return gap_dot, gap_shwa

def GuessMarkToMarkGaps(unused, font):

    # This function uses a lot of heuristics to guess appropriate distance between
    # adjacent marks, when placed under the same consonant

    ADD_MISSING   = 0
    OVERWRITE_ALL = 1
    CANCEL        = 2
    CHOICE_MAP = { ADD_MISSING   : "Add missing gaps only",
                   OVERWRITE_ALL : "Recompute all gaps",
                   CANCEL        : "Cancel" }

    choice = fontforge.ask("", "Some gaps may have been already set",
                               tuple(value for (key, value) in sorted(CHOICE_MAP.items())),
                               ADD_MISSING, CANCEL)

    if choice == CANCEL:
        return

    def CHOICE_SetGlyphCommentProperty(glyph, gap_name, gap_value):
        if choice == OVERWRITE_ALL or utils.GetGlyphCommentProperty(glyph, gap_name) is None:
            utils.SetGlyphCommentProperty(glyph, gap_name, gap_value)

    # Compute gap to shwa vs gap to column adjustment. 
    adjust_column_to_shwa = (font["afii57799"].left_side_bearing + font["afii57799"].right_side_bearing) - \
                            (font["afii57839"].left_side_bearing + font["afii57839"].right_side_bearing)
    adjust_column_to_shwa /= 2

    # Compute gap to shwa vs gap to dot adjustment (shwa/dot on the right).
    # Inherently zero or negative.
    source_contours = font["afii57799"].foreground.dup() # shwa, sort top to bottom
    source_contours = sorted(source_contours, key=lambda c: c.boundingBox()[1], reverse=True)

    adjust_dot_to_shwa_base = min(0,
        source_contours[1].boundingBox()[0] - source_contours[0].boundingBox()[0])

    # Compute gap from shwa vs gap from dot adjustment (shwa/dot on the left).
    # Inherently zero or negative.
    adjust_dot_to_shwa_mark = min(0,
        source_contours[0].boundingBox()[2] - source_contours[1].boundingBox()[2])

    # ========== Bar to Dot gap ==========

    gap_bar_dot, gap_bar_shwa = GuessGapsFromHataf(font["afii57800"])

    CHOICE_SetGlyphCommentProperty(font["afii57793"], "BarToBaseMark", gap_bar_dot) # bar to dot
    CHOICE_SetGlyphCommentProperty(font["afii57798"], "DotToBaseMark", gap_bar_dot) # dot to bar
    CHOICE_SetGlyphCommentProperty(font["afii57799"], "BarToBaseMark", gap_bar_shwa) # bar to shwa
    CHOICE_SetGlyphCommentProperty(font["afii57839"], "BarToBaseMark", gap_bar_shwa - adjust_column_to_shwa) # bar to column

    # ========== Dot to Dot gap ==========

    gap_dot_dot, gap_dot_shwa = GuessGapsFromHataf(font["afii57801"])

    CHOICE_SetGlyphCommentProperty(font["afii57793"], "DotToBaseMark", gap_dot_dot)  # dot to dot
    CHOICE_SetGlyphCommentProperty(font["afii57799"], "DotToBaseMark", gap_dot_shwa) # dot to shwa
    CHOICE_SetGlyphCommentProperty(font["afii57839"], "DotToBaseMark", gap_dot_shwa - adjust_column_to_shwa) # dot to column

    adjust_dot_to_bar = gap_bar_dot - gap_dot_dot
    gap_shwa_column = gap_dot_shwa - adjust_column_to_shwa + adjust_dot_to_shwa_mark

    CHOICE_SetGlyphCommentProperty(font["afii57839"], "2DotsToBaseMark", gap_shwa_column) # shwa to column
    CHOICE_SetGlyphCommentProperty(font["afii57799"], "ColumnToBaseMark", gap_shwa_column) # column to shwa - HACK
    CHOICE_SetGlyphCommentProperty(font["afii57793"], "ColumnToBaseMark", gap_shwa_column - adjust_dot_to_shwa_base) # column to dot - HACK
    CHOICE_SetGlyphCommentProperty(font["afii57798"], "ColumnToBaseMark", gap_shwa_column - adjust_dot_to_shwa_base + adjust_dot_to_bar) # column to bar - HACK

    # ========== Narrow Dot to Dot gap ==========

    # From narrow tsere
    source_contours = font["tsere.narrow"].foreground.dup()
    source_contours = sorted(source_contours, key=lambda c: c.boundingBox()[2], reverse=False)

    narrow_gap_dot_dot = source_contours[1].boundingBox()[0] - source_contours[0].boundingBox()[2]
    CHOICE_SetGlyphCommentProperty(font["tsere.narrow"], "DotToBaseNarrowMark", narrow_gap_dot_dot) # dot to dot narrow

    # ========== Narrow Bar to Dot gap ==========

    narrow_gap_bar_dot, narrow_gap_bar_shwa = GuessGapsFromHataf(font["hatafpatah.narrow"])

    CHOICE_SetGlyphCommentProperty(font["tsere.narrow"], "BarToBaseNarrowMark", narrow_gap_bar_dot) # narrow bar to dot
    CHOICE_SetGlyphCommentProperty(font["afii57799"], "BarToBaseNarrowMark", narrow_gap_bar_shwa) # narrow bar to shwa

    # ========== Narrow Dot/Bar to Column gap ==========

    # Use gaps from hataf-meteg precomposed glyphs
    source_contours = font["uniE806"].foreground.dup() # hataf-meteg-tsere
    source_contours = sorted(source_contours, key=lambda c: c.boundingBox()[2], reverse=False)

    gap_dot_column = source_contours[3].boundingBox()[0] - source_contours[2].boundingBox()[2]

    CHOICE_SetGlyphCommentProperty(font["afii57839"], "DotToBaseNarrowMark", gap_dot_column) # narrow dot to column

    source_contours = font["uniE807"].foreground.dup() # patah-meteg-tsere
    source_contours = sorted(source_contours, key=lambda c: c.boundingBox()[2], reverse=False)

    gap_bar_column = source_contours[1].boundingBox()[0] - source_contours[0].boundingBox()[2]

    CHOICE_SetGlyphCommentProperty(font["afii57839"], "BarToBaseNarrowMark", gap_bar_column) # narrow bar to column

    narrow_gap_column_dot, narrow_gap_column_shwa = GuessGapsFromHataf(font["uniE807"])
    adjust_narrow_dot_to_bar = gap_bar_column - gap_dot_column

    CHOICE_SetGlyphCommentProperty(font["tsere.narrow"], "ColumnToBaseMark", narrow_gap_column_dot) # narrow column to dot
    CHOICE_SetGlyphCommentProperty(font["patah.narrow"], "ColumnToBaseMark", narrow_gap_column_dot + adjust_narrow_dot_to_bar) # narrow column to bar - HACK
    CHOICE_SetGlyphCommentProperty(font["afii57839"], "2DotsToBaseNarrowMark", narrow_gap_column_shwa) # narrow shwa to column - HACK


