package SplitterUtils

import util.Time

import scala.collection.immutable.Map
import scala.collection.mutable.ArrayBuffer

object SplitOnTime {
  def
  split(track: (Array[Array[String]], Map[String, String]), timeBreak: Integer, minSegLength: Integer): ArrayBuffer[(Array[Array[String]], Map[String, String])] = {
    var result = ArrayBuffer[(Array[Array[String]], Map[String, String])]()

    val samples = track._1
    var previous = samples(0)(2)

    var newSamples = ArrayBuffer[Array[String]]()
    for (sample <- samples) {
      if (Math.abs(Time.timeDifference(previous, sample(2))) > timeBreak) {
        val newMeta = track._2 + ("amount_of_samples" -> newSamples.length.toString)
        result += ((newSamples.toArray, newMeta + ("invalid_flag" -> {
          if (newSamples.length < minSegLength) "amount_of_samples" else track._2("invalid_flag")
        })))

        newSamples = ArrayBuffer[Array[String]]()
      }
      newSamples += sample
      previous = sample(2)
    }

    val newMeta = track._2 + ("amount_of_samples" -> newSamples.length.toString)
    result += ((newSamples.toArray, newMeta + ("invalid_flag" -> {
      if (newSamples.length < minSegLength) "amount_of_samples" else track._2("invalid_flag")
    })))

    return result
  }
}