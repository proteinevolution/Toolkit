#!/usr/bin/perl -w

package Three2One;

# Module Three2One provides a mapping from three letter sequences to one letter.
# Its interface function is named three2OneLetter.

# Three to one letter code for PDB-sequences
my %three2one=(

    # defined amino acid mappings (last check using http://www.ebi.ac.uk/pdbe-srv/pdbechem/: January 2014)
    "ALA"=>"A","VAL"=>"V","PHE"=>"F","PRO"=>"P","MET"=>"M","ILE"=>"I","LEU"=>"L","ASP"=>"D",
    "GLU"=>"E","LYS"=>"K","ARG"=>"R","SER"=>"S","THR"=>"T","TYR"=>"Y","HIS"=>"H","CYS"=>"C",
    "ASN"=>"N","GLN"=>"Q","TRP"=>"W","GLY"=>"G","3AH"=>"H","AIB"=>"A","ALO"=>"T","ALY"=>"K",
    "ARM"=>"R","ASA"=>"D","ASB"=>"D","ASL"=>"D","AYA"=>"A","BCS"=>"C","BHD"=>"D","BMT"=>"T",
    "BUC"=>"C","C5C"=>"C","C6C"=>"C","CCS"=>"C","CME"=>"C","CSO"=>"C","CSP"=>"C","CSS"=>"C",
    "CSW"=>"C","CXM"=>"M","CY1"=>"C","CY3"=>"C","CYG"=>"C","CYQ"=>"C","DAH"=>"F","DAL"=>"A",
    "DAR"=>"R","DAS"=>"D","DCY"=>"C","DGL"=>"E","DGN"=>"Q","DHI"=>"H","DIL"=>"I","DIV"=>"V",
    "DLE"=>"L","DLY"=>"K","DNP"=>"A","DPN"=>"F","DPR"=>"P","DSN"=>"S","DTH"=>"T","DTR"=>"W",
    "DTY"=>"Y","DVA"=>"V","EFC"=>"C","FLA"=>"A","FME"=>"M","GLZ"=>"G","GMA"=>"E","GSC"=>"G",
    "HAR"=>"R","HIC"=>"H","HIP"=>"H","HMR"=>"R","HPQ"=>"F","HTR"=>"W","HYP"=>"P","IIL"=>"I",
    "IYR"=>"Y","KCX"=>"K","LLP"=>"K","LLY"=>"K","LYZ"=>"K","MAA"=>"A","MEN"=>"N","MHS"=>"H",
    "MIS"=>"S","MLE"=>"L","MPQ"=>"G","MSA"=>"G","MSE"=>"M","MVA"=>"V","NEP"=>"H","NLE"=>"L",
    "NLN"=>"L","NLP"=>"L","NMC"=>"G","OAS"=>"S","OCS"=>"C","OMT"=>"M","PAQ"=>"Y","PCA"=>"E",
    "PEC"=>"C","PHI"=>"F","PHL"=>"F","PR3"=>"C","PTR"=>"Y","SAC"=>"S","SAR"=>"G","SCH"=>"C",
    "SCS"=>"C","SCY"=>"C","SEL"=>"S","SEP"=>"S","SET"=>"S","SHC"=>"C","SHR"=>"K","SOC"=>"C",
    "SVA"=>"S","TIH"=>"A","TPL"=>"W","TPO"=>"T","TRO"=>"W","TYB"=>"Y","TYQ"=>"Y","TYS"=>"Y",
    "TYY"=>"Y","AGM"=>"R","GL3"=>"G","SMC"=>"C","ASX"=>"B","CGU"=>"E","CSX"=>"C","LED"=>"L",
    "KPI"=>"K",
 
    # changed mappings
    "BNN"=>"F", # previously "BNN"=>"A"
    "CSD"=>"C", # previously "CSD"=>"A"
    "DHA"=>"S", # previously "DHA"=>"A"
    "TPQ"=>"Y", # previously "TPQ"=>"A"
    "DA"=>"A", # previously "DA"=>""
    "DC"=>"C", # previously "DC"=>""
    "DG"=>"G", # previously "DG"=>""
    "DT"=>"T", # previously "DT"=>""
    "3DR"=>"N", # previously "3DR"=>""
    "CTG"=>"T", # previously "CTG"=>""
    "XTR"=>"T", # previously "XTR"=>""
    "B7C"=>"C", # previously "B7C"=>""
    "DDG"=>"G", # previously "DDG"=>""
    "DOC"=>"C", # previously "DOC"=>""
    "MP8"=>"P", # previously "MP8"=>""
    "FP9"=>"P", # previously "FP9"=>""

    # new mappings (previously not substituted)
    "LCG"=>"G","LCA"=>"A","5CM"=>"C","6OG"=>"G","GHP"=>"G","3MY"=>"Y","OMY"=>"Y","2JG"=>"S",
    "GPL"=>"K","SOS"=>"N","MLY"=>"K","2SO"=>"H","CAF"=>"C","CAS"=>"C","PFF"=>"F",
    "DI"=>"I","56A"=>"H","U2X"=>"Y","0G"=>"G","0C"=>"C","C1S"=>"C","MHO"=>"M",
    "MLZ"=>"K","PF5"=>"F","N7P"=>"P","GAU"=>"E","0AF"=>"W","2DT"=>"T", "CSL"=>"C",
    "ORN"=>"A","LYR"=>"K","4AK"=>"K","DPP"=>"A","0A1"=>"Y","5HC"=>"C","GTP"=>"G",

    # mappings of multiple structure components (mapping to a sequence of one-letter codes)
    "CR8"=>"HYG", "CR2"=>"GYG",

    # obsoleted three letter codes, using the mapping of the component which superceded it
    "5HP"=>"E","ASQ"=>"D","CEA"=>"C","CYM"=>"C","DSP"=>"D","HAC"=>"A","LTR"=>"W","NEM"=>"H",
    "STY"=>"Y","TRG"=>"K",

    # obsoleted three letter codes, using the mapping of the component which superceded it,
    # that differ from the previous mapping
    "BUG"=>"V", # previously "BUG"=>"L"

    # defined non-standard component mappings (to non amino acids)
    "GLX"=>"Z",
    "DU"=>"U", # previously "DU"=>""
    "BRU"=>"U", # previously "BRU"=>""
    "TLN"=>"U",
    "0U"=>"U",
    "UMS"=>"U",

    # one-letter code "X" is used by www.ebi.ac.uk/pdbe-srv/pdbechem with non-standard components,
    # if no one-letter code exists
    "2AS"=>"X", # previously "2AS"=>"D"
    "CHG"=>"X", # previously "CHG"=>"A"
    "PRR"=>"X", # previously "PRR"=>"A"
    "PDI"=>"X", # previously "PDI"=>""
    "QBT"=>"X",
    "2JF"=>"X",
    "3FG"=>"X",
    "ACE"=>"X",
    "NH2"=>"X",
    "IVA"=>"X",
    "STA"=>"X",
    "MYR"=>"X",
    "0QE"=>"X",
    "MPT"=>"X",
    "UNK"=>"X",
    "AME"=>"X",
    "ASJ"=>"X",
    "POL"=>"X",
    "DCL"=>"X",
    "VOL"=>"X",
    "2A1"=>"X",
    "IL0"=>"X",
    "TYE"=>"X",
    "AZI"=>"X",
    "05W"=>"X",
    "ORP"=>"X",
    "6FC"=>"X",
    "6FU"=>"X",
    "YAC"=>"X",
    "QAC"=>"X",
    "0A"=>"X",
    "0U1"=>"X",
    "PDF"=>"X",
    "2X0"=>"X",
    "HEM"=>"X",
    "00E"=>"X",
    "ACY"=>"X",
    "02N"=>"X",
    "04B"=>"X",

    # obsoleted codes not superceded by an other compound (keeping previous mapping)
    "ACL"=>"R","ALM"=>"A","ASK"=>"D","CLE"=>"L","CGL"=>"E","LYM"=>"K",

    # (yet) undefined codes
    # according to a mail from Johannes Soeding from 14 Jul 2014 it may be
    # biologically better to replace unknown codes by "X" instead of
    # simply omitting them (mapping them to "")
    "---"=>"X","MF7"=>"X","4L8"=>"X","54L"=>"X"
      );

