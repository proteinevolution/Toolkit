package models.tools

import play.api.data.Form
import play.api.data.Forms._
import shapeless._



sealed trait ToolModel {

val toolNameShort: String
val toolNameLong: String
val toolNameAbbreviation: String

}

case object Alnviz extends ToolModel {


  // --- Names for the Tool ---
  val toolNameShort: String = "alnviz"
  val toolNameLong: String = "Alnviz"
  val toolNameAbbreviation: String = "avz"

  // --- Alnviz specific values ---
  // Input Form Definition of this tool

  val hlist = "alignment" -> nonEmptyText :: "alignment_format" -> text :: HNil
  val myTuple = hlist.tupled

  //println(myTuple)
  //val test = Form(myTuple) TODO why doesn't this work?

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
  val toolNameShort:String        = "clans"
  val toolNameLong:String         = "Clans"
  val toolNameAbbreviation:String = "clns"


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
  val parameterValues = Map(
    "matrix" -> Set("BLOSUM62", "BLOSUM45", "BLOSUM80", "PAM30", "PAM70"),
    "alignment_format" -> Set("fas", "clu", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )
}

case object Csblast extends ToolModel {

  // --- Names for the Tool ---
  val toolNameShort:String        = "csblast"
  val toolNameLong:String         = "CS-BLAST"
  val toolNameAbbreviation:String = "cbl"

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

  val parameterValues = Map(
    "matrix" -> Set("BLOSUM62", "BLOSUM45", "BLOSUM80", "PAM30", "PAM70"),
    "alignment_format" -> Set("fas", "clu", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )
}

case object HHblits extends ToolModel{

  // --- Names for the Tool ---
  val toolNameShort:String        = "hhblits"
  val toolNameLong:String         = "hhblits"
  val toolNameAbbreviation:String = "HHBL"


  // --- HHblits
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "alignment" -> nonEmptyText,
      "alignment_format" -> text,
      "hhblitsdb" -> text
    )
  )
}

case object HHpred extends ToolModel {


  // --- Names for the Tool ---
  val toolNameShort:String        = "hhpred"
  val toolNameLong:String         = "HHpred"
  val toolNameAbbreviation:String = "HHPR"


  // --- HHPRED
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "alignment" -> nonEmptyText,
      "alignment_format" -> text,
      "hmmdb" -> text
    )
  )
}

case object Hmmer3 extends ToolModel {

  // --- Names for the Tool ---
  val toolNameShort:String        = "hmmer3"
  val toolNameLong:String         = "Hmmer3"
  val toolNameAbbreviation:String = "hm3"




  // --- Hmmer3 specific values ---
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "alignment" -> nonEmptyText,
      "alignment_format" -> text,
      "standarddb" -> text
    )
  )
}

case object Mafft extends ToolModel {

  // --- Names for the Tool ---
  val toolNameShort:String        = "mafft"
  val toolNameLong:String         = "Mafft"
  val toolNameAbbreviation:String = "mft"


  // --- Tcoffee specific values ---
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "sequences" -> nonEmptyText,
      "gapopen" -> bigDecimal(5,3),
      "offset" -> bigDecimal(5,3)
    )
  )
}

case object Psiblast extends ToolModel {


  // --- Names for the Tool ---
  val toolNameShort:String        = "psiblast"
  val toolNameLong:String         = "PSI-BLAST"
  val toolNameAbbreviation:String = "pbl"


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
  // TODO Move to TEL
  val parameterValues = Map(
    "matrix" -> Set("BLOSUM62", "BLOSUM45", "BLOSUM80", "PAM30", "PAM70"),
    "alignment_format" -> Set("fas", "clu", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )
}

case object Reformatb extends ToolModel {

  // --- Names for the Tool ---
  val toolNameShort:String        = "reformatb"
  val toolNameLong:String         = "Reformatb"
  val toolNameAbbreviation:String = "form"


  // --- Tcoffee specific values ---
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "sequences" -> nonEmptyText,
      "gapopen" -> bigDecimal(5,3),
      "offset" -> bigDecimal(5,3)
    )
  )
}

case object Tcoffee extends ToolModel {

  // --- Names for the Tool ---
  val toolNameShort:String        = "tcoffee"
  val toolNameLong:String         = "T-Coffee"
  val toolNameAbbreviation:String = "tcf"


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
}

