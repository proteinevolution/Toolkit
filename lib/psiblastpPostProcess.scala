import scala.io.Source
import java.io._

object psiblastpPostProcess {
  def main(args: Array[String]): Unit = {
    val outfile = args(0)
    var index = 0
    var index2 =0
    var database = ""
    var scoreE = ""
    var pattern = """[0-9]><\/a>(.*)\s*""".r
    var stopParsing = false
    val file = new File(outfile + "_overview")
    val bw = new BufferedWriter(new FileWriter(file))
    for (line <- Source.fromFile(outfile).getLines()) {
      // adds div to references of PSIBLAST
      // output in order to be able to hide id
      if(!stopParsing) {
        if (line.startsWith("<b>PSIBLAST")) {
          bw.newLine()
          bw.write("<div id='metaReferences'>" + line)
        }else if(line.startsWith("Database")){
          database = line
        }
        else if (line.matches("\\s*Score\\s*E\\s*")) {
          scoreE = "        " + line
        }
        else if (line.startsWith("Sequences producing significant alignments: ")) {
          bw.write("</div>")
          bw.write(database)
          bw.newLine()
          bw.write(scoreE)
          bw.newLine()
          bw.write("         " + line)
          bw.newLine()
        }
        else if (line.startsWith("<a title=")) {
          // adds checkboxes to the hits overview
          var indexadd = index + 1
          bw.write(s"<input type='checkbox' style='margin: 9px; padding: 9px;'  class='hits' value= '$index'> $indexadd <b>$line</b>")
          bw.newLine()
          index = index + 1
          // adds checkboxes to the alignments
          // (including the respective value 'index' from overview checkboxes)
        }
        else if (line.startsWith("><a title=")) {
          stopParsing = true
        }
        else {
          bw.write(line)
          bw.newLine()
        }
      }
    }
    bw.write("</PRE>\n</BODY>\n</HTML>")
    bw.close()

    var startParsing = false
    val file2 = new File(outfile + "_alignment")
    val bw2 = new BufferedWriter(new FileWriter(file2))
    bw2.write("<PRE>")
    bw2.newLine()
    for (line <- Source.fromFile(outfile).getLines()) {
      if (line.startsWith("><a title=")) {
        startParsing = true
      }
      if (startParsing) {
        if (line.startsWith("><a title=")) {
          var index2add = index2 + 1
	  bw2.write(s"<hr>")
          bw2.write(s"Number: $index2add")
          bw2.newLine()
          bw2.write(s"<input type='checkbox' style='margin: 5px; padding: 5px;'  class='hits' value= '$index2'><b>$line</b>")
          bw2.newLine()
          index2 = index2 + 1
        } else if (line.startsWith("Window for multiple hits")) {
          bw2.write(line)
          bw2.newLine()
        } else {
          bw2.write(line)
          bw2.newLine()
        }
      }
    }
    bw2.newLine()
    bw2.write("</PRE>")
    bw2.close()
  }
}

