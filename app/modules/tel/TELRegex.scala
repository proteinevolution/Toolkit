package modules.tel
import scala.util.matching.Regex

/**
  * Created by lzimmermann on 6/30/16.
  */
trait TELRegex {

  // For translating the runscript template into an executable instance
  val replaceString : Regex = """%([A-Za-z_\.]+)""".r("expression")

  // Elements of the markup of runscripts, currently constants and parameter string are supported
  val constantsString : Regex =  """([A-Z]+)""".r("constant")


  // A parameter String in an runscript starts with a percent sign, a parameter name and a represenation
  val parameterString: Regex  = """%([a-z_]+)\.([a-z_]+)""".r("paramName", "repr")

  val runscriptString : Regex = """%r""".r
  val regexJobID : Regex = """%JOBID""".r
  val regexPort : Regex = """%PORT""".r
}
