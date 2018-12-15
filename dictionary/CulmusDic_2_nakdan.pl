#!/usr/bin/perl -C63

# This program has been created by Maxim Iorsh (iorsh@users.sourceforge.net).
# It is a PUBLIC DOMAIN - you may do with it anything you wish.

# 22-Aug-08 | iorsh@users.sourceforge.net | Created
# 04-Sep-08 | iorsh@users.sourceforge.net | Optional $definiton field,
#           |                             | simple progress indicator
# 17-Jun-09 | iorsh@users.sourceforge.net | Fixed for empty description
# 09-Jan-11 | iorsh@users.sourceforge.net | Fixed for empty translation
# 29-Jan-11 | iorsh@users.sourceforge.net | Cleanup, another minor xlat fix

use strict;
use integer;
use XML::LibXML;
use FileHandle;
binmode(STDOUT, ":utf8");

sub GetChildText;

my $file = shift;

die "Can't find file \"$file\""
  unless -f $file;

my $parser = XML::LibXML->new();
my $doc = $parser->parse_file($file);

foreach my $variant ($doc->getElementsByTagName('variant'))
{
   my $attr_category = $variant->getAttribute('category');
   my $attr_vargender = $variant->getAttribute('gender');
   my $attr_stress = $variant->getAttribute('stress');

   if (($attr_category ne "noun") and ($attr_category ne "adjective"))
   {
      next;
   }

   my $word = GetChildText($variant, 'word');
   my $ktiv_male = GetChildText($variant, 'ktiv_male');

   next if (!defined $word || !defined $ktiv_male);

   my $definition = GetChildText($variant, 'definiton');

   # Fetch declensions
   my @declensions = ($word); # 0-base, 1-pl, 2-sc, 3-plsc, 4-fem, 5-fempl, 6-femsc, 7-femplsc
   foreach my $decl ($variant->getElementsByTagName('decl'))
   {
      my $attr_constr = $decl->getAttribute('constr');
      my $attr_number = $decl->getAttribute('number');
      my $attr_gender = $decl->getAttribute('gender');

      my $idx = (($attr_number eq "plural") ? 1 : 0) +
                (($attr_constr eq "status constructus") ? 2 : 0) +
                (($attr_gender eq "feminine") ? 4 : 0);

      $declensions[$idx] = $decl->getFirstChild->textContent;
   }

   print $ktiv_male . "|" .
      (($attr_category eq "noun") ? "n" : ($attr_category eq "adjective") ? "a" : "-") .
      (($attr_vargender eq "masculine") ? "m" : ($attr_vargender eq "feminine") ? "f" : "-") .
      ((defined $attr_stress) ? $attr_stress : 0) .
      "|" . $definition .
      "|" . $declensions[0] . "|" . $declensions[1] . "|" . $declensions[2] . "|" . $declensions[3] . "\n";
   if (defined $declensions[4])
   {
#      print "--" . $declensions[4] . "|" . $declensions[5] . "|" . $declensions[6] . "|" . $declensions[7] . "\n";
   }
}

print STDERR "\n";

sub GetChildText
{
   my ($elem, $child) = @_;
   bless $elem, "XML::LibXML::Element";

   my $child_item = $elem->getElementsByTagName($child)->item(0);
   my $child_text;

   if ($child_item && $child_item->getFirstChild)
   {
      $child_text = $child_item->getFirstChild->textContent;
   }

   return $child_text;
}
