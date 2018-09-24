import java.io.{ BufferedWriter, FileWriter }
import java.net.InetSocketAddress

import play.sbt.PlayRunHook
import sbt._

import scala.sys.process.Process

/**
  * Frontend build play run hook.
  * https://www.playframework.com/documentation/2.6.x/SBTCookbook
  */
object Yarn {
  def apply(base: File): PlayRunHook = {
    
    val frontendFolder: File = base / "frontend"
    
    object YarnProcess extends PlayRunHook {

      var process: Option[Process] = None

      /**
        * Change these commands if you want to use Yarn.
        */
      var yarnInstall: String = "yarn install"
      var yarnServe: String = "yarn serve"

      // Windows requires npm commands prefixed with cmd /c
      if (System.getProperty("os.name").toLowerCase().contains("win")){
        yarnInstall = "cmd /c" + yarnInstall
        yarnServe = "cmd /c" + yarnServe
      }

      /**
        * Executed before play run start.
        */
      override def beforeStarted(): Unit = {
        Process(yarnInstall, frontendFolder).!
      }

      /**
        * Executed after play run start.
        */
      override def afterStarted(addr: InetSocketAddress): Unit = {
        val port: String = addr.getPort.toString
        val file         = new File(frontendFolder, ".env.development.local")
        val bw           = new BufferedWriter(new FileWriter(file))
        bw.write(s"VUE_APP_BACKEND_PORT=$port")
        bw.close()
        process = Option(
          Process(yarnServe, frontendFolder).run
        )
      }

      /**
        * Executed after play run stop.
        * Cleanup frontend execution processes.
        */
      override def afterStopped(): Unit = {
        process.foreach(_.destroy())
        process = None
      }

    }

    YarnProcess
  }
}