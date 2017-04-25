package modules.parsers.Ops


import scala.util.parsing.combinator.RegexParsers

/**
  * Created by snam on 23.03.17.
  */

object QhostP {

  // Ignored: ARCH, SWAPTO, SWAPUS

case class Node(hostname : String, ncpu: Int, load: Double, memtot: Double, memuse: Double)


  def fromString( input: String ) : List[QhostP.Node] = {

    Parser.parse(input.split('\n').drop(3).mkString("\n")) // ignores first three lines

  }


  private object Parser extends RegexParsers {

    val hostname : QhostP.Parser.Parser[String] = """(?m)^[\S]+""".r
    val arch : QhostP.Parser.Parser[String] = """\S*""".r
    val ncpu : QhostP.Parser.Parser[Int] = """\d*""".r ^^ { _.toInt }
    val load : QhostP.Parser.Parser[Double] = """[+-]?([0-9]*[.])?[0-9]+|-""".r ^^ {
      case x if x == "-" => 0
      case x => x.toDouble
    }
    val memtot : QhostP.Parser.Parser[Double] = """[+-]?([0-9]*[.])?[0-9]+(G|M|K)""".r ^^ {
      case x if x.endsWith("M") => "0.".concat(x.dropRight(1).filterNot(_ == '.')).toDouble
      case x if x.endsWith("G") => x.dropRight(1).toDouble
    }
    val memuse : QhostP.Parser.Parser[Double] = """[+-]?([0-9]*[.])?[0-9]+(G|M|K)|-""".r ^^ {
      case x if x == "-" => 0
      case x if x.endsWith("M") => "0.".concat(x.dropRight(1).filterNot(_ == '.')).toDouble
      case x if x.endsWith("G") => x.dropRight(1).toDouble
    }
    val rest : QhostP.Parser.Parser[String] = """.*""".r


    val entry : QhostP.Parser.Parser[QhostP.Node] = hostname ~ arch ~ ncpu ~ load ~ memtot ~ memuse ~ rest ^^ {
      case h ~ a ~ n ~ l ~ mt ~ mu ~ r => Node(h,n,l,mt,mu)
    }

    val entries : QhostP.Parser.Parser[scala.List[QhostP.Node]] = rep1( entry )




    private[QhostP] def parse( input: String ): List[Node]  = {
      parseAll( entries, input ) match {
        case Success( es , _ ) => es
        case x: NoSuccess =>  throw new Exception(x.toString)
      }
    }
  }


}



/*
HOSTNAME                ARCH         NCPU  LOAD  MEMTOT  MEMUSE  SWAPTO  SWAPUS
-------------------------------------------------------------------------------
global                  -               -     -       -       -       -       -
node336                 lx26-amd64     64 25.83  503.9G   16.6G  722.6G  692.0K
node337                 lx26-amd64     64 58.20  503.9G   37.3G  722.6G     0.0
node443                 lx26-amd64     48 50.71  503.3G   52.8G  252.0G     0.0
node444                 lx26-amd64     48 38.03  503.3G   64.1G 1146.1G    3.0M
node445                 lx26-amd64     48 50.71  503.9G   16.9G 1146.1G     0.0
node446                 lx26-amd64     48 45.34  503.9G   32.8G 1453.1G     0.0
node448                 lx26-amd64     48 52.25  503.9G   20.1G  252.0G    1.3M
node455                 lx26-amd64     64 37.45  503.9G   40.3G  722.6G    9.1M
node501                 lx26-amd64     64 60.20  503.9G   32.0G  722.6G     0.0
node502                 lx26-amd64     64 31.44  503.9G   14.6G  624.6G     0.0
node503                 lx26-amd64     64     -  503.9G       -  624.6G       -
node504                 lx26-amd64     64  0.06  995.5G    4.4G  498.0G     0.0
node505                 lx26-amd64     64 58.45  995.5G   42.3G  498.0G     0.0
node506                 lx26-amd64     64 49.42  503.9G   71.8G  624.6G   16.5M
node507                 lx26-amd64     64     -  503.9G       -  624.6G       -
node508                 lx26-amd64     64 35.07  503.9G   65.0G  722.6G     0.0
node509                 lx26-amd64     64 58.12  995.5G   41.6G  498.0G   23.3M
node510                 lx26-amd64     64 65.04  976.4G   17.1G  489.0G     0.0
node511                 lx26-amd64     64  0.08  995.5G    2.3G  498.0G   13.8M
node512                 lx26-amd64     64 26.20  503.9G    8.6G  722.6G     0.0
node513                 lx26-amd64     64 77.72  503.9G   69.5G  722.6G     0.0

 */