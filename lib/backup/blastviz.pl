#!/usr/bin/perl -w
#
# @author Andreas Biegert
# @time 2005-05-20
#
use warnings;
use GD;

my $max_seqs = 300;

my $resfile;
my $imgfile;
my $htmlfile;
my @hits;
my $id;
my $qlen=-1;
my $line;
my $xscale;
my $im;
my $ypos;
my $x1;
my $y1;
my $x2;
my $y2;
my $white;
my $black;
my $red;
my $blue;
my $col;
my $leftopen;
my $rightopen;
my $basedir;
my $rgbval;
my $probcutoff=0;
my $imgwidth=800;
my $imgheight;
my $barheight=10;
my $barsep=8;
my $border=10;
my $headsep=48;
my $headheight=5;
my $edgerad=3;
my $scraplen=5;
my $marklen=5;
my $imgdir;
my $progtype;
my $name="";
my $ident;
my $len;
my $prevname="";
my $row;
my @rows;


# check arguments
if((scalar @ARGV)!=3){
    print "Usage: blastviz.pl BLASTRESFILE ID BASEDIR [max_seqs (default:100)]\n";
    exit(1);
}else{
    $resfile = $ARGV[0];
    $resfile=~/^\S+\.(\S+)$/;
    $progtype=$1;
    $id = $ARGV[1];
    $basedir = $ARGV[2];
    $imgdir = "";
    $htmlfile = $id.".html";
    $imgfile = $id.".png";
}
if (defined $ARGV[4]) {
	$max_seqs = $ARGV[4];
}

if ($progtype =~ /(\w+)(blast\w*)/) {
    $progtype = $1 . "_" . $2;
} else {
    $progtype = "nuc_blast";
}

