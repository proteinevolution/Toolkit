package MyPaths;

use lib "/cluster/lib";
use lib "/home/soeding/perl";
use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $VERSION);
use Exporter;
our $VERSION=1.00;
our @ISA          = qw(Exporter);
#our @EXPORT       = qw($nr $nre $nrf $nr90 $nr70 $nr90f $nr70f $dummydb $perl $hh $dsspdir $dssp $pdbdir $ncbidir $execdir $datadir $blastpgp $calhhm $hmmerdir $newdbs $olddbs $PBS $database_dir $bioprogs_dir $pdb_dir $tmp_dir);
our @EXPORT       = qw($nr $nre $nr90 $nr70 $dummydb $perl $hh $dsspdir $dssp $pdbdir $ncbidir $ncbipdir $execdir $datadir $blastpgp $calhhm $hmmerdir $newdbs $olddbs $PBS $database_dir $bioprogs_dir $pdb_dir $tmp_dir);

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
our $perl;                         # perl directory: querydb.pl, alignhits.pl
our $hh;                           # hhfilter, hhsearch, hhalign, hhmake, hhcorr 
our $calhhm;                       # newest scop database for hhsearch
our $dsspdir;                      # where are the dssp files?
our $dssp;                         # where is the dssp executable? 
our $pdbdir;                       # where are the pdb files?
our $ncbidir;                      # Where the NCBI legacy programs have been installed
our $ncbipdir;                     # Where the NCBI Blast+ programs have been installed
our $execdir;                      # Where the PSIPRED V2 programs have been installed
our $datadir;                      # Where the PSIPRED V2 data files have been installed
our $blastpgp;                     # blastpgp executable
our $hmmerdir;                     # hmmer executables (hmmbuild, hmmsearch etc.)
our $newdbs;                       # directory containing new HHpred databases
our $olddbs;                       # directory containing old HHpred databases
our $PBS="/usr/local/PBS/bin";     # location of PBS/torque binaries qstat, qsub, qdel, etc
our $database_dir = "/cluster/databases";
our $bioprogs_dir = "/cluster/www/toolkit/bioprogs";
our $pdb_dir = "/cluster/databases/pdb/all";
my $rootdir;
#my $hostname=`hostname`;
#my $servername=$ENV{"SERVER_NAME"};

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
    $pdb_dir = "$database_dir/pdb/all";
    $perl=     "$bioprogs_dir/hhpred";         # perl directory: reformat.pl, alignhits.pl
    $hh=       "$bioprogs_dir/hhpred";         # hhfilter
    $calhhm=   "$database_dir/hhpred/cal.hhm"; # 
    $pdbdir=   "$pdb_dir";                     # where are the pdb files?
    $newdbs ="$database_dir/hhpred/new_dbs";
    $olddbs ="$database_dir/hhpred/old_dbs";
    $nr    =   "$database_dir/standard/nr";             # nr database to be used
    $nre   =   "$database_dir/standard/nre";            # nr database to be used
#    $nrf   =   "$database_dir/standard/nrf";            # nr database to be used
    $nr90  =   "$database_dir/standard/nr90";           # large nr database to be used
    $nr70  =   "$database_dir/standard/nr70";           # large nr database to be used
