package models

import javax.inject.{Inject, Singleton}

import models.tel.TEL

/**
 *
 * Stores certain attributes to particular Values, like the full descriptive names of the parameter values.
 *
 * Created by lzimmermann on 14.12.15.
 */
@Singleton
class Values @Inject() (tel : TEL) {

  // Maps parameter values onto their full names descriptions, as they should appear in the view
  final val fullNames = Map(

    "fas"  -> "FASTA",
    "clu" -> "CLUSTALW",
    "sto" -> "Stockholm",
    "a2m" -> "A2M",
    "a3m" -> "A3M",
    "emb" -> "EMBL",
    "meg" -> "MEGA",
    "msf" -> "GCG/MSF",
    "pir" -> "PIR/NBRF",
    "tre" -> "TREECON",
    "BLOSUM62" -> "BLOSUM62",
    "BLOSUM45" -> "BLOSUM45",
    "BLOSUM80" -> "BLOSUM80",
    "PAM30" -> "PAM30",
    "PAM70" -> "PAM70"
  )
  final val alignmentFormats = Set("fas", "clu", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  final val standardDBParams = tel.getSetParam("standarddb")
  final val matrixParams = Set("BLOSUM62", "BLOSUM45", "BLOSUM80", "PAM30", "PAM70")
  final val outOrderParams = Set("Input", "Tree", "Gaps")
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