import org.apache.spark.sql.SparkSession
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.functions.countDistinct
import scala.io.Source
import Array._
import Structure.{SessionInputFormat, SessionWritable, StructureManager}
import util.{InputOutput, Settings}
import org.apache.hadoop.fs.Path
import DBSCAN._

object Distributer {
  def main(args: Array[String]) = {

    /** Requires 4 inputs. */
    val input_filepath = args(0).toString
    val output_filepath = args(1).toString
    val minPts = args(2).toString
    val epsilon = args(3).toString

    val settings = Settings.init()

    /** Running locally.
    val conf = new SparkConf()
            .setAppName("Distributer")
            .setMaster("local[2]")
            .setJars(Array("/Users/eriknorlander/dbscan-spark/target/scala-2.11/dbscan_2.11-1.0.jar"))
    */

    /** Running on AWS. */
    val conf = new SparkConf()
            .setAppName("Distributer")
            .setMaster("local")
            .setJars(Seq("s3://dbscan-spark/jars/DBSCAN-assembly-1.0.jar"))

    val sc = new SparkContext(conf)
    if (settings("process_existing_files") == 1 && InputOutput.getNumberFiles(input_filepath, sc, settings) > 0) {
      val data = StructureManager.prepareRDD(input_filepath, sc, settings)
    }

    val data = StructureManager.prepareRDD(input_filepath, sc, settings)

    /** Cluster the wifis by wifiId. */
    val clusterRDD = data.map(track => ClusterByWIFI.run(track, minPts, epsilon))
                         .flatMap(track => track)

    /** Writes clusters to JSON-format.*/
    clusterRDD.foreach(line => Writer.createJSON(line))
    val result = Writer.getJSON()
    //Writer.writeJSON(output_filepath)

    /** Writes clusters to CSV-format.*/
    //clusterRDD.foreach(line => Writer.createCSV(line))
    //Writer.writeCSV(output_csv)

    /** Writes clusters to CSV-format for analysis in MATLAB.*/
    //clusterRDD.foreach(line => Writer.createCSVnumerics(line))
    //Writer.writeCSVnumerics(output_csv_numerics)

    /** Writes information to S3. */
    val writeRDD = sc.parallelize(Seq(result))
    Writer.saveResult(output_filepath, writeRDD, sc)
    /** STOP THE SPARK SESSION! */
    sc.stop()
  }
}
