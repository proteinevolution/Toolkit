package models.results

/**
 */
import better.files._
import models.Constants
import play.twirl.api.Html


object HHpred extends Constants {

  /**
    * Renders file content as plain HTML. Can be used for scripts that produce HTML from the old Toolkit
    * @param filepath
    * @return
    */
  def html(filepath: String): Html = {
    Html(s"$jobPath/$filepath".toFile.contentAsString)
  }
}
