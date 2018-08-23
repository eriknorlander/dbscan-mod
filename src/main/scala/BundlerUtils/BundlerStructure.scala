package BundlerUtils

import scala.collection.immutable.Map
import scala.collection.mutable.ArrayBuffer

object BundlerStructure {
  /**
    * Creates a PIJ matrix of size 3*n. Structure:
    * rssid | access point ID | scan index
    */
  def createPIJ(scan: (Array[Array[String]], Map[String, String], ArrayBuffer[AccessPoint])): Array[Array[Int]] = {
    var pij = ArrayBuffer[Array[Int]]()

    for (sample <- scan._1) {
      if (!sample(10).equals("") || !sample(8).equals("")) {
        pij += Array(sample(10).toInt, findID(scan._3, sample(8)), 1)
      }
    }

    return pij.toArray
  }

  /**
    * From a list of access points, creates a 2*n matrix. Each row is an access point and each row contains latitude and longitude.
    * The order of the returned array is important and must be preserved (index operations are used on the matrix).
    */
  def createAccessPointPos(accessPoints: ArrayBuffer[AccessPoint]): Array[Array[Double]] = {
    var apPos = ArrayBuffer[Array[Double]]()
    for (accessPoint <- accessPoints) {
      apPos += Array(accessPoint.getLatitude(), accessPoint.getLongitude())
    }
    return apPos.toArray
  }

  /**
    * Finds which access point in the list has a certain bssid
    */
  def findID(accessPoints: ArrayBuffer[AccessPoint], bssid: String): Int = {
    for (accessPoint <- accessPoints) {
      if (bssid.equals(accessPoint.getBSSID())) return accessPoint.getID()
    }
    return -1 // Should never happen
  }

  /**
    * Splits tracks into scans
    */
  def splitToRDDScans(track: (Array[Array[String]], Map[String, String], ArrayBuffer[AccessPoint])): Array[(Array[Array[String]], Map[String, String], ArrayBuffer[AccessPoint])] = {
    var result = ArrayBuffer[(Array[Array[String]], Map[String, String], ArrayBuffer[AccessPoint])]()

    var currentScan = track._1(0)(0)
    var currentScanSamples = ArrayBuffer[Array[String]]()
    for (sample <- track._1) {
      if (sample(0).equals(currentScan)) {
        currentScanSamples += sample
      } else { // new scan started
        currentScanSamples += sample
        result += ((currentScanSamples.toArray, track._2, track._3))
        currentScanSamples = ArrayBuffer[Array[String]]()
      }
      currentScan = sample(0)
    }

    return result.toArray
  }

  /**
    * Splits tracks into scans, old method that is not used at the moment
    */
  def splitToScans(track: (Array[Array[String]], Map[String, String])): ArrayBuffer[ArrayBuffer[Array[String]]] = {
    var result = ArrayBuffer[ArrayBuffer[Array[String]]]()

    var currentScan = track._1(0)(0)
    var currentScanSamples = ArrayBuffer[Array[String]]()
    for (sample <- track._1) {
      if (sample(0).equals(currentScan)) {
        currentScanSamples += sample
      } else { // new scan started
        currentScanSamples += sample
        result += currentScanSamples
        currentScanSamples = ArrayBuffer[Array[String]]()
      }
      currentScan = sample(0)
    }

    return result
  }
}