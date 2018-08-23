package DBSCAN

import org.apache.spark.sql.functions.countDistinct
import scala.io.Source
import scala.util.Sorting
import scala.math.BigDecimal
import Array._

class DBSCAN_MOD(data : Array[(Double, Double, String)],
                  var minimumPoints : Int, var epsilonMeters : String) {

  /** Initializes the operation. Make 3 lists of values from .csv-input. */
  def run() : Array[Array[String]] = {
      var cluster = notNoise(minimumPoints, epsilonMeters, data)
      var stringified = cluster.map{ case (a,b,c,d,e,f,g,h)
      => Array(a.toString, b.toString, c.toString, d.toString, e.toString, f.toInt.toString, g, h) }
      return stringified
  }

  /** Investigates which points are noise and returns them.
  @param alpha   is the alpha-quantile of the k-dist CDF you want to use.
  @param minPts  is minimum required points within each the radius epsilon
                 to be considered part of the cluster.
  @param data    is input data.
  @return rtn    which is a logic array.
  */
  def notNoise(minimumPoints : Int, epsilonMeters : String, data : Array[(Double, Double, String)])
    : Array[(Double, Double, Double, Double, Double, Double, String, String)] = {

    val prepData = data.map(x => (x._1, x._2))
    val comp = new Compute(prepData)

    var C = 1
    val D = comp.euclidean(prepData)
    val n = D.length
    var IDX = Array.fill(n){0}

    val minPts = minimumPoints
    val epsilon = epsilonMeters.toDouble * Math.pow(10,-5)
    //val epsilon = comp.toLatLong(epsilonMeters)

    for (i <- 0 to n-1) {
      if (IDX(i) == 0) {
        var neighbours = regionQuery(i)
        if ((neighbours.length - neighbours.map(IDX)
                            .filter{case (a) => a!=0}.length) >= minPts) {
          expandCluster(i,neighbours,C)
          C += 1
        }
      }
    }

    /** Increase the amount of neighbours included. */
    def expandCluster(i : Int, neighbours : Array[Int], C : Int) {
      IDX(i) = C
      var expanded = neighbours
      var k = 0
      while (k < expanded.length) {
        var j = expanded(k)
        var neighbours2 = regionQuery(j)
        var A = neighbours2.map(IDX).filter{ case (a) => a == 0 || a == C }
        var logical = neighbours2.map(IDX) diff A
        if (neighbours2.length - logical.length >= minPts) {
          var temp = expanded ++ neighbours2
          expanded = temp.distinct
        }
        if (IDX(j) == 0) {
          IDX(j) = C
        }
        k += 1
      }
      val t1 = System.nanoTime()
    }

    /** Finds the amount of neighbours within a radius of epsilon.
        memory optimization by only looking at upper triangle? */
    def regionQuery(i : Int) : Array[Int] = {
      var indexD = D(i).zipWithIndex
      var filterD = indexD.filter{ case (k,_) => k < epsilon }
      return filterD.map(_._2)
    }

    val stat = new Stats(prepData, IDX)
    val clusterStats = stat.summary()

    /** Get start and end date for the cluster. */
    val nbrOfClusters = IDX.distinct.length
    val dates = data.map(_._3).zip(IDX)
    val begEnd = ofDim[String](nbrOfClusters, 2)

    var i = 0
    while(i < nbrOfClusters) {
      val datesOfCluster = dates.filter{ case (_,k) => k == i }.map(_._1)
      Sorting.quickSort(datesOfCluster)
      if (datesOfCluster.length > 0) {
        begEnd(i)(0) = datesOfCluster(0)
        begEnd(i)(1) = datesOfCluster.last
      }
      i += 1
    }

    val begEndTup = begEnd collect { case Array(x: String, y: String) => (x,y) }
    val rtn = clusterStats.zip(begEndTup)
                  .map{ case ((a,b,c,d,e,f),(g,h)) => (a,b,c,d,e,f,g,h) }
    return rtn
  }
}