#    $nr90f =   "$database_dir/standard/nr90f";          # large nr database to be used
#    $nr70f =   "$database_dir/standard/nr70f";          # reduced nr database to be used
    $dummydb=  "$database_dir/do_not_delete/do_not_delete"; # blast database consisting of just one sequence

} elsif (-e "/cluster/bioprogs/hhpred") {

    # Directory paths for 64 bit webserver cluster nodes
    $database_dir = "/cluster/databases";
    $bioprogs_dir = "/cluster/bioprogs";
    $pdb_dir = "/cluster/databases/pdb/all";
    $perl=     "$bioprogs_dir/hhpred";         # perl directory: reformat.pl, alignhits.pl
    $hh=       "$bioprogs_dir/hhpred";         # hhfilter
    $calhhm=   "$database_dir/hhpred/cal.hhm"; # 
    $pdbdir=   "$pdb_dir";                     # where are the pdb files?
    $dsspdir = "/cluster/dssp/data";           # where are the dssp files?
    $dssp=     "/cluster/dssp/bin/dsspcmbi";   # where is the dssp executable? 
    $newdbs ="$database_dir/hhpred/new_dbs";
    $olddbs ="$database_dir/hhpred/old_dbs";
    $nr    =   "$database_dir/nr";             # nr database to be used
    $nre   =   "$database_dir/nre";            # nr database to be used
#    $nrf   =   "$database_dir/nrf";            # nr database to be used
    $nr90  =   "$database_dir/nr90";           # large nr database to be used
    $nr70  =   "$database_dir/nr70";           # large nr database to be used
#    $nr90f =   "$database_dir/nr90f";          # large nr database to be used
#    $nr70f =   "$database_dir/nr70f";          # reduced nr database to be used
    $dummydb=  "$database_dir/do_not_delete/do_not_delete"; # blast database consisting of just one sequence

} elsif (-e "/cluster/bioprogs/hhpred") {

    # Directory path for my PC, SUN, etc
#    $database_dir="/home/soeding/nr";
    $database_dir="/raid/db/blast";
    $bioprogs_dir="/home/soeding/programs"; 
    $pdbdir=   "/raid/db/pdb/all";             # where are the pdb files?
    $perl=     "/home/soeding/perl";           # perl directory: reformat.pl, alignhits.pl
    $hh=       "/home/soeding/hh";             # hhfilter
    $calhhm=   "/home/soeding/nr/cal.hhm"; # 
    $dsspdir = "";
    $dsspdir = "/raid/db/dssp/data";           # where are the dssp files?
    $dssp=     "/raid/db/dssp/bin/dsspcmbi";   # where is the dssp executable? 
#    $newdbs =  "/raid/users/soeding";
    $nr    =   "$database_dir/nr";             # nr database to be used
    $nre   =   "$database_dir/nre";            # nr database to be used
#    $nrf   =   "$database_dir/nrf";            # nr database to be used
    $nr90  =   "$database_dir/nr90";           # large nr database to be used
    $nr70  =   "$database_dir/nr70";           # large nr database to be used
#    $nr90f =   "$database_dir/nr90f";          # large nr database to be used
#    $nr70f =   "$database_dir/nr70f";          # reduced nr database to be used
    $dummydb=  "$database_dir/do_not_delete/do_not_delete"; # blast database consisting of just one sequence


} else {

    # Directory paths for everyone with access to raid
    $database_dir="/raid/db/blast";
    $bioprogs_dir="/raid/progs/bioinf";
    $perl=     "/raid/users/soeding/perl";           # perl directory: reformat.pl, alignhits.pl
    $hh=       "/raid/users/soeding/hh";             # hhfilter
    $calhhm=   "/raid/users/soeding/scop50.2/db/cal.hhm"; # 
    $pdbdir=   "/raid/db/pdb/all";       # where are the pdb files?
    $dsspdir = "/raid/db/dssp/data";     # where are the dssp files?
    $dssp=     "/raid/db/dssp/bin/dsspcmbi"; # where is the dssp executable? 
    $newdbs ="/raid/users/soeding";
    $nr    =   "$database_dir/nr";             # nr database to be used
#    $nrf   =   "$database_dir/nrf";            # nr database to be used
    $nr90  =   "$database_dir/nr90";           # large nr database to be used
    $nr70  =   "$database_dir/nr70";           # large nr database to be used
#    $nr90f =   "$database_dir/nr90f";          # large nr database to be used
#    $nr70f =   "$database_dir/nr70f";          # reduced nr database to be used
    $dummydb=  "$database_dir/do_not_delete/do_not_delete"; # blast database consisting of just one sequence
}



$ncbidir = "$bioprogs_dir/blast";             # Where the NCBI legacy programs have been installed 
$ncbipdir= "$bioprogs_dir/blastplus/bin";     # Where the NCBI Blast+ programs have been installed
$execdir = "$bioprogs_dir/psipred/bin";       # Where the PSIPRED V2 programs have been installed
$datadir = "$bioprogs_dir/psipred/data";      # Where the PSIPRED V2 data files have been installed    
$blastpgp= "$ncbidir/blastpgp";
$hmmerdir= "$bioprogs_dir/hmmer/binaries";

return 1;
