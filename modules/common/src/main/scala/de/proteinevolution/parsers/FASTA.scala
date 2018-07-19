package de.proteinevolution.parsers
import scala.util.parsing.combinator._
import better.files._

case class FASTA(sequences: List[FASTA.Entry]) {

  /**
   * Is true when the sequence can be interpreted as a valid amino acid sequence
   */
  lazy val hasValidAminoAcidSeqs: Boolean =
    sequences.forall(_.isValidAminoAcidSeq)

  /**
   * Is true when the sequence can be interpreted as a valid DNA sequence
   */
  lazy val hasValidDNASeqs: Boolean = sequences.forall(_.isValidDNASeq)

  /**
   * Is true when the sequence can be interpreted as a valid RNA sequence
   */
  lazy val hasValidRNASeqs: Boolean = sequences.forall(_.isValidRNASeq)

  /**
   * Calculates the hash code from the xor of all FASTA sequences
   */
  def generateHashCode(function: String => Int): Int = {
    sequences
      .map(_.generateHashCode(function))
      .fold(0: Int)((hashCode, hashCodeSeq) => hashCode ^ hashCodeSeq)
  }
}

object FASTA {
  object Entry {
    trait EntryType
    trait SEQUENCE extends EntryType {
      val validChars: List[Char]
    }
    object AMINOACID extends EntryType with SEQUENCE {
      override val validChars = List('A',
                                     'C',
                                     'D',
                                     'E',
                                     'F',
                                     'G',
                                     'H',
                                     'I',
                                     'K',
                                     'L',
                                     'M',
                                     'N',
                                     'O',
                                     'P',
                                     'Q',
                                     'R',
                                     'S',
                                     'T',
                                     'U',
                                     'V',
                                     'W',
                                     'Y',
                                     '-',
                                     '.',
                                     '*')
    }
    object DNA extends EntryType with SEQUENCE {
      override val validChars = List('A', 'C', 'G', 'T', '-', '.', '*')
    }
    object RNA extends EntryType with SEQUENCE {
      override val validChars = List('A', 'C', 'G', 'U', '-', '.', '*')
    }
  }

  case class Entry(description: String, sequence: String) {
    lazy val isValidAminoAcidSeq: Boolean = {
      sequence.trim.toUpperCase.toCharArray
        .forall(Entry.AMINOACID.validChars.contains)
    }
    lazy val isValidDNASeq: Boolean = {
      sequence.trim.toUpperCase.toCharArray
        .forall(Entry.DNA.validChars.contains)
    }
    lazy val isValidRNASeq: Boolean = {
      sequence.trim.toUpperCase.toCharArray
        .forall(Entry.RNA.validChars.contains)
    }

    def generateHashCode(function: String => Int): Int =
      function(sequence.trim.toUpperCase)
  }

  /**
   * Creates a FASTA alignment from a file
   * @param fileName path to the file
   * @return
   */
  def fromFile(fileName: String): Option[FASTA] = {
    val x = fileName.toFile
    if (x.isEmpty) {
      None
    } else {
      fromString(x.contentAsString)
    }
  }

  /**
   * Creates a FASTA alignment directly from a string
   * @param input
   * @return
   */
  def fromString(input: String): Option[FASTA] =
    Parser.parse(input)

  private object Parser extends RegexParsers {

    lazy val header: FASTA.Parser.Parser[String] = """>.*""".r ^^ {
      _.tail.trim
    }
    lazy val seqLine: FASTA.Parser.Parser[String] = """[^>].*""".r ^^ { _.trim }

    lazy val sequence: FASTA.Parser.Parser[String] = rep1(seqLine) ^^ {
      _.mkString
    }

    lazy val entry: FASTA.Parser.Parser[FASTA.Entry] = header ~ sequence ^^ {
      case h ~ s => Entry(h, s)
    }

    lazy val entries: FASTA.Parser.Parser[scala.List[FASTA.Entry]] = rep1(entry)

    private[FASTA] def parse(input: String): Option[FASTA] = {
      parseAll(sequence, input) match {
        case Success(es, _) => Some(FASTA(List(Entry("empty header", es))))
        case _ =>
          parseAll(entries, input) match {
            case Success(es, _) => Some(FASTA(es))
            case _              => None
          }
      }
    }
  }
}
