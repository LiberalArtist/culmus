#!/usr/bin/perl -C63

# This program has been created by Maxim Iorsh (iorsh@users.sourceforge.net).
# It is a PUBLIC DOMAIN - you may do with it anything you wish.

# 22-Aug-08 | iorsh@users.sourceforge.net | Created

use strict;
use integer;
use XML::LibXML;
use FileHandle;
binmode(STDOUT, ":utf8");

my $anglit = "\x{05d0}\x{05e0}\x{05d2}\x{05dc}\x{05d9}\x{05ea}";
my $diacritics = "\x{05b0}\x{05b1}\x{05b2}\x{05b3}\x{05b4}\x{05b5}\x{05b6}\x{05b7}\x{05b8}\x{05b9}\x{05ba}\x{05bb}\x{05bc}\x{05c1}\x{05c2}";
my $hafnaya = "\x{05d4}\x{05e4}\x{05e0}\x{05d9}\x{05d4}";
my $tav = "\x{05ea}";

my $file_synonyms = shift;
my $file_synsets = shift;

die "Can't find file \"$file_synonyms\""
  unless -f $file_synonyms;

die "Can't find file \"$file_synsets\""
  unless -f $file_synsets;

my $parser = XML::LibXML->new();
my $xml_synonyms = $parser->parse_file($file_synonyms);
my $xml_synsets = $parser->parse_file($file_synsets);
my $total = 0;

# This hash represents the contents of the 'synsets' tag of the 'synonyms' file.
# $translations{$lemma} = ($synset_id1, $english1, $synset_id2, $english2, ...);
my %translations;

foreach my $tag_synonym ($xml_synonyms->getElementsByTagName('synonym'))
{
   my ($lemma, $synset_id, $english);

   # Get synset id from parent 'synset' tag
   $synset_id = $tag_synonym->parentNode->getAttribute('id');

   $lemma = $tag_synonym->getElementsByTagName('lemma')->item(0)
                        ->getFirstChild->textContent;

   # Skip entity if there is no translation
   next if !$tag_synonym->getElementsByTagName('teqs');

   $english = $tag_synonym->getElementsByTagName('teqs')->item(0)
                          ->getFirstChild->textContent;

   # Clean the translation
   $english =~ s/_/ /sg;

   push @{ $translations{$lemma} }, $synset_id, $english;
}

# This hash represents the contents of the 'morpho' tag of the 'synonyms' file.
# $morphocards{$lemma} = ($ktiv_male, $ktiv_haser);
my %morphocards;

foreach my $tag_morphocard ($xml_synonyms->getElementsByTagName('morphocard'))
{
   my ($lemma, $ktiv_male, $ktiv_haser);

   $lemma = $tag_morphocard->getElementsByTagName('lemma')->item(0)
                           ->getFirstChild->textContent;

   next if !$tag_morphocard->getElementsByTagName('undotted');

   $ktiv_male = $tag_morphocard->getElementsByTagName('undotted')->item(0)
                               ->getFirstChild->textContent;

   $ktiv_haser = $tag_morphocard->getElementsByTagName('dotted_without_dots')->item(0)
                                ->getFirstChild->textContent;

   $morphocards{$lemma} = [ ($ktiv_male, $ktiv_haser) ];
}

# This hash represents the contents of the 'synsets' file.
# $glossaries{$synset_id} = $glossary;
my %glossaries;

foreach my $tag_synset ($xml_synsets->getElementsByTagName('synset'))
{
   my ($synset_id, $glossary);

   $synset_id = $tag_synset->getAttribute('id');

   next if !$tag_synset->getElementsByTagName('gloss');

   $glossary = $tag_synset->getElementsByTagName('gloss')->item(0)
                          ->getFirstChild->textContent;

   # Clean the glossary
   $glossary =~ s/&quot;/"/sg;
   $glossary =~ s/;.*//sg;

   $glossaries{$synset_id} = $glossary;
}

# Aggregation of all the dictionary data.
my %dictionary;

