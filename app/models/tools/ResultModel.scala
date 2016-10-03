package models.tools



import scala.io.Source

/**
 * Created by snam on 03.10.16.
 */
trait ResultModel {

  val meta : String
  val header : String
  val seqs : String


    case class PSIBlast(jobID: String) extends ResultModel {



      val outfile = s"files/$jobID/results/out.psiblastp"

      val test = "Test"

      val meta = ""
      val header = ""
      val seqs = ""


    }



}
