package SplitterUtils

import util.Time

import scala.collection.immutable.Map
import scala.collection.mutable.ArrayBuffer

object CoarseSplitter {
  /**
    * Splits a track of length n into n-1 equal chunks (and one leftover chunk of smaller size) then checks bounds to
    * see if an actual track has been split in a wrong position
    */
  def split(track: (Array[Array[String]], Map[String, String]), chunkSize: Int, timeBreak: Int): ArrayBuffer[(Array[Array[String]], Map[String, String])] = {
    if (track._1.length < chunkSize) return ArrayBuffer(track)

    /** Split into chunks */
    var splitData = ArrayBuffer[Array[Array[String]]]()
    for (i <- 0 until track._1.length/chunkSize) splitData += track._1.slice(i*chunkSize, i*chunkSize + chunkSize)
    splitData += track._1.slice((track._1.length/chunkSize)*chunkSize, track._1.length) // Finally add the last chunk

    /** Check if it split in the middle of tracks */
    return checkBounds(splitData, track._2, timeBreak)
  }

  /**
    * Checks whether the boundaries of each chunk belong to the same track
    */
  def checkBounds(data: ArrayBuffer[Array[Array[String]]], meta: Map[String, String], timeBreak: Int): ArrayBuffer[(Array[Array[String]], Map[String, String])] = {
    var courseSplitData = ArrayBuffer[(Array[Array[String]], Map[String, String])]()

    var track1 = data(0)
    var track2 = data(1)

    var skip = false

    var i = 0 // track index
    while (i < data.length - 1) {
      track2 = data(i+1)
      val lastSample = track1(track1.length - 1)
      val firstSample = track2(0)
      if (Math.abs(Time.timeDifference(lastSample(2), firstSample(2))) < timeBreak) { // Overlap, need to join tracks
        var track1index = -1
        var track2index = -1

        var k = track1.length - 1 // 1st track index
        while (k > 0) {
          if(Math.abs(Time.timeDifference(track1(k)(2), track1(k-1)(2))) > timeBreak) {
            track1index = k
            k = 0
          }
          k -= 1
        }

        var j = 0 // 2nd track index
        while (j < track2.length - 1) {
          if(Math.abs(Time.timeDifference(track2(j)(2), track2(j+1)(2))) > timeBreak) {
            track2index = j
            j = track2.length
          }
          j += 1
        }

        if (track1index > 0) { // we have a leftover in track 1 that is needed
          courseSplitData += ((track1.slice(0, track1index), meta))
        }

        if (track2index == -1) { // Track 2 doesnt end
          var new_track = track1.slice(track1index, track1.length) ++ track2
          var found = false

          if ((i+1) == data.length - 1) { // if we already start on the last track
            found = true
            skip = true
          }

          while (!found) {
            i += 1
            val track2_new = data(i+1)
            var end_index = -1

            var m = 0 // 2nd track index
            while (m < track2_new.length - 1) {
              if(Math.abs(Time.timeDifference(track2_new(m)(2), track2_new(m+1)(2))) > timeBreak) {
                end_index = m
                found = true
                m = track2_new.length
              }
              m += 1
            }
            if (!found) {
              new_track = new_track ++ track2_new
            } else {
              new_track = new_track ++ track2_new.slice(0, end_index + 1)
              track1 = track2_new.slice(end_index + 1, track2_new.length)
            }

            if ((i+1) == data.length - 1) { // if we are at the last track, we are done anyways
              found = true
              skip = true
            }
          }
          courseSplitData += ((new_track, meta))
        } else {
          val t1 = track1.slice(track1index, track1.length)
          val t2 = track2.slice(0, track2index + 1)

          courseSplitData += ((t1 ++ t2, meta)) // Add joined track

          track1 = track2.slice(track2index + 1, track2.length) // Update track 1 in next iteration
        }
      } else {
        courseSplitData += ((track1, meta))
        track1 = track2
      }
      i += 1
    }

    if (!skip) courseSplitData += ((track1, meta))

    return courseSplitData
  }
}