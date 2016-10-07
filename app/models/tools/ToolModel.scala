package models.tools

import enumeratum._
import play.api.data.Form
import play.api.data.Forms._


sealed trait ToolModel extends EnumEntry {

  val toolNameShort : String
  val toolNameLong : String
  val toolNameAbbreviation : String
  val section : String
  val optional : String

}

object ToolModel extends PlayEnum[ToolModel] {

  val values = findValues

  // Related to the parameter specification of the tools
  val parameterGroups : Map[String, Seq[String]] = Map(
    "Alignment" -> List("alignment", "alignment_format", "standarddb")
  )
  val parameterCatchAllName : String = "Parameter"


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

  val parameterValues = Map(
    "matrix" -> Set("BLOSUM62", "BLOSUM45", "BLOSUM80", "PAM30", "PAM70"),
    "alignment_format" -> Set("fas", "clu", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )
}

  case object ClustalOmega extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "clustalomega"
    val toolNameLong = "Clustal Omega"
    val toolNameAbbreviation = "clo"
    val section = "alignment"
    val optional = ""


    // --- Tcoffee specific values ---
    // Returns the Input Form Definition of this tool
    val inputForm = Form(
      tuple(
        "sequences" -> nonEmptyText,
        "otheradvanced" -> text

      )
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

  val parameterValues = Map(
    "matrix" -> Set("BLOSUM62", "BLOSUM45", "BLOSUM80", "PAM30", "PAM70"),
    "alignment_format" -> Set("fas", "clu", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )
}
  case object GLProbs extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "glprobs"
    val toolNameLong = "GLProbs"
    val toolNameAbbreviation = "glp"
    val section = "alignment"
    val optional = ""


    // --- Tcoffee specific values ---
    // Returns the Input Form Definition of this tool
    val inputForm = Form(
      single(
        "sequences" -> nonEmptyText
      )
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

  val parameterValues = Map(
    "matrix" -> Set("BLOSUM62", "BLOSUM45", "BLOSUM80", "PAM30", "PAM70"),
    "alignment_format" -> Set("fas", "clu", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )

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

  val parameterValues = Map(
    "matrix" -> Set("BLOSUM62", "BLOSUM45", "BLOSUM80", "PAM30", "PAM70"),
    "alignment_format" -> Set("fas", "clu", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )

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

  val parameterValues = Map(
    "matrix" -> Set("BLOSUM62", "BLOSUM45", "BLOSUM80", "PAM30", "PAM70"),
    "alignment_format" -> Set("fas", "clu")
  )

}
  case object Kalign extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "kalign"
    val toolNameLong = "Kalign"
    val toolNameAbbreviation = "kal"
    val section = "alignment"
    val optional = ""


    // --- PatSearch specific values ---
    // Returns the Input Form Definition of this tool
    val inputForm = Form(
      tuple(
        "sequences" -> nonEmptyText,
        "outorder" -> text,
        "gapopen" -> bigDecimal,
        "gapextension" -> bigDecimal,
        "termgap" -> bigDecimal,
        "bonusscore" -> bigDecimal
      )
    )fill(("","input", 11.0, 0.85, 0.45, 0.0))

    val parameterValues = Map(
      "outorder" -> Set("Input","Tree", "Gaps")
    )
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
}

  case object Muscle extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "muscle"
    val toolNameLong = "MUSCLE"
    val toolNameAbbreviation = "msc"
    val section = "alignment"
    val optional = ""

    // --- MUSCLE specific values ---
    // Returns the Input Form Definition of this tool
    val inputForm = Form(
      tuple(
        "sequences" -> nonEmptyText,
        "maxrounds" -> number,
        "otheradvanced" -> text
      )
    )fill("",16,"")

  }
  case object PatSearch extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "patsearch"
    val toolNameLong = "PatSearch"
    val toolNameAbbreviation = "pts"
    val section = "search"
    val optional = ""


    // --- PatSearch specific values ---
    // Returns the Input Form Definition of this tool
    val inputForm = Form(
      tuple(
        "inputpattern" ->nonEmptyText,
        "type" -> text,
        "standarddb" -> text
      )
    )

    val parameterValues = Map(
      "type" -> Set("pro","reg")
    )
  }

  case object ProbCons extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "probcons"
    val toolNameLong = "ProbCons"
    val toolNameAbbreviation = "pcns"
    val section = "alignment"
    val optional = ""


    // --- Tcoffee specific values ---
    // Returns the Input Form Definition of this tool
    val inputForm = Form(
      tuple(
        "sequences" -> nonEmptyText,
        "consistency" -> number,
        "itrefine" -> number,
        "pretrain" -> number,
        "otheradvanced" -> text
      )
    )fill("", 2, 100, 0, "")

  }

case object Psiblast extends ToolModel {

  // --- Names for the Tool ---
  val toolNameShort       = "psiblast"
  val toolNameLong        = "PSI-BLAST"
  val toolNameAbbreviation = "pbl"
  val section = "search"
  val optional = ""

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

  // TODO Move to TEL
  val parameterValues = Map(
    "matrix" -> Set("BLOSUM62", "BLOSUM45", "BLOSUM80", "PAM30", "PAM70"),
    "alignment_format" -> Set("fas", "clu", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )
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
}

}

