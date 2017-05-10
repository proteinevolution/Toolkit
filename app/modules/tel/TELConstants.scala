package modules.tel

import models.Constants

/**
  * Created by lzimmermann on 6/30/16.
  */
trait TELConstants extends Constants {
  // TODO Get this from the configuration of the toolkit

  // How the file of the final execution is called
  final val executableName = "tool.sh"

  val TELPath = "tel"
  // Character used to have comments in the Shell files interpreted by TEL
  // Do NOT change this !
  val commentChar = '#'

  // PATHS // TODO Remove me
  val runscriptPath = s"$TELPath${SEPARATOR}runscripts$SEPARATOR"
  val contextPath   = s"$TELPath${SEPARATOR}context.d$SEPARATOR"
}
