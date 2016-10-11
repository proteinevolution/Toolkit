#!/usr/bin/perl -w

package ServerConfig;

use strict;
use vars qw(@ISA @EXPORT);
use Exporter;

our @ISA          = qw(Exporter);
our @EXPORT       = qw($tmp_dir $tmp_dir_relative $doc_rooturl $cgi_rooturl $pdb_dir $database_dir $hostname $web_dir 
$bioprogs_dir $is_public $java_exec $cluster_dir $data_dir $smtp_server $java_1_5_exec $pbs_dir %stable_servers);

# server config variables
our $tmp_dir;
our $tmp_dir_relative;
our $doc_rooturl;
our $cgi_rooturl;
our $pdb_dir="/cluster/databases/pdb/all";
our $database_dir="/cluster/databases";
our $bioprogs_dir;
our $is_public;
our $java_exec="/opt/blackdown-jdk-1.4.2.01/bin/java";
our $cluster_dir="/cluster";
our $data_dir="/cluster/data" ;
our $smtp_server="mailhost.tuebingen.mpg.de";
our $java_1_5_exec="/opt/jdk/bin/java";
our $pbs_dir="/usr/local/PBS/bin";
our $hostname=`hostname`;
chomp($hostname);
our $web_dir="/var/web";

# The names in this list are the names of the production web server(s). 
# Calls to &qsubmit() in Queue.pm will have a 'export TOOLKIT_STABLE="defined"' command included in their bash scripts
our %stable_servers=(
		     "10.35.1.25"=>1, 
		     "cerberus"=>1
		     );      
#our %stable_servers={"10.35.1.40"=>1, "chimaera"=>1};

if ( 1 || defined $ENV{'TOOLKIT_STABLE'} ||
      defined $stable_servers{$hostname} ||
     ((defined $ENV{'REMOTE_ADDR'}) && ($ENV{'REMOTE_ADDR'} !~ /10\.35/)) ||
     ((defined $ENV{'SERVER_PORT'}) && ($ENV{'SERVER_PORT'} =~ /8181/)) 
     ) {
    # external production environment
    $doc_rooturl = "http://protevo.eb.tuebingen.mpg.de";
    $cgi_rooturl = "http://protevo.eb.tuebingen.mpg.de/cgi-bin";
    $is_public = 1;
    $bioprogs_dir="/cluster/bioprogs_stable";
    $tmp_dir="/cluster/tmp";
    $tmp_dir_relative="../tmp";
} else {
    # chimaera, development environment
    $doc_rooturl = "http://10.35.1.40";
    $cgi_rooturl = "http://10.35.1.40/cgi-bin";
    $is_public = 0;
    $bioprogs_dir="/cluster/bioprogs_stable";
    $tmp_dir="/cluster/tmp/chimaera";
    $tmp_dir_relative="../tmp/chimaera";
}

return 1;
