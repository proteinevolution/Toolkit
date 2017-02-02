/**
  * Created by snam on 01.02.17.
  */

import javax.inject.Inject
import play.filters.csrf.CSRFFilter
import play.api.http.DefaultHttpFilters
import play.filters.gzip.GzipFilter

final class Filters @Inject() (
                          gzip: GzipFilter,
                          cSRFFilter: CSRFFilter
                        ) extends DefaultHttpFilters(gzip)
