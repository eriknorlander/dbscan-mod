package util

object MetaData {
  def init(init: Boolean): scala.collection.immutable.Map[String, String] = {
    var metadata = scala.collection.mutable.Map[String, String]()
    if (!init) return metadata.toMap

    /** Priority */
    metadata += ("priority" -> "")

    /** Invalid metadata */
    metadata += ("invalid_flag" -> "")

    /** Metadata calculated in Classifier/statistics */
    metadata += ("time_length" -> "")
    metadata += ("unique_wifis" -> "")
    metadata += ("unique_gps_coords" -> "")
    metadata += ("mean_gps_latitude" -> "")
    metadata += ("mean_gps_longitude" -> "")
    metadata += ("mean_gps_altitude" -> "")
    metadata += ("max_gps_accuracy" -> "")
    metadata += ("std_dev_latitude" -> "")
    metadata += ("std_dev_longitude" -> "")
    metadata += ("step_sum" -> "")
    metadata += ("mean_wifibt_latitude" -> "")
    metadata += ("mean_wifibt_longitude" -> "")
    metadata += ("mean_pressure" -> "")
    metadata += ("mean_gyro_heading" -> "")
    metadata += ("main_activity" -> "")
    metadata += ("amount_of_scans" -> "")
    metadata += ("unique_wifis" -> "")
    metadata += ("unqiue_bluetooths" -> "")

    /** Metadata derived during splitting */
    metadata += ("amount_of_samples" -> "")
    metadata += ("mean_velocity" -> "")
    metadata += ("mean_gps_accuracy" -> "")
    metadata += ("indoor_movement" -> "")
    metadata += ("indoor_points" -> "")
    metadata += ("usable_gps" -> "")
    metadata += ("near_buildings" -> "")

    /** Metadata derived during bundling */
    metadata += ("track_id" -> "")

    return metadata.toMap
  }
}

/**
  * Idea is that invalid_flag is set to corresponding value, e.g.
  * invalid_flag = "track_length"
  * Then track is marked as invalid due to too short track length
  * invalid_flag = "" means track is valid
  * invalid_flag = "-1" means unknown reason for invalidation
  */

/**
  * Possible invalid reasons:
  * 1: Main segment too short
  * 2: Too low accuracy, i.e. most likely not indoors
  * 3: No movement detected in indoor segment
  * 4: Segment too short <20
  * 5: Too few points that are indoors
  * 6: No usable GPS found in segment
  * 7: No buildings found for given GPS coordinates
  * 9: Unknown reason
  */