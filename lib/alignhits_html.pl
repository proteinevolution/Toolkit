#! /usr/bin/perl -w
# Extract a multiple alignment of hits from Blast or PsiBlast output
# Usage:   alignhits.pl [options] blast.out alignment-file

# Remark: because blast with -B option stumbles over u and U, all selenocysteines are changed into cysteines

my $rootdir;
BEGIN {
   if (defined $ENV{TK_ROOT}) {$rootdir=$ENV{TK_ROOT};} else {$rootdir="/cluster";}
};

use strict;
use lib "../lib";
use MyPaths; 
use constant ROUND_START_PATTERN => qr/Results from round\s+(\d+)/;
use constant END_OF_HEADER => qr/(No hits found)|(^Sequences)/;

# Default options
my $E_max      = 1E-4;
my $E_min      = -1.0;
my $P_max      = 1;
my $cov_thrshd = 0;
my $sc_thrshd  = -10;
my $qid_thrshd = 0;
my $bl         = -10; # minimum per-residue bit score with query at ends of HSP for loose end pruning
my $bs = -10; # minimum per-residue bit score with query at ends of HSP for strict end pruning
my $bg = 50; # below this number of end gaps the loose HSP pruning score is used
my $outformat  = "psi";
my $append     = 0;
my $query_file = "";
my $useblastplus = 0; # set to 1 if blastplus should be used internally
my $infile;
my $outfile;
my $v = 2;

sub fexit() {
	print("\nExtract a multiple alignment of hits from Blast or PsiBlast output (as text file, not html)\n");
	print("Usage:   alignhits.pl blast-file alignment-file [options]\n");
	print("Options for thresholds\n");
	print("  -e   e-value  : maximum e-value (default=$E_max)\n");
	print("  -qid percent  : minimum sequence identity to query in % (default=$qid_thrshd) \n");
	print("                  (seq-id = # identities in match columns / # hit residues in match columns)\n");
	print("  -cov coverage : minimum coverage in % (default=$cov_thrshd) \n");
	print("  -emin e-value : minimum e-value (default=$E_min)\n");
	print("Options for output format:\n");
	print("  -psi          : PsiBlast-readable format; inserts relative to query (=first) sequence omitted, \n");
	print("                  capitalization of residues same as query sequence (default)\n");
	print("  -a2m          : like FASTA, but capitalization of residues same as query sequence,\n");
	print("                  deletes '-', gaps aligned to lower case columns '.'\n");
	print("  -a3m          : like -a2m, but gaps aligned to inserts omitted\n");
	print("  -ufas         : unaligned fasta format (without gaps)\n");
	print("  -fas          : aligned fasta; all residues upper case, all gaps '-'\n");
	print("  -no_link      : delete all links in namelines \n");
	print("Other options:\n");
	print("  -v            : verbose mode (default=off)\n");
	print("  -append       : append output to file (default=overwrite)\n");
	print("  -best         : extract only the best HSP per sequence (default=off)\n");
	print("  -q   file     : insert a2m-formatted  query sequence into output alignment;\n");
	print("                  upper/lower case determines match/insert columns\n");
	print("  -Q   file     : like -q, but all query residues will be match states (upper case)\n");
	print("  -p   p-value  : maximum p-value of HSP IN MATCH COLUMNS (with query or -P alignment) (default=$P_max)\n");
	print("  -s/c value    : minimum score per column in 1/3 bits (with query or -P alignment) (default=$sc_thrshd) \n");
	print("  -b   float    : HSP pruning: min per-residue score in 1/3 bits (with query or -B alignment) at ends of HSPs\n");
	print("  -bl  float    : loose HSP pruning: min per-residue score in 1/3 bits (with query or -B alignment)\n");
	print("                  at ends of HSPs. Used when number of endgaps at the one end < bg (see -bg) (default=$bl)\n");
	print("  -bs  float    : strict HSP pruning: like -b, but used when number of endgaps >= bg (default=$bs)\n");
	print("  -bg  int      : below this number of end gaps the loose HSP pruning score is used,\n");
	print("                :  above the strict score is employed (default=$bg)\n");
	print("  -P   file     : read alignment file (in psiblast-readable format) and calculate PSSM\n");
	print("                  to be used with option -p (only in conjunction with -q or -Q options)\n");
	print("  -B   file     : read alignment file (in psiblast-readable format) and calculate PSSM\n");
	print("                  to be used with option -b (only in conjunction with -q or -Q options)\n");
	print("  -qs  start    : query residue defining the start of the HSP region to be used\n");
	print("  -qe  end      : query residue defining the end of the HSP region to be used\n");
	print("  -blastplus    : use blastplus family of programs instead of legacy blast\n");
	print("\n");
	print("Examples: \n");
	print("alignhits.pl 1enh.out 1enh.psi\n");
	print("alignhits.pl 1enh.out 1enh.a3m -e 1E-4 -cov 50 -s/c 1 -a2m\n");
	print("\n");
	exit;
}

# Activate autoflushing on STDOUT
$| = 1;

# internal  A  B  C  D  E  F  G  H  I  J  K  L  M  N  O  P  Q  R  S  T  U  V  W  X  Y  Z
my @ch2psi = (
	1,  4,  3,  4,  5,  6,  7, 8,  9,  21, 10, 11, 12, 13,
	21, 14, 15, 16, 17, 18, 3, 19, 20, 21, 22, 5
);

#           0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25
# psi       ?  A  B  C  D  E  F  G  H  I  K  L  M  N  P  Q  R  S  T  V  W  X  Y  Z  ?  ?

#           A  B  C  D  E  F  G  H  I  J  K  L  M  N  O  P  Q  R  S  T  U  V  W  X  Y  Z
my @ch2i = (
	0,  3,  4, 3, 6,  13, 7, 8,  9,  20, 11, 10, 12, 2,
	20, 14, 5, 1, 15, 16, 4, 19, 17, 20, 18, 6
);

