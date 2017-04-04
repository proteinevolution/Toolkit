package modules.tel

import javax.inject.{Inject, Singleton}
import modules.tel.env.Env
import modules.tel.param.Params


/**
  * TEL is the access point to get ExecutionContexts in which runscripts can be executed
  *
  * Created by lzimmermann on 26.05.16.
  */
@Singleton
class TEL @Inject() (env : Env,
                     params: Params) extends TELRegex with TELConstants   {


  // Ignore the following keys when writing parameters // TODO This is a hack and must be changed
  val ignore: Seq[String] = Array("jobid", "newSubmission", "start", "edit")

  // Each tool exection consists of the following subdirectories
  val subdirs : Seq[String] = Array("params", "results", "temp", "logs")

  val context: String = env.get("CONTEXT")

  /**
    * Returns the Array of all values and plain text names of the set params
    *
    * @param param
    */
  def generateValues(param : String) : Map[String, String] = params.generateValues(param)
}


object TEL {

  var port = ""

  var hostname = ""

  var memFactor : Double = 1

  var threadsFactor : Double = 1

}

