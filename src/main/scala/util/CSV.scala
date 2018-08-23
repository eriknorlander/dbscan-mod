package util

import java.io.PrintWriter

import org.apache.spark.SparkContext
import org.apache.hadoop.fs.{FileSystem, Path}

import scala.collection.mutable.ArrayBuffer
import scala.io

object CSV {
  /** TODO: Temp, rename to load for production */
  def loadT(filepath: String, sc: SparkContext): ArrayBuffer[Array[String]] = {
      var data = ArrayBuffer[Array[String]]()
      val fs = FileSystem.get(sc.hadoopConfiguration)
      val stream = fs.open(new Path(filepath))
      var line = stream.readLine()
      while (line != null) {
        val row = line.split(",").map(_.trim)
         if (!row(1).contains("learningwifi")) data += row
        line = stream.readLine()
      }
      stream.close()

      return data.slice(1,data.length-1) //skip header
  }

  def load(filepath: String, sc: SparkContext): ArrayBuffer[Array[String]] = {
    var data = ArrayBuffer[Array[String]]()
    val bufferedSource = io.Source.fromFile(filepath)
    for (line <- bufferedSource.getLines) {
      data += line.split(",").map(_.trim)
    }
    bufferedSource.close
    return data.slice(1,data.length-1) //skip header
  }

  def saveLocalSDKTracks(indexing: Array[Array[Int]], columns: String, path: String): Unit = {
    new PrintWriter(path) {
      write(columns + "\n")

      for (entry <- indexing) {
        for (num <- entry) {
          write(num + "");
          if (num != entry(2)) write(",");
        }
        write("\n")
      }

      close
    }
  }
}

class SimpleCSVHeader(header:Array[String]) extends Serializable {
  val index = header.zipWithIndex.toMap
  def apply(array:Array[String], key:String):String = array(index(key))
}