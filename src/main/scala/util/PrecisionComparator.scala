package util

  import org.apache.spark.rdd.RDD
  import scala.collection.mutable.ArrayBuffer
  import scala.io

  object PrecisionComparator {
    def compare(in: RDD[(String, Double, Double)]): Unit = {
      val data = in.collect()
      val ref = readRef()

      val distances = ArrayBuffer[Double]()
      var max = 0.0
      var min = 999999999.0
      var sum = 0.0

      for (scan <- data) {
        if (!scan._1.equals("null") && scan._2 != 0.0 && scan._3 != 0.0) {
          val timestamp = scan._1.substring(scan._1.indexOf(' '), scan._1.length)
          val pos1 = (scan._2, scan._3)
          var pos2 = (0.0, 0.0)

          var found = false
          var i = 0
          while (!found && i < ref.length) {
            if (ref(i)._1.equals(timestamp)) {
              pos2 = (ref(i)._2, ref(i)._3)
              found = true
            }
            i += 1
          }

          val dist = calcDistance(pos1, pos2)

          distances += dist
          sum += dist
          if (dist > max) max = dist
          if (dist < min) min = dist
        }

      }

      println()
      println ("-- Distances:")
      distances.foreach(d => println(d))

      println()
      println("-- Average: " + (sum/distances.length))
      println("-- Median: " + Maths.median(distances.toArray))
      println("-- Max: " + max)
      println("-- Min: " + min)

      calcDistribution(distances)

      println()
      val lessthan100 = distances.filter(x => x < 100.0)
      println("-- Median when removing all above 100: " + Maths.median(lessthan100.toArray))
      val lessthan160 = distances.filter(x => x < 160.0)
      println("-- Median when removing all above 160: " + Maths.median(lessthan160.toArray))
      val lessthan200 = distances.filter(x => x < 200.0)
      println("-- Median when removing all above 200: " + Maths.median(lessthan200.toArray))
      val lessthan300 = distances.filter(x => x < 300.0)
      println("-- Median when removing all above 300: " + Maths.median(lessthan300.toArray))
    }

    def calcDistribution(dist: ArrayBuffer[Double]): Unit = {
      var below20 = 0
      var below40 = 0
      var below60 = 0
      var below80 = 0
      var below100 = 0
      var below120 = 0
      var below140 = 0
      var below160 = 0
      var below180 = 0
      var below200 = 0
      var below220 = 0
      var below240 = 0
      var below260 = 0
      var below280 = 0
      var below300 = 0
      var below320 = 0
      var below340 = 0
      var below360 = 0
      var below380 = 0
      var below400 = 0
      var above400 = 0


      for (num <- dist) {
        if (num < 20.0) below20 +=1
        else if (num < 40.0) below40 +=1
        else if (num < 60.0) below60 +=1
        else if (num < 80.0) below80 +=1
        else if (num < 100.0) below100 +=1
        else if (num < 120.0) below120 +=1
        else if (num < 140.0) below140 +=1
        else if (num < 160.0) below160 +=1
        else if (num < 180.0) below180 +=1
        else if (num < 200.0) below200 +=1
        else if (num < 220.0) below220 +=1
        else if (num < 240.0) below240 +=1
        else if (num < 260.0) below260 +=1
        else if (num < 280.0) below280 +=1
        else if (num < 300.0) below300 +=1
        else if (num < 320.0) below320 +=1
        else if (num < 340.0) below340 +=1
        else if (num < 360.0) below360 +=1
        else if (num < 380.0) below380 +=1
        else if (num < 400.0) below400 +=1
        else if (num >= 400.0) above400 +=1
      }

      println()
      println("0.0 < x < 20.0\t" + below20)
      println("20.0 < x < 40.0\t" + below40)
      println("40.0 < x < 60.0\t" + below60)
      println("60.0 < x < 80.0\t" + below80)
      println("80.0 < x < 100.0\t" + below100)
      println("100.0 < x < 120.0\t" + below120)
      println("120.0 < x < 140.0\t" + below140)
      println("140.0 < x < 160.0\t" + below160)
      println("160.0 < x < 180.0\t" + below180)
      println("180.0 < x < 200.0\t" + below200)
      println("200.0 < x < 220.0\t" + below220)
      println("220.0 < x < 240.0\t" + below240)
      println("240.0 < x < 260.0\t" + below260)
      println("260.0 < x < 280.0\t" + below280)
      println("280.0 < x < 300.0\t" + below300)
      println("300.0 < x < 320.0\t" + below320)
      println("320.0 < x < 340.0\t" + below340)
      println("340.0 < x < 360.0\t" + below360)
      println("360.0 < x < 380.0\t" + below380)
      println("380.0 < x < 400.0\t" + below400)
      println("400.0 < x\t\t" + above400)
    }

  def readRef(): ArrayBuffer[(String, Double, Double)] = {
    val path = """C:\Users\Oskar\Documents\Skola\Examensarbete\resultForOskar.csv"""

    var data = ArrayBuffer[(String, Double, Double)]()
    val bufferedSource = io.Source.fromFile(path)
    for (line <- bufferedSource.getLines) {
      val l = line.split(",").map(_.trim)
      val timestamp = l(0).substring(l(0).indexOf(' '), l(0).length)

      data += ((timestamp, l(1).toDouble, l(2).toDouble))
    }
    bufferedSource.close

    return data
  }

  def calcDistance(pos1: (Double, Double), pos2: (Double, Double)): Double = {
    val R = 6378100

    val lat1 = Math.toRadians(pos1._1)
    val long1 = Math.toRadians(pos1._2)
    val lat2 = Math.toRadians(pos2._1)
    val long2 = Math.toRadians(pos2._2)

    val rho1 = R * Math.cos(lat1)
    val z1 = R * Math.sin(lat1)
    val xx1 = rho1 * Math.cos(long1)
    val y1 = rho1 * Math.sin(long1)

    val rho2 = R * Math.cos(lat2)
    val z2 = R * Math.sin(lat2)
    val xx2 = rho2 * Math.cos(long2)
    val y2 = rho2 * Math.sin(long2)

    val dot = xx1 * xx2 + y1 * y2 + z1 * z2
    val cos_theta = dot/Math.pow(R, 2)

    return R * Math.acos(cos_theta)
  }
}
