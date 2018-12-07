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

sub AddReferenceFromKey;
sub AddMeaningFromVariant;
sub AddReferencesToDictionary;

my $diacritics = "\x{05b0}\x{05b1}\x{05b2}\x{05b3}\x{05b4}\x{05b5}\x{05b6}\x{05b7}\x{05b8}\x{05b9}\x{05ba}\x{05bb}\x{05bc}\x{05c1}\x{05c2}";

my $file = shift;

die "Can't find file \"$file\""
  unless -f $file;

my $parser = XML::LibXML->new();
my $doc = $parser->parse_file($file);
my $counter = 0;
my $total = 0;

# This hash represents the entire dictionary.
# $synonim1 is actually the $key itself.
# $dictionary{$key} = (($synonim1, $synonim2, $synonim3,...),
#                      ($meaning1, $definition1, $translation1),
#                      ($meaning2, $definition2, $translation2),...);
my %dictionary;

# This array contains the references.
# $references[$i] = ($key_i, $reference_i, $meaning_i);
my @references;

foreach my $key ($doc->getElementsByTagName('key'))
{
   my ($heading, $reference_key, $meaning, $word, $definition, $en_trans);

   $heading = $key->getElementsByTagName('keyword')->item(0)
            ->getFirstChild->textContent;

   my @entry = ( [ $heading ], ); # array of arrays

   # Attemp to process the key as it were reference.
   # If successful, continue to the next key.
   next if AddReferenceFromKey (\@references, $key);

   foreach my $variant ($key->getElementsByTagName('variant'))
   {
      AddMeaningFromVariant (\@entry, $variant, $heading);
   }

   $dictionary{$heading} = [ @entry ] if $#entry > 0;
}

AddReferencesToDictionary(\@references, \%dictionary);

# Print in FOLDOC format
foreach my $key (keys %dictionary)
{
   my @entry = @{$dictionary{$key}};

   if ($#entry > 0)
   {
      print join("|", @{$entry[0]}) . "\n";

      for (my $i = 1; $i <= $#entry; $i++)
      {
         print " " . $entry[$i][0] . "\n";
         print "\t" . $entry[$i][1] . "\n" if $entry[$i][1];
         print "\t" . $entry[$i][2] . "\n";
      }
   }
}

print STDERR "\n";

sub AddReferenceFromKey
{
   my ($reference, $key) = @_;
   bless $key, "XML::LibXML::Element";

   return 0 if !$key->getElementsByTagName('reference');

   my $heading = $key->getElementsByTagName('keyword')->item(0)
                     ->getFirstChild->textContent;

   my $reference_key = $key->getElementsByTagName('reference')->item(0)
                           ->getFirstChild->textContent;

   my @ref_array = ($heading, $reference_key);

   if ($key->getElementsByTagName('variant'))
   {
      my $meaning = $key->getElementsByTagName('variant')->item(0)
                        ->getFirstChild->textContent;

      push @ref_array, $meaning;
   }

   push @$reference, [@ref_array];

   return 1;
}

sub AddMeaningFromVariant
{
   my ($entry, $variant, $heading) = @_;
   bless $variant, "XML::LibXML::Element";

   my $word_node = $variant->getElementsByTagName('word')->item(0)
                           ->getFirstChild;

   if (!defined $word_node)
   {
      print STDERR "Bad word in heading: " . $heading . "\n";
#     print STDERR "*[[" . $heading . "]]\n"; # For Wiktionary
      return 0;
   }

   my $word = $word_node->textContent;
   my $def_item = $variant->getElementsByTagName('definiton')->item(0);
   my $definition;

   if ($def_item && $def_item->getFirstChild)
   {
      $definition = $def_item->getFirstChild->textContent;
   }

   my $trans_item = $variant->getElementsByTagName('translation')->item(0);
   my $en_trans;

   # Skip if no English translation is present.
   if ($trans_item && $trans_item->getElementsByTagName('en') &&
       $trans_item->getElementsByTagName('en')->item(0)->getFirstChild)
   {
      $en_trans = $trans_item->getElementsByTagName('en')->item(0)
                             ->getFirstChild->textContent;
   }
   else
   {
#     print STDERR "Bad translation: " . $heading . "\n";
      return 0;
   }

   my @meaning = ($word, $definition, $en_trans);
   push @$entry, [@meaning];

   # This part is awfully slow. Give user some visual feedback.
   $counter++;
   print STDERR "." if !($counter%100);

   return 1;
}

sub AddReferencesToDictionary
{
   my ($references, $dictionary) = @_;

   # Add references as synonyms to the dictionary hash.
   for (my $i = 0; $i <= $#references; $i++)
   {
      my $heading = $references[$i][0];
      my $reference_key = $references[$i][1];

      next if !defined $dictionary{$reference_key};

      # When the reference entry has the form ($heading, $reference_key),
      # we treat the $reference_key as a synonym to the entire $heading
      # and simply add it to the list of synonyms in the appropriate
      # %dictionary entry.
      # When the reference entry has the form ($heading, $reference_key,
      # $meaning), we treat it as a synonym to a specific meaning of the
      # $heading. In this case a separate %dictionary entry must be created
      # which relates the $heading only to a part of the existing
      # $reference_key entry.

      if (scalar @{$references[$i]} == 2)
      {
         # Add to list of synonyms
         push @{$dictionary{$reference_key}[0]}, $heading;
      }
      else
      {
         my $meaning = $references[$i][2];
         my @ref_entry = @{$dictionary{$reference_key}};
         my @new_entry = ( [ $heading ], ); # array of arrays

         # Create a new entry and copy to it only relevant meanings.
         for (my $i = 1; $i <= $#ref_entry; $i++)
         {
            push @new_entry, [@{$ref_entry[$i]}] if ($ref_entry[$i][0] eq $meaning);
         }

         $dictionary{$heading} = [ @new_entry ];
      }
   }
}
