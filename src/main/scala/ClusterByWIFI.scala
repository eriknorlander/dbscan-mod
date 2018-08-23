import util.Time

import scala.collection.immutable.Map
import scala.collection.mutable.ArrayBuffer
import scala.collection.immutable.ListMap
import scala.util.Sorting
import DBSCAN._

/** Groups the data by wifiId. */
object ClusterByWIFI {

  def run(track: (Array[Array[String]], Map[String, String]),
      minPts : String, epsilon : String): ArrayBuffer[Array[Array[String]]] = {
    val wifiCol = ArrayBuffer(getCol(1,track._1)).remove(0)
    val distincts = wifiCol.distinct.map(_.toInt)
    val sizeMap = ListMap(wifiCol.map(_.toInt)
          .groupBy(i => i)
          .mapValues(_.size)
          .toSeq.sortBy(_._1):_*)
    var splitData = ArrayBuffer[Array[Array[String]]]()
    val cumsum_0 = wifiCol.map(_.toInt)
          .groupBy(i => i)
          .mapValues(_.size)
          .toSeq
          .sortBy(_._1)
          .map(_._2)
          .scanLeft(0)(_+_)
          .tail
    val cumsum = 0 +: cumsum_0

    /** Splits the data on wifiId. */
    var i = 1
    while(i < cumsum.length) {
      splitData += track._1.slice(cumsum(i-1), cumsum(i))
      i += 1
    }

    /** Performing DBSCAN on all the different wifis seperately. */
    i = 0
    var clusters = ArrayBuffer[Array[Array[String]]]()
    while(i < sizeMap.size) {
      val tupled = tuplify(2, 3, 4, splitData(i))
      val dbscan = new DBSCAN_MOD(tupled, minPts.toInt, epsilon)
      val clust = dbscan.run()
      //val classified = classify(sizeMap(distincts(i)), clust)

      var clustBuff = ArrayBuffer[Array[String]]()

      if(clust.length != 0) {
        for(k <- 0 to clust.length-1) {
          val temp = sizeMap(distincts(i)).toString +: clust(k).toVector
          val temp1 = distincts(i).toString +: temp
          clustBuff += temp1.toArray
        }
      } else {
        clustBuff += Array(distincts(i).toString, sizeMap(distincts(i)).toString, "", "", "", "", "", "", "", "")
      }
      clusters += clustBuff.toArray
      i += 1
    }
    return clusters
  }

  def getCol(n : Int, a : Array[Array[String]]) =
        a.map{_(n - 1)}.map(x => x.toDouble)

  def getColToString(n : Int, a : Array[Array[String]]) =
        a.map{_(n - 1)}.map(x => x.toString)

  def tuplify(a : Int, b : Int, c : Int, A : Array[Array[String]])
      : Array[(Double, Double, String)] = {
    val lat = getCol(a, A)
    val lon = getCol(b, A)
    val dat = getColToString(c, A)
    val T = lat.zip(lon).zip(dat).map{ case ((x,y),z) => (x,y,z)}
    return T
  }
}

/**
  def tuplifyLarge(A : Array[Array[String]]) : Array[(Double, Double, Double,
    Double, Double, Double, String, String)] = {
    val T = A.map{ case Array(a,b,c,d,e,f,g,h) => (a.toDouble,b.toDouble,c.toDouble,d.toDouble,e.toDouble,f.toDouble,g,h)}
    return T
  }
  def classify(size : Int, clust : Array[Array[String]]) : Int = {
    Check noise level.
    val clusters = tuplifyLarge(clust)
    val noise = 1 - clusters.map(_._6).sum/size.toDouble
    if (noise > 0.5) {
      return 1
    }

    Check if more than 1 cluster.
    if (clusters.length > 1) {

      Check for overlapping clusters in time.
      val first = clusters.map(x => x._7)
      val last = clusters.map(x => x._8)
      Sorting.quickSort(first)
      Sorting.quickSort(last)

      val first = dates.sortBy(_._1)(0)
      val last = dates.sortBy(_._2).last
      if(first._3 != last._3) {
        for(i <- 0 to clusters.length - 1) {
          if(first._2 < clusters(i)._1 || last._1 < clustrers(i)._2) {
            return 2
          }
        }
      }

  */
