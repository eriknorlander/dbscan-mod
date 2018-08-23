/* SimpleApp.scala */
import org.apache.spark.sql.SparkSession
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.functions.countDistinct
//import util.{InputOutput, Settings}
import scala.io.Source
import Array._
import Structure.{SessionInputFormat, SessionWritable, StructureManager}
import util.{InputOutput, Settings}
import org.apache.hadoop.fs.Path
import DBSCAN._

object Distributer {
  def main(args: Array[String]) = {

    val input_filepath = "input/swe_data_huge.csv"
    val output_filepath = "output/result.csv"

    val settings = Settings.init()

    val conf = new SparkConf().setAppName("Distributer").setMaster("local[2]")
    val sc = new SparkContext(conf)
    //val data = sc.textFile("test2.csv")

    if (settings("process_existing_files") == 1 && InputOutput.getNumberFiles(input_filepath, sc, settings) > 0) {
      val data = StructureManager.prepareRDD(input_filepath, sc, settings)
    }

    val data = StructureManager.prepareRDD(input_filepath, sc, settings)

    /** Group the wifis by wifiId. */
    val groupedRDD = data.map(track => GroupByWIFI.run(track)).flatMap(track => track)

    /** Compute the mean of all wifis in the set. */
    //val meanRDD = data.map(track => Mean.run(track, groupedRDD.collect()))
    //val testRDD = data.map(track => Mean.test(track))
    //A.foreach(line => println("\n \n \n" + "HERE ARE THE DAMN MEANS!" + "\n \n" + line + "\n \n \n"))
    //val dbscan = new DBSCAN_MOD(input_filepath, output_filepath, 100, 1, data)
    //dbscan.runOnSpark()
    sc.stop()
  }
}
