#! /bin/sh
# This program has been created by Maxim Iorsh (iorsh@users.sourceforge.net).
# It is a PUBLIC DOMAIN - you may do with it anything you wish.

# 22-Aug-08 | iorsh@users.sourceforge.net | Created

cp wiktionary-he-eng.* /usr/share/dictd/
dictdconfig -w

echo 'Restarting dictd service...'
/etc/init.d/dictd restart
