#!/usr/bin/perl -w
# Pattern Search - Update 2012 - Joern Marialke, Genecenter LMU
#
# Added : -sc Sequence Count constraint for degenerate Patterns
#########################################################################################

use strict;

#############################################
### variable declaration

my $DATABASES = "/cluster/databases/standard";

my $pattern;
my $database = "$DATABASES/nr";
my $outfile  = "result.out";

my $line;
my $count = 0;
my @org;
my @seq;
my $sequence  = "";
my $org_name  = "";
my $check     = 0;
my $grammatik = "pro";
my $temp;
my $sc = 1000; # Default value 1000 seqs

my $cr = chr(1000);

#############################################
### main program

# check arguments
if ( @ARGV < 2 ) {
	error();
	exit(1);
}

for ( my $i = 0 ; $i < @ARGV ; $i++ ) {

	if ( $ARGV[$i] eq "-i" ) {
		$pattern = $ARGV[ ++$i ];
	}
	elsif ( $ARGV[$i] eq "-d" ) {
		$database = $ARGV[ ++$i ];
	}
	elsif ( $ARGV[$i] eq "-o" ) {
		$outfile = $ARGV[ ++$i ];
	}
	elsif ( $ARGV[$i] eq "-reg" ) {
		$grammatik = "reg";
	}
	elsif ( $ARGV[$i] eq "-pro" ) {
                $grammatik = "pro";
        }
        elsif ( $ARGV[$i] eq "-sc" ) {
                $sc = $ARGV[ ++$i ];
        }

	else {
		print("\nERROR: Don't know this Argument: $ARGV[$i] \n\n");
		error();

		exit(1);
	}
}

open( OUT, ">$outfile" ) or die("Cannot open outfile!");

print OUT "Input Pattern: $pattern\n";

if ( $grammatik eq "pro" ) {

	# Make regular expression
	#print "Pattern prosite grammar: $pattern\n\n";
	$pattern =~ s/\{/[\^/g;
	$pattern =~ s/\}/]/g;
	$pattern =~ s/\(/\{/g;
	$pattern =~ s/\)/\}/g;
	$pattern =~ s/-//g;
	$pattern =~ s/[xX]/\./g;
	$pattern =~ s/</\^/g;
	$pattern =~ s/>/\$/g;
	$pattern =~ tr/[a-z]/[A-Z]/;
}
print OUT "Pattern regular expression: $pattern \n";
my @databases = split( / /, $database );

for ( my $i = 0 ; $i < scalar @databases ; $i++ ) {

	if ( $databases[$i] ne "" && length( $databases[$i] ) > 2 ) {
		if ( -e $databases[$i] ) {

			# Search in database
			print "\nSearch in database: $databases[$i]\n\n";
			open( IN, $databases[$i] ) or die("Cannot open database!");

			$/ = ">";    # set input field seperator
			while ( $line = <IN> ) {
				if ( $line eq ">" ) { next; }
				while ( $line =~ s/.>$// ) {
					$line .= " " . chr(001) . " " . <IN>;
				} # in the case that nameline contains a '>': '.' matches anything except '\n'
				$line =~ s/(.*)//
				  ; # divide into nameline and residues;'.' matches anything except '\n'
				$org_name = ">$1"
				  ; # don't move this line away from previous line $seq=~s/([^\n]*)//;
				$line =~ tr/A-Za-z//cd;	# remove all newlines, '.', '-'
				#$line =~ tr/\n> .-//d;    
				if ( $line =~ /$pattern/ ) {
					print "$org_name\n";
					push( @seq, $line );
					push( @org, $org_name );
				}
				last if @org == $sc;
			}
			close IN;
		} else {
			print "\n\nCannot open database: $databases[$i]\n\n";
		}
	}

}
for ( my $i = 0 ; $i < scalar @org ; $i++ ) {
	print OUT $org[$i] . "\n";
	print OUT $seq[$i] . "\n";
}

close OUT;

exit(0);

#####################################################
#### sub functions

sub error {
	print("Usage: search.pl [options]\n");
	print("\n");
	print("Options:\n\n");
	print("-i pattern\n\n");
	print("Optional:\n\n");
	print("-d Database (default: nr)\n");
	print("-o Output file (default: show on screen)\n");
	print("-reg Regular expression (default: prosite grammar)\n");
	print("-sc Sequence Count Constraint(default 100000)\n");
	print("\n");
}
