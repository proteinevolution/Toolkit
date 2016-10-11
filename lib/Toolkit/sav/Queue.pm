#!/usr/bin/perl -w
package Toolkit::Queue;
use base 'Toolkit::DBI';

Toolkit::Queue->table('queuejobs');
Toolkit::Queue->columns(All => qw/ id final status job_id/ );
Toolkit::Queue->has_a(job_id => 'Toolkit::Job');

my $max_parallel_jobs = 200;

sub update_status {
    my $self = shift;
    my $id = shift; 
    my $status = shift;

    my $qj = $self->retrieve(id => $id);
    $qj->status($status);
    $qj->update;
    $qj->job->update_status;
}

sub submit {
    my $self = shift;
    my $job = shift;
    my $array_ref = shift;
    my @commands = @{$array_ref};
    my $final = shift || 1;
    my $wait = shift || 0;
    
    my $qj = $self->create({job_id => $job->id,
			   status => 'q',
			   final  => $final});
    
    my $first_command = ""; # Queue->update auf running
    my $last_command = ""; # Queue->update auf error / done
    splice(@commands, 0, 0, $first_command);
    push(@commands, $last_command);

    &call($job, \@commands, $wait);
}

sub submit_parallel {
    my $self = shift;
    my $job = shift;
    my $array_ref = shift;
    my @commands = @{$array_ref};
    my $final = shift || 1;
    my $wait = shift || 0;
    
    # check max paralell jobs limit
    if (scalar(@commands) > $max_parallel_jobs) {
	return 0;
    }
    
}

sub call
{
    my $self  = shift;
    my $job = shift;
    my $array_ref = shift;
    my @commands = @{$array_ref};
    my $wait = $wait;
    
    # variables
    my $jobDir = $job->env("work_dir");
    
    my $fh;
    my $basename=$jobDir.$job->jobid;
    
    my $shFileName = $basename.".sh";
    open($fh, ">$shFileName") or die("Cannot open '$shFileName'!\n");

    print $fh "#!/bin/sh\n";
    print $fh "#PBS -e localhost:$basename.stderr\n";
    print $fh "#PBS -o localhost:$basename.stdout\n";
    print $fh "#PBS -q web\n";
    print $fh "#PBS -N TOOLKIT\n";
    print $fh "#PBS -A TOOLKIT\n";
    print $fh "#PBS -d $jobDir\n";
    print $fh "#PBS -m n\n";
    print $fh "#PBS -r n\n";
#    print $fh "# hostname: ".`hostname`;              # write web server name into shell file
#    print $fh "hostname > $basename.exec_host\n"; # write name of execution node into $qid.exec_host
#    if (defined $stable_servers{$hostname}) {
#	 	print $fh "export TOOLKIT_STABLE=\"defined\";\n";
#    }
    
    foreach (@commands) {
	print $fh $_ . "\n";
    }
    close($fh);
    system("chmod 777 $shFileName");
    
    my $command = "$pbs_dir/qsub $shFileName";
    my $pbsid = `$command`;
    chomp($pbsid);

    if ($pbsid !~ /^\d+\.\S+\.\S+$/) {
	# error!
	return 0;
    }
}


1;
