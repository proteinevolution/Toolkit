package models.graph


import scala.collection.mutable.ArrayBuffer

/**
  * Created by lukas on 3/6/16.
  */
object Converters {

  /* Actual Format Converters */

  abstract class ConverterFor[T] {

    val scriptname: String

    // Function that provides the shell arguments for the converter script.
    // Also, the script can then transform the files via argument
    def convert(from: T, to: T) : ArrayBuffer[String]
  }


  object ReformatConverter extends ConverterFor[AlignmentFormat] {

    val scriptname = "reformat_converter.sh"

    // parameters for the converter script
    def convert(from: AlignmentFormat, to: AlignmentFormat) : ArrayBuffer[String] = {
      ArrayBuffer(scriptname, from.toString.toLowerCase, to.toString.toLowerCase)
    }
  }

}
