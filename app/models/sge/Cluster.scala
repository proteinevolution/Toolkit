package models.sge

import javax.inject.{Inject, Singleton}



/**
  * Created by snam on 19.03.17.
  */

@Singleton
final class Cluster @Inject()(qhost : Qhost,
                        qstat : Qstat) {



  case class Load(cpuLoad : Double, memUsed : Double, loadEst : Double)


  def getLoad : Load = {

    // get the infos from the qstat command
    val cluster = qhost.get()

    //compute some kind of load status

    val c = cluster.map(_.load).toArray.sum / cluster.map(_.ncpu).toArray.sum
    val m = cluster.map(_.memtot).toArray.sum / cluster.map(_.memuse).toArray.sum

    val l = math.max(c,m) // take the resource which is more booked out to define the current load


    Load(c, m, l)

  }


}


/*
HOSTNAME                ARCH         NCPU  LOAD  MEMTOT  MEMUSE  SWAPTO  SWAPUS
-------------------------------------------------------------------------------
global                  -               -     -       -       -       -       -
node336                 lx26-amd64     64 25.83  503.9G   16.6G  722.6G  692.0K
node337                 lx26-amd64     64 58.20  503.9G   37.3G  722.6G     0.0
node443                 lx26-amd64     48 50.71  503.3G   52.8G  252.0G     0.0
node444                 lx26-amd64     48 38.03  503.3G   64.1G 1146.1G    3.0M
node445                 lx26-amd64     48 50.71  503.9G   16.9G 1146.1G     0.0
node446                 lx26-amd64     48 45.34  503.9G   32.8G 1453.1G     0.0
node448                 lx26-amd64     48 52.25  503.9G   20.1G  252.0G    1.3M
node455                 lx26-amd64     64 37.45  503.9G   40.3G  722.6G    9.1M
node501                 lx26-amd64     64 60.20  503.9G   32.0G  722.6G     0.0
node502                 lx26-amd64     64 31.44  503.9G   14.6G  624.6G     0.0
node503                 lx26-amd64     64     -  503.9G       -  624.6G       -
node504                 lx26-amd64     64  0.06  995.5G    4.4G  498.0G     0.0
node505                 lx26-amd64     64 58.45  995.5G   42.3G  498.0G     0.0
node506                 lx26-amd64     64 49.42  503.9G   71.8G  624.6G   16.5M
node507                 lx26-amd64     64     -  503.9G       -  624.6G       -
node508                 lx26-amd64     64 35.07  503.9G   65.0G  722.6G     0.0
node509                 lx26-amd64     64 58.12  995.5G   41.6G  498.0G   23.3M
node510                 lx26-amd64     64 65.04  976.4G   17.1G  489.0G     0.0
node511                 lx26-amd64     64  0.08  995.5G    2.3G  498.0G   13.8M
node512                 lx26-amd64     64 26.20  503.9G    8.6G  722.6G     0.0
node513                 lx26-amd64     64 77.72  503.9G   69.5G  722.6G     0.0

 */