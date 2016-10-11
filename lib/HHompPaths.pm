package HHompPaths;

use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $VERSION);
use Exporter;
our $VERSION=1.00;
our @ISA          = qw(Exporter);
#our @EXPORT       = qw($nr $nre $nrf $nr90 $nr70 $nr90f $nr70f $dummydb $perl $hh $dsspdir $dssp $pdbdir $ncbidir $execdir $datadir $blastpgp $calhhm $database_dir $bioprogs_dir $proftmb_dir $prof_db);
our @EXPORT       = qw($nr $nre $nr90 $nr70 $dummydb $perl $hh $dsspdir $dssp $pdbdir $ncbidir $execdir $datadir $blastpgp $calhhm $database_dir $bioprogs_dir $proftmb_dir $prof_db);

# Set directory paths and file locations
# filtered databases are replaced by unfiltered databases in Tuebingen
our $nr;                           # nr database from NCBI
our $nre;                          # nre database from NCBI
#our $nrf;                          # filtered nr database
our $nr90;                         # nr database with max seqid 90% (cd-hit)
our $nr70;                         # nr database with max seqid 90% (cd-hit)
#our $nr90f;                        # filtered nr database with max seqid 90% (cd-hit)
#our $nr70f;                        # filtered nr database with max seqid 70% (cd-hit)
our $dummydb;                      # blast database consisting of just one sequence
our $hh;                           # hhfilter, hhomp, hhalign, hhmake 
our $perl;                         # reformat.pl, alignhits.pl
our $calhhm;                       # newest scop database for hhomp
our $ncbidir;                      # Where the NCBI programs have been installed
our $execdir;                      # Where the PSIPRED V2 programs have been installed
our $datadir;                      # Where the PSIPRED V2 data files have been installed
our $dsspdir;                      # where are the dssp files?
our $dssp;                         # where is the dssp executable? 
our $blastpgp;                     # blastpgp executable
our $PBS="/usr/local/PBS/bin";     # location of PBS/torque binaries qstat, qsub, qdel, etc
our $pdbdir = "/cluster/databases/pdb/all";
our $database_dir = "/cluster/databases";
our $bioprogs_dir = "/cluster/www/toolkit/bioprogs";
our $proftmb_dir = "/cluster/user/michael/tools/proftmb";
our $prof_db = "$database_dir/standard/uniprot_sprot.fasta $database_dir/standard/uniprot_trembl.fasta";

my $rootdir;

open(IFCONFIG,"/sbin/ifconfig |");
undef $/;
my $ifconfig=<IFCONFIG>;
close(IFCONFIG);
$/="\n";

if (defined $ENV{TK_ROOT}) {

    # Directory paths for 64 bit nodes of toolkit
    $rootdir=$ENV{TK_ROOT};
    $database_dir = "$rootdir/databases";
    $bioprogs_dir = "$rootdir/bioprogs";
    $perl=     "$bioprogs_dir/hhomp"; 
    $hh=       "$bioprogs_dir/hhomp"; 
    $calhhm=   "$database_dir/hhpred/cal.hhm"; 
    $proftmb_dir = "$bioprogs_dir/proftmb";
    $prof_db = "$database_dir/standard/uniprot_sprot.fasta $database_dir/standard/uniprot_trembl.fasta";
    $nr    =   "$database_dir/standard/nr";             # nr database to be used
    $nre   =   "$database_dir/standard/nre";            # nr database to be used
#    $nrf   =   "$database_dir/standard/nrf";            # nr database to be used
    $nr90  =   "$database_dir/standard/nr90";           # large nr database to be used
    $nr70  =   "$database_dir/standard/nr70";           # large nr database to be used
#    $nr90f =   "$database_dir/standard/nr90f";          # large nr database to be used
#    $nr70f =   "$database_dir/standard/nr70f";          # reduced nr database to be used
    $dummydb=  "$database_dir/do_not_delete/do_not_delete"; # blast database consisting of just one sequence
}



$ncbidir = "$bioprogs_dir/blast";             # Where the NCBI programs have been installed 
$execdir = "$bioprogs_dir/psipred/bin";       # Where the PSIPRED V2 programs have been installed
$datadir = "$bioprogs_dir/psipred/data";      # Where the PSIPRED V2 data files have been installed    
$blastpgp= "$ncbidir/blastpgp";

return 1;
