import scala.io.Source
import java.io._

object psiblastpPostProcess {
  def main(args: Array[String]): Unit = {
    val outfile = args(0)
    var index = 0
    var index2 =0
    val file = new File(outfile + "_processed")
    val bw = new BufferedWriter(new FileWriter(file))
    for (line <- Source.fromFile(outfile).getLines()) {

      // adds div to references of PSIBLAST
      // output in order to be able to hide id

      if(line.startsWith("<b>PSIBLAST 2.3.0+</b>")){
        bw.newLine()
        bw.write("<div id='metaReferences'>" + line)
        bw.newLine()

      } else if(line.startsWith("Sequences producing significant alignments: ")){
        bw.write(line)
        bw.newLine()
        bw.write("</div>")

        // adds checkboxes to the hits overview

      } else if (line.startsWith("<a title=")) {
        bw.write(s"<input type='checkbox' style='margin: 9px; padding: 9px;'  class='hits' value= '$index'>$line")
        bw.newLine()
        index = index + 1


        // adds checkboxes to the alignments
        // (including the respective value 'index' from overview checkboxes)


      } else if (line.startsWith("><a title=")) {
        bw.write(s"Number: $index2")
        bw.newLine()
        bw.write(s"<input type='checkbox' style='margin: 5px; padding: 5px;'  class='hits' value= '$index2'>$line")
        bw.newLine()
        index2 = index2 + 1
      } else if(line.startsWith("Window for multiple hits")) {
        bw.newLine()
      }else {
        bw.write(line)
        bw.newLine()
      }
    }
    bw.close()
  }
}

