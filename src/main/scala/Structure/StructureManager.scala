package Structure

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import scala.collection.immutable.Map
import util.MetaData

object StructureManager {
  def prepareRDD(input_filepath: String, sc: SparkContext, settings: Map[String, Int]): RDD[(Array[Array[String]], Map[String, String])] = {
    val meta = MetaData.init(true)
    val rawRDD = sc.newAPIHadoopFile[SessionWritable, SessionWritable, SessionInputFormat](input_filepath)

    val data = rawRDD.map(pair => {
      val initTrackContents = pair._1.get().map(line => line.split(",").map(elem => elem.trim))
      val trackContents = initTrackContents.filter(sample => sample(1) != "type")
      //val x = trackContents.filter(sample => !sample(1).contains("learningwifi") && settings("skip_learningwifi") == 1)
      (trackContents, meta)
    })

    return data
  }
}
