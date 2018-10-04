import javax.inject.Inject
import com.mohiva.play.htmlcompressor.HTMLCompressorFilter
import com.mohiva.play.xmlcompressor.XMLCompressorFilter
import play.api.{ Environment, Mode }
import play.api.http.HttpFilters
import play.api.mvc.EssentialFilter
import play.filters.cors.CORSFilter

final class Filters @Inject()(
    cors: CORSFilter,
    htmlCompressorFilter: HTMLCompressorFilter,
    xmlCompressorFilter: XMLCompressorFilter,
    environment: Environment
) extends HttpFilters {

  private val corsConfig: List[CORSFilter] = if (environment.mode == Mode.Prod) cors :: Nil else Nil

  override def filters: Seq[EssentialFilter] = htmlCompressorFilter :: xmlCompressorFilter :: corsConfig

}
