package DBSCAN

import org.apache.spark.sql.functions.countDistinct
import scala.io.Source
import scala.util.Sorting
import Array._

/**Performs the DBSCAN-algorithm on a given dataset.*/
class DBSCAN(var input : String, var output : String,
                var epsilonMeters : Double, var minimumPoints : Double) {
  this.input = input
  this.output = output

  /**Initializes the operation. Make 3 lists of values from .csv-input.*/
  def run() {
    val ghost = new ReadWriter(input, output)
    var cluster = notNoise(epsilonMeters, minimumPoints, ghost.read())
    ghost.writeJSON(cluster)
  }

  /**Investigates which points are noise and returns them.
  @param epsilon is radius between points to be clustered.
  @param MinPts  is minimum required points within each the radius epsilon
                 to be considered part of the cluster.
  @param X       is input data of X-coordinates.
  @return isnoise which is a logic array.
  */
  def notNoise(epsilonMeters : Double, minPts : Double,
                  data : Array[(Double, Double, String)])
                  : Array[(Double, Double, Double, Double, Double, Double, String, String)] = {

    var C = 0
    val prepData = data.map(x => (x._1, x._2))
    val comp = new Compute(prepData)

    val epsilon = comp.toLatLong(epsilonMeters)
    println(epsilonMeters + " meter to lat-long: " + epsilon)

    val D = comp.euclidean(prepData)
    val n = D.length
    var IDX = Array.fill(n){0}
    var visited = Array.fill(n){0}
    var isnoise = Array.fill(n){0}

    for (i <- 0 to n-1) {
      if (visited(i) == 0) {

        visited(i) = 1

        var neighbours = regionQuery(i)
        if (neighbours.length < minPts) {
          isnoise(i) = 1
        } else {
          C += 1
          expandCluster(i,neighbours,C)
        }
      }
    }

    /**Increase the amount of neighbours included.
    Seems to have roughly a n^3 time complexity. Which is terrible!
    By only looking at distinct parts of temp we get approx n^2. But
    find more clusters! */
    def expandCluster(i : Int, neighbours : Array[Int], C : Int) {
      val t0 = System.nanoTime()
      IDX(i) = C
      var expanded = neighbours

      var k = 0
      while (k < expanded.length-1) {
        var j = expanded(k)
        if (visited(j) == 0) {
          visited(j) = 1
          var neighbours2 = regionQuery(j)
          if (neighbours2.length >= minPts) {
            var temp = expanded ++ neighbours2
            expanded = temp.distinct //distinct or not?
            if (IDX(j) == 0) { //Anders mod
              IDX(j) = C
            }
          }
        }
        //if (IDX(j) == 0) {
        //  IDX(j) = C
        //}
        k += 1
      }
      val t1 = System.nanoTime()
      println("expandCluster " + C + ": "+ (t1 - t0)/math.pow(10,9) + "s")
    }

    /**Finds the amount of neighbours within a radius of epsilon.
        memory optimization by only looking at upper triangle?*/
    def regionQuery(i : Int) : Array[Int] = {
      var indexD = D(i).zipWithIndex
      //var res = D(i).filter((k:Double) => k < epsilon)
      var filterD = indexD.filter{ case (k,_) => k < epsilon }
      return filterD.map(_._2)
    }

   /*Catching bad clusters and recognising them as noise
    val clusterSizes = IDX.groupBy(i=>i).mapValues(_.size)
    val clusterSizesMut = collection.mutable.Map(clusterSizes.toSeq: _*)
    val badClusters = clusterSizesMut.retain((k,v) => v < 2).keys.toArray
    if (badClusters.length > 0) {
      val indexedIDX = IDX.zipWithIndex
      val outliers = indexedIDX.filter{ case (i,_) => badClusters.contains(i)}.map(_._2)
      println("Outliers: " + outliers.deep)
      for(i <- 0 to outliers.length-1){
        IDX(outliers(i)) = 0
        isnoise(outliers(i)) = 1
      }
    }*/

    val stat = new Stats(prepData, IDX)
    val clusterStats = stat.summary()

    println("Statistical summary of clusters: ")
    clusterStats.foreach(line => println(line))

    /** Get start and end date for the cluster. */
    val nbrOfClusters = IDX.distinct.length
    var i = 0
    val dates = data.map(_._3).zip(IDX)
    val begEnd = ofDim[String](nbrOfClusters, 2)

    while(i < nbrOfClusters){
      val datesOfCluster = dates.filter{case (_,k) => k == i}.map(_._1)
      Sorting.quickSort(datesOfCluster)
      begEnd(i)(0) = datesOfCluster(0)
      begEnd(i)(1) = datesOfCluster.last
      i += 1
    }
    println("Statistical summary of clusters: ")
    clusterStats.foreach(line => println(line))
    val begEndTup = begEnd collect { case Array(x:String, y:String) => (x,y)}
    val rtn = clusterStats.zip(begEndTup)
                          .map{case ((x,y,z,a,b,c),(i,j)) => (z,y,z,a,b,c,i,j)}
    return rtn

  }
}
