package models

/**
 *
 * Defines the parameters the tools.
 *
 * Created by lzimmermann on 19.12.15.
 */
// String Parameter of the Tool
abstract class Param(name: String)

case class StringParam(name: String) extends Param(name: String)

// File Parameter of the Tool
case class FileParam(name: String) extends Param(name: String)

case class ConstParam(name: String) extends Param(name: String)

case class ResFileParam(name: String) extends Param(name: String)



case class Interpreter(name: String)

case class HelperScript(name: String)


case class KeyValuePair(key: String, value: Param, prefix: String, sep: String)