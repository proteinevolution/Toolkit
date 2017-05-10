package modules.parsers
import scala.util.parsing.combinator._
import better.files._

object FASTA {

  case class Entry(description: String, sequence: String)

  def fromFile(fn: String): List[Entry] = {
    val x = fn.toFile
    if (x.isEmpty) {
      List.empty
    } else {
      fromString(x.contentAsString)
    }
  }

  def fromString(input: String): List[Entry] =
    Parser.parse(input)

  private object Parser extends RegexParsers {

    lazy val header: FASTA.Parser.Parser[String]  = """>.*""".r ^^ { _.tail.trim }
    lazy val seqLine: FASTA.Parser.Parser[String] = """[^>].*""".r ^^ { _.trim }

    lazy val sequence: FASTA.Parser.Parser[String] = rep1(seqLine) ^^ { _.mkString }

    lazy val entry: FASTA.Parser.Parser[FASTA.Entry] = header ~ sequence ^^ {
      case h ~ s => Entry(h, s)
    }

    lazy val entries: FASTA.Parser.Parser[scala.List[FASTA.Entry]] = rep1(entry)

    private[FASTA] def parse(input: String): List[Entry] = {
      parseAll(entries, input) match {
        case Success(es, _) => es
        case x: NoSuccess   => throw new Exception(x.toString)
      }
    }
  }
}
