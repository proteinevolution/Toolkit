package de.proteinevolution.tel

import com.typesafe.config.ConfigFactory
import de.proteinevolution.models.Constants

trait TELConstants {
  // TODO Get this from the configuration of the toolkit

  // How the file of the final execution is called n
  final val executableName = "tool.sh"

  // Character used to have comments in the Shell files interpreted by TEL
  // Do NOT change this !
  val commentChar = '#'

}
