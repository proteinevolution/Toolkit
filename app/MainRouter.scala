import controllers._
import de.proteinevolution.auth.AuthRouter
import de.proteinevolution.backend.BackendRouter
import de.proteinevolution.cluster.ClusterRouter
import de.proteinevolution.help.HelpRouter
import de.proteinevolution.jobs.JobsRouter
import de.proteinevolution.results.ResultsRouter
import javax.inject.{ Inject, Singleton }
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

@Singleton
class MainRouter @Inject()(
    controller: Application,
    auth: Auth,
    job: JobController,
    search: Search,
    service: Service,
    uptime: UptimeController,
    resultsRouter: ResultsRouter,
    assets: Assets,
    webjarsRouter: webjars.Routes,
    authRouter: AuthRouter,
    clusterRouter: ClusterRouter,
    helpRouter: HelpRouter,
    backendRouter: BackendRouter,
    jobsRouter: JobsRouter
) extends SimpleRouter {

  private lazy val mainRoutes: Routes = {
    case GET(p"/")                    => controller.index()
    case GET(p"/sitemap.xml")         => assets.versioned(path = "/public", file = "sitemap.xml")
    case GET(p"/ws")                  => controller.ws
    case GET(p"/ws/config")           => controller.wsConfig
    case POST(p"/maintenance")        => controller.maintenance
    case GET(p"/uptime")              => uptime.uptime
    case GET(p"/buildinfo")           => uptime.buildInfo
    case GET(p"/assets/$file*")       => assets.versioned(path = "/public", file = file)
    case GET(p"/static/get/$static")  => service.static(static)
    case GET(p"/jobs")                => search.get // TODO in use?
    case GET(p"/index/page/info")     => search.getIndexPageInfo
    case GET(p"/tool/list")           => search.getToolList
    case GET(p"/suggest/$jobID")      => search.autoComplete(jobID)
    case GET(p"/check/tool/$tool")    => search.existsTool(tool)
    case GET(p"/robots.txt")          => controller.robots
    case GET(p"/$static")             => controller.static(static)
    case GET(p"/api/tools/$toolName") => service.getTool(toolName)
  }

  private lazy val uiRoutes: Routes = {
    case GET(p"/hhpred")          => controller.showTool(toolName = "hhpred")
    case GET(p"/tools/$toolName") => controller.showTool(toolName)
    case GET(p"/jobs/$idString")  => controller.showJob(idString)
    case GET(p"/recent/updates")  => controller.recentUpdates
  }

  private lazy val jobRoutes: Routes = {
    case POST(p"/api/job/" ? q"toolName=$toolName")                           => job.submitJob(toolName)
    case GET(p"/api/job/$jobID/checkHash")                                    => job.checkHash(jobID)
    case GET(p"/api/job/$jobID")                                              => service.getJob(jobID)
    case GET(p"/api/job/result/$jobID/$tool/$panel")                          => service.getResult(jobID, tool, panel)
    case GET(p"/search/check/jobid/$jobID/" ? q_o"resubmitJobID=$resubmitID") => search.checkJobID(jobID, resubmitID)
  }

  private lazy val authRoutes: Routes = {
    case POST(p"/signin")                       => auth.signInSubmit
    case GET(p"/verification/$userName/$token") => auth.verification(userName, token) // extern
  }

  override lazy val routes: Routes = {
    mainRoutes
      .orElse(authRoutes)
      .orElse(jobRoutes)
      .orElse(uiRoutes)
      .orElse(jobsRouter.withPrefix("/api/jobs").routes)
      .orElse(backendRouter.withPrefix("/backend").routes)
      .orElse(helpRouter.withPrefix("/help").routes)
      .orElse(clusterRouter.withPrefix("/cluster").routes)
      .orElse(authRouter.withPrefix("/auth").routes)
      .orElse(webjarsRouter.withPrefix("/webjars").routes)
      .orElse(resultsRouter.withPrefix("/results").routes)
  }

}
