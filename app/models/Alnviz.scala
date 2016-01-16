package models


// TODO Dependency injection might come in handy here


/**
 * Singleton object that stores general information about a tool
 *
 * Created by lzimmermann on 14.12.15.
 */

object Alnviz extends ToolModel {

  val toolname = "alnviz"
  val fullName = "Alignment Visualizer"


  // Parameter List
  val parameters = Vector(FileParam("alignment"), StringParam("format"))

  // Declares the required Interpreters
  val interpreters = Vector(Interpreter("perl"))

  // Declare the required helper scripts (helpers directory)
  var helpers = Vector(HelperScript("reformat.pl"))

  //Map parameter identifier to the full names
  val parameterNames = Map(
    "alignment" -> "Sequence Alignment",
    "format"    -> "Alignment Format")

  // Specifies a finite set of values the parameter is allowed to assumepe
  val parameterValues = Map(
    "format" -> Set("fas", "clue", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )

  val exec : Vector[CallComponent] = Vector(interpreters(0),  helpers(0),
                                      KeyValuePair("i", parameters(1), "-", "="),
                                      KeyValuePair("o", ConstParam("clu"), "-", "="),
                    KeyValuePair("f", parameters(0), "-", "="),
                    KeyValuePair("a", ResFileParam("result"), "-", "="))
}
case class Alnviz(alignment: String, format: String)