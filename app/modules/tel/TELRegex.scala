package modules.tel
import scala.util.matching.Regex

/**
  * Created by lzimmermann on 6/30/16.
  */
trait TELRegex {

  // For translating the runscript template into an executable instance
  val replaceString : Regex = """%([A-Za-z_\.]+)""".r("expression") // TODO Not needed anymore, remove

  // A constant string starts with a percent sign
  final val envString: Regex =  """%([A-Z]+)""".r("constant")
  // A parameter String in an runscript starts with a percent sign, a parameter name and a representation
  final val parameterString: Regex  = """%([a-z_]+)\.([a-z_]+)""".r("paramName", "repr")


  val runscriptString : Regex = """%r""".r
  val regexJobID : Regex = """%JOBID""".r
  val regexPort : Regex = """%PORT""".r
}
