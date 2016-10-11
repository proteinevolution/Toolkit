#!/usr/bin/perl -w
use lib "/cluster/www/toolkit/lib";

package Toolkit::PbsWorker;
use base 'Toolkit::DBI';
use YAML::Parser::Syck;
use Toolkit::Config;
use Toolkit::QueueJob;

Toolkit::PbsWorker->table('queue_workers');
Toolkit::PbsWorker->columns(All => qw/ id type commands commandfile queue_job_id pbsid options status created_on/ );
Toolkit::PbsWorker->has_a(queue_job_id => 'Toolkit::QueueJob');

sub exec {
    my $self = shift;
    #my $options = YAML::Parser::Syck::Parse($self->options);
    my $qj = $self->queue_job;
	
    my $fh;
    my $job_dir = $qj->action->job_dir;
    my $basename = $job_dir."/".$qj->action->jobid;
    my $commandfile = $job_dir."/".$self->id.".sh";
    $self->commandfile($commandfile);
    $self->update;

    open($fh, ">$commandfile") or die("Cannot open '$commandfile'!\n");    
    print $fh "#!/bin/bash\n";
    print $fh "#PBS -e localhost:$job_dir/stderr\n";
    print $fh "#PBS -o localhost:$job_dir/stdout\n";
    print $fh "#PBS -q web\n";
    print $fh "#PBS -N TOOLKIT\n";
    print $fh "#PBS -A TOOLKIT\n";
    print $fh "#PBS -d $job_dir\n";
    print $fh "#PBS -m n\n";
    print $fh "#PBS -r n\n";
    print $fh "export RAILS_ENV=$RAILS_ENV;\n";
    print $fh $SCRIPTS."/qupdate.rb ".$self->id." $STATUS_RUNNING\n"; 
    print $fh ($self->commands)."\n";
    print $fh $SCRIPTS."/qupdate.rb ".$self->id." $STATUS_DONE\n"; 
    close($fh);
    system("chmod 777 $commandfile");
    
    my $command = "$PBS/qsub $commandfile";
    my $pbsid = qx/$command/;
    chomp($pbsid);
    $self->pbsid($pbsid);
    $self->update;
}

sub destroy {
}

1;
