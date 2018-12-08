// Copyright (C) 2012 by Maxim Iorsh <iorsh@users.sourceforge.net>.
// Distributed under the term of the GNU General Public License version 2.

package org.ivrix.hspell;

import java.lang.String;
import java.util.List;
import java.util.ArrayList;

public class HSplit
{
    public String prefix;
    public String baseword;
    public int preflen;
    public int prefspec;
    public List<HLingData> lingdata = new ArrayList();

    public HSplit(String word, String baseword_, int preflen_, int prefspec_)
    {
        baseword = baseword_;
        preflen = preflen_;
        prefspec = prefspec_;

	prefix = word.substring(0, preflen_);
    }
}