#  0    1    2    3    4    5    6    7    8    9   10   11    12  13   14   15   16   17   18   19   20
#  A    R    N    D    C    Q    E    G    H    I    L    K    M    F    P    S    T    W    Y    V    X
#   The Gonnet matrix is in units of 10*log10()
my @GONNET = (
	[
		2.4,  -0.6, -0.3, -0.3, 0.5,  -0.2, 0.0, 0.5,
		-0.8, -0.8, -1.2, -0.4, -0.7, -2.3, 0.3, 1.1,
		0.6,  -3.6, -2.2, 0.1,  0.0
	],    # A
	[
		-0.6, 4.7,  0.3,  -0.3, -2.2, 1.5,  0.4,  -1.0,
		0.6,  -2.4, -2.2, 2.7,  -1.7, -3.2, -0.9, -0.2,
		-0.2, -1.6, -1.8, -2.0, 0.0
	],    # R
	[
		-0.3, 0.3,  3.8,  2.2,  -1.8, 0.7,  0.9,  0.4,
		1.2,  -2.8, -3.0, 0.8,  -2.2, -3.1, -0.9, 0.9,
		0.5,  -3.6, -1.4, -2.2, 0.0
	],    # N
	[
		-0.3, -0.3, 2.2,  4.7,  -3.2, 0.9,  2.7,  0.1,
		0.4,  -3.8, -4.0, 0.5,  -3.0, -4.5, -0.7, 0.5,
		0.0,  -5.2, -2.8, -2.9, 0.0
	],    # D
	[
		0.5,  -2.2, -1.8, -3.2, 11.5, -2.4, -3.0, -2.0,
		-1.3, -1.1, -1.5, -2.8, -0.9, -0.8, -3.1, 0.1,
		-0.5, -1.0, -0.5, 0.0,  0.0
	],    # C
	[
		-0.2, 1.5,  0.7,  0.9,  -2.4, 2.7,  1.7,  -1.0,
		1.2,  -1.9, -1.6, 1.5,  -1.0, -2.6, -0.2, 0.2,
		0.0,  -2.7, -1.7, -1.5, 0.0
	],    # Q
	[
		0.0,  0.4,  0.9,  2.7,  -3.0, 1.7,  3.6,  -0.8,
		0.4,  -2.7, -2.8, 1.2,  -2.0, -3.9, -0.5, 0.2,
		-0.1, -4.3, -2.7, -1.9, 0.0
	],    # E
	[
		0.5,  -1.0, 0.4,  0.1,  -2.0, -1.0, -0.8, 6.6,
		-1.4, -4.5, -4.4, -1.1, -3.5, -5.2, -1.6, 0.4,
		-1.1, -4.0, -4.0, -3.3, 0.0
	],    # G
	[
		-0.8, 0.6,  1.2,  0.4,  -1.3, 1.2,  0.4,  -1.4,
		6.0,  -2.2, -1.9, 0.6,  -1.3, -0.1, -1.1, -0.2,
		-0.3, -0.8, -2.2, -2.0, 0.0
	],    # H
	[
		-0.8, -2.4, -2.8, -3.8, -1.1, -1.9, -2.7, -4.5,
		-2.2, 4.0,  2.8,  -2.1, 2.5,  1.0,  -2.6, -1.8,
		-0.6, -1.8, -0.7, 3.1,  0.0
	],    # I
	[
		-1.2, -2.2, -3.0, -4.0, -1.5, -1.6, -2.8, -4.4,
		-1.9, 2.8,  4.0,  -2.1, 2.8,  2.0,  -2.3, -2.1,
		-1.3, -0.7, 0.0,  1.8,  0.0
	],    # L
	[
		-0.4, 2.7,  0.8,  0.5,  -2.8, 1.5,  1.2,  -1.1,
		0.6,  -2.1, -2.1, 3.2,  -1.4, -3.3, -0.6, 0.1,
		0.1,  -3.5, -2.1, -1.7, 0.0
	],    # K
	[
		-0.7, -1.7, -2.2, -3.0, -0.9, -1.0, -2.0, -3.5,
		-1.3, 2.5,  2.8,  -1.4, 4.3,  1.6,  -2.4, -1.4,
		-0.6, -1.0, -0.2, 1.6,  0.0
	],    # M
	[
		-2.3, -3.2, -3.1, -4.5, -0.8, -2.6, -3.9, -5.2,
		-0.1, 1.0,  2.0,  -3.3, 1.6,  7.0,  -3.8, -2.8,
		-2.2, 3.6,  5.1,  0.1,  0.0
	],    # F
	[
		0.3,  -0.9, -0.9, -0.7, -3.1, -0.2, -0.5, -1.6,
		-1.1, -2.6, -2.3, -0.6, -2.4, -3.8, 7.6,  0.4,
		0.1,  -5.0, -3.1, -1.8, 0.0
	],    # P
	[
		1.1,  -0.2, 0.9,  0.5,  0.1,  0.2,  0.2, 0.4,
		-0.2, -1.8, -2.1, 0.1,  -1.4, -2.8, 0.4, 2.2,
		1.5,  -3.3, -1.9, -1.0, 0.0
	],    # S
	[
		0.6,  -0.2, 0.5,  0.0, -0.5, 0.0,  -0.1, -1.1,
		-0.3, -0.6, -1.3, 0.1, -0.6, -2.2, 0.1,  1.5,
		2.5,  -3.5, -1.9, 0.0, 0.0
	],    # T
	[
		-3.6, -1.6, -3.6, -5.2, -1.0, -2.7, -4.3, -4.0,
		-0.8, -1.8, -0.7, -3.5, -1.0, 3.6,  -5.0, -3.3,
		-3.5, 14.2, 4.1,  -2.6, 0.0
	],    # W
	[
		-2.2, -1.8, -1.4, -2.8, -0.5, -1.7, -2.7, -4.0,
		-2.2, -0.7, 0.0,  -2.1, -0.2, 5.1,  -3.1, -1.9,
		-1.9, 4.1,  7.8,  -1.1, 0.0
	],    # Y
	[
		0.1,  -2.0, -2.2, -2.9, 0.0, -1.5, -1.9, -3.3,
		-2.0, 3.1,  1.8,  -1.7, 1.6, 0.1,  -1.8, -1.0,
		0.0,  -2.6, -1.1, 3.4,  0.0
	],    # V
	[
		0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
		0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
	]     # X
);

# A  R  N  D  C  Q  E  G  H  I  L  K  M  F  P  S  T  W  Y  V  X
my @BLOSUM62 = (
	[
		4,  -1, -2, -2, 0, -1, -1, 0, -2, -1, -1, -1,
		-1, -2, -1, 1,  0, -3, -2, 0, 0
	],
	[
		-1, 5,  0,  -2, -3, 1,  0,  -2, 0, -3, -2, 2,
		-1, -3, -2, -1, -1, -3, -2, -3, -1
	],
	[
		-2, 0, 6, 1, -3, 0, 0, 0, 1, -3, -3, 0, -2, -3, -2, 1, 0, -4, -2, -3, -1
	],
	[
		-2, -2, 1,  6, -3, 0,  2,  -1, -1, -3, -4, -1,
		-3, -3, -1, 0, -1, -4, -3, -3, -1
	],
	[
		0,  -3, -3, -3, 9,  -3, -4, -3, -3, -1, -1, -3,
		-1, -2, -3, -1, -1, -2, -2, -1, -2
	],
	[
		-1, 1,  0,  0, -3, 5,  2,  -2, 0, -3, -2, 1,
		0,  -3, -1, 0, -1, -2, -1, -2, -1
	],
	[
		-1, 0,  0,  2, -4, 2,  5,  -2, 0, -3, -3, 1,
		-2, -3, -1, 0, -1, -3, -2, -2, -1
	],
	[
		0,  -2, 0,  -1, -3, -2, -2, 6,  -2, -4, -4, -2,
		-3, -3, -2, 0,  -2, -2, -3, -3, -1
	],
	[
		-2, 0,  1,  -1, -3, 0,  0, -2, 8, -3, -3, -1,
		-2, -1, -2, -1, -2, -2, 2, -3, -1
	],
	[
		-1, -3, -3, -3, -1, -3, -3, -4, -3, 4, 2, -3,
		1,  0,  -3, -2, -1, -3, -1, 3,  -1
	],
	[
		-1, -2, -3, -4, -1, -2, -3, -4, -3, 2, 4, -2,
		2,  0,  -3, -2, -1, -2, -1, 1,  -1
	],
	[
		-1, 2,  0,  -1, -3, 1,  1,  -2, -1, -3, -2, 5,
		-1, -3, -1, 0,  -1, -3, -2, -2, -1
	],
	[
		-1, -1, -2, -3, -1, 0,  -2, -3, -2, 1, 2, -1,
		5,  0,  -2, -1, -1, -1, -1, 1,  -1
	],
	[
		-2, -3, -3, -3, -2, -3, -3, -3, -1, 0, 0, -3,
		0,  6,  -4, -2, -2, 1,  3,  -1, -1
	],
	[
		-1, -2, -2, -1, -3, -1, -1, -2, -2, -3, -3, -1,
		-2, -4, 7,  -1, -1, -4, -3, -2, -2
	],
	[
		1, -1, 1, 0, -1, 0, 0, 0, -1, -2, -2, 0, -1, -2, -1, 4, 1, -3, -2, -2, 0
	],
	[
		0,  -1, 0,  -1, -1, -1, -1, -2, -2, -1, -1, -1,
		-1, -2, -1, 1,  5,  -2, -2, 0,  0
	],
	[
		-3, -3, -4, -4, -2, -2, -3, -2, -2, -3, -2, -3,
		-1, 1,  -4, -3, -2, 11, 2,  -3, -2
	],
	[
		-2, -2, -2, -3, -2, -1, -2, -3, 2, -1, -1, -2,
		-1, 3,  -3, -2, -2, 2,  7,  -1, -1
	],
	[
		0, -3, -3, -3, -1, -2, -2, -3, -3, 3, 1, -2,
		1, -1, -2, -2, 0,  -3, -1, 4,  -1
	],
	[
		0,  -1, -1, -1, -2, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -2, 0,  0,  -2, -1, -1, -1
	]
);

