package util

object Settings {
  /**
    * Initializes default settings, for boolean configurations 1 is true 0 is false
    * The idea is that this could easily be taken in as user input in the future if necessary
    */
  def init(): scala.collection.immutable.Map[String, Int] = {
    var settings = scala.collection.mutable.Map[String, Int]()

    /** Non-production code settings (debugging) */
    settings += ("local" -> 1)
    settings += ("s3" -> 1)
    settings += ("hdfs" -> 0)

    settings += ("testing" -> 0)
    settings += ("print_track_statistics" -> 0)

    /** General settings */
    settings += ("process_existing_files" -> 1)
    settings += ("stream_processing" -> 0)
    settings += ("stream_window_time" -> 2)

    settings += ("skip_learningwifi" -> 1)

    settings += ("move_processed" -> 0)
    settings += ("delete_processed" -> 0)

    /** Coarse splitter settings */
    settings += ("chunk_size" -> 40000)
    settings += ("time_break" -> 60) // Also used in regular Splitter

    /** Splitter settings */
    settings += ("accuracy_threshold" -> 15)
    settings += ("velocity_threshold" -> 5)
    settings += ("min_seg_length" -> 20)
    settings += ("gps_age_filtering" -> 5)
    settings += ("max_seg_concatenation_length" -> 10)
    settings += ("gaussian_window_width_velocity" -> 40)

    /** Classifier settings */
    settings += ("run_classifier" -> 1)

    /** Bundler settings */
    settings += ("run_bundler" -> 1)

    /** Result/output settings */
    settings += ("save_metadata" -> 1)
    settings += ("save_models" -> 1)

    /** Wifi Group settings */
    settings += ("wifi_group" -> 1)
    return settings.toMap
  }
}
