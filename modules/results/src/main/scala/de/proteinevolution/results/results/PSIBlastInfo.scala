package de.proteinevolution.results.results

case class PSIBlastInfo(
    db_num: Int,
    db_len: Int,
    hsp_len: Int,
    iter_num: Int,
    eval: Double = -1
) extends SearchToolInfo
