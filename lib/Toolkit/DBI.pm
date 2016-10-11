#!/usr/bin/perl -w
package Toolkit::DBI;

use YAML::Parser::Syck;
use Toolkit::Config;
use base 'Class::DBI';

Toolkit::DBI->connection("dbi:mysql:$DB_NAME;$DB_HOST", $DB_USERNAME, $DB_PASSWORD);

sub accessor_name_for {
    my ($class, $column) = @_;
    $column =~ s/_id$//;
    return $column;
}

sub AUTOLOAD {
	my $self = shift;
	my $attr = $AUTOLOAD;
	$attr =~ s/.*:://;
	return unless $attr =~ /[^A-Z]/; #skip all uppercase methods
	my $name = lc($attr);

	if (scalar(@_)==1) {
		my $ref = YAML::Parser::Syck::Parse($self->$name());
		if (defined ${$ref}{$_[0]}) {
			return ${$ref}{shift(@_)};
#		} elsif(defined ${$ref}[$_[0]])  {
#			return ${$ref}[shift(@_)];
		}
	} else {
		return YAML::Parser::Syck::Parse($self->$name());
	}
}

1;
