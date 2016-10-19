#!/usr/bin/perl
#
#  Christian Mayer 
#  christian.mayer@mpg.tuebingen.de


use strict;

$|= 1; 

my $informat;   # inputformat
my $outformat ; # outputformat
my $infile;     # infile
my $outfile;    # outfile
my @names;   	# names of sequences read in
my @seqs;    	# residues of sequences read in
my $param=0;  	
my $name;
my $tolower=0;	#holds 1 if set
my $toupper=0;	#..
my $break_residues = 0; # number of residues in one line for sequences in fas, a2m and a3m output
my $num = 0;
my $info = 0;
#----------------------------------------------------------------------
#----------------------------------------------------------------------
#---MAIN---------------------------------------------------------------
#----------------------------------------------------------------------
#----------------------------------------------------------------------

#----------------------------------------------------------------------
#---CHECKING-ARGUMENTS-------------------------------------------------
#----------------------------------------------------------------------
if (@ARGV<4){ usage(); exit(1); }

foreach (@ARGV){
    if( $_=~/-f=(\S+)/ )   { $infile=$1;         }
    elsif( $_=~/-i=(\S+)/ ){ $informat=$1;       }
    elsif( $_=~/-o=(\S+)/ ){ $outformat=$1;      }
    elsif( $_=~/-num/i )   { $num=1;             }
    elsif( $_=~/-a=(\S+)/) { $outfile=$1;        }
    elsif( $_=~/-b=(\S+)/) { $break_residues=$1; }
    elsif( $_ eq "-v" )    { $info=1;            }
    elsif( $_ eq "-h" )    { usage(); exit(0);   } 
    elsif( $_=~/-tolower/i){ $tolower=1;         }
    elsif( $_=~/-toupper/i){ $toupper=1;         }
    else{
	print("ERROR: Unknown argument: $_\n");
	exit(1);
    }
}

my $basename="";
if( $infile =~ /^.*\/(.+?)$/ ){ $basename = $1; }
else{ $basename = $infile; }
#display version
if($info==1){
    print("Version: 1.5\n");
    print("Changes since 1.4\n");
    print("- shortened names in PHYLIP output format are numbered to be unique in case of ambiguities.");
    print("Changes since 1.3\n");
    print("- CLUSTAL sequence names are shortened to 35 characters\n");
    print("- MSF sequence names are shortened to 13 characters\n");
    print("- GenBank format readable.\n");
    print("- Bugs in reading NEXUS format fixed.\n");
    print("- Inserting newlines into sequences possible (-b option).\n");
    print("Version: 1.3\n");
    print("Changes since 1.2:\n");
    print("- Version information\n");
    print("- Help message\n");
    print("- CLUSTAL files may contain lines with trailing numbers.\n");
    print("- Possibility to change sequence letters to upper-/lowercase\n");
    print("- PHYLIP output modified: header length limit is 10, special chars \"()[],;:\" are removed.\n" );
    exit(0);
}

if($tolower==1 && $toupper==1){
	print("ERROR: -tolower and -toupper cannot be used together!\n");
	exit(1);
}


#	print users' parameters
print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
print("infile        = $infile\n");
print("input format  =  $informat \n");
print("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");
print("outfile       = $outfile\n");
print("output format = $outformat \n");

print("-----------------------------------------------------------\n");
if($num==1){
    print("Add numberprefix\n");
}
if($tolower==1){
    print("Sequences to lowercase\n");
}
if($toupper==1){
    print("Sequences to uppercase\n");
}
if( $break_residues>0 && ($outformat eq "fas" || $outformat eq "a2m" || $outformat eq "a3m") ){
    print("Number of residues in one line is set to $break_residues\n");
}

print("-----------------------------------------------------------\n");


#check infile
if (not(-e $infile)){usage();print("\nFile does not exist!\n");}
if (-z $infile){usage();print("\nFile is empty!\n");}

#check outformat
if($outformat ne "tre" && $outformat ne "pir" && $outformat ne "pir2" && $outformat ne "phy" && $outformat ne "nex" && $outformat ne "meg" && $outformat ne "msf" && $outformat ne "emb" && $outformat ne "sto" && $outformat ne "clu" && $outformat ne "fas" && $outformat ne "a2m" && $outformat ne "a3m" && $outformat ne "ufas" && $outformat ne "psi" && $outformat ne "raw"){
    usage();
    print("ERROR: Unknown outformat $outformat\n");
    exit(1);
}
#---------------------------------------------------
#-----ADDRESSES-INPUTFILE-TO-READ-FUNCTION----------
#---------------------------------------------------

SWITCH:{
          
    if($informat eq "fas" || $informat eq "a2m" || $informat eq "a3m"){read_fas_a2m_a3m();last SWITCH;}

    if($informat eq "clu"){read_clu();last SWITCH;}

    if($informat eq "sto"){read_sto();last SWITCH;}

    if($informat eq "emb"){read_emb();last SWITCH;}

    if($informat eq "gbk"){read_gbk();last SWITCH;}

    if($informat eq "meg"){read_meg();last SWITCH;}
    
    if($informat eq "msf"){read_msf();last SWITCH;}

    if($informat eq "nex"){read_nex();last SWITCH;}

    if($informat eq "phy"){read_phy();last SWITCH;}
    
    if($informat eq "pir"){read_pir();last SWITCH;}   

    if($informat eq "tre"){read_tre();last SWITCH;}

    usage();
    print("ERROR: Unkown input format!\n\n");
    exit(1);
}

#debug_print();

#changes ~ into - and
#        _ into -
#
my $mes1=0;
my $mes2=0;
foreach my $line (@seqs){
    $line =~ s/\s//g;
    if ($line =~ /^.*~.*$/ && !$mes1){print("Changed '~' in '-' in file: $infile\n"); $mes1=1;}
    if ($line =~ /^.*_.*$/ && !$mes2){print("Changed '_' in '-' in file: $infile\n"); $mes2=1;}
    $line =~ s/~|_/-/g;
    $line=~tr/A-Za-z.-/A-Za-z.-/d; #delete found but unreplaced chars
    if($tolower==1){
     	$line=~tr/A-Z/a-z/;
    }elsif($toupper==1){
    	$line=~tr/a-z/A-Z/;
    }
}


