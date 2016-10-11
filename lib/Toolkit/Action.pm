#!/usr/bin/perl -w
package Toolkit::Action;

use lib "/cluster/www/toolkit/lib";
use warnings;
use strict;
use File::Basename;
use File::Spec::Functions;
use Toolkit::Config;
use Toolkit::QueueJob;

use base 'Toolkit::DBI';
Toolkit::Action->table('actions');
Toolkit::Action->columns(All => qw/ id status type params job_id forward_controller forward_action flash forwarded created_on/ );
Toolkit::Action->has_many(queue_jobs => 'Toolkit::QueueJob');
Toolkit::Action->has_a(job_id => 'Toolkit::Job');

sub update_status {
    my $self = shift;
    my @qjobs = $self->queue_jobs;
    my $status = $STATUS_INIT;
    my $final = 0;
    foreach my $qjob (@qjobs) {
	if ($qjob->final) { $final = 1; }
	if ($STATUS_CMP1{$qjob->status} < $STATUS_CMP1{$status}) {
	    $status = $qjob->status;
	}
    }
    if ($STATUS_CMP2{$self->status} < $STATUS_CMP2{$status}) {
	$status = $self->status;
    }
    if (!$final && $status eq $STATUS_DONE) {
	$status = $STATUS_RUNNING;
    }
    $self->status($status);
    $self->update;
    $self->job->update_status;
}


sub url_for_job_dir {
    my $self = shift;
    $self->job->url_for_job_dir;
}
sub job_dir {
    my $self = shift;
    $self->job->job_dir;
}
sub url_for_job_dir_abs {
    my $self = shift;
    $self->job->url_for_job_dir_abs;
}

sub jobid {
    my $self = shift;
    $self->job->jobid;
}

sub queue {
    my $self = shift;    
    my $qj = Toolkit::QueueJob->create({action_id => $self->id,
					status => $STATUS_INIT});
    return $qj;
}

sub params_to_file {
    my $self = shift;
    my $filepath = shift; 
    my @fields = @_;

    foreach my $key (@fields) {
	if(defined $self->params($key) && $self->params($key) ne "") {
	    my $val = $self->params($key);
	    if(-f $val) {
		rename $val, $filepath; 
	    } else {
		open(OUT, ">$filepath");
		print OUT $val;
		close(OUT);
	    }
	    return 1;
	}
    }
    return 0;
}

sub param {
	my $self = shift;
	return $self->params(shift);
}

1;


