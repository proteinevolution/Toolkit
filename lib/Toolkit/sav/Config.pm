#!/usr/bin/perl -w
package Toolkit::Config;

use strict;
use vars qw(@ISA @EXPORT);
use Exporter;
use YAML::Parser::Syck;

our @ISA          = qw(Exporter);
our @EXPORT       = qw($TOOLKIT_ROOT $DB_NAME $DB_HOST $DB_USERNAME $DB_PASSWORD $_YAML_DB_EXTENSION 
							  $database_dir $bioprogs_dir
		       			  $STATUS_INIT $STATUS_QUEUED $STATUS_RUNNING $STATUS_DONE $STATUS_ERROR
			   		     );

# server config variables
our $TOOLKIT_ROOT = '/var/web/railstoolkit';
our $SHARED_ROOT = '/cluster/railstoolkit';
our $CONFIG = $SHARED_ROOT.'/config';

our $database_dir = '/cluster/databases';
our $bioprogs_dir = '/cluster/bioprogs';

our $DB_NAME = &rails_db_config('database');
our $DB_USERNAME = &rails_db_config('username');
our $DB_PASSWORD = &rails_db_config('password');
our $DB_HOST = &rails_db_config('host');

our $_YAML_DB_EXTENSION = '_yml';

our $STATUS_INIT = 'i';
our $STATUS_QUEUED = 'q';
our $STATUS_RUNNING = 'r';
our $STATUS_DONE = 'd';
our $STATUS_ERROR = 'e';

sub rails_db_config {
    my $key = shift;
    my $database_file = $CONFIG.'/database.yml';
    
    open (DBCONF, "<$database_file") or die ("Cannot open: $!");
    my @lines = <DBCONF>;
    close(DBCONF) or die ("Cannot close: $!");

    my $hash = YAML::Parser::Syck::Parse(join("", @lines));
    my $env;
    if (defined $ENV{RAILS_ENV}) {
	$env = $ENV{RAILS_ENV};
    } else {
	$env = 'development';
    }
    return ${$hash}{$env}{$key};
}

1;

