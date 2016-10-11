#!/usr/bin/perl -w

use Three2One;

my $overallsuccess = 1;
$overallsuccess = test(test1(), "test1") && $overallsuccess;
$overallsuccess = test(test2(), "test2") && $overallsuccess;
$overallsuccess = test(test3(), "test3") && $overallsuccess;
$overallsuccess = test(test4(), "test4") && $overallsuccess;
$overallsuccess = test(test5(), "test5") && $overallsuccess;

if ($overallsuccess) {
    print "All tests passed.\n";
} else {
    print "At least one test failed.\n";
}

sub test {
    my $result = shift();
    my $name = shift();
    if ($result) {
	print("$name successful.\n");
    } else {
	print("$name failed.\n");
    }
    $result;
}

sub test1 {
    Three2One::three2OneLetter("HAR") eq "R";
}

sub test2 {
    Three2One::three2OneLetter("ABC") eq "X";
    # side effect of warning printed to stderr is not tested.
}

sub test3 {
    !(Three2One::three2OneLetter("HAR") eq "H");
}

sub test4 {
    my %warnings = ();
    my $oneLetter = Three2One::three2OneLetter("HAR", \%warnings);
    $oneLetter eq "R" && !defined($warnings{ 'Three2One' });
}

sub test5 {
    my %warnings = ();
    my $oneLetter = Three2One::three2OneLetter("ABC", \%warnings);
    "X" eq $oneLetter && defined($warnings{ 'Three2One' });
}