foreach my $lemma ( keys %translations )
{
   my ($synset_id, $english);
   my ($ktiv_male, $ktiv_haser);
   my ($glossary);

   my @lemma_synsets = @{ $translations{$lemma} };

   for (my $i = 0; $i < @lemma_synsets; $i += 2)
   {
      $synset_id = $lemma_synsets[$i];
      $english = $lemma_synsets[$i+1];

      # when the $lemma is prepended with exclamation sign,
      # its glossary definition is probably invalid. Skip the glossary,
      # but strip he exclamation sign and keep the entry anyway.
      if ($lemma =~ /$\!/)
      {
         $lemma =~ s/$\!//;
      }
      else
      {
         $glossary = $glossaries{$synset_id};
      }

      if ($morphocards{$lemma})
      {
          ($ktiv_male, $ktiv_haser) = @{ $morphocards{$lemma} };
      }
      else
      {
          # Create ktiv haser automatically
          $ktiv_haser = $lemma;
      }

      # Due to some glitch in databases ktiv male / haser is sometimes dotted
      $ktiv_haser =~ s/[$diacritics]//sg;
      $ktiv_male =~ s/[$diacritics]//sg;

      my @entry = ($synset_id, $lemma, $glossary, $english); 

      push @{ $dictionary{$ktiv_haser} }, [@entry];

      if ($ktiv_male && ($ktiv_male ne $ktiv_haser) )
      {
         push @{ $dictionary{$ktiv_male} }, [@entry];
      }
   }
}

# clean redundant and duplicate entries in the dictionary
foreach my $keyword ( keys %dictionary )
{
   my @entry_arr = @{ $dictionary{$keyword} };
   my ($nikud, $glossary, $english);

   # sort entries by $nikud, $english, reverse $glossary
   my @sorted_entry_arr = sort
      {
         return (@{$a}[1] cmp @{$b}[1]) if (@{$a}[1] cmp @{$b}[1]);
         return (@{$a}[3] cmp @{$b}[3]) if (@{$a}[3] cmp @{$b}[3]);
         return (@{$b}[2] cmp @{$a}[2]) if (@{$b}[2] cmp @{$a}[2]);
      }
      @entry_arr;

   # dispose duplicate entries (all fields same, or $nikud and $english same
   # while $glossary missing

   for (my $i = @entry_arr-2; $i >= 0; $i--)
   {
      if ( ($sorted_entry_arr[$i][1] eq $sorted_entry_arr[$i+1][1]) &&
           ($sorted_entry_arr[$i][3] eq $sorted_entry_arr[$i+1][3]) &&
           ( ($sorted_entry_arr[$i][2] eq $sorted_entry_arr[$i+1][2]) ||
             (!$sorted_entry_arr[$i+1][2]) ) )
      {
         splice @sorted_entry_arr, $i+1, 1;
      }
   }

   $dictionary{$keyword} = [@sorted_entry_arr];

   for (my $i = 0; $i < @sorted_entry_arr; $i++)
   {
      $nikud = $sorted_entry_arr[$i][1];
      $glossary = $sorted_entry_arr[$i][2];
      $english = $sorted_entry_arr[$i][3];
   }
}

# Create XML file in Culmus format
my $xml_out = XML::LibXML::Document->new();
my $root_out = $xml_out->createElement('culmus');
$xml_out->setDocumentElement($root_out);

# clean redundant and duplicate entries in the dictionary
foreach my $keyword ( sort keys %dictionary )
{
   my @entry_arr = @{ $dictionary{$keyword} };
   my ($nikud, $glossary, $english);

   my $key_out = $xml_out->createElement('key');
   my $keyword_out = $xml_out->createElement('keyword');
   my $keyword_text_out = XML::LibXML::Text->new($keyword);
   $keyword_out->appendChild($keyword_text_out);
   $key_out->appendChild($keyword_out);
   $root_out->appendChild($key_out);

   for (my $i = 0; $i < @entry_arr; $i++)
   {
      $nikud = $entry_arr[$i][1];
      $glossary = $entry_arr[$i][2];
      $english = $entry_arr[$i][3];

      my $var_out = $xml_out->createElement('variant');

      my $word_out = $xml_out->createElement('word');
      my $word_text_out = XML::LibXML::Text->new($nikud);
      $word_out->appendChild($word_text_out);

      my $trans_out = $xml_out->createElement('translation');
      my $en_out = $xml_out->createElement('en');
      my $en_text_out = XML::LibXML::Text->new($english);
      $en_out->appendChild($en_text_out);
      $trans_out->appendChild($en_out);

      $var_out->appendChild($word_out);

      if ($glossary)
      {
         my $def_out = $xml_out->createElement('definiton');
         my $def_text_out = XML::LibXML::Text->new($glossary);
         $def_out->appendChild($def_text_out);

         $var_out->appendChild($def_out);
      }

      $var_out->appendChild($trans_out);

      $key_out->appendChild($var_out);

      $total++;
   }
}

print STDERR $total . "\n";

print $xml_out->toString;

