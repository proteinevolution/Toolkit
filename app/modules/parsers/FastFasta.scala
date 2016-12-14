package modules.parsers
import fastparse.all._
import modules.parsers.FastFasta.Entry

/**
  * Fasta combinator
  *
  */

sealed trait FastFasta {


  lazy val header = """>.*""".r
  lazy val seqLine = """[^>].*""".r

}

sealed trait FastFastaParser {

  def parse(input: String) : List[Entry] = List.empty

}


object FastFasta extends FastFasta with FastFastaParser{

  case class Entry (description: String, sequence: String)

  def fromFile( fn: String ): List[Entry] = {
    val lines = scala.io.Source.fromFile(fn).getLines().mkString("\n")
    fromString(lines)
  }

  def fromString( input: String ): List[Entry] =
    parse(input)


}