import controllers._
import de.proteinevolution.auth.AuthRouter
import de.proteinevolution.results.ResultsRouter
import javax.inject.{ Inject, Singleton }
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

@Singleton
class MainRouter @Inject()(
    controller: Application,
    auth: Auth,
    backend: Backend,
    cluster: ClusterController,
    data: DataController,
    forwardModal: ForwardModalController,
    job: JobController,
    jobs: Jobs,
    search: Search,
    service: Service,
    stats: Stats,
    uptime: UptimeController,
    resultsRouter: ResultsRouter,
    assets: Assets,
    webjarsRouter: webjars.Routes,
    authRouter: AuthRouter
) extends SimpleRouter {

  private lazy val mainRoutes: Routes = {
    case GET(p"/")                        => controller.index()
    case GET(p"/sitemap.xml")             => assets.versioned(path = "/public", file = "sitemap.xml")
    case GET(p"/ws")                      => controller.ws
    case GET(p"/ws/config")               => controller.wsConfig
    case POST(p"/maintenance")            => controller.maintenance
    case GET(p"/uptime")                  => uptime.uptime
    case GET(p"/buildinfo")               => uptime.buildInfo
    case GET(p"/files/$mainID/$filename") => controller.file(filename = filename, mainID = mainID)
    case GET(p"/assets/$file*")           => assets.versioned(path = "/public", file = file)
    case GET(p"/static/get/$static")      => service.static(static)
    case GET(p"/jobs")                    => search.get // TODO in use?
    case GET(p"/index/page/info")         => search.getIndexPageInfo
    case GET(p"/tool/list")               => search.getToolList
    case GET(p"/suggest/$jobID")          => search.autoComplete(jobID)
    case GET(p"/check/tool/$tool")        => search.existsTool(tool)
    case GET(p"/robots.txt")              => controller.robots
    case GET(p"/$static")                 => controller.static(static)
    case GET(p"/api/tools/$toolName")     => service.getTool(toolName)
  }

  private lazy val uiRoutes: Routes = {
    case GET(p"/hhpred")                             => controller.showTool(toolName = "hhpred")
    case GET(p"/tools/$toolName")                    => controller.showTool(toolName)
    case GET(p"/jobs/$idString")                     => controller.showJob(idString)
    case GET(p"/get/help/$tool")                     => data.getHelp(tool)
    case GET(p"/recent/updates")                     => data.recentUpdates
    case GET(p"/forward/modal/$toolName/$modalType") => forwardModal.getForwardModalOptions(modalType, toolName)
    case GET(p"/load")                               => cluster.getLoad
  }

  private lazy val backendRoutes: Routes = {
    case GET(p"/backend/index")        => backend.index
    case GET(p"/backend/statistics")   => backend.statistics
    case GET(p"/backend/runusersweep") => backend.runUserSweep
    case GET(p"/runjobsweep")          => backend.runJobSweep
    case GET(p"/backend/users")        => backend.users
    case POST(p"/backend/users")       => backend.users
  }

  private lazy val jobRoutes: Routes = {
    case GET(p"/api/jobs")                                                    => job.listJobs
    case POST(p"/api/job/" ? q"toolName=$toolName")                           => job.submitJob(toolName)
    case POST(p"/api/job/$jobID/start")                                       => job.startJob(jobID)
    case GET(p"/api/job/$jobID/checkHash")                                    => job.checkHash(jobID)
    case GET(p"/api/job/$jobID")                                              => service.getJob(jobID)
    case GET(p"/api/job/result/$jobID/$tool/$panel")                          => service.getResult(jobID, tool, panel)
    case DELETE(p"/api/job/$jobID")                                           => job.delete(jobID)
    case GET(p"/api/job/load/$jobID")                                         => job.loadJob(jobID)
    case POST(p"/api/frontend/submit/$toolName")                              => stats.frontendCount(toolName)
    case PUT(p"/jobs/$status/$jobID/$key")                                    => jobs.setJobStatus(status, jobID, key)
    case PUT(p"/jobs/sge/$jobID/$sgeID/$key")                                 => jobs.SGEID(jobID, sgeID, key)
    case PUT(p"/jobs/dateviewed/$mainID")                                     => jobs.updateDateViewed(mainID)
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
      .orElse(backendRoutes)
      .orElse(uiRoutes)
      .orElse(authRouter.withPrefix("/auth").routes)
      .orElse(webjarsRouter.withPrefix("/webjars").routes)
      .orElse(resultsRouter.withPrefix("/results").routes)
  }

}
