#!/usr/bin/perl -w
package Toolkit::Job;

use lib "/cluster/www/toolkit/lib";
use warnings;
use strict;
use Toolkit::Config;
use Toolkit::Action;

use base 'Toolkit::DBI';
Toolkit::Job->table('jobs');
Toolkit::Job->columns(All => qw/ id type parent_id jobid user_id status tool created_on updated_on viewed_on/ );
Toolkit::Job->has_many(actions => 'Toolkit::Action');

sub retrieve_by_jobid {
    my $self = shift;
    my $jobid = shift;
    return $self->retrieve(jobid => $jobid);
}

sub update_status {
    my $self = shift;
    my @actions = $self->actions;
    my $status = $STATUS_INIT;
    foreach my $action (@actions) {
	if ($STATUS_CMP1{$action->status} < $STATUS_CMP1{$status}) {
	    $status = $action->status;
	}
    }
    if ($STATUS_CMP2{$self->status} < $STATUS_CMP2{$status}) {
	$status = $self->status;
    }
    $self->status($status);
    $self->update;
}

sub url_for_job_dir {
    my $self = shift;
    "/tmp/development/".$self->id;
}
sub job_dir {
    my $self = shift;
    $TMP."/".$self->id;
}
sub url_for_job_dir_abs {
    my $self = shift;
    $DOC_ROOTURL."tmp/development/".$self->id;
}
sub last_action {
    my $self = shift;
    return ($self->actions)[-1];
}
1;


