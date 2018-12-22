#!/usr/bin/perl -C63

# This program has been created by Maxim Iorsh (iorsh@users.sourceforge.net).
# It is a PUBLIC DOMAIN - you may do with it anything you wish.

# 22-Aug-08 | iorsh@users.sourceforge.net | Created
# 04-Sep-08 | iorsh@users.sourceforge.net | Optional $definiton field,
#           |                             | simple progress indicator
# 17-Jun-09 | iorsh@users.sourceforge.net | Fixed for empty description
# 09-Jan-11 | iorsh@users.sourceforge.net | Fixed for empty translation
# 29-Jan-11 | iorsh@users.sourceforge.net | Cleanup, another minor xlat fix
# 15-Dec-18 | iorsh@users.sourceforge.net | Process only single Hebrew words
# 22-Dec-18 | iorsh@users.sourceforge.net | Added verbs with conjugations

use strict;
use integer;
use utf8;   # This script contains unicode characters
use XML::LibXML;
use FileHandle;
binmode(STDOUT, ":utf8");

sub GetChildText;

my $file = shift;

die "Can't find file \"$file\""
  unless -f $file;

my $parser = XML::LibXML->new();
my $doc = $parser->parse_file($file);
my @tenses = ('past', 'present', 'future', 'imperative', 'infinitive');
my @binyanim = ('kal', 'nifal', 'hifil', 'hufal', 'piel', 'pual', 'hitpael');
my %verbs;

foreach my $verb ($doc->getElementsByTagName('verb'))
{
   my $binyan = $verb->getAttribute('binyan');
   my @conjugations;

   foreach my $conj ($verb->getElementsByTagName('conjugation'))
   {
      my $tense = $conj->getAttribute('tense');
      my ($tense_index) = grep { $tenses[$_] eq $tense } (0 .. @tenses-1);
      $conjugations[$tense_index] = $conj->getFirstChild->textContent;
   }

   # Put binyan beyond the last tense
   $conjugations[@tenses] = $binyan;

   $verbs{$conjugations[0]} = \@conjugations;
}

foreach my $variant ($doc->getElementsByTagName('variant'))
{
   my $attr_category = $variant->getAttribute('category');
   my $attr_vargender = $variant->getAttribute('gender');
   my $attr_stress = $variant->getAttribute('stress');

   if (($attr_category ne "noun") and ($attr_category ne "adjective") and ($attr_category ne "verb"))
   {
      next;
   }

   my $word = GetChildText($variant, 'word');
   my $ktiv_male = GetChildText($variant, 'ktiv_male');

   next if (!defined $word || !defined $ktiv_male);

   # Process only single Hebrew words
   next if ($ktiv_male !~ /^[א-ת\'\"\x{05F3}\x{05F4}]+$/);

   my $definition = GetChildText($variant, 'definiton');

   # Process verbs
   if ($attr_category eq "verb")
   {
      if ($verbs{$word})
      {
         my @conjugs = @{$verbs{$word}};
         my $binyan = $conjugs[@tenses];
         my ($binyan_index) = grep { $binyanim[$_] eq $binyan } (0 .. @binyanim-1);

         print $ktiv_male .
            "|" . "v" . $binyan_index. "-" .
            "|" . $definition .
            "|" . $conjugs[0] . "|" . $conjugs[1] . "|" . $conjugs[2] . "|" . $conjugs[3] . "|" . $conjugs[4] . "\n";
      }
      next;
   }

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
