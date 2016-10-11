#!/usr/bin/perl -w
use lib "/cluster/www/toolkit/lib";

package Toolkit::QueueJob;
use base 'Toolkit::DBI';
use Toolkit::PbsWorker;
use Toolkit::Config;
use Toolkit::YAML;

Toolkit::QueueJob->table('queue_jobs');
Toolkit::QueueJob->columns(All => qw/ id final status action_id parallel commands options parent_id created_on/ );
Toolkit::QueueJob->has_a(action_id => 'Toolkit::Action');
# make this dynamic depending on config env
Toolkit::QueueJob->has_many(workers => 'Toolkit::PbsWorker');

my $max_parallel_jobs = 200;

sub update_status {
    my $self = shift;
    my @qworkers = $self->workers;
    my $status = $STATUS_INIT;
    foreach my $qw (@qworkers) {
	if ($STATUS_CMP1{$qw->status} < $STATUS_CMP1{$status}) {
	    $status = $qw->status;
	}
    }
    if ($STATUS_CMP2{$self->status} < $STATUS_CMP2{$status}) {
	$status = $self->status;
    }
    $self->status($status);
    $self->update;
    $self->action->update_status;
}

sub submit {
    my $self = shift;
    my $cmds_aref = shift; # ref to array with commands
    my @commands = @{$cmds_aref};
    my $final = shift || 1;
    my $opts_href = shift || {};

    $self->status($STATUS_QUEUED);
    $self->commands(join("\n", @commands));
    $self->final($final);
    $self->options(Toolkit::YAML->dump_hash($opts_href));
    $self->update;
    
    my $qw = $self->spawn_worker($self->commands, $opts_href);
    $qw->exec;
    return $self;
}

sub submit_parallel {
    my $self = shift;
    my $cmds_aref = shift; # ref to array with commands
    my @commands = @{$cmds_aref};
    my $final = shift || 1;
    my $opts_href = shift || {};

    $self->status($STATUS_QUEUED);
    $self->commands(join("\n", @commands));
    $self->final($final);
    $self->options(Toolkit::YAML->dump_hash($opts_href));
    $self->update;
    
    my  @qws;
    foreach my $cmd (@commands) {
	push(@qws, $self->spawn_worker($cmd, $opts_href));
    }
    foreach my $qw (@qws) {
	$qw->exec;
    }
    return $self;
}

sub spawn_worker {
    my $self = shift;
    my $cmds = shift;
    my $opts_hash_ref = shift;
    # make this dynamic!
    my $qw = Toolkit::PbsWorker->create({
	options => Toolkit::YAML->dump_hash($opts_hash_ref),
	commands => $cmds,
	status => $STATUS_INIT,
	queue_job_id => $self->id
	});
    return $qw;
}

1;
