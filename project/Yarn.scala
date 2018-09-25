import java.io.{ BufferedWriter, FileWriter }
import java.net.InetSocketAddress
import play.sbt.PlayRunHook
import sbt._
import scala.sys.process.Process

object Yarn {

  def apply(base: File): PlayRunHook = {

    val frontendFolder: File = base / "frontend"

    object YarnProcess extends PlayRunHook {

      var process: Option[Process] = None

      val (yarnInstall, yarnServe): (String, String) =
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
          ("cmd /c yarn install", "cmd /c yarn serve")
        } else {
          ("yarn install", "yarn serve")
        }

      override def beforeStarted(): Unit = {
        Process(yarnInstall, frontendFolder).!
      }

      override def afterStarted(addr: InetSocketAddress): Unit = {
        val port: String = addr.getPort.toString
        val file         = new File(frontendFolder, ".env.development.local")
        val bw           = new BufferedWriter(new FileWriter(file))
        bw.write(s"VUE_APP_BACKEND_PORT=$port")
        bw.close()
        process = Option(Process(yarnServe, frontendFolder).run)
      }

      override def afterStopped(): Unit = {
        process.foreach(_.destroy())
        process = None
      }

    }

    YarnProcess
  }

}
