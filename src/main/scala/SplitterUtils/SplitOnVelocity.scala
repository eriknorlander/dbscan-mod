package SplitterUtils

import util.Time

import scala.collection.immutable.Map
import scala.collection.mutable.ArrayBuffer

object SplitOnVelocity {
  def
  split(track: (Array[Array[String]], Map[String, String]), threshold: Integer, gpsAgeFiltering: Integer, gwwv: Integer): Array[(Array[Array[String]], Map[String, String])] = {
    /** Calc accuracy smoothed */
    // val acc = track._1.map(sample => sample(6)) // +accSmoothed

    /** Calc velocity smoothed */
    // val nonZeroGPS = track._1.map(sample => calcNonZeroGPS(sample(4), sample(5)))
    val newGPS = track._1.map(sample => calcNewGPS(sample(7), gpsAgeFiltering))
    val validGPS = track._1.map(sample => calcValidGPS(sample(3), sample(7), gpsAgeFiltering))

    /** getDistances */
    // First input: gpspos struct without first element, Second input: gpspos struct without last element
    val gpsPos = track._1.map(sample => Array(sample(4).toDouble, sample(5).toDouble))
    val x1 = gpsPos.drop(1)
    val x2 = gpsPos.dropRight(1)

    val R = 6378100
    var vel = ArrayBuffer[Double]()
    for (i <- 0 until x1.length) {
      val lat1 = Math.toRadians(x1(i)(0))
      val long1 = Math.toRadians(x1(i)(1))
      val lat2 = Math.toRadians(x2(i)(0))
      val long2 = Math.toRadians(x2(i)(1))

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

      val dist = R * Math.acos(cos_theta)

      val time = Time.timeDifference(track._1(i)(2), track._1(i+1)(2))

      if (time != 0) {
        vel += dist/time
      } else if (dist != 0) {
        vel += threshold + 1
      } else {
        vel += 0.0
      }
    }

    /** Find trustable velocities */
    for (i <- 0 until vel.length) {
      // Maybe add nonZeroGPS? but then all velocities would get 0?
      if (newGPS(i) == 0 || newGPS(i+1) == 0) {
        vel(i) = 0.0
      }
      if (validGPS(i) == 0 || validGPS(i+1) == 0) {
        vel(i) = 0.0
      }
    }

    /** Split on velocity */
    var result = ArrayBuffer[(Array[Array[String]], Map[String, String])]()

    var newSamples = ArrayBuffer[Array[String]]()
    var velocitySum = 0.0
    var j = 0
    var previousVelocity = vel(0)
    for (velocity <- vel) {
      if (lowHigh(previousVelocity, velocity, threshold)) {
        val nMeta = track._2 + ("mean_velocity" -> (velocitySum/newSamples.length).toString)
        val newMeta = nMeta + ("amount_of_samples" -> newSamples.length.toString)

        if (velocity >= threshold.toDouble) {
          val a = newMeta + ("invalid_flag" -> "mean_velocity")
          result += ((newSamples.toArray, a))
        } else {
          result += ((newSamples.toArray, newMeta))
        }
        newSamples = ArrayBuffer[Array[String]]()
        velocitySum = 0
      }
      newSamples += track._1(j)
      velocitySum += velocity
      previousVelocity = velocity
      j += 1
    }

    val nMeta = track._2 + ("mean_velocity" -> (velocitySum/newSamples.length).toString)
    val newMeta = nMeta + ("amount_of_samples" -> newSamples.length.toString)

    if (previousVelocity >= threshold.toDouble) {
      val a = newMeta + ("invalid_flag" -> "mean_velocity")
      result += ((newSamples.toArray, a))
    } else {
      result += ((newSamples.toArray, newMeta))
    }

    return result.toArray
  }

  def calcNonZeroGPS(lat: String, long: String): Int = {
    if (lat == "" || long == "") return 0
    if (lat.toDouble != 0.0 && long.toDouble != 0.0) return 1 else return 0
  }

  def calcNewGPS(age: String, gpsAgeFiltering: Int): Int = {
    if (age == "") return 0
    if (age.toInt < gpsAgeFiltering) return 1 else return 0
  }

  def calcValidGPS(valid: String, age: String, gpsAgeFiltering: Int): Int = {
    if (valid == "" || age == "") return 0
    if (valid.toInt == 1 && age.toInt < gpsAgeFiltering) return 1 else return 0
  }

  /** Checks if two velocities should be in seperate tracks (checks high/low) */
  def lowHigh(vel1: Double, vel2: Double, threshold: Integer): Boolean = {
    val t = threshold.toDouble
    if (vel1 < t && vel2 >= t) return true
    if (vel1 >= t && vel2 < t) return true
    return false
  }
}