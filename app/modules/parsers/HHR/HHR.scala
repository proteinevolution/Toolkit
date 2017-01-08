package modules.parsers.HHR


import java.util.regex.Pattern
import java.util.regex.Matcher

import modules.parsers.HHR.HHR.{Header, HeaderParser, HitListParser}
import scala.util.parsing.combinator.RegexParsers

/**
  * Created by zin on 20.12.16.
  */


object HHR {

  case class Header(Query: String,
                    Match_columns : Int,
                    No_of_Seqs: String,
                    Neff: String,
                    Searched_HMMs: Int,
                    Date: String,
                    Command: String)


  case class HitList(Hits: List[String],
                     Probs : List[Double],
                     Evals: List[Double],
                     Pvals: List[BigDecimal],
                     Scores: List[Double],
                     SS: List[Double],
                     Cols: List[Int],
                     Query_HMMs: List[String],
                     Template_HMMs: List[String])


  case class Alignments(Headers: List[String],
                        QuerySeqs : List[String],
                        QueryCon: List[String],
                        TemplateCon: List[String],
                        TemplateSeqs : List[String],
                        TemplateDSSP : List[String],
                        TemplatePRED : List[String],
                        Confidence: List[String])


  class HeaderParser extends Serializable {

    private val Query = Pattern.compile("""(?m)^Query(.*?)\n""")
    private val Match_columns = Pattern.compile("""(?m)^Match_columns(.*?)\n""")
    private val No_of_Seqs = Pattern.compile("""(?m)^No_of_seqs(.*?)\n""")
    private val Neff = Pattern.compile("""(?m)^Neff(.*?)\n""")
    private val Searched_HMMs = Pattern.compile("""(?m)^Searched_HMMs(.*?)\n""")
    private val Date = Pattern.compile("""(?m)^Date(.*?)\n""")
    private val Command = Pattern.compile("""(?m)^Command(.*?)\n""")

    def parseRecord(record: String) :  HHR.Header = {

      lazy val matcher1 = Query.matcher(record)
      lazy val matcher2 = Match_columns.matcher(record)
      lazy val matcher3 = No_of_Seqs.matcher(record)
      lazy val matcher4 = Neff.matcher(record)
      lazy val matcher5 = Searched_HMMs.matcher(record)
      lazy val matcher6 = Date.matcher(record)
      lazy val matcher7 = Command.matcher(record)

      if (matcher1.find && matcher2.find && matcher3.find && matcher4.find && matcher5.find && matcher6.find && matcher7.find) {
        println("HHR header found")
        buildHeaderRecord(matcher1, matcher2, matcher3, matcher4, matcher5, matcher6, matcher7) }

       else {
        println("no HHR header found")
        HeaderParser.nullObjectHeaderRecord
      }
    }


    /** TODO refactor the builder. Actually one matcher should be enough and a combined regex should cover the whole header
      * by grouping like in this example:
      * https://github.com/alvinj/ScalaApacheAccessLogParser/blob/master/src/main/scala/AccessLogParser.scala
      */


    private def buildHeaderRecord(matcher1: Matcher, matcher2: Matcher, matcher3: Matcher,
                                  matcher4: Matcher, matcher5: Matcher, matcher6: Matcher,
                                  matcher7: Matcher) = {
      Header(
        matcher1.group(1).trim,
        Integer.parseInt(matcher2.group(1).trim),
        matcher3.group(1).trim,
        matcher4.group(1).trim,
        Integer.parseInt(matcher5.group(1).trim),
        matcher6.group(1).trim,
        matcher7.group(1).trim
        )
    }
  }

  class HitListParser extends RegexParsers {

    // TODO

    override def skipWhitespace = false

    def CRLF = "\r\n" | "\n"
    def EOF = "\\z".r


