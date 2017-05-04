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
    val mountpoint = g.document.getElementById("main-content").asInstanceOf[HTMLDivElement]



    val Index = g.Index.asInstanceOf[MithrilComponent]
    val News = g.News.asInstanceOf[MithrilComponent]
    val Backend = g.Backend.asInstanceOf[MithrilComponent]
    val JobManager = g.JobManager.asInstanceOf[MithrilComponent]
    val Toolkit = g.Toolkit.asInstanceOf[MithrilComponent]
    val JobListComponent = g.JobListComponent.asInstanceOf[MithrilComponent]


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

    m.mount(g.document.getElementById("off-canvas-joblist").asInstanceOf[HTMLDivElement], JobListComponent)

  }
}


