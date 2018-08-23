package DBSCAN

import org.apache.spark.sql.functions.countDistinct
import org.apache.spark.mllib.linalg.Matrix
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.mllib.stat.MultivariateStatisticalSummary
import scala.io.Source
import scala.util.Sorting
import Array._

/** A class that assumes normal distribution of the clusters.
    We all <3 Gaussians.
*/
class Stats(var data : Array[(Double, Double)], var IDX : Array[Int]) {
  this.data = data
  this.IDX = IDX

  /** Computes the means of all clusters, approximating the midpoint. */
  def summary() : Array[(Double, Double, Double, Double, Double, Double)] = {
    val distClusters = IDX.distinct.filterNot{case i => i == 0}
    val nbrOfClusters = distClusters.length
    //println(nbrOfClusters)
    val C = data.zip(IDX)
    var i = 0
    var M = ofDim[Double](nbrOfClusters, 6)
    while(i < nbrOfClusters) {
      val cluster = C.filter{ case ((_,_),k) => k == distClusters(i)}.map(_._1)

      M(i)(0) = cluster.map(_._1).sum/cluster.length
      M(i)(1) = cluster.map(_._2).sum/cluster.length
      val Sxx = cluster.map(_._1)
                      .map(x => x - M(i)(0))
                      .map(x => Math.pow(x,2))
                      .sum
      val Syy = cluster.map(_._2)
                      .map(y => y - M(i)(1))
                      .map(y => Math.pow(y,2))
                      .sum
      val Sxy = cluster.map{ case (x: Double,y: Double)
                             => (x - M(i)(0),y - M(i)(1)) }
                      .map{ case (x: Double, y: Double) => (x*y)}
                      .sum
      M(i)(2) = Sxx/cluster.length
      M(i)(3) = Syy/cluster.length
      M(i)(4) = Sxy/cluster.length
      M(i)(5) = cluster.length
            //println(Sxx.toString + " " + Syy.toString + " " + Sxy.toString)
      i += 1
    }
    val info = M collect {case Array(x: Double, y: Double, z: Double, a: Double, b: Double, c: Double)
                  => (x,y,z,a,b,c)}
    return info
  }
}
