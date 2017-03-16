/**
  * Created by snam on 01.02.17.
  */

import javax.inject.Inject
import com.mohiva.play.htmlcompressor.HTMLCompressorFilter
import com.mohiva.play.xmlcompressor.XMLCompressorFilter
import play.filters.csrf.CSRFFilter
import play.api.http.HttpFilters
import play.api.mvc.EssentialFilter
import play.filters.gzip.GzipFilter

final class Filters @Inject() (
                          gzip: GzipFilter,
                          htmlCompressorFilter: HTMLCompressorFilter,
                          xmlCompressorFilter: XMLCompressorFilter,
                          cSRFFilter: CSRFFilter
                        ) extends HttpFilters {

  override def filters: Seq[EssentialFilter] = Seq(
    htmlCompressorFilter,
    xmlCompressorFilter
  )

}
