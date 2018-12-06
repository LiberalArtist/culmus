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

my $file = shift;

die "Can't find file \"$file\""
  unless -f $file;

my $parser = XML::LibXML->new();
my $doc = $parser->parse_file($file);
my $total = 0;

my $xml_out = XML::LibXML::Document->new();
my $root_out = $xml_out->createElement('culmus');
$xml_out->setDocumentElement($root_out);


foreach my $page ($doc->getElementsByTagName('page'))
{
   my ($title, $title_re, $rev, $text, $key, $meaning, $trans, $expl, $redir, $var);

   $title = $page->getElementsByTagName('title')->item(0)
            ->getFirstChild->textContent;

   # Skip pages belonging to special namespaces
   next if $title =~ /:/;

   my @chars =  ($title =~ /./g); # convert $title to a list of chars
   $title_re = $chars[0];

   # For the purpose of matching, space can be interchanged with makaf.
   for (my $i = 1; $i < @chars; $i ++)
   {
      if ($chars[$i] =~ /[\s\x{05be}]/)
      {
#         $chars[$i] = "\[\\s\x{05be}\]";
      }
   }

   for (my $i = 1; $i < @chars; $i ++)
   {
      $title_re = $title_re . "[" . $diacritics . "]*" . $chars[$i];
   }

   $rev = $page->getElementsByTagName('revision')->item(0);
   $text = $rev->getElementsByTagName('text')->item(0)
            ->getFirstChild->textContent;

   my $key_out = $xml_out->createElement('key');
   my $keyword_out = $xml_out->createElement('keyword');
   my $keyword_text_out = XML::LibXML::Text->new($title);
   $keyword_out->appendChild($keyword_text_out);
   $key_out->appendChild($keyword_out);
   $root_out->appendChild($key_out);

   if ($text =~ /^#$hafnaya\s*\[\[(.*?)\]\]/s)
   {
      $redir = $1;
   }
   elsif ($text =~ /^#REDIRECT\s*\[\[(.*?)\]\]/s)
   {
      $redir = $1;
   }

   if ($redir)
   {
      if ($redir =~ /^(.*?)\#(.*?)$/s)
      {
         $redir = $1;
         $var = $2;
      }

      my $ref_out = $xml_out->createElement('reference');
      my $ref_text_out = XML::LibXML::Text->new($redir);
      $ref_out->appendChild($ref_text_out);
      $key_out->appendChild($ref_out);

      if ($var)
      {
         my $var_out = $xml_out->createElement('variant');
         my $var_text_out = XML::LibXML::Text->new($var);
         $var_out->appendChild($var_text_out);
         $key_out->appendChild($var_out);
      }

      next;
   }

   my @chunks = split (/(?<!=)==(?!=)/, $text);
   my %meanings;

   my $n_meanings = @chunks / 2;
   my $i;

   next if @chunks < 2; # Seems to be invalid page

   for ($i = 1; $i <=$n_meanings; $i ++)
   {
      $key = $chunks[$i*2 - 1];

      $chunks[$i*2-1] =~ /^\s*($title_re)/s;
      $meaning = $1;

      # skip if there is no translation.
      next if !($chunks[$i*2] =~ /\{\{$tav\|$anglit\|(.*?)\}\}/s);
      $trans = $1;

      # Clean the translation a bit
      $trans =~ s/\|/, /sg;

      $chunks[$i*2] =~ /\n\#\s*(.*?)\./s;
      $expl = $1;

      # Remove wiki markup from explanation.
      $expl =~ s/\[\[.*?\|(.*?)\]\]/$1/sg;
      $expl =~ s/[\[\]]//sg;

      # Further clean explanation.
      $expl =~ s/\{\{/\[/sg; # remove curled braces
      $expl =~ s/\}\}/\]/sg;
      $expl =~ s/<small>/\[/sg;
      $expl =~ s/<\/small>/\]/sg;

      my $var_out = $xml_out->createElement('variant');

      my $word_out = $xml_out->createElement('word');
      my $word_text_out = XML::LibXML::Text->new($meaning);
      $word_out->appendChild($word_text_out);

      my $def_out = $xml_out->createElement('definiton');
      my $def_text_out = XML::LibXML::Text->new($expl);
      $def_out->appendChild($def_text_out);

      my $trans_out = $xml_out->createElement('translation');
      my $en_out = $xml_out->createElement('en');
      my $en_text_out = XML::LibXML::Text->new($trans);
      $en_out->appendChild($en_text_out);
      $trans_out->appendChild($en_out);

      $var_out->appendChild($word_out);
      $var_out->appendChild($def_out);
      $var_out->appendChild($trans_out);

      $key_out->appendChild($var_out);

      $total++;
   }
}

print STDERR $total . "\n";

print $xml_out->toString;
