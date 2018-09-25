import javax.inject.Inject
import com.mohiva.play.htmlcompressor.HTMLCompressorFilter
import com.mohiva.play.xmlcompressor.XMLCompressorFilter
import play.api.http.HttpFilters
import play.api.mvc.EssentialFilter
import play.filters.cors.CORSFilter

final class Filters @Inject()(
    corsFilter: CORSFilter,
    htmlCompressorFilter: HTMLCompressorFilter,
    xmlCompressorFilter: XMLCompressorFilter
) extends HttpFilters {

  override def filters: Seq[EssentialFilter] = htmlCompressorFilter :: xmlCompressorFilter :: corsFilter :: Nil

}
