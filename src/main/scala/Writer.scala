
import java.io._
import scala.io.Source
import java.text.SimpleDateFormat
import java.util.{Calendar, Date}
import org.apache.spark.sql.SparkSession
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.functions.countDistinct
import org.apache.hadoop.fs.{FileSystem, Path}
import scala.io.Source
import Array._
import Structure.{SessionInputFormat, SessionWritable, StructureManager}
import util.{InputOutput, Settings, Time}
import org.apache.hadoop.fs.Path
import DBSCAN._

object Writer {
  val tab = "  "
  val json = new StringBuilder()
  val csv = new StringBuilder()

  def writeJSON(output : String) {
    val pw = new PrintWriter(new File(output))
    pw.write(json.toString)
    pw.close
    json.clear
  }

  /** Formats the input data to JSON. */
  def createJSON(data : Array[Array[String]]) {
    val wifiHead = "{" + "\n" +
                  tab + "\"wifiId\": " + "\"" + data.head.head + "\", \n" +
                  tab + "\"samples\": " + "\"" + data.head(1) + "\", \n" +
                  tab + "\"clusters\": " + "[" + "\n"

    json.append(wifiHead)
    var k = 0
    while(k < data.length) {
      val cluster = new StringBuilder()
      if(data(k)(2) != "") {
        cluster.append(tab + tab + "{" + "\n" +
              tab + tab + tab + "\"lat\": " + "\"" + data(k)(2) + "\", \n" +
              tab + tab + tab + "\"lon\": " + "\"" + data(k)(3) + "\", \n" +
              tab + tab + tab + "\"Sxx\": " + "\"" + data(k)(4) + "\", \n" +
              tab + tab + tab + "\"Syy\": " + "\"" + data(k)(5) + "\", \n" +
              tab + tab + tab + "\"Sxy\": " + "\"" + data(k)(6) + "\", \n" +
              tab + tab + tab + "\"n\": "   + "\"" + data(k)(7) + "\", \n" +
              tab + tab + tab + "\"Str\": " + data(k)(8).toString() + ", \n" +
              tab + tab + tab + "\"End\": " + data(k)(9).toString() + "\n")
        if (k == data.length-1) {
          cluster.append(tab + tab + "}" + "\n")
        } else {
          cluster.append(tab + tab + "}," + "\n")
        }
      }
      json.append(cluster)
      k += 1
    }
    json.append(tab + "]" + "\n" + "}" + "\n")
  }

  def getJSON() : String = {
    return json.toString
  }

  def writeCSVnumerics(output : String) {
    val header = "\"wifiId\",\"samples_total\",\"gpslatitude\",\"gpslongitude\",\"Sxx\",\"Syy\",\"Sxy\",\"number_of_points\"" + "\n"
    val pw = new PrintWriter(new File(output))
    pw.write(header + csv.toString)
    pw.close
    csv.clear
  }

  /** Formats the input data to CSV. */
  def createCSVnumerics(data : Array[Array[String]]) {
    for (i <- 0 to data.length - 1) {
      for (k <- 0 to data(i).length - 3) {
        csv.append(data(i)(k) + ",")
      }
      csv.deleteCharAt(csv.length-1)
      csv.append("\n")
    }
  }

  def writeCSV(output : String) {
    val header = "\"wifiId\",\"samples_total\",\"gpslatitude\",\"gpslongitude\",\"Sxx\",\"Syy\",\"Sxy\",\"number_of_points\",\"Starttime\",\"Endtime\"" + "\n"
    val pw = new PrintWriter(new File(output))
    pw.write(header + csv.toString)
    pw.close
    csv.clear
  }

  /** Formats the input data to CSV. */
  def createCSV(data : Array[Array[String]]) {
    for(i <- 0 to data.length - 1){
      data(i).foreach(line => csv.append(line + ","))
      csv.deleteCharAt(csv.length-1)
      csv.append("\n")
    }
  }

  /** Saves the result of CPS to a /clustered_{ID} folder where ID is the current timestamp */
  def saveResult(path : String, trackModels : RDD[String], sc : SparkContext) {
    val runID = Time.getCurrentTimestamp()
    val output = path + "/clustered_" + runID
    sc.hadoopConfiguration.set("mapreduce.fileoutputcommitter.marksuccessfuljobs", "false")
    sc.hadoopConfiguration.set("parquet.enable.summary-metadata", "false")
    trackModels.saveAsTextFile(output)
    val fs = FileSystem.get(sc.hadoopConfiguration)
    val nbrFiles = Option(new File(output).list).map(_.filter(_.endsWith(".crc")).size).getOrElse(0)
    for (i <- 0 to nbrFiles-1) {
      fs.delete(new Path(output + "/.part-0000" + i.toString + ".crc"), true)
      fs.rename(new Path(output + "/part-0000" + i.toString), new Path(output + "/result_step_" + i.toString + ".json"))
    }
  }
}
