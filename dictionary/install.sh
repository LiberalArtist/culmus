#! /bin/sh
# This program has been created by Maxim Iorsh (iorsh@users.sourceforge.net).
# It is a PUBLIC DOMAIN - you may do with it anything you wish.

# 22-Aug-08 | iorsh@users.sourceforge.net | Created
# 17-Sep-08 | iorsh@users.sourceforge.net | Added Hebrew WordNet
# 28-Nov-08 | iorsh@users.sourceforge.net | Added Waldstein's dictionary

cp wiktionary-he-eng.* /usr/share/dictd/
cp wordnet-he-eng.* /usr/share/dictd/
cp waldstein-he-eng.* /usr/share/dictd/
dictdconfig -w

echo 'Restarting dictd service...'
/etc/init.d/dictd restart
