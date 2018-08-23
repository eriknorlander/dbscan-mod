package DBSCAN

import java.io._
import scala.io.Source
import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

class ReadWriter(var input : String, var output : String) {

  this.input = input
  this.output = output

  /** Reads a csv file from String input and returns a tuple-array of with
    (longitude, latitude).
  */
  def read() : Array[(Double, Double, String)] = {
    Source.fromFile(input)
      .getLines().drop(1)
      .map(x => (x.split(",")(0).toDouble, x.split(",")(1).toDouble,
                 x.split(",")(2).toString))
      .toArray
  }

  /** Writes a csv file from  Array[(Double,Double)] data to the directory output
  */
  def writeCSV(data : Array[(Double,Double)]) {
    val pw = new PrintWriter(new File(output))
    data.foreach(line => pw.write(line.toString()
      .filterNot(c => c == '(' || c == ')')  + "\n"))
    pw.close
  }

  /** Writes a json file from  Array[(Double,Double)] data to the directory output
  we want: mean, std, number of points, number of clusters, time for each cluster.
  */
  def writeJSON(clusters: Array[(Double, Double, Double, Double, Double, Double, String, String)]) = {
    val pw = new PrintWriter(new File(output))
    val tab = "  "
    var k = 0
    val length = clusters.length
    while(k < length) {
      var json = "{" + "\n" +
                tab + "\"lat\": " + "\"" + clusters(k)._1.toString() + "\", \n" +
                tab + "\"lon\": " + "\"" + clusters(k)._2.toString() + "\", \n" +
                tab + "\"Sxx\": " + "\"" + clusters(k)._3.toString() + "\", \n" +
                tab + "\"Syy\": " + "\"" + clusters(k)._4.toString() + "\", \n" +
                tab + "\"Sxy\": " + "\"" + clusters(k)._5.toString() + "\", \n" +
                tab + "\"n\": "   + "\"" + clusters(k)._6.toInt.toString() + "\", \n" +
                tab + "\"Str\": " + clusters(k)._7.toString() + ", \n" +
                tab + "\"End\": " + clusters(k)._8.toString() + "\n" +
                "}" + "\n"
                pw.write(json)
                k += 1
    }
    pw.close
  }
}
