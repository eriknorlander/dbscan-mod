package BundlerUtils

import scala.collection.immutable.Map
import scala.collection.mutable.ArrayBuffer

object AccessPointPositioning {

  /**
    * Adds the access points positions to track data
    */
  def addAccessPointPositions(track: (Array[Array[String]], Map[String, String])): (Array[Array[String]], Map[String, String], ArrayBuffer[AccessPoint]) = {
    val accessPoints = findAccessPointPositions(track)
    return (track._1, track._2, accessPoints) // Now track is a triplet: (scans, metadata, accessPoints)
  }

  /**
    * Estimates positions of access points (wifis and bluetooths)
    */
  def findAccessPointPositions(track: (Array[Array[String]], Map[String, String])): ArrayBuffer[AccessPoint] = {
    var uniqueAP = ArrayBuffer[AccessPoint]() // bssid, lat, long
    var apID = 0

    for (sample <- track._1) {
      val bssid = sample(8)
      // If we haven't calculated pos of this unique access points
      if (!containsAccessPoint(uniqueAP, bssid)) {
        var latSum = 0.0
        var longSum = 0.0
        var latOcc = 0 // Number of times this unique access point appeared in this track
        var longOcc = 0

        // Collect positioning data of current access point
        for (sample <- track._1) {
          // If we find the same access point
          if(sample(8).equals(bssid)) {
            if (sample(11) != "") {
              latSum += sample(11).toDouble
              latOcc += 1
            }
            if (sample(12) != "") {
              longSum += sample(12).toDouble
              longOcc += 1
            }
          }
        }

        /** Convert lat/long to 2D cartesian coordinates */
        val projector = new GoogleMapsProjection()
        val point = projector.fromLatLngToPoint(latSum/latOcc, longSum/longOcc, 15)

        if (point.x != 0.0 && point.y != 0.0) {
          val accessPoint = new AccessPoint(bssid, point.x, point.y, apID)
          uniqueAP += accessPoint
          apID += 1
        }

      }
    }

    return uniqueAP
  }

  /**
    * Checks whether a list of access points contains an access point with a certain bssid
    */
  def containsAccessPoint(accessPoints: ArrayBuffer[AccessPoint], bssid: String): Boolean = {
    for (ap <- accessPoints) {
      if (ap.getBSSID().equals(bssid)) return true
    }
    return false
  }

  /**
    * Gets the average position of access points
    */
  def getAveragePos(accessPoints: ArrayBuffer[AccessPoint]): (Double, Double) = {
    var latSum = 0.0
    var longSum = 0.0

    for (ap <- accessPoints) {
      latSum += ap.getLatitude()
      longSum += ap.getLongitude()
    }

    val lat = latSum/accessPoints.length
    val long = longSum/accessPoints.length

    val xyPoint = new PointF(lat, long)
    val projector = new GoogleMapsProjection
    val point = projector.fromPointToLatLng(xyPoint, 15)
    return (point.x, point.y)
  }
}