import controllers._
import de.proteinevolution.auth.AuthRouter
import de.proteinevolution.backend.BackendRouter
import de.proteinevolution.cluster.ClusterRouter
import de.proteinevolution.help.HelpRouter
import de.proteinevolution.jobs.JobsRouter
import de.proteinevolution.message.MessageRouter
import de.proteinevolution.results.ResultsRouter
import de.proteinevolution.search.SearchRouter
import de.proteinevolution.verification.VerificationRouter
import de.proteinevolution.ui.UiRouter
import javax.inject.{ Inject, Singleton }
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

@Singleton
class MainRouter @Inject()(
    controller: Application,
    uptime: UptimeController,
    resultsRouter: ResultsRouter,
    assets: Assets,
    webjarsRouter: webjars.Routes,
    authRouter: AuthRouter,
    clusterRouter: ClusterRouter,
    helpRouter: HelpRouter,
    backendRouter: BackendRouter,
    jobsRouter: JobsRouter,
    searchRouter: SearchRouter,
    uiRouter: UiRouter,
    messageRouter: MessageRouter,
    verificationRouter: VerificationRouter
) extends SimpleRouter {

  private lazy val mainRoutes: Routes = {
    case GET(p"/") => controller.index()
    case GET(p"/sitemap.xml") =>
      assets.versioned(path = "/public", file = "sitemap.xml")
    case GET(p"/uptime")    => uptime.uptime
    case GET(p"/buildinfo") => uptime.buildInfo
    case GET(p"/assets/$file*") =>
      assets.versioned(path = "/public", file = file)
    case GET(p"/robots.txt") => controller.robots
  }

  private lazy val uiRoutes: Routes = {
    case GET(p"/$static")         => controller.static(static)
    case GET(p"/hhpred")          => controller.showTool(toolName = "hhpred")
    case GET(p"/tools/$toolName") => controller.showTool(toolName)
    case GET(p"/jobs/$idString")  => controller.showJob(idString)
  }

  override lazy val routes: Routes = {
    mainRoutes
      .orElse(messageRouter.withPrefix("/ws").routes)
      .orElse(uiRoutes)
      .orElse(uiRouter.withPrefix("/ui").routes)
      .orElse(searchRouter.withPrefix("/search").routes)
      .orElse(jobsRouter.withPrefix("/api/jobs").routes)
      .orElse(backendRouter.withPrefix("/backend").routes)
      .orElse(helpRouter.withPrefix("/help").routes)
      .orElse(clusterRouter.withPrefix("/cluster").routes)
      .orElse(authRouter.withPrefix("/auth").routes)
      .orElse(verificationRouter.withPrefix("/verification").routes)
      .orElse(webjarsRouter.withPrefix("/webjars").routes)
      .orElse(resultsRouter.withPrefix("/results").routes)
  }

}
