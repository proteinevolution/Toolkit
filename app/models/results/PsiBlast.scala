package models.results

/**
 *
 * Created by snam on 18.10.16.
 *
 * Tool model for parsing result files
 *
 */


import models.Constants
import scala.io.Source



object PsiBlast extends Constants {



  def evalues(mainID:String) = {

    // Convention over configuration! No need to configure the result files in some models or to pass the job object to the view

    val path = s"$jobPath$mainID/results/evalues.dat"

    val lines = Source.fromFile(path).getLines().toList

    lines

  }




}
