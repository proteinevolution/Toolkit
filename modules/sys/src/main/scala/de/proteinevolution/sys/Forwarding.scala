package de.proteinevolution.sys

import javax.inject.Inject

import com.typesafe.config.ConfigFactory
import de.proteinevolution.sys.ConstructR.Script.ForwardingScript

import scala.concurrent.Future

class Forwarding @Inject()() {

  private val envMode: String = ConfigFactory.load().getString("toolkit_mode")

  val _ = envMode

  def apply(jobID: String,
            tool: String,
            numListStr: String,
            db: String,
            mode: String,
            filename: String): List[Future[Int]] = {

    val (_, _, _, _, _, _) = (jobID, tool, numListStr, db, mode, filename)
    // Example for retrieveAlnEval.sh

    sys.env("") // source environment

    new ConstructR[ForwardingScript].addCmd("", "").run()

  }

}
