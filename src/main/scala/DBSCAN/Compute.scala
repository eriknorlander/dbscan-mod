package DBSCAN

import org.apache.spark.sql.functions.countDistinct
import scala.io.Source
import scala.util.Sorting
import Array._

class Compute(var data : Array[(Double, Double)]) {
  this.data = data

  /** Computes euclidean distance between 2 points. */
  def distance(lon1 : Double, lat1 : Double, lon2 : Double, lat2 : Double) : Double = {
    val dist = math.sqrt(math.pow(lon2-lon1,2) + math.pow(lat2-lat1,2))
    return dist
  }

  /** Computes the euclidean distance matrix for all points in an array. */
  def euclidean(coord : Array[(Double, Double)]) : Array[Array[Double]] = {
    val n = coord.length
    var euclidean = ofDim[Double](n,n)
    for (i <- 0 to n-1) {
      for (j <- i+1 to n-1) {
        euclidean(i)(j) = distance(coord(i)._1, coord(i)._2, coord(j)._1, coord(j)._2)
        euclidean(j)(i) = euclidean(i)(j)
      }
    }
    val t1 = System.nanoTime()
    return euclidean
  }

  /** Computes the CDF of kdist and return epsilon. */
  def kdistCDF(D : Array[Array[Double]], k : Int, alpha : Double) : Double = {
    D.foreach(x => Sorting.quickSort(x))
    val kdist = getCol(k-1, D)
    Sorting.quickSort(kdist)
    val CDF = kdist.map(x => x/kdist.sum)
                   .map{var s = 0.0; d => { s += d; s} }
    val index = CDF.filter{ case x => (x <= alpha) }.length
    val epsilon = kdist(index)
    return epsilon
  }

  def getCol(n : Int, a : Array[Array[Double]]) =
        a.map{_(n - 1)}.map(x => x.toDouble)

 /** Generally used geo measurement function, slows down things significantly if you
     compute every distance in eucliean this way. */
  def toMeters(lat1 : Double, lon1 : Double, lat2 : Double, lon2 : Double) : Double = {
    var R = 6378.137 // Radius of earth in KM
    var dLat = lat2 * math.Pi / 180 - lat1 * math.Pi / 180
    var dLon = lon2 * math.Pi / 180 - lon1 * math.Pi / 180
    var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
    math.cos(lat1 * math.Pi / 180) * math.cos(lat2 * math.Pi / 180) *
    math.sin(dLon/2) * math.sin(dLon/2)
    var c = 2 * math.atan2(math.sqrt(a), math.sqrt(1-a))
    var d = R * c
    return d * 1000 // meters
  }

  /** Takes two samples from the dataset to figure out how long a meter is in lat long
      at these coordinates and then compares with */
  def toLatLong(radii : Double) : Double = {
    var k = 1
    while(data(0)._1 == data(k)._1 && data(0)._2 == data(k)._2) {
      k += 1
    }
    var measureMeter = toMeters(data(0)._1, data(0)._2, data(k)._1, data(k)._2)
    var measureLatLong = distance(data(0)._1, data(0)._2, data(k)._1, data(k)._2)
    return radii * measureLatLong / measureMeter
  }
}
