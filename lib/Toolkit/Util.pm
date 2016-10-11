#!/usr/bin/perl -w
use lib "/cluster/www/toolkit/lib";

package Toolkit::Util;

use Toolkit::Job;
use vars qw(@ISA @EXPORT);
use Exporter;

our @ISA		= qw(Exporter);
our @EXPORT = qw(&url_for);

sub url_for {
	my $controller = undef;
	my $jobid = undef;
	my $job = undef;
	my $action = undef;
	my $anchor = "";
	
	for(my $i = 0; $i < scalar(@_); $i++) {
		if ($_[$i] eq "controller") {
			$i++;
			$controller = $_[$i];
		} elsif ($_[$i] eq "action") {
			$i++;
			$action = $_[$i];
		} elsif ($_[$i] eq "job") {
			$i++;
			$job = $_[$i];
		} elsif ($_[$i] eq "jobid") {
			$i++;
			$jobid = $_[$i];
		} elsif ($_[$i] eq "anchor") {
			$i++;
			$anchor = "#".$_[$i];
		}
	}
	
	if (!defined $controller && defined $jobid) {
		$job = Toolkit::Job->retrieve_by_jobid($jobid);
		$controller = $job->tool;
	}
	
	my $ret = "";
	if (defined $jobid) {
		$ret = "/$controller/$action/$jobid$anchor";
	} elsif (defined $job) {
		$ret = "/$controller/$action/$job$anchor";
	} else {
		$ret = "/$controller/$action$anchor";	
	}
	return $ret;
}

1;
