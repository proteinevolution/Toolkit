#!/usr/bin/perl -w
use lib "/cluster/www/toolkit/lib";
package Toolkit::Log;

use Toolkit::Config;

use Log::Log4perl qw(:easy);
Log::Log4perl->init("$LIB/Toolkit/log.conf");

#Functions for logger
$SIG{__DIE__} = sub {
        $Log::Log4perl::caller_depth++;
        LOGDIE @_;
    };
$SIG{__WARN__} = sub {
        local $Log::Log4perl::caller_depth =
            $Log::Log4perl::caller_depth + 1;
        WARN @_;
    };

return 1;
