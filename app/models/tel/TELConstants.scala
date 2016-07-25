package models.tel

import models.Constants
import better.files._
import models.Implicits._


/**
  * Created by lzimmermann on 6/30/16.
  */
trait TELConstants extends Constants {
  // TODO Get this from the configuration of the toolkit
  val TELPath = "tel"
  // Character used to have comments in the Shell files interpreted by TEL
  // Do NOT change this !
  val commentChar = '#'

  // FILES
  val constantsFile = s"$TELPath${SEPARATOR}CONSTANTS".toFile
  val paramsDFile =  s"$TELPath${SEPARATOR}params.d".toFile
  val initFile =  s"$TELPath${SEPARATOR}init.sh".toFile

  // PATHS
  val typesPath = s"$TELPath${SEPARATOR}types"
  val runscriptPath = s"$TELPath${SEPARATOR}runscripts${SEPARATOR}"
  val contextPath = s"$TELPath${SEPARATOR}context.d${SEPARATOR}"
}