    def parse(lines: String) = {

      val hitlist = """(?s)No Hit(.*?)No 1""".r.findFirstIn(lines).get
      val hitlist_trimmed = hitlist.substring(hitlist.indexOf("No Hit"),hitlist.indexOf("No 1")).trim
      //val hitListNew = hitlist_trimmed.split("\n").drop(1).mkString("\n")
      val hitListNew = hitlist_trimmed.split("\n").drop(1)



    }


  }

  class AlignmentsParser extends RegexParsers {

    // TODO
  }
}


object HeaderParser {

  private[HHR] val nullObjectHeaderRecord = Header("", 0, "", "", 0, "", "")

  private val parser = new HeaderParser

  def fromFile( fn: String ) : HHR.Header = {
    val lines = scala.io.Source.fromFile(fn).getLines().take(8).mkString("\n")
    fromString( lines )
  }

  def fromString(lines: String) : HHR.Header = parser.parseRecord(lines)


}


object HitListParser {

  // TODO

  private val parser = new HitListParser

  def fromFile( fn: String ) = {
    val lines = scala.io.Source.fromFile(fn).getLines().mkString("\n")
    fromString( lines )
  }

  def fromString(lines: String) = parser.parse(lines)

}

object AlginmentsParser {

  // TODO
}

/*

Query         NP_877456#7 putative ATP-dependent DNA ligase [Bacteriophage phiKMV]
Match_columns 46
No_of_seqs    36 out of 36
Neff          3.63477
Searched_HMMs 52178
Date          Sat Jan  7 15:40:43 2017
Command       hhsearch -cpu 4 -i ../results/query.a3m -d /ebio/abt1_share/toolkit_support1/code/databases/hh-suite/mmcif70old -o ../results/hhsearch.hhr -z 1 -b 1 -dbstrlen 10000 -cs /ebio/abt1_share/toolki\
t_support1/code/bioprogs/tools/hh-suite-build/data/context_data.lib

No Hit                             Prob E-value P-value  Score    SS Cols Query HMM  Template HMM
1 1A0I_A DNA LIGASE, ADENOSINE-5  93.0   0.018 3.4E-07   37.9   0.0   43    1-43    240-282 (348)
2 4D05_A ATP-DEPENDENT DNA LIGAS  92.2    0.03 5.8E-07   35.3   0.0   42    1-45    177-220 (258)
3 4D05_B ATP-DEPENDENT DNA LIGAS  92.2    0.03 5.8E-07   35.3   0.0   42    1-45    177-220 (258)
4 2Q2U_C Chlorella virus DNA lig  84.7    0.35 6.7E-06   32.1   0.0   43    1-45    210-274 (319)
5 1FVI_A PBCV-1 DNA LIGASE (E.C.  82.9    0.48 9.1E-06   30.7   0.0   15    1-15    188-202 (297)
6 3GDE_A DNA ligase (E.C.6.5.1.1  78.4    0.91 1.7E-05   32.2   0.0   41    3-45    426-473 (558)
7 1VS0_A Putative DNA ligase-lik  55.8       6 0.00011   25.8   0.0   30    1-33    189-219 (310)
8 1VS0_B Putative DNA ligase-lik  55.8       6 0.00011   25.8   0.0   30    1-33    189-219 (310)
9 2CFM_A THERMOSTABLE DNA LIGASE  45.9      11 0.00021   26.8   0.0   30    3-34    426-455 (561)
10 3L2P_A DNA ligase 3 (E.C.6.5.1  34.4      22 0.00042   25.6   0.0   23    3-28    434-456 (579)
11 3W1B_A DNA ligase 4 (E.C.6.5.1  26.1      38 0.00072   24.5   0.0   30    3-34    461-490 (610)
12 2HIV_A Thermostable DNA ligase  25.7      38 0.00074   24.7   0.0   30    3-34    464-493 (621)
13 1X9N_A DNA ligase I (E.C. 6.5.  23.8      44 0.00085   25.2   0.0   31    3-35    524-554 (688)

No 1
>1A0I_A DNA LIGASE, ADENOSINE-5'-TRIPHOSPHATE; LIGASE, DNA REPLICATION; HET: ATP; 2.6A {Enterobacteria phage T7} SCOP: b.40.4.6, d.142.2.1
Probab=93.03  E-value=0.018  Score=37.92  Aligned_cols=43  Identities=47%  Similarity=0.749  Sum_probs=33.7  Template_Neff=9.400

Q NP_877456#7       1 PEITVDGRIVGYVMGKTG-KNVGRVVGYRVELEDGSTVAATGL   42 (45)
Q Consensus         1 Pede~DG~I~g~~~G~~g~a~eg~viG~~V~lEdG~~v~a~gi   43 (46)
|..+.|.+|+|+.+|+.+.++.|.+.++.+.+++|..+..+++
T Consensus       240 ~~~~~d~~v~g~~~g~~~~~~~g~~~~~~~~~~~g~~~~~~~~  282 (348)
T 1A0I_A          240 PENEADGIIQGLVWGTKGLANEGKVIGFEVLLESGRLVNATNI  282 (348)
T ss_dssp             CCEEEEEEEEEEECCCTTTSSCSSCCEEEEECTTSCEEEEBCC
T ss_pred             CcccccEEEEEEEeCCCCcccCCceEEEEEEEeCCcEEEEEee
Confidence            4567899999999999665677877788888888877665554


No 2
>4D05_A ATP-DEPENDENT DNA LIGASE; LIGASE; HET: SO4, AMP; 1.65A {PSYCHROMONAS SP. SP041}
Probab=92.21  E-value=0.03  Score=35.26  Aligned_cols=42  Identities=29%  Similarity=0.491  Sum_probs=30.6  Template_Neff=9.500

Q NP_877456#7       1 PEITVDGRIVGYVMGKTG-KNVGRVVGY-RVELEDGSTVAA-TGLSE   44 (45)
Q Consensus         1 Pede~DG~I~g~~~G~~g~a~eg~viG~-~V~lEdG~~v~a-~gis~   45 (46)
|..+.|.+|+|+.+|+  +.++| ++|. .|..++|..+.+ +|+|+
T Consensus       177 ~~~~~d~~Ivg~~~g~--~~~~g-~~g~~~v~~~~~~~~~vg~G~s~  220 (258)
T 4D05_A          177 KFEDAEATVIAYLPGK--GKYEG-LLGAILVKNEEGVTFKIGSGFSD  220 (258)
T ss_dssp             SCEEEEEEEEEEEEBC--GGGBT-SEEEEEEEETTSCEEEECSSCCH
T ss_pred             ehhcceEEEEEEECCC--Ceecc-cceEEEEEcCCCCEEEEeCCCCH
Confidence            4577899999999998  56777 5555 777777766665 35554


No 3
>4D05_B ATP-DEPENDENT DNA LIGASE; LIGASE; HET: SO4, AMP; 1.65A {PSYCHROMONAS SP. SP041}
Probab=92.21  E-value=0.03  Score=35.26  Aligned_cols=42  Identities=29%  Similarity=0.491  Sum_probs=30.6  Template_Neff=9.500

Q NP_877456#7       1 PEITVDGRIVGYVMGKTG-KNVGRVVGY-RVELEDGSTVAA-TGLSE   44 (45)
Q Consensus         1 Pede~DG~I~g~~~G~~g~a~eg~viG~-~V~lEdG~~v~a-~gis~   45 (46)
|..+.|.+|+|+.+|+  +.++| ++|. .|..++|..+.+ +|+|+
T Consensus       177 ~~~~~d~~Ivg~~~g~--~~~~g-~~g~~~v~~~~~~~~~vg~G~s~  220 (258)
T 4D05_B          177 KFEDAEATVIAYLPGK--GKYEG-LLGAILVKNEEGVTFKIGSGFSD  220 (258)
T ss_dssp             CCEEEEEEEEEEEECC--GGGTT-SEEEEEEEETTSCEEEECSSCCH


*/