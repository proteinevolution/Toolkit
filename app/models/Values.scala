package models

import models.tools.Alnviz

/**
 *
 * Stores certain attributes to particular Values, like the full descriptive names of the parameter values.
 *
 * Created by lzimmermann on 14.12.15.
 */
object Values {

  // Maps parameter values onto their full names descriptions
  val fullNames = Map(

    "fas"  -> "FASTA",
    "clus" -> "CLUSTALW",
    "sto" -> "Stockholm",
    "a2m" -> "A2M",
    "a3m" -> "A3M",
    "emb" -> "EMBL",
    "meg" -> "MEGA",
    "msf" -> "GCG/MSF",
    "pir" -> "PIR/NBRF",
    "tre" -> "TREECON"
  )

  val modelMap : Map[String, ToolModel] = Map(
    "alnviz" -> Alnviz
  )

}
