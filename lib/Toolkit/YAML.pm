#!/usr/bin/perl -w
use lib "/cluster/www/toolkit/lib";

package Toolkit::YAML;

sub dump_hash {
    my %hash = @{$_[0]};
    my $ret = "";
    foreach my $key (keys %hash) {
	$ret .= $key.": ".$hash{$key}."\n";
    }
    return $ret;
}
1;
