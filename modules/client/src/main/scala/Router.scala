import com.tgf.pizza.scalajs.mithril._
import org.scalajs.dom.raw.HTMLDivElement

import scala.scalajs.js
import scala.scalajs.js.Dictionary

object Router {
  import js.Dynamic.{ global => g }

  def main(args: Array[String]): Unit = {

    m.route.mode = "hash"
    val mountpoint = g.document.getElementById("main-content").asInstanceOf[HTMLDivElement]

    val Index            = g.Index.asInstanceOf[MithrilComponent]
    val News             = g.News.asInstanceOf[MithrilComponent]
    val Backend          = g.Backend.asInstanceOf[MithrilComponent]
    val JobManager       = g.JobManager.asInstanceOf[MithrilComponent]
    val Toolkit          = g.Toolkit.asInstanceOf[MithrilComponent]
    val JobListComponent = g.JobListComponent.asInstanceOf[MithrilComponent]
    val SearchComponent  = g.SearchBarComponent.asInstanceOf[MithrilComponent]

    //g.console.log("Router initialized")

    val routes: Dictionary[MithrilComponent] = js.Dictionary(
      "/"                 -> Index,
      "/tools/:toolname"  -> m.component(Toolkit, js.Dynamic.literal("isJob" -> false)).asInstanceOf[MithrilComponent],
      "/jobs/:jobID"      -> m.component(Toolkit, js.Dynamic.literal("isJob" -> true)).asInstanceOf[MithrilComponent],
      "/backend/:section" -> Backend,
      "/news"             -> News,
      "/jobmanager"       -> JobManager,
      "/:path..."         -> ErrorComponent
    )

    m.route(mountpoint, "/", routes)

    // in absence of multi-tenancy support: mount the joblist, which gets redrawn independently from other view changes, in a separate mithril instance

    g.jobList = g.m.deps.factory(g.window)
    g.jobList.mount(g.document.getElementById("sidebar-joblist").asInstanceOf[HTMLDivElement], JobListComponent)

    g.search = g.m.deps.factory(g.window)
    g.search.mount(
      g.document.getElementById("sidebar-search").asInstanceOf[HTMLDivElement],
      m.component(SearchComponent, js.Dynamic.literal("id" -> "side-search", "placeholder" -> " "))
    )

    g.jobListOffCanvas = g.m.deps.factory(g.window)
    g.jobListOffCanvas
      .mount(g.document.getElementById("off-canvas-joblist").asInstanceOf[HTMLDivElement], JobListComponent)

  }
}
