package modules.parsers
import scala.util.parsing.combinator._

/**
  * Created by zin on 20.12.16.
  */

object HHR {

  case class Entry( header: Map[String, String], hitList: Array[List[String]], aln : Array[(String, List[(String, Int, String, Int, String)])] )

  def fromFile( fn: String ): List[Entry] = {
    val lines = scala.io.Source.fromFile(fn).getLines().mkString("\n")
    fromString( lines )
  }

  def fromString( input: String ): List[Entry] =
    Parser.parse(input)


  private object Parser extends RegexParsers {

    lazy val header : String = ""
    lazy val hitList: String = ""
    lazy val aln : String = ""

    def parse(input: String) : List[Entry] = ???

  }

}
