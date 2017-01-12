package modules.analysis.distances

/**
  * Jensen Shannon Divergence
  *
  * Created by zin on 12.01.17.
  */

object JensenShannon {

  def apply (p: Array[Double], q: Array[Double]): Double = {
    var sumP : Double = 0.0
    var sumQ : Double = 0.0

    for (i <- p.indices) {
      sumP += p(i) * Math.log( (2*p(i))/(p(i)+q(i)))
      sumQ += q(i) * Math.log (2*q(i)/(p(i)+q(i)))
    }
    sumP + sumQ
  }

}
