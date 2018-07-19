import com.tgf.pizza.scalajs.mithril._
import components.error.ErrorComponent
import org.scalajs.dom.document
import org.scalajs.dom.window
import scala.scalajs.js
import scala.scalajs.js.Dictionary

object Router {

  import js.Dynamic.{ global => g }

  def main(args: Array[String]): Unit = {
    m.route.mode = "hash"
    val mountpoint       = document.getElementById("main-content")
    val Index            = g.Index.asInstanceOf[MithrilComponent]
    val News             = g.News.asInstanceOf[MithrilComponent]
    val Backend          = g.Backend.asInstanceOf[MithrilComponent]
    val JobManager       = g.JobManager.asInstanceOf[MithrilComponent]
    val Toolkit          = g.Toolkit.asInstanceOf[MithrilComponent]
    val JobListComponent = g.JobListComponent.asInstanceOf[MithrilComponent]
    val SearchComponent  = g.SearchBarComponent.asInstanceOf[MithrilComponent]
    val routes: Dictionary[MithrilComponent] = js.Dictionary(
      "/" -> Index,
      "/tools/:toolname" -> m
        .component(Toolkit, js.Dynamic.literal("isJob" -> false))
        .asInstanceOf[MithrilComponent],
      "/jobs/:jobID" -> m
        .component(Toolkit, js.Dynamic.literal("isJob" -> true))
        .asInstanceOf[MithrilComponent],
      "/backend/:section" -> Backend,
      "/news"             -> News,
      "/jobmanager"       -> JobManager,
      "/:path..."         -> ErrorComponent
    )
    m.route(mountpoint, "/", routes)
    // in absence of multi-tenancy support: mount the joblist, which gets redrawn independently from other view changes, in a separate mithril instance
    g.jobList = m.deps.factory(window)
    g.jobList
      .mount(document.getElementById("sidebar-joblist"), JobListComponent)
    g.search = m.deps.factory(window)
    g.search.mount(
      document.getElementById("sidebar-search"),
      m.component(
        SearchComponent,
        js.Dynamic.literal("id" -> "side-search", "placeholder" -> " ")
      )
    )
    g.jobListOffCanvas = m.deps.factory(window)
    g.jobListOffCanvas
      .mount(document.getElementById("off-canvas-joblist"), JobListComponent)
  }

}