# Check if there were any sequences, names readable from the file. 
if (scalar(@seqs)==0 || scalar(@names)==0){
    print("ERROR: Cannot get sequences from $infile in $informat format!\n");
    exit(1);

}



if($outfile eq "STDOUT"){ $outfile = "&STDOUT"; }
    
open(OUTFILE, ">$outfile") or die("Cannot create $outfile");


#---------------------------------------------------
#---WRITE-OUTPUTFILE---------------------------
#---------------------------------------------------


SWITCH:{

   if ($outformat  eq "fas" || $outformat eq "a2m" || $outformat eq "a3m" || $outformat eq "ufas"){write_fas_a2m_a3m_ufas();last SWITCH;}

   if ($outformat  eq "clu") {write_clu(); last SWITCH;}
    
   if ($outformat  eq "sto") {write_sto(); last SWITCH;}

   if ($outformat  eq "emb") {write_emb(); last SWITCH;}

   if ($outformat  eq "msf") {write_msf(); last SWITCH;}

   if ($outformat  eq "meg") {write_meg(); last SWITCH;}

   if ($outformat  eq "nex") {write_nex(); last SWITCH;}

   if ($outformat  eq "phy") {write_phy(); last SWITCH;}

   if ($outformat  eq "pir") {write_pir(); last SWITCH;} 

   if ($outformat  eq "pir2") {write_pir2(); last SWITCH;}

   if ($outformat  eq "psi") {write_psi(); last SWITCH;}

   if ($outformat  eq "tre") {write_tre(); last SWITCH;}
   
   if ($outformat  eq "raw") {write_raw(); last SWITCH;}
   
   usage();
   print("ERROR: Unkown output format!\n\n");
   exit(1);  
}


close(OUTFILE) or die("Cannot close $outfile");



exit(0);










#--------------------------------------------------------------------
#---FUNCTIONS--------------------------------------------------------
#--------------------------------------------------------------------
sub usage{
    print("\n-i=inputformat:\n");   
    print("a2m: FASTA with \"\.\" and \"-\",\n");
    print("a3m: FASTA \".\" removed,\n");
    print("clu: CLUSTAL format,\n");
    print("emb: EMBL/SWISS-PROT format,\n");
    print("gbk: GenBank format,\n");
    print("fas: FASTA format,\n");
    print("meg: MEGA format,\n");
    print("msf: GCG/MSF format,\n");
    print("nex: PAUP/NEXUS format,\n");
    print("phy: PHYLIP format,\n");
    print("pir: PIR/NBRF format,\n");
    print("sto: STOCKHOLM format,\n"); 
    print("tre: TREECON format.\n");
  

    print("\n-o=outputformat:\n");
    print("a2m: FASTA with \"\.\" and \"-\",\n");
    print("a3m: FASTA \".\" removed,\n");
    print("clu: CLUSTAL format,\n");
 #   print("emb: EMBL format,\n");
    print("fas: FASTA format,\n");
    print("meg: MEGA format,\n");
    print("msf: GCG/MSF format,\n");
    print("nex: PAUP/NEXUS format,\n");
    print("phy: PHYLIP format,\n");
    print("pir: PIR/NBRF format,\n");
    print("sto: STOCKHOLM format,\n"); 
    print("tre: TREECON format,\n");
    print("ufas: unaligned FASTA format.\n");
    print("psi: format for jumpstart of PSI-BLAST (-B option) with an alignment.\n");
    print("raw: Each sequence in one line without header.\n");
    print("\n-f=infile\n");
    print("-a=outfile\n"); 
    print("-b=[INTEGER] : Insert newlines into sequence(s) every [INTEGER] residues (valid option for fas, a2m and a3m output formats)\n");
    print("-num         : add numberprefix to sequence names, optional\n");
    print("-tolower     : change all letters to lowercase\n");
    print("-toupper     : change all letters to uppercase\n");
    print("-v           : display version information\n");
    print("-h           : display this message\n");
}

sub debug_print{   
    
    print("\nDEBUG OUTPUT BEGIN\n");
    for(my $n=0; $n<scalar(@names); $n++)
    {
	print(">$names[$n]\n");
	print("$seqs[$n]\n");
	
    }
    print("\nDEBUG OUTPUT END\n");
}
  
#-------------------------------------------------------------------
#---READ-FUNCTIONS--------------------------------------------------
#-------------------------------------------------------------------
sub read_fas_a2m_a3m{
    my $line;
    my $n=-1;
    open(INFILE, $infile) || die("\nERROR: Cannot open $infile!\n");
    while ($line = <INFILE>) #scan through PsiBlast-output line by line
    {
	if ($line=~/^>(.*)/) # nameline detected
	{
	    if($n==-1){print("\n$infile: OK (FASTA)\n");}
	    $n++;
	    $names[$n]=$1;
	    $seqs[$n]="";
	}
	else
	{  
	    chomp($line);
	    if ($line eq ""){next;}
	    if ($n==-1) {
	                print("\nERROR: $infile is not correct FASTA format!\n");
			exit(1);
	                }
	    $seqs[$n].=$line;
	}
    }
    close(INFILE) || die("\nERROR: Cannot close $infile!\n");


    if ($informat eq "a3m")
    {
    my @len_ins;   # $len_ins[$j] will count the maximum number of inserted residues after match state $i.
    my $j;       # counts match states
    my @inserts; # $inserts[$j] contains the insert (in small case) of sequence $i after the $j'th match state
    my $insert;
    my $k;

    # Determine length of longest insert 
    for ($k=0; $k<=$n; $k++)
    {
	# split into list of single match states and variable-length inserts
	# ([A-Z]|-) is the split pattern. The parenthesis indicate that split patterns are to be included as list elements
	# The '#' symbol is prepended to get rid of a perl bug in split
	@inserts = split(/([A-Z]|-)/,"#".$seqs[$k]."#"); 
	$j=0;
        #	printf("Sequence $k: $seqs[$k]\n");
        #	printf("Sequence $k: @inserts\n");
	
	# Does sequence $k contain insert after match state j that is longer than $ngap[$j]?
	foreach $insert (@inserts) 
	{
	    if( !defined $len_ins[$j] || length($insert)>$len_ins[$j]) {$len_ins[$j]=length($insert);}
	    $j++;
        #	    printf("$insert|");
	}
        #	printf("\n");
    }
    my $ngap;
    
    # Fill up inserts with gaps '.' up to length $len_ins[$j]
    for ($k=0; $k<=$n; $k++)
    {
	# split into list of single match states and variable-length inserts
	@inserts = split(/([A-Z]|-)/,"#".$seqs[$k]."#");
	$j=0;
	
	print("@inserts\n");
	
	# append the missing number of gaps after each match state
	foreach $insert (@inserts) 
	{
	    if($outformat eq "fas") {for (my $l=length($insert); $l<$len_ins[$j]; $l++) {$insert.="-";}}
	    else                    {for (my $l=length($insert); $l<$len_ins[$j]; $l++) {$insert.=".";}}
	    $j++;
	}
	$seqs[$k] = join("",@inserts);
	$seqs[$k] =~ tr/\#//d; # remove the '#' symbols inserted at the beginning and end
    }
    }  
}


