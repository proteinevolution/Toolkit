package models.tools

import enumeratum._
import play.api.data.Form
import play.api.data.Forms._
import shapeless._


sealed trait ToolModel extends EnumEntry {

  val toolNameShort : String
  val toolNameLong : String
  val toolNameAbbreviation : String
  val section : String
  val optional : String
  val inputOptions : HList
  //val minimalForm : Form[]
  //val inputFormAlignment: Form[(String, String, Option[String])]


}

object ToolModel extends PlayEnum[ToolModel] {

  val values = findValues

case object Alnviz extends ToolModel {



  case class Test(id: Int, name: String)

  // --- Names for the Tool ---
  val toolNameShort = "alnviz"
  val toolNameLong = "Alnviz"
  val toolNameAbbreviation = "avz"
  val section = "alignment"
  val optional = ""

  // --- Alnviz specific values ---
  // Input Form Definition of this tool

  val inputOptions = "alignment" -> nonEmptyText :: "alignment_format" -> text :: HNil
  val myTuple = inputOptions.tupled

  //println(myTuple)
  //val test = Form(myTuple) TODO why doesn't this work?

  val inputFormAlignment = Form(
  tuple(
    "alignment" -> nonEmptyText,
    "alignment_format" -> text,
    "text" -> text
  )
  )



  val inputForm = Form(
    tuple(
      "alignment" -> nonEmptyText,
      "alignment_format" -> text
    )
  )

  val resultFileNames = Vector("result")

  // Specifies a finite set of values the parameter is allowed to assumepe
  val parameterValues = Map(
    "alignment_format" -> Set("fas", "clu", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )
}


case object Clans extends ToolModel {

  // --- Names for the Tool ---
  val toolNameShort        = "clans"
  val toolNameLong         = "Clans"
  val toolNameAbbreviation = "clns"
  val section = "classification"
  val optional = ""


  // --- Clans specific values ---
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "alignment" -> nonEmptyText,
      "matrix" -> text,
      "num_iter" -> number,
      "evalue" -> number,
      "standarddb" -> text,
      "psiblastmode" -> boolean,
      "protblastmode" -> boolean,
      "firstevalue" -> number,
      "complexityfilter" -> boolean,
      "ungapped" -> boolean,
      "customid" -> text
    )
  )

  val inputOptions = "alignment" -> nonEmptyText :: "alignment_format" -> text :: HNil

  val parameterValues = Map(
    "matrix" -> Set("BLOSUM62", "BLOSUM45", "BLOSUM80", "PAM30", "PAM70"),
    "alignment_format" -> Set("fas", "clu", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )
}

case object Csblast extends ToolModel {

  // --- Names for the Tool ---
  val toolNameShort        = "csblast"
  val toolNameLong         = "CS-BLAST"
  val toolNameAbbreviation = "cbl"
  val section = "search"
  val optional = ""

  // --- Tcoffee specific values ---
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "alignment" -> nonEmptyText,
      "alignment_format" -> text,
      "matrix" -> text,
      "num_iter" -> number,
      "evalue" -> number,
      "gap_open" -> number,
      "gap_ext" -> number,
      "desc" -> number,
      "standarddb" -> text
    )
  )

  val inputOptions = "alignment" -> nonEmptyText :: "alignment_format" -> text :: HNil

  val parameterValues = Map(
    "matrix" -> Set("BLOSUM62", "BLOSUM45", "BLOSUM80", "PAM30", "PAM70"),
    "alignment_format" -> Set("fas", "clu", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )
}

case object HHblits extends ToolModel{

  // --- Names for the Tool ---
  val toolNameShort        = "hhblits"
  val toolNameLong         = "HHblits"
  val toolNameAbbreviation = "HHBL"
  val section = "search"
  val optional = ""



  // --- HHblits
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "alignment" -> nonEmptyText,
      "alignment_format" -> text,
      "hhblitsdb" -> text
    )
  )

  val inputOptions = "alignment" -> nonEmptyText :: "alignment_format" -> text :: HNil

}

