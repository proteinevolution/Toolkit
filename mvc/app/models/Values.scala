package models

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
    "clu" -> "CLUSTALW",
    "sto" -> "Stockholm",
    "a2m" -> "A2M",
    "a3m" -> "A3M",
    "emb" -> "EMBL",
    "meg" -> "MEGA",
    "msf" -> "GCG/MSF",
    "pir" -> "PIR/NBRF",
    "tre" -> "TREECON"
  )
}



/*
case object CLU extends AlignmentFormat("clu")
case object STO extends AlignmentFormat("sto")
case object EMB extends AlignmentFormat("emb")
case object GBK extends AlignmentFormat("gbk")
case object MEG extends AlignmentFormat("meg")
case object MSF extends AlignmentFormat("msf")
case object NEX extends AlignmentFormat("nex")
case object PHY extends AlignmentFormat("phy")
case object PIR extends AlignmentFormat("pir")
case object TRE extends AlignmentFormat("tre")

 */