#!/usr/bin/perl -C63

# This program has been created by Maxim Iorsh (iorsh@users.sourceforge.net).
# It is a PUBLIC DOMAIN - you may do with it anything you wish.

# 22-Aug-08 | iorsh@users.sourceforge.net | Created
# 27-Apr-08 | iorsh@users.sourceforge.net | Fixed for empty entries
# 29-Jan-11 | iorsh@users.sourceforge.net | Fixed extraction of description,
#           |                             | more lexical data extracted.

use strict;
use integer;
use utf8;   # This script contains unicode characters
use XML::LibXML;
use FileHandle;
binmode(STDOUT, ":utf8");

sub AddTranslationToXml;
sub AddDeclensionsToXml;
sub GetDescription;
sub GetNituah;
sub GetDeclensions;

sub WikiPrint;

my $WIKIAUTOLIST = 0;

my $diacritics = "\x{05b0}\x{05b1}\x{05b2}\x{05b3}\x{05b4}\x{05b5}\x{05b6}\x{05b7}\x{05b8}\x{05b9}\x{05ba}\x{05bb}\x{05bc}\x{05c1}\x{05c2}";
my $hebletters = qr/[\x{05d0}-\x{05ea}]/;
my $shinsindot = qr/[\x{05c1}\x{05c2}]/;
my $hebchar = qr/[\x{05b0}-\x{05ea}]/;
my $hafnaya = "\x{05d4}\x{05e4}\x{05e0}\x{05d9}\x{05d4}";

my $re_root = qr/(${hebletters}${shinsindot}?)[-־](${hebletters}${shinsindot}?)[-־](${hebletters}${shinsindot}?)/;

my $file = shift;

die "Can't find file \"$file\""
  unless -f $file;

my $parser = XML::LibXML->new();
my $doc = $parser->parse_file($file);
my $total = 0;

my $xml_out = XML::LibXML::Document->new();
my $root_out = $xml_out->createElement('culmus');
$xml_out->setDocumentElement($root_out);

our ($title, $key); # We want to use WikiPrint from anywhere

foreach my $page ($doc->getElementsByTagName('page'))
{
   my ($title_re, $rev, $block, $text, $meaning, $trans, $expl, $redir, $var);

#next if $total > 100;

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
   $block = $rev->getElementsByTagName('text')->item(0)->getFirstChild;

   # Skip empty entries
   next if (!$block);

   $text = $block->textContent;

   my $key_out = $xml_out->createElement('key');
   $key_out->appendTextChild("keyword", $title);

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

      $key_out->appendTextChild("reference", $redir);
      $key_out->appendTextChild("variant", $var) if $var;

      $root_out->appendChild($key_out);
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

      next if ($chunks[$i*2-1] !~ /^\s*($title_re)/s);

      $meaning = $1;

      my $var_out = $xml_out->createElement('variant');

      AddTranslationToXml($var_out, $chunks[$i*2], "אנגלית", "en");
      AddTranslationToXml($var_out, $chunks[$i*2], "רוסית", "ru");

      $expl = GetDescription($chunks[$i*2]);
      my $nituah_hash_ref = GetNituah($chunks[$i*2]);

      AddDeclensionsToXml($var_out, $nituah_hash_ref->{"nituah"});

      $var_out->setAttribute("category", $nituah_hash_ref->{"category"}) if $nituah_hash_ref->{"category"};
      $var_out->setAttribute("gender", $nituah_hash_ref->{"gender"}) if $nituah_hash_ref->{"gender"};
      $var_out->appendTextChild("word", $meaning) if $meaning;
      $var_out->appendTextChild("definiton", $expl) if $expl;
      $var_out->appendTextChild("nituah", $nituah_hash_ref->{"nituah"}) if $nituah_hash_ref->{"nituah"};
      $var_out->appendTextChild("ktiv_male", $nituah_hash_ref->{"ktiv_male"}) if $nituah_hash_ref->{"ktiv_male"};
      $var_out->appendTextChild("root", $nituah_hash_ref->{"root"}) if $nituah_hash_ref->{"root"};
      $var_out->getElementsByTagName("root")->item(0)
                 ->setAttribute("var", $nituah_hash_ref->{"rootvar"}) if $nituah_hash_ref->{"rootvar"};
      $var_out->appendTextChild("mishkal", $nituah_hash_ref->{"mishkal"}) if $nituah_hash_ref->{"mishkal"};

      $key_out->appendChild($var_out);

      $total++;
   }

   $root_out->appendChild($key_out);
}

