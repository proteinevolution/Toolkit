#!/usr/bin/perl -w
package Toolkit::Config;

use strict;
use vars qw(@ISA @EXPORT);
use Exporter;
use YAML::Syck;

our @ISA		= qw(Exporter);
our @EXPORT = qw($RAILS_ENV $TOOLKIT_ROOT $CONFIG $LIB $DATABASES $PDB $BIOPROGS $TMP $JOBSCRIPTS
		 $SCRIPTS $DOC_ROOTURL $JAVA_EXEC $JAVA_1_5_EXEC $DATA $PBS $SMTP_SERVER $DB_NAME $DB_HOST 
		 $DB_USERNAME $DB_PASSWORD $_YAML_DB_EXTENSION $STATUS_INIT $STATUS_QUEUED $STATUS_RUNNING 
		 $STATUS_DONE $STATUS_ERROR %STATUS_CMP1 %STATUS_CMP2
		 );

# get Server by environment variable
our $RAILS_ENV;
if (defined $ENV{RAILS_ENV} && $ENV{RAILS_ENV} ne "") {
	$RAILS_ENV = $ENV{RAILS_ENV};
} else {
	$RAILS_ENV = 'development';
}

# server config variables
our $TOOLKIT_ROOT = $ENV{TK_ROOT};

our $CONFIG = $TOOLKIT_ROOT.'/config';
our $LIB = $TOOLKIT_ROOT.'/lib';

our $DATABASES = $TOOLKIT_ROOT.'/databases/';
our $PDB = $DATABASES.'/pdb/all';
our $BIOPROGS = $TOOLKIT_ROOT.'/bioprogs/';
our $TMP = $TOOLKIT_ROOT.'/tmp/'.$RAILS_ENV;
our $JOBSCRIPTS	= $TOOLKIT_ROOT.'/perl';
our $SCRIPTS = $TOOLKIT_ROOT.'/script';

our $DOC_ROOTURL = &rails_env_server_config('DOC_ROOTURL');

our $JAVA_EXEC = &rails_env_server_config('JAVA_EXEC');
our $JAVA_1_5_EXEC = &rails_env_server_config('JAVA_1_5_EXEC');
our $DATA = &rails_env_server_config('DATA');
our $PBS = &rails_env_server_config('PBS');

our $SMTP_SERVER = &rails_env_server_config('SMTP_SERVER');

our $DB_NAME = &rails_db_config('database');
our $DB_USERNAME = &rails_db_config('username');
our $DB_PASSWORD = &rails_db_config('password');
our $DB_HOST = &rails_db_config('host');

our $_YAML_DB_EXTENSION = '_yml';

our $STATUS_INIT = &rails_env_server_config('STATUS_INIT');
our $STATUS_QUEUED = &rails_env_server_config('STATUS_QUEUED');
our $STATUS_RUNNING = &rails_env_server_config('STATUS_RUNNING');
our $STATUS_DONE = &rails_env_server_config('STATUS_DONE');
our $STATUS_ERROR = &rails_env_server_config('STATUS_ERROR');

our %STATUS_CMP1 = ( $STATUS_INIT    => 5,
		 $STATUS_QUEUED  => 3, 
		 $STATUS_RUNNING => 2, 
		 $STATUS_DONE    => 4, 
		 $STATUS_ERROR   => 1
		 );
our %STATUS_CMP2 = ( $STATUS_INIT    => 5,
		 $STATUS_QUEUED  => 4, 
		 $STATUS_RUNNING => 3, 
		 $STATUS_DONE    => 2, 
		 $STATUS_ERROR   => 1
		 );


##########################################################
# help functions
##########################################################

sub rails_db_config {
    my $key = shift;
    my $database_file = $CONFIG.'/database.yml';
    
    open (DBCONF, "<$database_file") or die ("Cannot open: $!");
    my @lines = <DBCONF>;
    close(DBCONF) or die ("Cannot close: $!");

    my $hash = YAML::Parser::Syck::Parse(join("", @lines));
    return ${$hash}{$RAILS_ENV}{$key};
}

sub rails_env_server_config {
	my $key = shift;
	my $env_file = $CONFIG.'/environments/'.$RAILS_ENV.'.yml';
    
	open (ENVCONF, "<$env_file") or die ("Cannot open: $env_file!");
	my @lines = <ENVCONF>;
	close(ENVCONF) or die ("Cannot close: $!");

	my $hash = YAML::Parser::Syck::Parse(join("", @lines));
	return ${$hash}{$key};
}


1;

