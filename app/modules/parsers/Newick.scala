package modules.parsers
import scala.util.parsing.combinator._

sealed abstract class Tree
case class Node(edges: List[Edge], name: Option[String]) extends Tree
case class Leaf(name: Option[String])                    extends Tree
case class Edge(to: Tree, weight: Option[Double])        extends Tree

sealed trait Newick {
  val OPEN_PAREN  = "("
  val CLOSE_PAREN = ")"
  val SEMICOLON   = ";"
  val COLON       = ":"
  val COMMA       = ","
}

object newickParser extends RegexParsers with Newick {
  private[this] def number: Parser[Double] = """\d+(\.\d*)?""".r ^^ { _.toDouble }

  private[this] def string: Parser[String] = """[a-zA-Z0-9#!?=&%$@_|.- ]+""".r ^^ { _.toString }

  private[this] def name: Parser[Option[String]] = string.?

  private[this] def length: Parser[Option[Double]] = (COLON ~> number).?

  private[this] def edges: Parser[List[Edge]] = repsep(edge, COMMA)

  private[this] def node: Parser[Node] = (OPEN_PAREN ~> edges <~ CLOSE_PAREN) ~ name ^^ {
    case edges ~ name => Node(edges, name)
  }

  private[this] def leaf: Parser[Leaf] = name ^^ {
    case name => Leaf(name)
  }

  private[this] def edge: Parser[Edge] = subtree ~ length ^^ {
    case subtree ~ length => Edge(subtree, length)
  }

  private[this] def subtree: Parser[Tree] = node | leaf

  private[this] def tree: Parser[Tree] = (subtree | edge) <~ SEMICOLON

  def apply(src: String): Tree = parseAll(tree, src) match {
    case Success(result, _) => result
    case failure: NoSuccess => throw new Exception(failure.msg)
  }
}

object newickPrinter extends Newick {
  private[this] def printName(name: Option[String]): String = name.getOrElse("")

  private[this] def printWeight(weight: Option[Double]): String = weight match {
    case None    => ""
    case Some(w) => COLON + w
  }

  private[this] def printTree(e: Tree): String = e match {
    case Node(edges, name) =>
      OPEN_PAREN +
        edges.map(printTree).reduceLeft((acc, t) => acc + COMMA + t) +
        CLOSE_PAREN +
        printName(name)
    case Leaf(name) =>
      printName(name)
    case Edge(to, weight) =>
      printTree(to) //+ printWeight(weight)
  }

  def apply(tree: Tree): String = printTree(tree) + SEMICOLON
}
