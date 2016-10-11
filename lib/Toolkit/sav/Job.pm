#!/usr/bin/perl -w
package Toolkit::Job;

use lib '/var/web/railstoolkit/lib';
use warnings;
use strict;
use Toolkit::Config;

use base 'Toolkit::DBI';
Toolkit::Job->table('jobs');
Toolkit::Job->columns(All => qw/ id name parent_id jobid params_yml user_id status tool created_on updated_on viewed_on env_yml config_yml forward_controller forward_action forward_params_yml/ );
Toolkit::Job->has_many(queuejobs => 'Toolkit::Queue');

my %q2q_prec = ( $STATUS_INIT    => 5,
		 $STATUS_QUEUED  => 3, 
		 $STATUS_RUNNING => 2, 
		 $STATUS_DONE    => 4, 
		 $STATUS_ERROR   => 1
		 );
my %q2j_prec = ( $STATUS_INIT    => 5,
		 $STATUS_QUEUED  => 4, 
		 $STATUS_RUNNING => 3, 
		 $STATUS_DONE    => 2, 
		 $STATUS_ERROR   => 1
		 );

sub retrieve_by_jobid {
    my $self = shift;
    my $jobid = shift;
    return $self->retrieve(jobid => $jobid);
}

sub update_status {
    my $self = shift;
    my @qjobs = $self->queuejobs;
    my $status = $STATUS_INIT;
    my $final = 0;
    foreach my $qjob (@qjobs) {
	if ($qjob->final) { $final = 1; }
	if ($q2q_prec{$qjob->status} < $q2q_prec{$status}) {
	    print $qjob->id." "." ".$qjob->status." ".$status;
	    $status = $qjob->status;
	    print " ".$status."\n";
	}
    }
    if ($q2j_prec{$self->status} < $q2j_prec{$status}) {
	$status = $self->status;
    }
    if (!$final && $status eq $STATUS_DONE) {
	$status = $STATUS_RUNNING;
    }
    $self->status($status);
    $self->update;
}

1;


