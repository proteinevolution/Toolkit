package models.tel

/**
  * Created by lzimmermann on 6/30/16.
  */
object TELRegex {

  // For translating the runscript template into an executable instance
  val replaceeString = """%([A-Za-z_\.]+)""".r("expression")

  // Elements of the markup of runscripts, currently constants and parameter string are supported
  val constantsString =  """([A-Z]+)""".r("constant")
  val parameterString = """([a-z_]+)\.([a-z_]+)""".r("paramName", "selector")

  val runscriptString = """%r""".r
}