###################################################################################################
# Process input options

my $ARGC = scalar(@ARGV);
if ( $ARGC < 2 ) { &fexit; }

# Variable declarations
my $no_link = 0; # show links in namelines

my $i;    # residue index
my $j;    # residue index
my $k;    # sequence index
my $options = "";
my $line;    # line read in from file
my $query_length = 0;    # number of residues in query sequence
my $query_match  =
  0;    # number of upper-case residues (=match states) in query sequence
my $capitalize = 0;    # capitalize query
my $db_size;           # size of original database in residues (e.g. for nr)
my $nameline;          # >target_name
my $Evalue;            # e-value of hit
my $score;             # bit score of hit
my $hit_length;        # number of residues in HSP
my $coverage;          # hit-length/$query_length
my $score_col;         # score per column
my $score_min = 0;     # $score_min=-3*log($P_max)/log(2);

my $query_name="";        # name of query file
my @query_name;
my $queryseq;          # residues of query read in  with -q or -q2m option
my $first_res;         # index of first query residue in pairwise alignment
my $last_res;          # index of last  query residue in pairwise alignment
my @query_res;         # query residues from current pairwise alignment
my @target_res;        # target residues from current pairwise alignment
my $query_res;         # query residues from current pairwise alignment
my $target_res;        # target residues from current pairwise alignment
my $line_number = 0;
my $new_hit = ""; 	  	# new sequence record; is only written if coverage threshold is exceeded
my $nhit = 0;    			# counts the number of sequences already in alignment
my @hitnames;    			# $hitnames[$nhit] is the nameline of the ihit-th hit
my @hitseqs;     			# $hitseqs[$nhit] contains the residues of the ihit-th hit
my @match; 		  			# for -q option: $match[$i]=1 if $i-th query residue is capital letter in query, else 0
my $qid;         			# $qid is sequence identity with query (for -q option: CONSIDER ONLY MATCH STATES)
my $len;         			# $len is sequence number of residues of seq k aligned with a match state in query
my $b;    					# minimum per-residue bit score with query at ends of HSP
my $pfile  = "";  		# alignment file used to calculate PSSM for -p and s/c options
my $bfile  = "";  		# alignment file used to calculate PSSM for -b option
my $GAP    = 11;  		# gap opening penalty in third bits
my $EXTEND = 1;   		# gap extension penalty in thrird bits
my @queryseq;
my $skip = 0;  			# skip this target sequence because it might be a synthetic fusion protein
my $best            = 0;    # extract only the best HSP per sequence
my $rescaled_Gonnet = 0;    # Gonnet matrix not yet rescaled to 1/3 bits
my @qp = ();    			# $qb[$i][$a] is PSSM from alignment read in with -B option
my @qb = ();    			# $qp[$i][$a] is PSSM from alignment read in with -P option
my $qstart;	# query residue defining the start of the HSP region to be used
my $qend;	# query residue defining the end of the HSP region to be used

for ( $i = 0 ; $i < $ARGC ; $i++ ) { $options .= "$ARGV[$i] "; }