case object HHpred extends ToolModel {


  // --- Names for the Tool ---
  val toolNameShort        = "hhpred"
  val toolNameLong         = "HHpred"
  val toolNameAbbreviation = "HHPR"
  val section = "search"
  val optional = "3ary"


  // --- HHPRED
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "alignment" -> nonEmptyText,
      "alignment_format" -> text,
      "hmmdb" -> text
    )
  )

  val inputOptions = "alignment" -> nonEmptyText :: "alignment_format" -> text :: HNil

}

case object Hmmer3 extends ToolModel {

  // --- Names for the Tool ---
  val toolNameShort        = "hmmer3"
  val toolNameLong         = "Hmmer3"
  val toolNameAbbreviation = "hm3"
  val section = "search"
  val optional = ""




  // --- Hmmer3 specific values ---
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "alignment" -> nonEmptyText,
      "alignment_format" -> text,
      "standarddb" -> text
    )
  )

  val inputOptions = "alignment" -> nonEmptyText :: "alignment_format" -> text :: HNil

}

case object Mafft extends ToolModel {

  // --- Names for the Tool ---
  val toolNameShort        = "mafft"
  val toolNameLong         = "Mafft"
  val toolNameAbbreviation = "mft"
  val section = "alignment"
  val optional = ""

  // --- Mafft specific values ---
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "sequences" -> nonEmptyText,
      "gapopen" -> bigDecimal(5,3),
      "offset" -> bigDecimal(5,3)
    )
  )fill(("",1.53,0.00))

  val inputOptions = "alignment" -> nonEmptyText :: "alignment_format" -> text :: HNil

}

case object Psiblast extends ToolModel {


  // --- Names for the Tool ---
  val toolNameShort       = "psiblast"
  val toolNameLong        = "PSI-BLAST"
  val toolNameAbbreviation = "pbl"
  val section = "search"
  val optional = ""

  // --- Tcoffee specific values ---
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "alignment" -> nonEmptyText,
      "alignment_format" -> text,
      "matrix" -> text,
      "num_iter" -> number,
      "evalue" -> number,
      "gap_open" -> number,
      "gap_ext" -> number,
      "desc" -> number,
      "standarddb" -> text
    )
  ).fill(("", "", "", 1, 10, 11, 1, 200, ""))

  val inputOptions = "alignment" -> nonEmptyText :: "alignment_format" -> text :: HNil

  // TODO Move to TEL
  val parameterValues = Map(
    "matrix" -> Set("BLOSUM62", "BLOSUM45", "BLOSUM80", "PAM30", "PAM70"),
    "alignment_format" -> Set("fas", "clu", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )
}

case object Reformatb extends ToolModel {

  // --- Names for the Tool ---
  val toolNameShort        = "reformatb"
  val toolNameLong         = "Reformatb"
  val toolNameAbbreviation = "form"
  val section = "utils"
  val optional = ""


  // --- Tcoffee specific values ---
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "sequences" -> nonEmptyText,
      "gapopen" -> bigDecimal(5,3),
      "offset" -> bigDecimal(5,3)
    )
  )

  val inputOptions = "alignment" -> nonEmptyText :: "alignment_format" -> text :: HNil

}

case object Tcoffee extends ToolModel {

  // --- Names for the Tool ---
  val toolNameShort = "tcoffee"
  val toolNameLong = "T-Coffee"
  val toolNameAbbreviation = "tcf"
  val section = "alignment"
  val optional = ""


  // --- Tcoffee specific values ---
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "sequences" -> nonEmptyText,
      "mlalign_id_pair" -> boolean,
      "mfast_pair" -> boolean,
      "mslow_pair" -> boolean
    )
  )
  val inputOptions = "alignment" -> nonEmptyText :: "alignment_format" -> text :: HNil

  }
}

