package BundlerUtils

/**
  * Access point object which holds an access points position (lat, long), its unique bssid and its unique id which is an index.
  */
class AccessPoint(bssid: String, latitude: Double, longitude: Double, id: Int) {
  def getBSSID(): String = {
    return bssid
  }

  def getLatitude(): Double = {
    return latitude
  }

  def getLongitude(): Double = {
    return longitude
  }

  def getID(): Int = {
    return id
  }
}