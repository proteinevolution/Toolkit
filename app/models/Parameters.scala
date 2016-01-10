package models

/**
 *
 * Defines the parameters the tools.
 *
 * Created by lzimmermann on 19.12.15.
 */
// String Parameter of the Tool
abstract class Param(name: String, default: Option[String])

case class StringParam(name: String, default: Option[String]) extends Param(name: String, default: Option[String])

// File Parameter of the Tool
case class FileParam(name: String, default: Option[String]) extends Param(name: String, default: Option[String])
