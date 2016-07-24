package models.tel

import models.Constants
import better.files._
import models.Implicits._


/**
  * Created by lzimmermann on 6/30/16.
  */
trait TELConstants {
  // TODO Get this from the configuration of the toolkit
  val TELPath = "tel"
  // Character used to have comments in the Shell files interpreted by TEL
  // Do NOT change this !
  val commentChar = '#'

  // FILES
  val constantsFile = s"$TELPath${Constants.SEP}CONSTANTS".toFile
  val paramsDFile =  s"$TELPath${Constants.SEP}params.d".toFile
  val initFile =  s"$TELPath${Constants.SEP}init.sh".toFile

  // PATHS
  val typesPath = s"$TELPath${Constants.SEP}types"
  val runscriptPath = s"$TELPath${Constants.SEP}runscripts${Constants.SEP}"
  val contextPath = s"$TELPath${Constants.SEP}context.d${Constants.SEP}"
}