sub read_clu{
    open(INFILE, $infile) || die("\nERROR: Cannot open $infile!\n"); 
    my $line;
    my $line_no=0;
    my $n=0;
    my %snhash;
    my $flag=0;
    my $block_check=0;
    while($line = <INFILE>){
	$line_no++;
	if ($line =~/^\s*$/  && $flag==0) {next;}      #leerzeilen am anfang des files uberspringen
	if ($flag == 0 && ($line =~/^clustal(.*)/i || $line =~/^msaprobs(.*)/i)) {
	    # Die erste nicht leere Zeile muss mit clustal anfangen.
	    # Aus Kompatibilität zum Resultat von MSAProbs darf sie auch mit msaprobs anfangen.
	    #print("\nfile: OK (CLUSTAL)\n");
	    $flag=1;
	}
	elsif($flag==1){	    	    
	    if ( $line =~ /^[:\.\*\s]*$/ ){$block_check=0;next;} #.:* indicate a residue
	    $line =~ /\s*(\S+?)\s+([A-Za-z~_.-]*).*$/;#[a-zA-Z.-]	    
	    if ($2=~ /^\s*$/){ print("\nWARNING: Empty sequence in file: $infile, line: $line_no\n"); }
	    if(!(exists $snhash{$1})){
		$names[$n]=$1;
		$seqs[$n]=$2;
		$snhash{$1}=$n++;
		$block_check=1;
		
	    }
	    else{
		if ($block_check) {die ("\nERROR: sequence $1 appears more than once per block in file: $infile\n");}
		$seqs[$snhash{$1}].=$2;		
	    }   
	}
	else{
	    print("ERROR: $infile is not correct CLUSTAL format\n");
	    exit(1);
	}

    }
    close(INFILE) || die("\nERROR: Cannot close $infile!\n");
}



sub read_emb{
    open(INFILE, $infile) || die("\nERROR: Cannot open $infile!\n");  
    my $line;
    my $identification_no=0;
    my $terminator_no=0;
    my $seq_no=0;
    my $tmp;
    my $id;
    my $n=-1;
    my $seq_block_flag=0;
    #print("read_embl\n");
    while($line=<INFILE>){
	if( $line =~ /^ID\s+(\S+)/i ){
	    $names[++$n]=$1;
	    $seqs[$n]="";
	}elsif($line =~ /^AC\s+(\S+)/i){
	    $names[$n] .= " AC:$1";	
	}elsif($line =~ /^OS\s+(.+)/i){
	    $names[$n] .= " $1";
	}elsif($line =~ /^SQ/i){
	    $seq_block_flag=1;	
	}elsif($line =~ /\/\//){
	    $seq_block_flag=0;
	}elsif($seq_block_flag){
	    if($line =~ /^\s*\d*\s*(.+?)\s*\d*\s*$/){ $tmp=$1; }
	    $tmp =~ s/\s+//g;
	    $seqs[$n].=$tmp;
	}else{
	    next;
	}
    }
    close(INFILE) || die("\nERROR: Cannot close $infile!\n"); 
}


sub read_gbk{
    open(INFILE, $infile) || die("\nERROR: Cannot open $infile!\n");  
    my $line;
    my $identification_no=0;
    my $terminator_no=0;
    my $seq_no=0;
    my $tmp;
    my $id;
    my $n=-1;
    my $seq_block_flag=0;
    #print("read_embl\n");
    while($line=<INFILE>){
	if($line =~/^LOCUS\s+(\S+)\s*/){
	    $names[++$n]=$1;
	    $seqs[$n]="";	
	}elsif($line =~ /^ACCESSION\s+(\S+)/i){
	    $names[$n] .= " AC:".$1;	
	}elsif($line =~ /^SOURCE\s+(.+)/i){
	    $names[$n] .= " ".$1;
	}elsif($line =~ /^ORIGIN/i){
	    $seq_block_flag=1;	
	}elsif($line =~ /\/\//){
	    $seq_block_flag=0;
	}elsif($seq_block_flag){
	    $line =~ /^\s*\d*\s*(.+?)\s*\d*\s*$/;
	    $tmp=$1;	    
	    $tmp =~ s/\s+//g;
	    $seqs[$n].=$tmp;
	}else{
	    next;
	}
    }
    close(INFILE) || die("\nERROR: Cannot close $infile!\n"); 
}