# Set E-value thresholds etc. for inclusion in alignment
if ( $options =~ s/-emax\s+(\S+)//g ) { $E_max      = $1; }
if ( $options =~ s/-emin\s+(\S+)//g ) { $E_min      = $1; }
if ( $options =~ s/-e\s+(\S+)//g )    { $E_max      = $1; }
if ( $options =~ s/-qid\s+(\S+)//g )  { $qid_thrshd = $1; }
if ( $options =~ s/-cov\s+(\S+)//g )  { $cov_thrshd = $1; }
if ( $options =~ s/-best\s+//g )      { $best       = 1; }

# Set score-per-column, pvalue and end-pruning thresholds for inclusion in alignment
if ( $options =~ s/-pmax\s+(\S+)//g ) {
	$P_max     = $1;
	$score_min = -3 * log( abs($P_max) ) / log(2);
}
if ( $options =~ s/-p\s+(\S+)//g ) {
	$P_max     = $1;
	$score_min = -3 * log( abs($P_max) ) / log(2);
}
if ( $options =~ s/-s\/c\s+(\S+)//g ) { $sc_thrshd = $1; }
if ( $options =~ s/-bl\s+(\S+)//g )   { $bl        = $1; }
if ( $options =~ s/-bs\s+(\S+)//g )   { $bs        = $1; }
if ( $options =~ s/-bg\s+(\S+)//g )   { $bg        = $1; }
if ( $options =~ s/-b\s+(\S+)//g )    { $bs        = $1; $bl = $1; }

# Set output format
if ( $options =~ s/-psi//g )  { $outformat  = "psi"; }
if ( $options =~ s/-fas//g )  { $outformat  = "fas"; }
if ( $options =~ s/-a2m//g )  { $outformat  = "a2m"; }
if ( $options =~ s/-a3m//g )  { $outformat  = "a3m"; }
if ( $options =~ s/-ufas//g ) { $outformat  = "ufas"; }
if ( $options =~ s/-no_link//g ) { $no_link  = 1; }
if ( $options =~ s/-c//g )    { $capitalize = 1; }

# Set input and output file
if ( $options =~ s/-i\s+(\S+)//g ) { $infile  = $1; }
if ( $options =~ s/-o\s+(\S+)//g ) { $outfile = $1; }
if ( $options =~ s/-app//g )       { $append  = 1; }
if ( $options =~ s/-qs\s+(\d+)//g )       { 
	$qstart  = int($1); 
	if ($qstart < 1) { $qstart = undef; }
}
if ( $options =~ s/-qe\s+(\d+)//g )       { $qend  = int($1); }

# Verbose mode?
if ( $options =~ s/-v\s*(\d)//g ) { $v = $1; }
if ( $options =~ s/-v//g )        { $v = 2; }

# Include query sequence as first sequence in alignment?
if ( $options =~ s/-q\s+(\S+)//g ) { $query_file = $1; $capitalize = 0; }
if ( $options =~ s/-q2m\s+(\S+)//g ) {
	$query_file = $1;
	$capitalize = 0;
}    # for downwards compatibility
if ( $options =~ s/-Q\s+(\S+)//g ) { $query_file = $1; $capitalize = 1; }

# Set filenames for PSSMs
if ( $options =~ s/-P\s+(\S+)//g ) { $pfile = $1; }
if ( $options =~ s/-B\s+(\S+)//g ) { $bfile = $1; }

if ( $options =~ s/-blastplus//g )  { $useblastplus  = 1; }

# Read infile and outfile
if ( !$infile  && $options =~ s/^\s*([^- ]\S+)\s*// ) { $infile  = $1; }
if ( !$outfile && $options =~ s/^\s*([^- ]\S+)\s*// ) { $outfile = $1; }

# Warn if unknown options found or no infile/outfile
if ( $options !~ /^\s*$/ ) {
	$options =~ s/^\s*(.*?)\s*$/$1/g;
	die("Error: unknown options '$options'\n");
}
if ( !$infile || !$outfile ) { &fexit(); }
if ( ( $pfile || $bfile ) && !$query_file ) {
	die("Error: you must specify a query sequence file (-q/-Q) when using the -B or -P option\n");
}

if ( $v >= 3 ) {
	print("\n");
	print("E-value maximum        : $E_max\n");
	if ( $E_min >= 0 ) { print("E-value minimum        : $E_min\n"); }
	print("coverage threshold     : $cov_thrshd\n");
	print("score/column threshold : $sc_thrshd\n");
	print("sequence id threshold  : $qid_thrshd\n");
	if ( $P_max < 1 ) { print("p-value max            : $P_max\n"); }
	if ( $bs > -10 )  { print("end pruning            : $b\n"); }
	if ($query_file)  { print("Query sequence file    : $query_file\n"); }
	if ($pfile)       { print("-P file   : $pfile\n"); }
	if ($bfile)       { print("-B file   : $bfile\n"); }
	print("Blast file             : $infile\n");
	print("Output file            : $outfile\n");
	print("Output format          : $outformat\n");
}

#Include query sequence as first sequence in alignment?
if ($query_file) {
	open( QUERYFILE, "<$query_file" )
	  or die("ERROR: Cannot open $query_file: $!\n");
	
	while ( $line = <QUERYFILE> )    # Read name line
	{
		if ( $line =~ /^>(.*)/ ) {
			push(@query_name,$1);
		}
		else{
			chomp($line);
			if ($v > 8) { print "\nvor replace:\n$line"; }
			$line =~ s/\s+//g;           # remove white space
			if ($v > 8) { print "\nnach replace:\n$line"; }
			if ($v > 8) { print "\n\nvorher: \n$hitseqs[0]\n"; }
			push(@hitseqs,$line);
			if ($v > 8) { print "nachher: \n$hitseqs[0]\n"; }
		}
	}
		
	if ($v > 8) { 
		foreach my $name(@query_name){
			foreach my $seq(@hitseqs){
				print "Queryname: $name\n";	
				print "\n\nQuery-seq: $seq\n";
			}
		}
	}
	
	close(QUERYFILE);

	# Prepare name line of hit
	if ( $outformat eq "psi" ) {
		print("Warning: output format $outformat not supported with blastplus program family yet by alignhits_html.pl\n");
		foreach my $qname(@query_name){
			$qname =~
			/^(\S{1,20})\S*\s*(.*)/;    # delete everything after first block
			$line = sprintf( "%s", $1 );
			$line =~ tr/ /_/;
			my $hit = sprintf( "%-31.31s ", $line );
			push(@hitnames,$hit);
		}
	}
	else {
		foreach my $qname(@query_name){
			my $hit = sprintf( ">%s", $qname );
			push(@hitnames,$hit);
		}
	}
	
	$hitseqs[0] =~ tr/-.//d;          # delete all gaps from query
	$queryseq = $hitseqs[0];
	$hitseqs[0] =~ tr/a-z/A-Z/;      # capitalize hitseq[0] and delete gaps
	$hitseqs[0] =~ tr/Uu/Cc/;
	$nhit = scalar @hitseqs;

	# Capitalize query?
	if ($capitalize) { $queryseq =~ tr/a-z/A-Z/; }
	$query_match =
	  ( $queryseq =~ tr/A-Z/A-Z/ );    # count number of match states in query

	# Determine match columns as those with upper case residue in query
	@queryseq = unpack( "C*", $queryseq );
	for ( $j = 0 ; $j < @queryseq ; $j++ ) {
		if ( $queryseq[$j] >= 65 && $queryseq[$j] <= 90 ) { $match[$j] = 1; }
		else { $match[$j] = 0; }
	}
}

if ($v > 8) {
    if (defined $hitseqs[0]) {
	print "Query-Sequence:\n$hitseqs[0]\n";
    }
}

# Generate PSSM from alignment file (-B option)?
if ($pfile) { &MakePSSM( $pfile, \@qp ); }
if ($bfile) { &MakePSSM( $bfile, \@qb ); }

my $blast_format = "prot";

# Scan Blast output file for query length (needed for coverage)
open INFILE, "<$infile" or die("cannot open $infile: $!\n");
#print "LINE 527 ".$infile."\n";
# $line_number is the number of the next line to read. The first line has number 1.
$line_number++;
#########################################################################################
# Scan the Input File for query size: "(NNN letters)" or "Length=NNN"
# and (not needed) database size
my $rounds = 0;
while ( $line = <INFILE>) {
	if($v==4){print "LINE 532 ".$line.""};
	if ( $line =~ /BLASTN/ ) {    # check for nuc-Blast output
		$blast_format = "nuc";
		print("Warning: $blast_format format not supported with blastplus program family yet by alignhits_html.pl\n");
	}
	if ( $line =~ /^\s*\((\d+)\s+letters\)/ || $line =~ /^Length=(\d+)/ ) {
	    $query_length = $1;
	} elsif ( $line =~ /^.*\s(\S+) total letters/ ) {
	    $db_size = $1;
	} elsif ( $line =~ ROUND_START_PATTERN ) {
	    $rounds = $1;
	} elsif ( $line =~ END_OF_HEADER ) {
	    $line_number++;
	    last;
	}
	$line_number++;
}

$db_size =~ tr/,//d;
if ( $v >= 3 ) {
    print("Query length = $query_length\n");
    print("Database length = $db_size residues\n");
}
$db_size =
  0.6 * scalar($db_size)
  ;    # EFFECTIVE data base length (same factor used by BLAST)

# Search for "Results from round"
# If found, we are looking at PsiBlast output and have to search for the beginning of the last round
if (1 < $rounds) {
    # if 1 < $rounds, the output already have been shortened to the last round, i.e. by calling shorten_psiblast_output.pl.
    # Therefore, this should not occur.

    # PsiBlast output! Search for line number with last occurence of "Results from round"
    #if ( $v >= 3 ) {
    if ( $v >= 1 ) {
	print("PsiBlast output with possibly several rounds detected. Searching for last round...\n");
    }
    my $last_line = $line_number;
    while ( $line = <INFILE> )    #scan through PsiBlast-output line by line
    {
	if ( $line =~ ROUND_START_PATTERN ) { $last_line = $line_number; }
	$line_number++;
    }

    # Advance to line with last occurence of "Results from round"
    close INFILE;
		
    open INFILE, "<$infile" || die("cannot open $infile: $!");
    for ( $j = 1; $j <= $last_line ; $j++ ) { <INFILE>; }
}

########################################################################################
# Read target namelines and HSPs (Evalue, score etc. and pairwise alignment)

while ( $line = <INFILE> )    #scan through PsiBlast-output line by line
{
	if($v==4){print "LINE 583 ".$line};

	# Update JMA Oct, 2011 add Uniprot Ids to names
	# This block extracts the name lines from the blast file
	# After this block the algorithm exits the Document
	if ( $line =~ s/^>// ) {
		$line =~ s/\s+/ /g;
		chomp($line);
		$nameline = $line;
# After commenting THIS Block forwarding works, with small glitch, Uniprot Id not matched		
#		while ( $line = <INFILE> ) {
#			print "LINE 600 ".$line;
#			if ( $line =~ /^\s+Length =/ ) {
#				 last; 
#			}
#			chomp($line);
#			$nameline .= $line;
#		}
		$nameline =~ s/<a name\s*=\s*[\w\d]+><\/a>//g;    # test for html output!!!
		$nameline =~ s/\s+/ /g;
		$nameline =~ s/\s+gi\|/   gi\|/g;
		
		
		# Is sequence a synthetic fusion protein ?
		if ( $nameline =~	/(\[synthetic| synthetic|construct|cloning|vector|chimeric)/i )
		{
			$skip = 1;
		}
		else { $skip = 0; }
	}

	# New HSP found?
	elsif ( !$skip && $line =~ /^ Score =/ ) {
		if ($best) { $skip = 1; }  # skip all following hits with same sequence?

		# First check whether E-value is small enough
		if ( $line =~ /^ Score =\s*(\S+)\s*bits\s*\S*\s*Expect =\s*(\S+)/ ) {
			$score  = $1;
			$Evalue = $2;
		}
		else {
			print("\nWARNING: wrong format in blast output. Expecting Score = ... Expect = ..\n$line\n");
		}
		$Evalue =~ s/^(e|E)/1$1/;    # Expect = e-123 -> 1e-123
		$Evalue=~tr/,//d;            # remove comma at the end
		if ( $Evalue > $E_max || $Evalue < $E_min ) {
			$new_hit = "";
			next;
		}                            # reject hit

		# Record sequence identity
		# (not needed, qid calculated afterwards WITHOUT counting target residues aligned to gaps in query)
		$line = <INFILE>;
		$line =~ /^ Identities =\s*\S+\/(\S+)\s+\((\S+)%\)/;
		$qid = $2;

		my $wrong_format = 0;

		if ( $blast_format eq "nuc" ) {
		    # not adapted to blastplus family yet. Don't use!
			# Skip three lines and read following line
			$line = <INFILE>;
			$line = <INFILE>;
			$line = <INFILE>;
			$line = <INFILE>;

			# Read pairwise alignment
			$query_res  = "";
			$target_res = "";
			if ( $line !~ /^Query:\s*(\d+)\s+\S+/ ) {
				print("WARNING: wrong format of blast output in $infile: $!  ---> Skipping HSP!\n");
				next;
			}
			$line =~ /^Query:\s*(\d+)\s+\S+/;
			$first_res = $1;
			while ( $line =~ /Query:\s*\d+\s+(\S+)\s+(\d+)/ )    # Cycle in this loop until no new "Query:" lines are found
			{
				$query_res .= $1;
				$last_res = $2;
				$line     = <INFILE>;
				$line     = <INFILE>;
				if ( $line !~ /^Sbjct:\s*\d+\s+(\S+)/ ) {
					print("WARNING: wrong format of blast output in $infile: $!  ---> Skipping HSP!\n");
					$wrong_format = 1;					
					last;
				}
				$target_res .= $1;
				$line = <INFILE>;
				$line = <INFILE>;
				$line = <INFILE>;
			}      # end while(1)
		}
		else {

			# Skip another line and read following line
			$line = <INFILE>;
			$line = <INFILE>;

			# Read pairwise alignment
			$query_res  = "";
			$target_res = "";
			if ( $line !~ /^Query:?\s*(\d+)\s+\S+/ ) {
				print("WARNING: wrong format of blast output in $infile: $!  ---> Skipping HSP!\n");
				next;
			}
			$line =~ /^Query:?\s*(\d+)\s+\S+/;
			if($v==4){print "LINE 697 ".$line."\n"};
			$first_res = $1;
			while ( $line =~ /Query:?\s*\d+\s+(\S+)\s+(\d+)/)    # Cycle in this loop until no new "Query:" lines are found
			{
				$query_res .= $1;
				$last_res = $2;
				$line     = <INFILE>;
				$line     = <INFILE>;
				if ( $line !~ /^Sbjct:?\s*\d+\s+(\S+)/ ) {
					print("WARNING: wrong format of blast output in $infile: $!  ---> Skipping HSP!\n");
					$wrong_format = 1;
					last;
				}
				$target_res .= $1;
				$line = <INFILE>;
				$line = <INFILE>;
			}      # end while(1)
		}
		
		#check for ERRORs
		if ($wrong_format) {
			next;
		}

		# Check lengths
		if ( length($target_res) != length($query_res) ) {
			print("ERROR: Query and target lines do not have the same length!\n");
			print("Q: $query_res\n");
			print("T: $target_res\n");
			exit 1;
		}

		# Check whether hit has sufficient score per column
		$hit_length = ( $target_res =~ tr/a-zA-Z/a-zA-Z/ );
		$score_col  = $score / $hit_length;

		#	if ($score_col<$sc_thrshd) {next;}                # Reject hit?

		# cut hit if $qstart or $qend is defined
		if ($qstart && $first_res < $qstart) {
			if ($last_res < $qstart) { next; }
			@query_res  = unpack( "C*", $query_res);
			my $i = 0;
			while ($i <= $#query_res) {
				if ($query_res[$i++] != 45) { 
					if (++$first_res == $qstart) { last; }
				}
			}
			if ($i > $#query_res) { next; }
			$query_res = substr($query_res, $i);
			$target_res = substr($target_res, $i);
		}
		if ($qend && $last_res > $qend) {
			if ($qend < $first_res) { next; }
			@query_res  = unpack( "C*", $query_res);
			my $i = $#query_res;
			while ($i >= 0) {
				if ($query_res[$i] != 45) {
					if (--$last_res == $qend) { last; }
				}
				$i--;
			}
			if ($i < 0) { next; }
			$query_res = substr($query_res, 0, $i);
			$target_res = substr($target_res, 0, $i);
		}
		if ($qstart || $qend) {
			$hit_length = ( $target_res =~ tr/a-zA-Z/a-zA-Z/ );
			unless ($hit_length) { next; }
		}
		@query_res  = unpack( "C*", $query_res );
		@target_res = unpack( "C*", $target_res );

		# Prune ends of HSP which are not reliably homologous
		if ( ( $bs > -9 || $bl > -9 ) && !&PruneHSP() ) {
			next;
	}    # if entire HSP is pruned away, goto next alignment

	# Check whether hit has sufficient sequence identity and coverage with query
		if ( !$query_file ) {
			$len = 0;
			$qid = 0;
			for ( $i = 0 ; $i < scalar(@query_res) ; $i++ ) {
				if ( $target_res[$i] != 45 && $query_res[$i] != 45 )
				{    # count only non-gap target residues in match columns!
					$len++;
					if ( $query_res[$i] == $target_res[$i] ) { $qid++; }
				}
			}
			$coverage = 100 * $len / $query_length;
		}
		else {
			$len = 1;
			$qid = 0;
			$j   = $first_res - 1;    # if first_res=1 then $j=0
			for ( $i = 0 ; $i < scalar(@query_res) ; $i++ ) {
				if ( $query_res[$i] != 45 ) {
					if ( $target_res[$i] != 45 && $match[$j] )
					{    # count only non-gap target residues in match columns!
						$len++;
						if ( $query_res[$i] == $target_res[$i] ) { $qid++; }
					}
					$j++;    # $j = next position in query
				}
			}
			$coverage = 100 * $len / $query_match;
		}
		if ( $len == 0 )                       { next; }    # Reject hit?
		if ( 100 * $qid / $len < $qid_thrshd ) { next; }    # Reject hit?
		if ( $coverage < $cov_thrshd )         { next; }    # Reject hit?

		#	print("Q: $query_res\n");
		#	print("T: $target_res\n\n");

		# Check score per column
		if ( $sc_thrshd > -9 || $score_min > 0 ) {
			if ( !&CheckScorePerColumn() ) { next; }
		}

		if ( $v >= 3 ) {
			printf( "nhit=%-2i  qid=%-3i  qlen=%-3i  qid=%-3i%% s/c=%-6.3f\n", $nhit, $qid, $len, 100 * $qid / $len, $score_col );
		}

		# Record residues
		$new_hit = "-" x ( $first_res - 1 );    # Print gaps at beginning of sequence
		if ( $outformat eq "psi" ) {
			for ( $i = 0 ; $i < scalar(@query_res) ; $i++ ) {
				if ( $query_res[$i] != 45 )
				{                      # residues aligned to gaps are ignored
					$new_hit .= uc( chr( $target_res[$i] ) ); # UPPER case if aligned with a query residue (match state)
				}
			}
		}
		else {
			for ( $i = 0 ; $i < scalar(@query_res) ; $i++ ) {
				if ( $query_res[$i] != 45 ) {
					$new_hit .= uc( chr( $target_res[$i] ) ); # UPPER case if aligned with a query residue (match state)
				}
				else {
					if ( $target_res[$i] == 45 ) {
						$new_hit .= ".";
					}  # Both target and query may have gaps if they were pruned
					else {
						$new_hit .= lc( chr( $target_res[$i] ) );
					} # lower case if aligned with a gap in the query (insert state)
				}
			}
		}
		$new_hit .= "-" x ( $query_length - $last_res );   # Print gaps at end of sequence
		$new_hit =~ tr/Uu/Cc/;
		# Here the sequences and names are set !
		if($v==4){print "LINE 848 ADD SQ and NAME\n"};
		$hitseqs[$nhit] = $new_hit;

		if ($v > 8) { print "Hit - number: $nhit!\n"; printf("%s\n",$new_hit); }

		# Prepare name line of hit
		if ( $outformat eq "psi" ) {
			$nameline =~
			  /^(\S{1,20})\S*\s*(.*)/;    # delete everything after first block
			$line = sprintf( "%s:(%i-%i)", $1, $first_res, $last_res );
			$line =~ tr/ /_/;
			$hitnames[$nhit] = sprintf( "%-31.31s ", $line );
			if($v == 5){print "LINE 860 ".$hitnames[$nhit]};
		}
		else {
			if ($nameline =~ /^<a.*/ ) {
				$nameline =~ /^(.*?<\/a>)\s*(.*)/;  # delete everything after first block
				if($v==4){print "LINE 864  1 ".$1." END LINE 846\n"};
				if($v==4){print "LINE 865  2 ".$2." END LINE 846\n"};
				$hitnames[$nhit] =
			  		sprintf( ">%s:(%i-%i) %s  E=%g s/c=%4.2f id=%.0f%% cov=%.0f%%",
						$1, $first_res, $last_res, $2, $Evalue, $score_col,
						100 * $qid / $len, $coverage );
						if($v==4){print "LINE 870 ".$hitnames[$nhit]."\n"};
						if($v==5){print "LINE 872 ".$hitnames[$nhit]};
			} else {
				$nameline =~ /^(\S*)\s*(.*)/;  # delete everything after first block
				$hitnames[$nhit] =
			  		sprintf( ">%s:(%i-%i) %s  E=%g s/c=%4.2f id=%.0f%% cov=%.0f%%",
						$1, $first_res, $last_res, $2, $Evalue, $score_col,
						100 * $qid / $len, $coverage );
						if($v==5){print "LINE 879 ".$hitnames[$nhit]};
			}
		}

		$nhit++;
	}    # end elseif new HSP found

}    # end while ($line)

########################################################################################
close INFILE;

# If output format is fasta or a2m we have to insert gaps:
if ( $outformat ne "psi" ) {
	my @len_ins
	  ; # $len_ins[$j] will count the maximum number of inserted residues after match state $j.
	my @inserts
	  ; # $inserts[$j] contains the insert (in small case) of sequence $k after the $j'th match state
	my $insert;
	my $ngap;

# For each match state determine length of LONGEST insert after this match state and store in @len_ins
	for ( $k = 0 ; $k < $nhit ; $k++ ) {

# split into list of single match states and variable-length inserts
# ([A-Z]|-) is the split pattern. The parenthesis indicate that split patterns are to be included as list elements
# The '#' symbol is prepended to get rid of a perl bug in split
		$j       = 0;
		@inserts = split( /([A-Z]|-)/, "#" . $hitseqs[$k] . "#" );

		if ($v > 8) {
		    #printf("Sequence $k: $hitseqs[$k]\n");
		    #printf("Sequence $k: @inserts\n");
		}
		foreach $insert (@inserts) {
			if ( !defined $len_ins[$j] || length($insert) > $len_ins[$j] ) {
				$len_ins[$j] = length($insert);
			}
			$j++;

			#	    printf("$insert|");
		}

		#	printf("\n");
	}

# After each match state insert residues and fill up with gaps to $len_ins[$i] characters
	for ( $k = 0 ; $k < $nhit ; $k++ ) {

		# split into list of single match states and variable-length inserts
		@inserts = split( /([A-Z]|-)/, "#" . $hitseqs[$k] . "#" );
		$j       = 0;

		# append the missing number of gaps after each match state
		foreach $insert (@inserts) {
			if ( $outformat eq "fas" ) {
				for ( my $l = length($insert) ; $l < $len_ins[$j] ; $l++ ) {
					$insert .= "-";
				}
			}
			else {
				for ( my $l = length($insert) ; $l < $len_ins[$j] ; $l++ ) {
					$insert .= ".";
				}
			}
			$j++;
		}
		$hitseqs[$k] = join( "", @inserts );
		$hitseqs[$k] =~
		  tr/\#//d;   # remove the '#' symbols inserted at the beginning and end
	}
}

if ($query_file) {

	# Determine match states
	my @qa2m =
	  unpack( "C*", $hitseqs[0] );   # $hitseq[0] is query sequence WITH INSERTS
	my @matchali = ();
	my $L        = scalar(@qa2m);
	$j = 0;
	for ( $i = 0 ; $i < @match ; $i++ ) {
		while ( $j < $L && !( $qa2m[$j] >= 65 && $qa2m[$j] <= 90 ) ) {
			$matchali[ $j++ ] = 0;
		}    #move to column with next upper case residue
		$matchali[ $j++ ] =
		  $match[$i];    #is next query residue upper-case or not?
	}

	# Set all match states to upper case, non-match states to lower case
	my @res;
	for ( $k = 0 ; $k < $nhit ; $k++ ) {
		@res = unpack( "C*", $hitseqs[$k] );

		#       printf("Q: %s\n",$hitseqs[0]);
		#       printf("T: %s\n",$hitseqs[$k]);
		for ( $i = 0 ; $i < @res ; $i++ ) {
			if ( $matchali[$i] ) {
				if ( $res[$i] >= 97 && $res[$i] <= 122 ) {
					$res[$i] -= 32;
				}    #convert to upper case
			}
			else {
				if ( $res[$i] >= 65 && $res[$i] <= 90 ) {
					$res[$i] += 32;
				}    # convert to lower case
				elsif ( $res[$i] == 45 ) { $res[$i] = 46; } # convert '-' to '.'
			}

#	    printf("%3i  Q:%s T:%s  match=%i len=%i\n",$i,chr($qa2m[$i]),chr($res[$i]),$qid[$k],$len);
		}
		if ($v > 8) {
		    #print "(match with query) Hit number $k:\n$hitseqs[$k]\n";
		}
		$hitseqs[$k] = pack( "C*", @res );
	}
}

# cut result alignment if $qstart or $qend is defined
if (scalar(@hitseqs) && ($qstart || $qend)) {
	my $start = $qstart ? $qstart : 1;
	my $end = $qend ? $qend : $query_length;
	my $ali_start = length($hitseqs[0]);
	my $ali_len = 0;
	my $c = 0;
	my @query_chars = unpack("c*", $hitseqs[0]);
	for my $i (0 .. $#query_chars) {
		if ($query_chars[$i] >= 65 && $query_chars[$i] <= 90 || $query_chars[$i] >= 97 && $query_chars[$i] <= 122) {
			$c++;
			if ($c == $start) { $ali_start = $i; }
			if ($c == $end) { 
				if ($ali_start <= $i) { $ali_len = $i - $ali_start + 1; } 
				last; 
			}
		}
	}
	for my $i (0 .. $#hitseqs) { $hitseqs[$i] = substr($hitseqs[$i], $ali_start, $ali_len); }
}


# Remove gaps? Captialize?
if ( $outformat eq "ufas" ) {
	for ( $k = 0 ; $k < $nhit ; $k++ ) {
		$hitseqs[$k] =~ tr/a-z.-/A-Z/d;
	}    # Transform to upper case and remove all gaps
}
elsif ( $outformat eq "fas" ) {
	for ( $k = 0 ; $k < $nhit ; $k++ ) {
		$hitseqs[$k] =~ tr/a-z./A-Z-/;
	}    # Transform to upper case
}
elsif ( $outformat eq "a3m" ) {
	for ( $k = 0 ; $k < $nhit ; $k++ ) {
		$hitseqs[$k] =~ tr/.//d;
	}    # Remove gaps aligned to inserts
}

# Write sequences into output file
if ($append) {
	open( OUTFILE, ">>$outfile" ) || die("cannot open $outfile:$!");
}
else { open( OUTFILE, ">$outfile" ) || die("cannot open $outfile:$!"); }
if ( $outformat eq "psi" ) {
	for ( $k = 0 ; $k < $nhit ; $k++ ) {
		$hitseqs[$k] =~ tr/./-/;
		if ($no_link) {
		    $hitnames[$k] =~ s/<a .*?>//g;
		    $hitnames[$k] =~ s/<\/a>//g;
		    if($v==5){print "LINE 1047 ".$hitnames[$k]."\n"};
		}
		
		printf( OUTFILE "%s %s\n", $hitnames[$k], $hitseqs[$k] );
	}
}
else {
	for ( $k = 0 ; $k < $nhit ; $k++ ) {
		if ($no_link) {
			# ><a href="http://www.uniprot.org/uniprot/B4QXC8">
		    $hitnames[$k] =~ s/<a .*?>//g;
		    $hitnames[$k] =~ s/<\/a>//g;
		    if($v==5){print "LINE 1058 ".$hitnames[$k]."\n"};
		}
		printf( OUTFILE "%s\n%s\n", $hitnames[$k], $hitseqs[$k] );
	}
}
if($v==5){print "Line 1057 Debug\n"};
for(my $i=0; $i<@hitnames;$i++){
	if($v==5){print "Line 1059 Hitnames ".$hitnames[$i]."\n"};
}


close OUTFILE;

if ( $v >= 2 ) {
	printf("$nhit sequences extracted from $infile and written to $outfile\n");
}

# Return number of hits in one byte (0-255)
#if    ( $nhit < 110 )  { exit $nhit; }
#elsif ( $nhit < 1100 ) { exit 100 + int( $nhit / 10 ); }
#elsif ( $nhit < 5500 ) { exit 200 + int( $nhit / 100 ); }
#else { exit 255; }
exit(0);












# Prune ends of HSP
sub PruneHSP() {
	my $L = scalar(@query_res);    # length of current pairwise alignment
	my $smin;                      # minimum score at current position $i
	my $score;                     # actual score at current position $i
	my $i1;                        # last pruned residue of HSP on  left side
	my $i2;                        # last pruned residue of HSP on right side
	my $gap = 0;    # gap 0: no gap currently open   1:gap opened
	my $i;          # next column to read from pairwise alignment
	my $j;          # next position to read from query (sequence or profile)
	my $gapsleft
	  ; # number of gaps aligned with capital residues of query on left side of HSP
	my $gapsright
	  ; # number of gaps aligned with capital residues of query on right side of HSP
	my $bleft;
	my $bright;

	#    my $q=pack("C*",@query_res);
	#    my $t=pack("C*",@target_res);
	#   print("Q: $q\nT: $t\n");

	if ($query_file) {

  # Count gaps in target that are aligned with match residues to the left of HSP
		$gapsleft = 0;
		for ( $j = $first_res - 2 ; $j >= 0 ; $j-- ) {
			if ( $match[$j] == 0 ) { last; }
			$gapsleft++;
		}

 # Count gaps in target that are aligned with match residues to the right of HSP
		$gapsright = 0;
		for ( $j = $last_res ; $j < $query_length ; $j++ ) {
			if ( $match[$j] == 0 ) { last; }
			$gapsright++;
		}
	}
	else {
		$gapsleft  = $first_res - 1;
		$gapsright = $query_length - $last_res;
	}
	if ( $gapsleft >= $bg ) { $bleft = $bs; }
	else { $bleft = $bl; }
	if ( $gapsright >= $bg ) { $bright = $bs; }
	else { $bright = $bl; }

#    printf("%10.10s %3i %3i  %3i %3i \n",$nameline,$first_res,$last_res,$gapsleft,$gapsright);

	$i1 = -1;    # last pruned residue of HSP on  left side
	$i2 = $L;    # last pruned residue of HSP on right side

	if ($bfile) {

		# Calculate scores at the end with query PSSM

		if ( $bleft > -9 ) {

			$smin  = 0;
			$score = 0;
			$i     = 0;
			$j     = $first_res - 1;    # if first_res=1 then $j=0
			while ( $i < $L && $i < $i1 + 50 ) {
				$smin += $bleft;
				if ( $query_res[$i] == 45 ) {    # gap in query sequence
					if ( !$gap ) { $score -= $GAP; $gap = 1; }
					else { $score -= $EXTEND; }
				}
				elsif ( $target_res[$i] == 45 ) {    # gap in target sequence
					if ( !$gap ) { $score -= $GAP; $gap = 1; }
					else { $score -= $EXTEND; }
					$j++;                            # since $query_res[$i]!=45
				}
				else {
					$score += $qb[$j][ $target_res[$i] - 65 ];
					$gap = 0;
					$j++;
				}
				if ( $score < $smin ) { $i1 = $i; $smin = 0; $score = 0; }

				#	    printf("%3i  %3i  %6.2f  %6.2f\n",$i,$i1,$score,$smin);
				$i++;
			}
		}

		if ( $bright > -9 ) {

			$smin  = 0;
			$score = 0;
			$i     = $L - 1;
			$j     = $last_res - 1;    # if $last_res=$L then $j=L-1
			while ( $i > $i1 && $i > $i2 - 50 ) {
				$smin += $bright;
				if ( $query_res[$i] == 45 ) {
					if ( !$gap ) { $score -= $GAP; $gap = 1; }
					else { $score -= $EXTEND; }
				}
				elsif ( $target_res[$i] == 45 ) {
					if ( !$gap ) { $score -= $GAP; $gap = 1; }
					else { $score -= $EXTEND; }
					$j--;              # since $query_res[$i]!=45
				}
				else {
					$score += $qb[$j][ $target_res[$i] - 65 ];
					$gap = 0;
					$j--;
				}
				if ( $score < $smin ) { $i2 = $i; $smin = 0; $score = 0; }

				#	    printf("%3i  %3i  %6.2f  %6.2f\n",$i,$i2,$score,$smin);
				$i--;
			}
		}

		for ( $i = 0 ; $i <= $i1 ; $i++ ) { $target_res[$i] = 45; } # 45=ord('-')
		for ( $i = $i2 ; $i < $L ; $i++ ) { $target_res[$i] = 45; } # chr(45)='-'

	}
	else {

		# Calculate scores at the end with Gonnet matrix and query sequence

		if ( !$rescaled_Gonnet ) { &RescaleGonnet(); }

		if ( $bleft > -9 ) {

			$smin  = 0;
			$score = 0;
			$i     = 0;
			while ( $i < $L && $i < $i1 + 20 ) {
				$smin += $bleft;
				if ( $query_res[$i] == 45 ) {
					if ( !$gap ) { $score -= $GAP; $gap = 1; }
					else { $score -= $EXTEND; }
				}
				elsif ( $target_res[$i] == 45 ) {
					if ( !$gap ) { $score -= $GAP; $gap = 1; }
					else { $score -= $EXTEND; }
				}
				else {
					$score +=
					  $GONNET[ $ch2i[ $query_res[$i] - 65 ] ]
					  [ $ch2i[ $target_res[$i] - 65 ] ];
					$gap = 0;
				}
				if ( $score < $smin ) { $i1 = $i; $smin = 0; $score = 0; }

				#	    printf("%3i  %3i  %6.2f\n",$i,$i1,$score);
				$i++;
			}
		}

		if ( $bright > -9 ) {

			$smin  = 0;
			$score = 0;
			$i     = $L - 1;
			while ( $i > $i1 && $i > $i2 - 20 ) {
				$smin += $bright;
				if ( $query_res[$i] == 45 ) {
					if ( !$gap ) { $score -= $GAP; $gap = 1; }
					else { $score -= $EXTEND; }
				}
				elsif ( $target_res[$i] == 45 ) {
					if ( !$gap ) { $score -= $GAP; $gap = 1; }
					else { $score -= $EXTEND; }
				}
				else {
					$score +=
					  $BLOSUM62[ $ch2i[ $query_res[$i] - 65 ] ]
					  [ $ch2i[ $target_res[$i] - 65 ] ];
					$gap = 0;
				}
				if ( $score < $smin ) { $i2 = $i; $smin = 0; $score = 0; }

				#           printf("%3i  %3i  %6.2f\n",$i,$i2,$score);
				$i--;
			}
		}

		for ( $i = 0 ; $i <= $i1 ; $i++ ) { $target_res[$i] = 45; } # 45=ord('-')
		for ( $i = $i2 ; $i < $L ; $i++ ) { $target_res[$i] = 45; } # chr(45)='-'
	}

	#    $t=pack("C*",@target_res);
	#    printf(STDERR "T: $t %i\n\n",$i1+1+$L-$i2);

	return $i2 - $i1 - 1;    # return number of unpruned residues left
}

# Check score per column of HSP with query IN MATCH COLUMNS
sub CheckScorePerColumn() {

	#    my $q=pack("C*",@query_res);
	#    my $t=pack("C*",@target_res);
	#   print("\nQ: $q\nT: $t\n");

	my $score = 0;
	my $gap   = 0;           # gap 0: no gap currently open   1:gap opened
	$j = $first_res - 1;     # if first_res=1 then $j=0

	if ($pfile) {

		# Calculate scores with query PSSM

		for ( $i = 0 ; $i < scalar(@query_res) ; $i++ ) {
			if ( $match[$j] ) {
				if ( $query_res[$i] == 45 ) {    # query has gap
					if ( !$gap ) { $score -= $GAP; $gap = 1; }
					else { $score -= $EXTEND; }
				}
				elsif ( $target_res[$i] == 45 ) {
					if ( !$gap ) { $score -= $GAP; $gap = 1; }
					else { $score -= $EXTEND; }
				}
				else {
					$score += $qp[$j][ $target_res[$i] - 65 ];
					$gap = 0;
				}

#		printf("%3i  %s %s %3i  %6.2f\n",$i,chr($query_res[$i]),chr($target_res[$i]),$j-$first_res,$score);
			}
			else {
				$gap = 0;
			}
			if ( $query_res[$i] != 45 ) { $j++; }
		}

	}
	else {

		# Calculate scores with Gonnet matrix and query sequence
		if ( !$rescaled_Gonnet ) { &RescaleGonnet(); }

		for ( $i = 0 ; $i < scalar(@query_res) ; $i++ ) {
			if ( $match[$j] ) {
				if ( $query_res[$i] == 45 ) {    # query has gap
					if ( !$gap ) { $score -= $GAP; $gap = 1; }
					else { $score -= $EXTEND; }
				}
				elsif ( $target_res[$i] == 45 ) {
					if ( !$gap ) { $score -= $GAP; $gap = 1; }
					else { $score -= $EXTEND; }
				}
				else {
					$score +=
					  $GONNET[ $ch2i[ $query_res[$i] - 65 ] ]
					  [ $ch2i[ $target_res[$i] - 65 ] ];
					$gap = 0;
				}

#		printf("%3i  %s %s %3i  %6.2f\n",$i,chr($query_res[$i]),chr($target_res[$i]),$j-$first_res,$score);
			}
			else {
				$gap = 0;
			}
			if ( $query_res[$i] != 45 ) { $j++; }    # query has residue
		}
	}

   #    printf ("score=%-6.2f  s/c=%-6.2f ",$score,$score/($j-$first_res+1.01));
	$score_col = $score / ( $j - $first_res + 1.1 );
	if ( $score_col < $sc_thrshd ) { return 0; }     # reject?
	if ( $score < $score_min )     { return 0; }     # reject?

	#    printf ("-> accepted!\n");
	return 1;
}

# Create a PSSM and store results in @{$q}[$i][$a]
sub MakePSSM() {
	my $alifile = $_[0];
	my $q       = $_[1];
	my $basename;
	my $rootname;
	my $pssmfile;

	# Create dummy database?
	if ( !-e "$dummydb.phr" ) {
		system("cp $query_file $dummydb");
		if ($useblastplus) {
		    system("$ncbipdir/makeblastdb -in $dummydb");
		} else {
		    system("$ncbidir/formatdb -i $dummydb");
		}
	}
	if ( $alifile =~ /(.*)\..*/ ) { $basename = $1; }
	else { $basename = $alifile; }
	if ( $basename =~ /.*\/(.*)/ ) { $rootname = $1; }
	else { $rootname = $basename; }

	# Make ASCII PSSM matrix from binary checkpoint file
	if ($useblastplus) {
	    $pssmfile="$basename.mtx";
	    &System(
"$ncbipdir/psiblast -db $dummydb -query $query_file -num_iterations 1 -inclusion_ethresh 0.01 -num_alignments 0 -in_msa $alifile -out_ascii_pssm $pssmfile 1> $basename.blalog 2> $basename.blalog"

# makemat below is not supported any more. use option -out_ascii_pssm ? is this the same as the .mtx file used below?
		);
	    if ( !-e "$pssmfile" ) {
		die(
"Error: Could not generate checkpoint file in '$ncbipdir/psiblast -db $dummydb -query $query_file -num_iterations 1 -inclusion_ethresh 0.01 -num_alignments 0 -in_msa $alifile -out_ascii_pssm $pssmfile 1> $basename.blalog 2> $basename.blalog'\n"
		    );
	    }
	} else {
	    $pssmfile="$basename.chk";
	    &System(
"$ncbidir/blastpgp -b 0 -j 1 -h 0.01 -d $dummydb -i $query_file -B $alifile -C $pssmfile 1> $basename.blalog 2> $basename.blalog"
		);
	    if ( !-e $pssmfile ) {
		die(
"Error: Could not generate checkpoint file in '$ncbidir/blastpgp -b 0 -j 1 -h 0.01 -d $dummydb -i $query_file -B $alifile -C $pssmfile 1> $basename.blalog 2> $basename.blalog'\n"
		    );
	    }
	}
# makemat doesn't exist with blast+. Probably the .mtx can directly created by psiblast.
	if (!$useblastplus) {
	    system("cp -f $query_file $basename.fasta");
	    system("echo $rootname.chk > $basename.pn\n");
	    system("echo $rootname.fasta > $basename.sn\n");
	    system("$ncbidir/makemat -P $basename");
	}

# Read in PSSM
# The PSSM file *.mtx contains one line for each column, beginning with line 15.
# The columns of these lines give the log-odds 100*log(p(i,a)/f(a)) in the following order:
# ? A ? C D E F G H I K L M N P Q R S T V W X Y ? ? ?

	open( PSIMTX, "<$pssmfile" )
	  || die("Error: cannot open $pssmfile: $!\n");
	for ( $i = 1 ; $i <= 14 ; $i++ ) { $line = <PSIMTX>; }
	my @in;
	my $a;
	$i = 0;
	while ( $line = <PSIMTX> ) {
		@in = split( /\s+/, $line );
		@{$q}[$i] = ();
		for ( $a = 0 ; $a < 26 ; $a++ ) {
			${$q}[$i][$a] = 0.01 * $in[ $ch2psi[$a] ];
		}
		$i++;
	}

	my $L = $i;
	close(PSIMTX);
	system(
"rm -f $basename.pn $basename.sn $basename.mn $basename.chk $basename.mtx"
	);
	if ( $v >= 4 ) {
		for ( $a = 0 ; $a < 26 ; $a++ ) { printf( "    %s ", chr( 65 + $a ) ); }
		print("\n");
		for ( $i = 0 ; $i < $L ; $i++ ) {
			for ( $a = 0 ; $a < 26 ; $a++ ) {
				printf( "%5.2f ", ${$q}[$i][$a] );
			}
			print("\n");
		}
	}
	system(
"rm -f $basename.pn $basename.sn $basename.mn $basename.chk $basename.fasta $basename.blalog $basename.mtx $basename.aux $basename.ss $basename.ss2"
	);
}

# Rescale Gonnet matrix to 1/3 bit units
sub RescaleGonnet() {
	for ( $i = 0 ; $i <= 20 ; $i++ ) {
		for ( $j = 0 ; $j <= 20 ; $j++ ) {
			$GONNET[$i][$j] *= 0.9966;    # *=0.1*ln(10)/ln(2)*3
		}
	}
	$rescaled_Gonnet = 1;
}

sub System() {
	if ( $v >= 3 ) { print("$_[0]\n"); }
	return system( $_[0] ) / 256;
}


