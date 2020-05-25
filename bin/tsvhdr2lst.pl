#!/usr/bin/perl -w

use strict;

use utf8;

binmode STDOUT,':utf8';
binmode STDIN,':utf8';
binmode STDERR,':utf8';

if($#ARGV == 1 && $ARGV[0] eq "-h") { 
print <<EOF;
usage: tsvhdr2lst.pl < infile.tsv > outfile.lst

converts a tab separated input file to a gazetteer list file 
where the separator character is also a tab.
The input file must contain the field names in the first row.

Example input:
entry	field1	field2
asdf	12	xxx
jkl	13	yyy

Output:
asdf	field1=12	field2=xxx
jkl	field1=13	field2=yyy
EOF
exit 0;
}
my $linenr = 0;
my @colnames = ();
my $entrycolnr = 0;
my $nrcols = 0;
while(<STDIN>) {
  $linenr++;
  chomp;
  if($linenr == 1) {
    ## process the header line
    @colnames = split(/\t/);
    $nrcols = (scalar @colnames);
    ## must have at least two columns
    if($nrcols < 2) {
      print STDERR "Not at least two columns but $nrcols in input file\n";
      exit(1);
    }
  } else {
    my @values = split(/\t/,$_,-1);
    if((scalar @values) != $nrcols) {
      print STDERR "Not exactly $nrcols columns in line $linenr: $_\n";
      exit(1);
    }
    print $values[$entrycolnr];
    for(my $i = 0; $i < (scalar @values); $i++) {
      print "\t",$colnames[$i],"=",$values[$i];
    }
    print "\n";
  }
}
print STDERR "Lines including the header: $linenr\n";
