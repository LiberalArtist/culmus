// Copyright (C) 2012 by Maxim Iorsh <iorsh@users.sourceforge.net>.
// Distributed under the term of the GNU General Public License version 2.

package org.culmus.nakdan;

import java.io.*;
import java.util.*;

public final class DictionaryDataReader // Singleton
{
    private static DictionaryDataReader instance = null;
    private HashMap<String, HashSet<LexicalItem>> dict = new HashMap();

    private DictionaryDataReader() {}

    public static DictionaryDataReader getInstance()
    {
	if (instance == null)
	{
	    instance = new DictionaryDataReader();
	    instance.load("nakdan.txt");

	    if (instance.dict.size() == 0)
		instance = null;
	}
	return instance;
    }

    private void load(String dictres)
    {
        // read dictionary data into list of strings
	InputStream stream = this.getClass().getResourceAsStream(dictres);
	if (stream == null)
	{
	    return;
	}

        Scanner scanner = new Scanner(stream, "UTF-8").useDelimiter("\n");
        ArrayList<String> list = new ArrayList<String>();

        while (scanner.hasNext())
        {
            list.add(scanner.next());
        }
        scanner.close();

        // produce map: word w/o diacritics -> list of matches with diacritics
        for (String entry : list)
        {
            String[] keyval = entry.split("\\|", 2);

            if (!dict.containsKey(keyval[0]))
                dict.put(keyval[0], new HashSet<LexicalItem>());

            Set lexitems = dict.get(keyval[0]);
            lexitems.add(new LexicalItem(entry));
        }
    }

    public Set<LexicalItem> get(String entry)
    {
	return dict.get(entry);
    }
}