print STDERR $total . "\n";

print $xml_out->toString;

sub WikiPrint
{
   my ($user_string) = @_;
   my $local_key = $key;

   $local_key =~ s/\{\{.*?\|//sg;
   $local_key =~ s/\}\}//sg;
   print STDERR "*[[".$title."#".$local_key."|".$local_key."]]".$user_string."\n";
}

sub GetDescription
{
   my ($chunk) = @_;
   my $desc;

   # Match description: separate line beginning with '#' symbol
   $chunk =~ /^\#\s*(.*?)$/m;
   $desc = $1;

   # Discard anything after the first dot, with the dot.
   $desc =~ s/\..*$//s;

   # Remove wiki markup from description.
   $desc =~ s/\[\[[^\[\]]*?\|(.*?)\]\]/$1/sg;
   $desc =~ s/[\[\]]//sg;

#      if ($WIKIAUTOLIST)
#      { 
#         if ($desc =~ /<small>(.*?)<\/small>/si)
#         {
#            my $tag = $1;
#            $key =~ s/\{\{.*?\|//sg;
#            $key =~ s/\}\}//sg;
#            print STDERR "*[[".$title."#".$key."|".$key."]] <small>".$tag."</small>\n";
#         }
#      }

   # Further clean description.
   $desc =~ s/\{\{משלב\|/\[/sg;
   $desc =~ s/\{\{רובד\|/\[/sg;
   $desc =~ s/\{\{/\[/sg; # remove curled braces
   $desc =~ s/\}\}/\]/sg;
   $desc =~ s/<small>/\[/sig;
   $desc =~ s/<\/small>/\]/sig;

   return $desc;
}

sub GetCategory
{
   my ($nituah) = @_;

   return if ($nituah !~ /\|\s*חלק\sדיבר=(.*?)\|/s);

   my $category = $1;

   return "noun"        if ($category =~ /שם[־-\s]עצם/);
   return "verbal noun" if ($category =~ /שם[־-\s]פעולה/);
   return "adjective"   if ($category =~ /שם[־-\s]תואר/);
   return "adposition"  if ($category =~ /מילת[־-\s]יחס/);
   return "compound"    if ($category =~ /צרף/);
}

sub GetGender
{
   my ($nituah) = @_;

   return if ($nituah !~ /\|\s*מין=(.*?)\|/s);

   my $gender = $1;

   return "both"      if ($gender =~ /זכר/ && $gender =~ /נקבה/);
   return "masculine" if ($gender =~ /זכר/);
   return "feminine"  if ($gender =~ /נקבה/);
}

sub GetDeclensions
{
   my ($declensions) = @_;
   my $decl_hash_ref;

   my @decls = split(/[,;]/, $declensions);

   foreach (@decls)
   {
      # 'm' - masculine, 'f' - feminine
      # 's' - singular, 'p' - plural
      # 'c' - status constructus
      if (/ס"ר\s*(${hebchar}+)־/)
      {
         $decl_hash_ref->{"pc"} = $1;
      }
      elsif (/נ"ר\s*(${hebchar}+)/)
      {
         $decl_hash_ref->{"fp"} = $1;
      }
      elsif (/נ'\s*(${hebchar}+)/)
      {
         $decl_hash_ref->{"f"} = $1;
      }
      elsif (/ר'\s*(${hebchar}+)־/)
      {
         $decl_hash_ref->{"pc"} = $1;
      }
      elsif (/ס'\s*(${hebchar}+)־/)
      {
         $decl_hash_ref->{"c"} = $1;
      }
      elsif (/ר'\s*(${hebchar}+)/)
      {
         $decl_hash_ref->{"p"} = $1;
      }
      elsif (/(${hebchar}+)־/)
      {
         $decl_hash_ref->{"c"} = $1;
      }
   }

   my $detected = ($decl_hash_ref->{"pc"} ? 1 : 0) +
                  ($decl_hash_ref->{"fp"} ? 1 : 0) +
                  ($decl_hash_ref->{"f"} ? 1 : 0) +
                  ($decl_hash_ref->{"c"} ? 1 : 0) +
                  ($decl_hash_ref->{"p"} ? 1 : 0);

   if ($detected < @decls)
   {
      WikiPrint();
   }

   return $decl_hash_ref;
}

sub GetNituah
{
   my ($chunk) = @_;
   my $nituah_hash_ref;

   # Modified example from (?>pattern), perlre 5.8.8
   if ( $chunk =~ /\{\{ניתוח\sדקדוקי
	((
	(?> [^{}]+ )
	|
	\{\{ [^{}]* \}\}
	)+)
	\}\}
	/sx)
   {
      my $nituah = $1;

      $nituah_hash_ref->{"nituah"} = $nituah;

      $nituah_hash_ref->{"category"} = GetCategory($nituah);
      $nituah_hash_ref->{"gender"} = GetGender($nituah);

      if ($nituah =~ /\|\s*כתיב\sמלא=(.*?)\s*\|/s)
      {
         $nituah_hash_ref->{"ktiv_male"} = $1;
      }

      if ($nituah =~ /$re_root/s)
      {
         $nituah_hash_ref->{"root"} = $1.$2.$3;
         if ($nituah =~ /\{\{שרש\|[א-ת]*\s([א-ת])\|/s)
         {
            $nituah_hash_ref->{"rootvar"} = ord($1)-ord("א")+1; # "א" => 1, "ב" => 2 etc.
         }
      }

      if ($nituah =~ /\{\{משקל\|(.*?)\}\}/s)
      {
         $nituah_hash_ref->{"mishkal"} = $1;
      }
   }

   return $nituah_hash_ref;
}

sub AddTranslationToXml
{
   my ($var_out, $chunk, $heb_locale, $locale) = @_;
   bless $var_out, "XML::LibXML::Element";

   # return if there is no translation.
   return 0 if !($chunk =~ /\{\{ת\|$heb_locale\|(.*?)\}\}/s);

   my $trans = $1;

   # Clean the translation a bit
   $trans =~ s/\|/, /sg;

   # <translation>...</translation>
   my $trans_out = $var_out->getElementsByTagName('translation')->item(0);

   if (!$trans_out)
   {
      $trans_out = $var_out->ownerDocument->createElement('translation');
      $var_out->appendChild($trans_out);
   }

   # <en>melon</en>
   $trans_out->appendTextChild($locale, $trans);

   return 1;
}

sub AddDeclensionsToXml
{
   my ($var_out, $nituah) = @_;
   bless $var_out, "XML::LibXML::Element";

   # return if there are no declensions.
   return 0 if ($nituah !~ /\|\s*נטיות=(.*?)\s*$/s);

   my $declensions = $1;
   return 0 if !$declensions;

   # <declensions>...</declensions>
   my $decls_out = $var_out->ownerDocument->createElement('declensions');
   $decls_out->appendTextNode($declensions);

   my $decl_ref = GetDeclensions($declensions);

   foreach my $k (keys %$decl_ref)
   {
      my $decl_out = $var_out->ownerDocument->createElement('decl');
      $decl_out->appendTextNode($decl_ref->{$k});

      $decl_out->setAttribute("gender", "feminine") if $k =~ /f/;
      $decl_out->setAttribute("number", "plural") if $k =~ /p/;
      $decl_out->setAttribute("constr", "status constructus") if $k =~ /c/;

      $decls_out->appendChild($decl_out);
   }

   $var_out->appendChild($decls_out);

   return 1;
}
