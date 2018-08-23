package util

import java.io.File

import BundlerUtils.AccessPoint
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.collection.immutable.Map
import scala.collection.mutable.ArrayBuffer

object InputOutput {
  /**
    * Gets the number of files in a directory, works both on local regular filesystems and hdfs
    */
  def getNumberFiles(path: String, sc: SparkContext, settings: Map[String, Int]): Int = {
    //if (settings("local") == 1) return new File(path).listFiles.length

    //val fs = FileSystem.get(sc.hadoopConfiguration) // <- this works locally on windows and on hdfs
    //val status = fs.listStatus(file)
    //return status.length

    val file = new Path(path)
    return file.depth()
  }

  /**
    * Saves the result of CPS to a /trackmodel_{ID} folder where ID is the current timestamp
    */
  def saveResult(path: String, trackModels: RDD[String], settings: Map[String, Int]) {
    val runID = Time.getCurrentTimestamp()

    trackModels.saveAsTextFile(path + "/trackmodel_" + runID)
  }

  /**
    * Saves metadata to /metadata_{ID} folder
    */
  def saveMetadata(tracks: RDD[(Array[Array[String]], Map[String, String])], path: String, runID: String) {
    val outTracks = tracks.map(track => formatTrack(track))
    outTracks.saveAsTextFile(path + "/metadata_" + runID)
  }

  /**
    * Saves models to /track_models_{ID} folder
    */
  def saveModels(models: RDD[String], path: String, runID: String) {
    models.saveAsTextFile(path + "/track_models_" + runID)
  }

  /**
    * Formats full track output model with track_id as its header and all metadata map contents below header. Finally
    * it adds all of the scans in the track after metadata contents.
    */
  def formatTrackModel(track: (String, Iterable[(String, Map[String, String], String)])): String = {
    val res = StringBuilder.newBuilder

    res.append("-----------------------------------------------\n")
    res.append("Track ID: " + track._1 + "\n")
    res.append("-----------------------------------------------\n")

    val itr = track._2.toArray

    val metadata = itr(0)._2.collect {
      case (k, v) if k != "track_id" => s"$k: $v"
    }.mkString("\n")

    res.append(metadata + "\n")
    res.append("-----------------------------------------------\n")

    for (scan <- itr) if (!scan._3.equals("null")) res.append(scan._3 + "\n")

    res.append("-----------------------------------------------\n")

    return res.toString()
  }

  /**
    * Formats a track with track_id as its header and all map contents as content below header. Empty line
    * between tracks as separator.
    */
  def formatTrack(track: (Array[Array[String]], Map[String, String])): String = {
    val map = track._2
    val id = map("track_id")
    val content = map.collect {
      case (k, v) if k != "track_id" => s"$k: $v"
    }.mkString("\n")
    s"""
       |-----------------------------------------------
       |Track ID: $id
       |-----------------------------------------------
       |$content
     """.stripMargin
  }

  /**
    * Formats the output for each scan comma separated as: track_id,timestamp,lat,long
    * If scan only had one sample, dismiss the scan
    */
  def formatModel(scan: (Array[Array[String]], Map[String, String], ArrayBuffer[AccessPoint]), pos: (Double, Double)): String = {
    if (pos == null) return "null"
    val id = scan._2("track_id")
    val timestamp = scan._1(0)(2)
    return id + "," + timestamp + "," + pos._1 + "," + pos._2
  }
}