# Read hit list
open (RESFILE,"<$resfile") or die "unable to open blastresultfile $resfile for reading\n";
while($line=<RESFILE>) {

    if (scalar(@hits) > $max_seqs) { last; }
    chomp $line;
    my $evalue;
    my $score;
    my $qbeg=0;
    my $qend=0;
    my $sbeg=0;
    my $send=0;
    my $prob;
    if (-1 == $qlen
	&& ($line =~ /^\s+\((\d+) letters\)/ || ($line =~ /^Length=(\d+)/))) {
	$qlen=$1;
    } elsif ($line =~ /^><a name = (.*)><\/a><a \S+\s*>\S+<\/a>\s*(.+)\s*$/ ||
             $line =~ /^><a .*?<\/a><a name=(.*)><\/a>\s*(.+?)\s*$/) {
	$name=$1;
	print "NAME >> ".$name."\n";
	if (length($2)>55) {
	    $ident=substr($2,0,55);
	} else {
	    $ident=$2;
	}
    } elsif ($line =~ /^><a name\s*=\s*([^>]*)><\/a>\s*\S+\s+(.+)\s*$/
	     || $line =~ /^>\s*\S+\s*<a name=(.*)><\/a>\s*(.+?)\s*$/) {
	$name = $1;
	if (length($2)>55) {
	    $ident=substr($2,0,55);
	} else {
	    $ident=$2;
	}
    } elsif($line =~ /^\s+Length = (\d+)/ || $line =~ /^Length=(\d+)/) {
	$len=$1;
    } elsif($line =~ /^\s+Score =\s+(\d+).+Expect = (\S+)/) {
	$score=$1;
	$evalue=$2;
	$emptylines = 0;
	while($line=<RESFILE>) {
	    if($line =~ /^Query:?\s+(\d+)\s+\S+\s+(\d+)/) {
		if(!$qbeg) {
		    $qbeg=$1;
		}
		$qend=$2;
		$emptylines = 0;
	    } elsif($line =~ /^Sbjct:?\s+(\d+)\s+\S+\s+(\d+)/) {
		if(!$sbeg) {
		    $sbeg=$1;
		}
		$send=$2;
		$emptylines = 0;
	    } elsif ($line =~ /^\s*$/) {
		if (1 == $emptylines) {
		    last;
		}
		$emptylines = 1;
	    } else {
		$emptylines = 0;
	    }
	}
	#print "L135 --- Start  $evalue   \n";
	if ($evalue=~/^e/) {$evalue="1".$evalue;}
	if ($evalue=~/(\d+)e-(\d+)/) {$evalue=$1.".0e-".$2;}
	if ($evalue=~/,/){$evalue=~ s/,//g};

        #print "L140 --- End  $evalue   \n";
	if ($evalue!=0&&$evalue<=1) {
	    $prob=(-1/2)*(log($evalue)/log(10));
	} elsif($evalue==0) {
	    $prob=100;
	} else {
	    $prob=0;
	}
	if ($prob>=$probcutoff) {
	    $row=&calcRow($qbeg,$qend,\@rows);
	    &putBar($row,$qbeg,$qend,\@rows);
	    my %tmp;
	    $tmp{'name'}=$name;
	    $tmp{'ident'}=$ident;
	    $tmp{'len'}=$len;
	    $tmp{'evalue'}=$evalue;
	    $tmp{'score'}=$score;
	    $tmp{'qbeg'}=$qbeg;
	    $tmp{'qend'}=$qend;
	    $tmp{'sbeg'}=$sbeg;
	    $tmp{'send'}=$send;
	    $tmp{'row'}=$row;
	    $tmp{'prob'}=$prob;
	    $prevname=$name;
	    push(@hits, \%tmp);
	}
    }
}

close RESFILE;

$xscale=($imgwidth-2*$border)/$qlen;
$imgheight=2*$border+$headheight+$headsep+scalar(@rows)*($barsep+$barheight)-$barheight;
#print hits
foreach my $i (@hits) {
    my %hit = %$i;
    print $hit{'name'}."\t".$hit{'ident'}."\t".$hit{'len'}."\t".$hit{'evalue'}."\t".$hit{'score'}."\t".$hit{'qbeg'}."\t".$hit{'qend'}."\t".$hit{'sbeg'}."\t".$hit{'send'}."\t".$hit{'row'}."\n";
}



# visualize hits
$im = new GD::Image($imgwidth,$imgheight);
$white = $im->colorAllocate(255,255,255);
$black = $im->colorAllocate(0,0,0);
$red = $im->colorAllocate(255,0,0);
$blue = $im->colorAllocate(0,0,255);
$grey = $im->colorAllocate(150,150,150);
$im->transparent($white);

$x1=$border+$xscale;
$y1=$border;
$x2=$border+$qlen*$xscale;
$y2=$border+$headheight;
$im->filledRectangle($x1,$y1,$x2,$y2,$grey);

for(my $i=10;$i<=$qlen;$i+=10) {
    if ($i%100==0) {
	my $xoffset=length($i)*(5/2);
	$im->line($border+$i*$xscale,$border,$border+$i*$xscale,$border+$headheight+2*$marklen,$grey);
	$im->string(gdSmallFont,$border+$i*$xscale-$xoffset,$border+$headheight+2*$marklen,$i,$black);
    } else {
	$im->line($border+$i*$xscale,$border,$border+$i*$xscale,$border+$headheight+$marklen,$grey);
    }
}

open(HTMLFILE,">$basedir/$htmlfile") or die "unable to open htmlfile $htmlfile for writing\n";
print HTMLFILE "<map name=\"blastmap\">\n";

for(my $i=0; $i<scalar(@hits); $i++) {
    my %hit = %{$hits[$i]};
    $ypos = $border+$headheight+$headsep+$hit{'row'}*($barheight+$barsep);
    $x1=$border+$xscale*$hit{'qbeg'};
    $y1=$ypos;
    $x2=$border+$xscale*$hit{'qend'};
    $y2=$ypos+$barheight;

    my $title=$hit{'ident'}."  E=".$hit{'evalue'}." Score=".$hit{'score'};
    &printMapEntry(HTMLFILE,$hit{'name'},$title,$x1,$y1,$x2,$y2);

    if ($hit{'sbeg'}>1) {
	$leftopen=1;
    } else {
	$leftopen=0;
    }
    if ($hit{'send'}<$hit{'len'}) {
	$rightopen=1;
    } else {
	$rightopen=0;
    }


    my $minsat=0.8;           # minimum saturation
    my $signif = ($hit{'prob'}-$probcutoff)/(100-$probcutoff);
    my $satur  = 1.0-(1.0-$minsat)*(1.0-$signif);
    my ($red,$grn,$blu);
    my $col=100*$signif;
    if ($col>20) {
	# red (1,0,0)-> dark yellow (1,0.7,0) transition
	$col-=20.0;
	$col/=80;
	$red = 1;
	$grn = 0.7*(1-$col);
	$blu = 0;
    } elsif ($col>6) {
	# dark yellow (1,0.7,0) -> green (0,1,0) transition
	$col-=6.0;
	$col/=14;
	$red = $col;
	$grn = 0.7+0.3*(1-$col);
	$blu = 0;
    } elsif ($col>3) {
	# green (0,1,0) -> cyan (0,0.7,1) transition
	$col-=3.0;
	$col/=3;
	$red = 0;
	$grn = 1-0.3*(1-$col);
	$blu = 1-$col;
    } else {
	# cyan (0,0.7,1) -> blue (0,0,1) transition
	$col/=3;
	$red = 0;
	$grn = 0.7*$col;
	$blu = 1;
    }
    print "Processing hit ".$hit{'ident'}." with name '".$hit{'name'}."' and prob=".$hit{'prob'}.$col." ...\n";
    $col = $im->colorResolve(255*$red,255*$grn,255*$blu);
    if (-1 == $col) {
	$col = colorClosest(255*$red,255*$grn,255*$blu);
    }
    &drawBar($x1,$y1,$x2,$y2,$col,$leftopen,$rightopen,$hit{'ident'}); # print tag (not template name)
}
print HTMLFILE "</map>\n";

my $slider_width = int($imgwidth + $border + 2 - $xscale);
my $domain_start = 1;
my $domain_end = $qlen;
# my $domain_start = scalar(@hits) ? $hits[0]->{"qbeg"} : 1;
# my $domain_end = scalar(@hits) ? $hits[0]->{"qend"} : $qlen;
print HTMLFILE "
<HTML>
<BODY>
<style type='text/css'>
div.slider {position: absolute; height:40px;}
div.slider div.label {position:absolute; top: 0px; height:12px; width:30px; cursor:default;}
div.slider div.bar {position:absolute; top:14px; height:16px;}
div.slider div.bar div.handle {position:absolute; width:16px; height:16px; cursor:move;}
div.slider div.bar div.span {position:absolute; top:6px; height:6px; background-color:#000000;}
</style>

<div class='row' style='position:relative; height:${border}px'>
<div id='slider' class='slider' style='top:5px; left: " . int($xscale - 7) . "px; width: ${slider_width}px;'>
<div id='slider_label_left' class='label' style='text-align:right;'></div>
<div id='slider_label_right' class='label' style='text-align:left;'></div>
<div id='slider_bar' class='bar' style='width: ${slider_width}px;'>
<div id='slider_bar_handle_left' class='handle'><img src='/images/arrow_right.png' alt=''/></div>
<div id='slider_bar_handle_right' class='handle'><img src='/images/arrow_left.png' alt=''/></div>
<div id='slider_bar_span' class='span'></div>
</div>
</div>
<div class='row' style='position:absolute; top:${headsep}px; width: " . ($border + $qlen * $xscale - 5) ."px;'>
<input type='hidden' id='domain_start' name='domain_start'/>
<input type='hidden' id='domain_end' name='domain_end'/>
<input type='submit' name='submitform_slider' onClick=\"setFwActionDirect('${progtype}_form', '/$progtype/resubmit_domain/$id')\" class='feedbutton' style='border-width:2px;' value='Resubmit'/>
</div>
</div>
<script src='toolkit.js' type='text/javascript'></script>
<script type='text/javascript'>domain_slider_show($qlen, $domain_start, $domain_end);
test();
</script>
";
print HTMLFILE "<p><img src=$imgfile\ border=\"0\" alt=\"blasthits\" usemap=\"#blastmap\"></p>";
print HTMLFILE "</BODY>
</HTML>";
close(HTMLFILE);

# write image to file
open (OUTFILE,">$basedir/$imgfile") or die "unable to open imgfile $imgfile for writing\n";
print OUTFILE $im->png;
close OUTFILE;

print "\nDone! Results written to '$htmlfile' and '$imgfile'.\n";

exit(0);

#############################################################################
# functions
sub drawBar() {
    my $x1 = $_[0];
    my $y1 = $_[1];
    my $x2 = $_[2];
    my $y2 = $_[3];
    my $col = $_[4];
    my $leftopen = $_[5];
    my $rightopen = $_[6];
    my $tag = $_[7];

    $im->filledRectangle($x1+$edgerad,$y1,$x2-$edgerad,$y2,$col);

    if ($leftopen) {
	# Ragged border: local alignment
	$poly = new GD::Polygon;
        $poly->addPt($x1+$edgerad,$y1);
        $poly->addPt($x1+$edgerad,$y2);
        $poly->addPt($x1+$edgerad-$scraplen,$y1+0.75*($y2-$y1));
	$poly->addPt($x1+$edgerad,$y1+0.5*($y2-$y1));
	$poly->addPt($x1+$edgerad-$scraplen,$y1+0.25*($y2-$y1));
	$im->filledPolygon($poly,$col);
    } else {
	# Rounded border: alignment up to end
	$im->filledArc($x1+$edgerad,$y1+$edgerad,2*$edgerad,2*$edgerad,0,360,$col);
	$im->filledArc($x1+$edgerad,$y2-$edgerad,2*$edgerad,2*$edgerad,0,360,$col);
	$im->filledRectangle($x1,$y1+$edgerad,$x1+$edgerad,$y2-$edgerad,$col);
    }
    if ($rightopen) {
	# Ragged border: local alignment
        $poly = new GD::Polygon;
        $poly->addPt($x2-$edgerad,$y1);
        $poly->addPt($x2-$edgerad,$y2);
        $poly->addPt($x2-$edgerad+$scraplen,$y1+0.75*($y2-$y1));
	$poly->addPt($x2-$edgerad,$y1+0.5*($y2-$y1));
	$poly->addPt($x2-$edgerad+$scraplen,$y1+0.25*($y2-$y1));
	$im->filledPolygon($poly,$col);
    } else {
	# Rounded border: alignment up to end
	$im->filledArc($x2-$edgerad,$y1+$edgerad,2*$edgerad,2*$edgerad,0,360,$col);
	$im->filledArc($x2-$edgerad,$y2-$edgerad,2*$edgerad,2*$edgerad,0,360,$col);
	$im->filledRectangle($x2-$edgerad,$y1+$edgerad,$x2,$y2-$edgerad,$col);
    }
    # add label
    if (length($tag)*6>($x2-$x1-2*$edgerad)) {
	$tag = substr($tag,0,($x2-$x1-2*$edgerad)/6);
    }
    my $xoffset=length($tag)*(6/2);
    $im->string(gdSmallFont,(($x2+$x1)/2)-$xoffset,$y1-2,$tag,$white);
}

sub printMapEntry() {
    my $fh = $_[0];
    my $m = $_[1];
    my $mapstring = $_[2];
    my $x1 = $_[3];
    my $y1 = $_[4];
    my $x2 = $_[5];
    my $y2 = $_[6];

    if ($x1 =~ /(\d+)\.\d+/) {$x1 = $1};
    if ($y1 =~ /(\d+)\.\d+/) {$y1 = $1};
    if ($x2 =~ /(\d+)\.\d+/) {$x2 = $1};
    if ($y2 =~ /(\d+)\.\d+/) {$y2 = $1};
    print $fh "<area shape=\"rect\" coords=\"$x1,$y1,$x2,$y2\" href=\"/$progtype/results/$id#$m\" title=\"$mapstring\" />\n";
}

# Data structure:
# Each hit is recorded with its residue range in an array @bar=($beg,$end)
# The addresses of several @bar's may be contained in a @row. Each @row contains at least one @bar.
# The adresses of all occupied @row's are contained in array @rows.

# Determine index of @rows where to place new hit @bar=($beg,$end)
sub calcRow() {
    my $beg=$_[0];      # first residue of residue range
    my $end=$_[1];      # last residue
    my $rowsref=$_[2];
    my $b;
    my $e;
    my $fits;
    my $bar;
    my @bars;

    for(my $r=0; $r<scalar(@$rowsref); $r++) {
	@bars=@{${$rowsref}[$r]};
	$fits=1;
	foreach $bar (@bars) {
	    ($b,$e)=@$bar;
	    if (!($beg>$e || $end<$b)) {
		$fits=0;
		last;
	    }
	}
	if ($fits) {
	    return $r;
	}
    }
    return scalar(@$rowsref);
}

# Insert @bar=($beg,$end) for current hit into @rows
sub putBar() {
    my $pos=$_[0];      # row in which to insert @bar=($beg,$end)
    my $beg=$_[1];      # first residue of residue range
    my $end=$_[2];      # last residue
    my $rowsref=$_[3];  # data structure @rows
    my @bar=($beg, $end);

    # Do we place @bar into new row of @rows?
    if ($pos >= scalar(@$rowsref)) {
	my @newrow=(\@bar);
	${$rowsref}[$pos]=\@newrow;
    } else {
	my $rowref=$rows[$pos]; # get address of @row with index $pos
	push(@$rowref, \@bar);  # add \@bar to @row
    }
    return;
}





