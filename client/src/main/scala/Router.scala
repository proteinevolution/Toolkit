/**
  * Created by snam on 05.01.17.
  */


import co.technius.scalajs.mithril._
import org.scalajs.dom.raw.HTMLDivElement
import scala.scalajs.js
import scala.scalajs.js.Dictionary



object Router extends js.JSApp {
  import js.Dynamic.{ global => g }


  def main(): Unit = {
    m.route.mode = "hash"
    val mountpoint = js.Dynamic.global.document.getElementById("main-content").asInstanceOf[HTMLDivElement]



    val Index = js.Dynamic.global.Index.asInstanceOf[MithrilComponent]
    val News = js.Dynamic.global.News.asInstanceOf[MithrilComponent]
    val Backend = js.Dynamic.global.Backend.asInstanceOf[MithrilComponent]
    val JobManager = js.Dynamic.global.JobManager.asInstanceOf[MithrilComponent]
    val Toolkit = js.Dynamic.global.Toolkit.asInstanceOf[MithrilComponent]


    //g.console.log("Router initialized")


    val routes : Dictionary[MithrilComponent] = js.Dictionary(
      "/" -> Index,
      "/tools/:toolname" -> m.component(Toolkit, js.Dynamic.literal(
        "isJob" -> false)).asInstanceOf[MithrilComponent],
      "/jobs/:jobID" -> m.component(Toolkit, js.Dynamic.literal(
        "isJob" -> true)).asInstanceOf[MithrilComponent],
      "/backend/:section" -> Backend,
      "/news" -> News,
      "/jobmanager" -> JobManager,
      "/:path..." -> ErrorComponent
    )

    m.route(mountpoint,"/", routes)

  }
}