sub read_meg{
    open(INFILE, $infile) || die("\nERROR: Cannot open $infile!\n"); 
    my $line;
    my $mega_flag=0;
    my $seq_flag=0;
    my $n=-1;
    my %name_no;
    my $interleaved_flag=1;
    while($line=<INFILE>){
	# remove comments	
	while($line =~ /^(.*?)\".*?\"(.*)$/){ $line=$1.$2; }
	# check for mega header
	if($line =~ /^\s*\#mega.*/i){$mega_flag=1;}
	elsif(!$mega_flag){
	    print("ERROR: File $infile not in MEGA format!\n"); 
	    exit(1);
	}elsif($line =~ /^\s*\#(\S+)\s+(.+)\s*$/){
	    my $name = $1;
	    my $seq = $2;  
	    $seq =~ s/\s+//g;
	    $seq_flag=1;
	    if( !exists($name_no{$name}) ){
		$name_no{$name}=++$n;	    
		$names[$name_no{$name}]=$name;
		$seqs[$name_no{$name}].=$seq;
	    }else{
		if($interleaved_flag==0){
		    print("ERROR: File $infile not in interleaved MEGA format\n Cannot find #name identifier!\n"); 
			exit(1);
		}
		$seqs[$name_no{$name}].=$seq;
	    }
	}elsif($seq_flag && $line=~/^\s*(\S+)\s*$/){
	    $interleaved_flag=0;
	    my $seq = $1;
	    $seq=~ s/\s+//g;
	    $seqs[$n].=$seq;
	}
    }
    close(INFILE) || die("\nERROR: Cannot close $infile!\n");  
}




sub read_msf{

	# changed by Michael Remmert --- 02.09.2006
    
	open(INFILE, $infile) || die("\nERROR: Cannot open $infile!\n");     
    
	my $seq_flag=0;   
	my $name_flag=0;
	my $line;
	my $n = 0;
	my %hash;
	
	while ($line = <INFILE>) {
		chomp($line);
		if ($line =~ /^\s*$/) { next; }
		if ($line =~ /^\d+$/) { next; }
		if ($line =~ /^\s*Name:\s*(\S+)\s+/i) {
			$names[$n] = $1;
			$seqs[$n] = "";
			$hash{$1} = $n;
			$n++;
			$name_flag = 1;
		} elsif ($line =~ /^\/\// && $name_flag == 1) {
			$seq_flag = 1;
		} elsif ($seq_flag == 1) {
			if ($line =~ /^\s*(\S+)\s+(.*)$/) {
				my $name = $1;
				my $seq = $2;
				$seq =~ s/ //g;
				if (!defined $hash{$name}) {
					print "ERROR: Identifier $name not in name list!\n";
					exit(1);
				}
				$seqs[$hash{$name}] = $seqs[$hash{$name}] . $seq;
			}
		}
	}
	
	if($name_flag == 0){
		print("ERROR: Missing header!\n");
		exit(1);
	}
	if($seq_flag == 0){
		print("ERROR: No sequences found in file: $infile\n");
		exit(1);
	}

	#check if every sequence has the same length and check for ~ 
	my $length = length($seqs[0]);
	my $i = 1;
	while ($i < scalar(@names)) {
		if (length($seqs[$i]) != $length) {
			print("ERROR: All sequences must have the same length!\n");
			exit(1);
		}
		$i++;
	}
	
	close(INFILE) || die("\nERROR: Cannot close $infile!\n");
}




sub read_nex{
    open(INFILE, $infile) || die("\nERROR: Cannot open $infile!\n"); 
    my $line;
    my $seq_flag=0;
    my $n=0;
    my $block_flag=1;
    my $ntax;
    my $nchar=-1;
    my @data;
    my $tmp;
    my $matrix_trailing="";
    while($line=<INFILE>){
	# remove comments
	while($line =~ /^(.*?)\[.*?\](.*)$/){ $line=$1.$2; }
	# get the number of taxa and characters 
	if( $line =~ /^\s*dimensions\s+ntax=\s*(\d+)\s+nchar=\s*(\d+)\s*;/ ){
	    $ntax = $1;
	    $nchar = $2;
	# data block
	}elsif ($line =~ /^\s*matrix\s+(.*)$/i){
	    $matrix_trailing = $1;
	    $seq_flag=1;
	}elsif($line =~ /^\s*;\s*/ && $seq_flag) { 
	    last; 
	}elsif ($line =~ /^\s*$/ && $seq_flag){
	    $block_flag=0;
	    $n=0;
	}elsif($seq_flag){
	    if($block_flag==0){
		$line =~ /^\s*\S+\s+(.*)$/;
		$data[$n++] .= $1;
	    }else{
		$data[$n++] = $matrix_trailing." ".$line;
		$matrix_trailing="";
	    }
	}      
    }    
    close(INFILE) || die("\nERROR: Cannot close $infile!\n");  

    if($nchar==-1){
	print("ERROR: Cannot find 'dimension' line with nchar field!\n");
	exit(1);
    }

    if( $block_flag==1){    
	$n=-1;
	my $dat = "";
	for(my $i=0; $i<scalar(@data); ++$i){
	    $data[$i] =~ s/\s+/ /g;
	    $dat.=$data[$i];
	}
	my $err_ctrl = 0;
	while( $dat !~ /^\s*$/ ){
	    $dat =~ /^(.)/;
	    if($1 eq ";"){ last; }
	    ++$err_ctrl;
	    $dat =~ /^\s*(\S+)\s(.*)$/;
	    $names[++$n]=$1;
	    $dat = $2;
	    my $len = 0;
	    while($len!=$nchar && $err_ctrl<99999999){
		++$err_ctrl;
		$dat =~ /^(.)(.*)$/;
		$dat = $2;
		my $nl = $1;
		if($nl !~/\s/){
		    $seqs[$n] .= $nl;
		    $len += length($nl);
		}
	    } 

	    if($err_ctrl==99999999){
		print("ERROR: NEXUS seems to be damaged, check sequence length and nchar field\n");
		exit(1);
	    }
	}
    }else{
	$n=0;
	for(my $i=0; $i<scalar(@data); ++$i){
	    $data[$i] =~ s/\s+/ /g;
	    $data[$i] =~ /^\s*(\S+)\s+(.+)$/;
	    $names[$n] = $1;
	    $tmp = $2;
	    $tmp =~ s/\s+//g;	
	    $seqs[$n++] .= $tmp;
	}
    }

}


sub read_phy{
  open(INFILE, $infile) || die("\nERROR: Cannot open $infile!\n");   
  my $line;
  my $seq_flag = 0;
  my $n = -1;
  my $inter = 0;
  while( $line = <INFILE> ){
      if($line =~ /^ *([0-9]*) +([0-9]*).*$/ && $seq_flag==0){
      
      }elsif($line =~/^(\S+)\s+(.*)$/){ 
	  $seq_flag = 1;
	  $names[++$n] = $1;
	  my $seq = $2;
	  $seq =~ s/\s+//g;
	  $seqs[$n] = $seq; 
      }elsif($line =~/^[\d|\s]*$/ && $seq_flag==1){
	  $inter = 1;
	  $n=0;
      }elsif($line =~ /^\s*\d*\s*(.+)\s*\d*\s*$/ && $seq_flag==1){
	  my $seq = $1;
	  $seq =~ s/\s+//g;
	  $seqs[$n] .= $seq;
	  if($inter==1){ ++$n; }
      }
  }
 
}



sub read_pir{
    open(INFILE, $infile) || die("\nERROR: Cannot open $infile!\n");   
    my $line;
    my $tmp;
    my $n=-1;
    my $seq_end_flag=1;
    while($line= <INFILE>){

	chomp($line);
	$tmp=$line;
	$tmp=~ s/ //g;
	if ($tmp eq ""){next;}
	
	if ($line =~ /^>.{2};(.*)$/){
	    $n++;
	    $names[$n]=$1;
	    chop($names[$n]);
	    if(!($line=<INFILE>)) {print("ERROR: $infile is not in correct PIR format!\n"); exit(1);}
	    if ($line =~ /^.*?:.*?:.*?:(\S+):/) {
		$names[$n].="_$1";
	    }
	    if(!$seq_end_flag){print("WARNING: Sequence: \"$names[$n-1]\" in file: $infile is not correct terminated with \*\n"); }
	    $seq_end_flag=0;
	}elsif($line =~ /^(.*)\*\s*$/){
	    if($n<0){print("ERROR: $infile is not in correct PIR format!\n"); exit(1);}
	    $tmp=$1;
	    $tmp =~  s/ //g;
	    $seqs[$n].=$tmp;
	    $seq_end_flag=1;
	}else{
	    if($n<0){print("ERROR: $infile is not in correct PIR format!\n"); exit(1);}
	    $line =~ s/ //g;
	    $seqs[$n].=$line;
	    $seq_end_flag=0;
	}
	
    }
    if(!$seq_end_flag){print("WARNING: Sequence: \"$names[$n]\" in file: $infile is not correct terminated with \*\n"); }       
    close(INFILE) || die("\nERROR: Cannot close $infile!\n"); 
}




sub read_sto{
    my %seqhash;
    my $first_block=1;
    my $line;    
    open(INFILE, $infile) || die("\nERROR: Cannot open $infile!\n");
    my $n=0;
    while ($line = <INFILE>) #scan through PsiBlast-output line by line
    {
	if ($line=~/^\#/) {next;}                    # skip commentary and blank line
	if ($line=~/\/\//) {last;}                   # reached end of file
	if ($line=~/^\s*$/){$first_block=0; next;}             # new sequence block starts
	$line=~/\s*(\S+)\s+(\S+)/;
	if (!(exists $seqhash{$1})) 
	{
	    $names[$n]=$1;
	    $seqs[$n]=$2;
	    $seqhash{$1}=$n++;
	    $first_block=1;
	    }
	else
	{
	    if ($first_block) {die ("\nERROR: sequence $1 appears more than once per block in file: $infile\n");}
	    $seqs[$seqhash{$1}].=$2;
	}
    }
    close(INFILE) || die("\nERROR: Cannot close file:$infile\n");
}




sub read_tre{
    open(INFILE, $infile) || die("\nERROR: Cannot open file:$infile\n"); 
    my $line;
    my $seq_length=0;
    my $length_flag=0;
    my $read_name_flag=1;
    my $n=-1;
    my $write_length=0;
    while($line = <INFILE>){
	if ($line =~ /^\s*$/){next;}
	if ($line =~ /^\s*(\d+)\s*$/){
	    $seq_length=$1;
	    $length_flag=1;
	}elsif(!$length_flag){
	    print("ERROR: file:$infile is not in correct TREECON format\n");
	    exit(1);
	    
	}elsif($read_name_flag){
	    $n++;
	    $line =~ /^\s*(.*)\s*$/;
	    $names[$n]=$1;
	    $read_name_flag=0;
	    $write_length=0;
	}elsif(!$read_name_flag){
	    $line =~ s/\s//g;
	    $seqs[$n].=$line;
	    $write_length+=length($line);
	    if($write_length == $seq_length){
		$read_name_flag=1;
	    }elsif($write_length>$seq_length){
		print("WARNING: sequence $names[$n] is not as long as predicted (should be $seq_length, really is: $write_length!\n");
		$read_name_flag=1;
	    }
	}
	else{print("ERROR: fatal error!\n");exit(1);}
    }
    close(INFILE) || die("\nERROR: Cannot close file:$infile\n");  
}



sub read_dna_strider{
    open(INFILE, $infile) || die("\nERROR: Cannot open $infile\n"); 
    my $line;
    while($line=<INFILE>){
    }
    
    close(INFILE) || die("\nERROR: Cannot close file:$infile\n");  
}




#---------------------------------------------------------------------
#---WRITE-FUNCTIONS---------------------------------------------------
#---------------------------------------------------------------------
sub write_fas_a2m_a3m_ufas{

     for (my $k=0; $k<scalar(@names); $k++) {

	if ($outformat eq "fas")
	{
	    # Substitute all '.' by '-', 
	    $seqs[$k]=~tr/a-z./A-Z-/;                    
	}
	elsif ($outformat eq "ufas")
	{
	   # Remove all gaps
       $seqs[$k]=~tr/a-z.-/A-Z/d;
        }
	elsif ($outformat eq "a3m")
	{

	    # Remove all '.' gaps
	    $seqs[$k]=~tr/.//d;
	  
	}

	$names[$k] =~ s/%/percent/g;

	if ($num==1) { print(OUTFILE ">".($k+1)."|$names[$k]\n");  }
	else         { print(OUTFILE ">$names[$k]\n");             }
	
	if($break_residues>0){
	    for(my $pos=0; $pos<length($seqs[$k]); $pos += $break_residues){
		my $part_seq=substr($seqs[$k],$pos,$break_residues);
		print(OUTFILE "$part_seq\n");
	    }
	}else{ 
	    print(OUTFILE "$seqs[$k]\n");
	}
	
     }

     my $n=scalar(@names);
     if ($n==1) {print("Reformating $infile with 1 sequence from $informat to $outformat done\n");}
     else       {print("Reformating $infile with $n sequences from $informat to $outformat done\n");}
     print("Wrote file: $outfile\nready!\n");
    
}


sub write_clu{

    print(OUTFILE "CLUSTAL multiple sequence alignment\n\n");
    my $max_name_length=0; 
    my $seq_length=length($seqs[0]);
    my $max_seq_length=0;
    for (my $k=0; $k<scalar(@names); $k++) {
	$names[$k]=~/^\s*(.*?)\s*$/;
	$names[$k]=$1;
	$names[$k]=~s/\s/_/g;
	$seqs[$k]=~tr/./-/;   
	if($max_name_length<length($names[$k])){
	    $max_name_length=length($names[$k]);
	}
	if($seq_length != length($seqs[$k])){
	    my $reslength= length($seqs[$k]);
	    my $resnum=$k+1;
	    print("WARNING: Sequence $resnum has $reslength Residues instead of $seq_length in file $infile\n");  
	}
	if($max_seq_length <  length($seqs[$k])){
	    $max_seq_length =  length($seqs[$k]);
	}
	if ($num==1){
	    $names[$k]=($k+1).$names[$k];
	}
    }
    
    if($max_name_length>20){
    	for (my $k=0; $k<scalar(@names); $k++) {
		if(length($names[$k])>20){
			$names[$k] = substr($names[$k], 0, 20);
			
		}
		$names[$k] .= "_$k";
	}	
	$max_name_length=21+length(scalar(@names));
    }
    
    for(my $pos=0; $pos<$max_seq_length; $pos += 60){
	  for (my $k=0; $k<scalar(@names); $k++) {
	      my $space=" " x ($max_name_length-length($names[$k])+2);
	      print(OUTFILE "$names[$k]$space");
	      my $seq_60=substr($seqs[$k],$pos,60);
	      print(OUTFILE "$seq_60\n");
	  }
	  print(OUTFILE "\n");
    }
    my $n=scalar(@names);
    if ($n==1) {print("Reformating $infile with 1 sequence from $informat to $outformat done\n");}
    else       {print("Reformating $infile with $n sequences from $informat to $outformat done\n");}
    print("Wrote file: $outfile\nready!\n");

}

sub write_psi{
  
    my $max_name_length=0; 
    my $seq_length=length($seqs[0]);
    my $max_seq_length=0;
    for (my $k=0; $k<scalar(@names); $k++) {
	$names[$k]=~s/\s/_/g;
	$seqs[$k]=~tr/./-/;   
	if($max_name_length<length($names[$k])){
	    $max_name_length=length($names[$k]);
	}
	if($seq_length != length($seqs[$k])){
	    my $reslength= length($seqs[$k]);
	    my $resnum=$k+1;
	    print("WARNING: Sequence $resnum has $reslength Residues instead of $seq_length in file $infile\n");  
	}
	if($max_seq_length <  length($seqs[$k])){
	    $max_seq_length =  length($seqs[$k]);
	}
	if ($num==1){
	    $names[$k]=($k+1).$names[$k];
	}
    }
    
    if($max_name_length>30){
    	for (my $k=0; $k<scalar(@names); $k++) {
		if(length($names[$k])>30){
			$names[$k] = substr($names[$k], 0, 30);
			
		}
		$names[$k] .= "_$k";
	}	
	$max_name_length=30+length(scalar(@names));
    }

#    for(my $pos=0; $pos<$max_seq_length; $pos += 60){
	  for (my $k=0; $k<scalar(@names); $k++) {
	      my $space=" " x ($max_name_length-length($names[$k])+2);
	      print(OUTFILE "$names[$k]$space");	      
#	      my $seq_60=substr($seqs[$k],$pos,60);
#	      print(OUTFILE "$seq_60\n");
	      print(OUTFILE "$seqs[$k]\n");
	  }
	  print(OUTFILE "\n");
#    }
    
    my $n=scalar(@names);
    if ($n==1) {print("Reformating $infile with 1 sequence from $informat to $outformat done\n");}
    else       {print("Reformating $infile with $n sequences from $informat to $outformat done\n");}
    print("Wrote file: $outfile\nready!\n");
 
}

sub write_sto{

    print(OUTFILE "# STOCKHOLM 1.0\n");
    my $max_name_length=0; 
    my $seq_length=length($seqs[0]);
    my $max_seq_length=0;
    for (my $k=0; $k<scalar(@names); $k++) {
	$names[$k]=~s/\s/_/g;  
	if($max_name_length<length($names[$k])){
	    $max_name_length=length($names[$k]);
	}
	if($seq_length != length($seqs[$k])){
	    my $reslength= length($seqs[$k]);
	    my $resnum=$k+1;
	    print("WARNING: Sequence $resnum has $reslength Residues instead of $seq_length in file $infile\n");  
	}
	if($max_seq_length <  length($seqs[$k])){
	    $max_seq_length =  length($seqs[$k]);
	}
	if ($num==1){
	    $names[$k]=($k+1).$names[$k];
	}
	print(OUTFILE "#= GS $names[$k]\n");
    }
    print(OUTFILE "\n");
    my $pos=0;
    while($max_seq_length>$pos){
	  for (my $k=0; $k<scalar(@names); $k++) {
	      my $space=" " x ($max_name_length-length($names[$k])+2);
	      print(OUTFILE "$names[$k]$space");
	      my $seq_60=substr($seqs[$k],$pos,60);
	      print(OUTFILE "$seq_60\n");
	  }
          print(OUTFILE "\n");
	  $pos+=60;
    }
    ## Add file End signal to stockholm output
    print(OUTFILE "//");

    my $n=scalar(@names);
    if ($n==1) {print("Reformating $infile with 1 sequence from $informat to $outformat done\n");}
    else       {print("Reformating $infile with $n sequences from $informat to $outformat done\n");}
    print("Wrote file: $outfile\nready!\n");
 
}

sub write_emb{
   
    for (my $k=0; $k<scalar(@names); $k++) {
	if ($num==1){
	    $names[$k]=($k+1).$names[$k];
	}
	print(OUTFILE "ID   $names[$k]\n");
	print(OUTFILE "SQ   ");
	my $seq_length=length($seqs[$k]);
	my $pos=0;
	while($pos < $seq_length){
	    my $seq_60=substr($seqs[$k],$pos,60);
	    $pos+=length($seq_60);
	    $seq_60=~s/(.{10})/$1 /g;	    
	    printf(OUTFILE "%-66s   %u\n",$seq_60 ,$pos);	
	    print(OUTFILE "     ");
	}
	print(OUTFILE "//\n");
    }

    my $n=scalar(@names);
    if ($n==1) {print("Reformating $infile with 1 sequence from $informat to $outformat done\n");}
    else       {print("Reformating $infile with $n sequences from $informat to $outformat done\n");}
    print("Wrote file: $outfile\nready!\n");

}


sub write_meg{
  
    print(OUTFILE "#MEGA\n");
    print(OUTFILE "#TITLE\n\n");
    my $max_name_length=0;
    my $seq_length=length($seqs[0]);
    for (my $k=0; $k<scalar(@names); $k++) {
	$names[$k]=~s/\s/_/g;
	$names[$k]=~s/^(.*)$/\#$1/;
	if (length($names[$k])>34){
	       $names[$k]=~s/^(.{34}).*/$1/g;
	}
	if ($num==1){
	    $names[$k]=($k+1).$names[$k];
	}
	if($max_name_length<length($names[$k])){
	    $max_name_length=length($names[$k]);  
	} 
	if($seq_length!=length($seqs[$k])){
	    print("WARNING: Sequence $names[$k] should have the same length as other sequnces!\n");
	}
    }
    my $pos=0;
    while($seq_length>$pos){
	for (my $k=0; $k<scalar(@names); $k++) {
	    my $space=" " x ($max_name_length-length($names[$k])+2);
	    print(OUTFILE "$names[$k]$space");
	    my $seq_60=substr($seqs[$k],$pos,60);
	    print(OUTFILE "$seq_60\n");
	}
	$pos+=60;
	print(OUTFILE "\n\n");
    }
    my $n=scalar(@names);
    if ($n==1) {print("Reformating $infile with 1 sequence from $informat to $outformat done\n");}
    else       {print("Reformating $infile with $n sequences from $informat to $outformat done\n");}
    print("Wrote file: $outfile\nready!\n");
 
}


sub write_msf{
  

    # number of digits needed to enumerate all sequences
    my $nrs = scalar(@names);
    my $dc = 0;
    while( $nrs>0 ){ 
	++$dc;
	$nrs = int($nrs/10);
    }

    my $max_name_length=0;
    my $seq_length=length($seqs[0]);
    for (my $k=0; $k<scalar(@names); $k++) {
	if ($num==1){
	    $names[$k]=($k+1).$names[$k];
	}
	if($max_name_length<length($names[$k])){
	    $max_name_length=length($names[$k]);
	}
	if($seq_length!=length($seqs[$k])){
	    print("WARNING: Sequence $names[$k] differs in length from the other sequences!\n");
	}
	$seqs[$k]=~s/-/./g;
	$seqs[$k]=~s/~/./g;
    } 
    my $check = 0;
    my @seq_checks = ();
    for (my $k=0; $k<scalar(@names); $k++){
	my @chars = unpack("C*", $seqs[$k]);
	for(my $i=0; $i<scalar(@chars); $i++){
	    $check += $chars[$i];
	    $seq_checks[$k] += $chars[$i];
	}
    } 

    print(OUTFILE "MSF of: $basename  from: 1  to: $seq_length \n\n");
    print(OUTFILE " $basename  MSF: $seq_length  Type: P  Check: $check ..\n\n");

    for (my $k=0; $k<scalar(@names); $k++) {
	my $l= length($seqs[$k]);
	$names[$k] = substr($names[$k],0,9-$dc)."_".$k;
	$names[$k] =~ s/\|/_/g;
	
	printf(OUTFILE " Name: %-10s    Len:    %u  Check: $seq_checks[$k]  Weight:  1.00\n", $names[$k], $l);
	
    }

    print(OUTFILE "\n//\n\n");
  
    my $pos=0;
    while($seq_length>$pos){
	# create line with numbers
	my $mm = &min($pos+50, $seq_length);
	my $md = $mm-$pos;
	my $mm4 = $md + &min(&max(int($md/10),0),4);	
	my $m_half = int($mm4/2);
	if( $mm4%2==0 ){
	    printf(OUTFILE "%13s%-".$m_half."d%".$m_half."d\n","",$pos+1,$mm);
	}else{
	    printf(OUTFILE "%13s%-".($m_half)."d%".($m_half+1)."d\n","",$pos+1,$mm);
	}
	for (my $k=0; $k<scalar(@names); $k++) {
	    printf(OUTFILE "%-10s   ",$names[$k]);
	    my $seq_50=substr($seqs[$k],$pos,50);
	    $seq_50=~s/(.{10})/$1 /g;
	    $seq_50=~s/^(.+?).$/$1/;
	    print(OUTFILE "$seq_50\n");
	}
	$pos+=50;
        print(OUTFILE "\n");  
    }



    my $n=scalar(@names);
    if ($n==1) {print("Reformating $infile with 1 sequence from $informat to $outformat done\n");}
    else       {print("Reformating $infile with $n sequences from $informat to $outformat done\n");}
    print("Wrote file: $outfile\nready!\n");
  
}


sub write_nex{
  
    my $seq_number=scalar(@seqs);
    my $max_name_length=0;
    my $seq_length=length($seqs[0]);
    for (my $k=0; $k<scalar(@names); $k++) {
	if ($num==1){
	    $names[$k]=($k+1).$names[$k];
	}
	if($max_name_length<length($names[$k])){
	    $max_name_length=length($names[$k]);
	}
    }
    print(OUTFILE "#NEXUS\n\n");
    print(OUTFILE "BEGIN DATA;\n");
    print(OUTFILE "DIMENSIONS NTAX=$seq_number NCHAR=$seq_length;\n");
    print(OUTFILE "FORMAT DATATYPE=?  SYMBOLS =?  MISSING=? GAP=-  INTERLEAVE=yes;\n\n");
    print(OUTFILE "MATRIX\n");
    my $pos=0;
    while($seq_length>$pos){
	for (my $k=0; $k<scalar(@names); $k++) {
	    my $space=" " x ($max_name_length-length($names[$k])+2);
	    print(OUTFILE "$names[$k]$space");
	    my $seq_60=substr($seqs[$k],$pos,60);
	    print(OUTFILE "$seq_60\n");
	}
	$pos+=60;
	print(OUTFILE "\n\n");
    }
    print(OUTFILE ";\nEND;\n");
    my $n=scalar(@names);
    if ($n==1) {print("Reformating $infile with 1 sequence from $informat to $outformat done\n");}
    else       {print("Reformating $infile with $n sequences from $informat to $outformat done\n");}
    print("Wrote file: $outfile\nready!\n");
  
}

sub write_phy{
 
    my $seq_number=scalar(@seqs);
    my $max_name_length=0;
    my $seq_length=length($seqs[0]);
    my @names_mod;

    for (my $k=0; $k<scalar(@names); $k++) {
	$names_mod[$k]=$names[$k];
	if ($num==1){
	    $names_mod[$k]=($k+1).$names_mod[$k];
	}
	$names_mod[$k] =~ s/(,|;|:|\(|\)|\[|\]|\|)//g;
	if( length($names_mod[$k])>10 ){	
	    #try to get a unique part of the name that is at most 10 chars long
	    if( $names_mod[$k] =~ /([A-Z]{3}\d{5}\.\d)/i){
		$names_mod[$k] =  sprintf("%-10s",$1);
	    }elsif( $names_mod[$k] =~ /(\d{1,10})/ ){ 
		$names_mod[$k] =  sprintf("%-10s",$1);
	    }else{
		$names_mod[$k] = substr($names_mod[$k], 0, 10);
	    }
	}else{
	    $names_mod[$k] = sprintf("%-10s",$names_mod[$k]);
	}
    }

    #check for uniquness of names
    my $uniq=1;
    my %hn;
    for (my $k=0; $k<scalar(@names_mod); $k++) {
	if( exists($hn{$names_mod[$k]}) ){
	    $uniq=0;
	    last;
	}else{
	    $hn{$names_mod[$k]}=1;
	}
    }
    
    #ok, brute force! -put numbers for the names
    if($uniq==0){
	for (my $k=0; $k<scalar(@names_mod); $k++) {
	    $names_mod[$k] = sprintf("%-10s", "#".($k+1));
	}
    }
    
    print(OUTFILE "$seq_number $seq_length\n");
    my $bool=1;
    my $pos=0;
    while($seq_length>$pos){
	if($bool==1){
	    $bool=0;
	    for (my $k=0; $k<scalar(@names); $k++) {
		#my $space=" " x ($max_name_length-length($names[$k])+2);
		print(OUTFILE "$names_mod[$k]");
		my $seq_60=substr($seqs[$k],$pos,60);
		print(OUTFILE "$seq_60\n");
	    }
	}else{
	    for (my $k=0; $k<scalar(@names); $k++) {
		my $seq_60=substr($seqs[$k],$pos,60);
		print(OUTFILE "$seq_60\n");
	    }   
	}
	$pos+=60;
	print(OUTFILE "\n");
    }
    my $n=scalar(@names);
    if ($n==1) {print("Reformating $infile with 1 sequence from $informat to $outformat done\n");}
    else       {print("Reformating $infile with $n sequences from $informat to $outformat done\n");}
    print("Wrote file: $outfile\nready!\n");

}


sub write_pir{

    for (my $k=0; $k<scalar(@names); $k++) {
	if ($num==1){
	    $names[$k]=($k+1).$names[$k]; 
	}
	my $seq_length=length($seqs[$k]);
	print(OUTFILE ">P1; $names[$k]\n$names[$k] $seq_length bases\n");
	$seqs[$k]=~s/(.{10})/$1 /g;
	$seqs[$k]=~s/(.{66})/$1\n/g;
	print(OUTFILE "$seqs[$k]*\n");
    }
    my $n=scalar(@names);
    if ($n==1) {print("Reformating $infile with 1 sequence from $informat to $outformat done\n");}
    else       {print("Reformating $infile with $n sequences from $informat to $outformat done\n");}
    print("Wrote file: $outfile\nready!\n");

}

sub write_pir2{

    for (my $k=0; $k<scalar(@names); $k++) {
	if ($num==1){
	    $names[$k]=($k+1).$names[$k]; 
	}
	$names[$k] =~ s/\s+//g;	
	$names[$k] =~ s/\.//g;
	$names[$k] =~ s/\|+/_/g;
	my $seq_length=length($seqs[$k]);
	print(OUTFILE ">P1;\n$names[$k]\n");
	$seqs[$k]=~s/-/\./g;
	print(OUTFILE "$seqs[$k]\n");
    }
    my $n=scalar(@names);
    if ($n==1) {print("Reformating $infile with 1 sequence from $informat to $outformat done\n");}
    else       {print("Reformating $infile with $n sequences from $informat to $outformat done\n");}
    print("Wrote file: $outfile\nready!\n");

}


sub write_tre{
 
    my $seq_length=length($seqs[0]);
    print(OUTFILE " $seq_length\n");
    for (my $k=0; $k<scalar(@names); $k++) {
	if ($num==1){
	    $names[$k]=($k+1).$names[$k];
	}
	my $seq_length=length($seqs[$k]);
	
	print(OUTFILE "$names[$k]\n");
	$seqs[$k]=~s/(.{60})/$1\n/g;
	print(OUTFILE "$seqs[$k]\n");
    }
    my $n=scalar(@names);
    if ($n==1) {print("Reformating $infile with 1 sequence from $informat to $outformat done\n");}
    else       {print("Reformating $infile with $n sequences from $informat to $outformat done\n");}
    print("Wrote file: $outfile\nready!\n");

}

sub write_raw{
 
    for (my $k=0; $k<scalar(@seqs); $k++) {
	print(OUTFILE "$seqs[$k]\n");
    }
    my $n=scalar(@names);
    if ($n==1) {print("Reformating $infile with 1 sequence from $informat to $outformat done\n");}
    else       {print("Reformating $infile with $n sequences from $informat to $outformat done\n");}
    print("Wrote file: $outfile\nready!\n");

}

sub min{
    my $a = shift;
    my $b = shift;
    if( $a < $b ){ return $a; }
    return $b;
}
sub max{
    my $a = shift;
    my $b = shift;
    if( $a > $b ){ return $a; }
    return $b;
}