# Function substitution2one
# Parameters:
#
# threeLetterCode    String
#                    The three letter code to be mapped to one letter.
#                    Only all upper case codes are recognized.
#
# warningsRef        Optional Reference to Hash
#                    If not supplied, three2OneLetter prints its warnings to
#                    the standard error output.
#                    If supplied, substitution2one does not print warnings,
#                    but places the warning (if given) in the hash, using
#                    'Three2One' as the key and the message as the value.
#
# returns            the resulting one letter code as a one letter string.
#                    Special case: If threeLetterCode is only whitespace,
#                    warns and returns an empty string.
# exceptions         supplies a warning if no mapping for threeLetterCode
#                    is available.
sub three2OneLetter
{
    my $key = shift();
    
    my $oneLetter = $three2one{$key};
    if (!defined($oneLetter)) {
	my $warningsRef = shift();
	my $warning;
	my $substitution;
	if ($key =~ /^\s*$/) {
	    $warning = "WARNING Three2One.pm: threeLetterCode only consists of whitespace.\n";
	    $substitution = "";
	} else {
	    $warning = "WARNING Three2One.pm: Unknown threeLetterCode \"$key\".\nPlease add it to \%three2one using http://www.ebi.ac.uk/pdbe-srv/pdbechem/ to find a proper value.\n";
	    $substitution = "X";
	}

	if ($warning) {
	    if ($warningsRef) {
		$warningsRef->{'Three2One'} = $warning;
	    } else {
		print(STDERR $warning);
	    }
	    return $substitution;
	}
    }
    return $oneLetter;
}
