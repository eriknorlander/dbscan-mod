package util

import java.io.PrintWriter

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.collection.mutable.ListBuffer

object SaveData {
  def saveSDKTracks(indexing: ListBuffer[Array[Int]], outputpath: String, sc: SparkContext) {
    val fs = FileSystem.get(sc.hadoopConfiguration)
    new PrintWriter(fs.create(new Path(outputpath + "test.csv"))){
      write("start,end,valid\n")

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

  def saveRDDSDKTracks(x: Array[ListBuffer[(Array[String], Long)]], outputpath: String, sc: SparkContext) {
    val fs = FileSystem.get(sc.hadoopConfiguration)
    new PrintWriter(fs.create(new Path(outputpath + "test.csv"))){
      write("start,end,valid\n")

      for (track <- x) {
        write(track.length)
        //  write(track(0)._2 + "," + track(track.length - 1)._2)
      //  write("\n")
      }

      close
    }
  }
}