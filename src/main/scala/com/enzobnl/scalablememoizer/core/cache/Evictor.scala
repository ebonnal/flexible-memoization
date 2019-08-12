package com.enzobnl.scalablememoizer.core.cache


import scala.collection.{immutable, mutable}


class Evictor {
  val maxEntryNumber: Long = 0L
  val removeRatio: Float = 0f
  val removeThreshold: Double = maxEntryNumber * (1 - removeRatio)

  def evict(map: mutable.Map[Long, Any], hash: Long, value: => Any): Unit = ()

  def onHit(map: mutable.Map[Long, Any], hash: Long, value: => Any): Unit = ()
}


class FIFOEvictor(override val maxEntryNumber: Long,
                  override val removeRatio: Float = 0.05f) extends Evictor {

  // remembers keys in arriving order
  var fifoList: List[Long] = List[Long]()

  override def evict(map: mutable.Map[Long, Any], hash: Long, value: => Any): Unit = {

    if (map.size == maxEntryNumber) {
      fifoList = fifoList.dropWhile(hash => {
        if (map.size > removeThreshold){
          map.remove(hash)
          true
        }
        else{
          false
        }
      })
    }
    fifoList = hash :: fifoList

  }
}

class LRUEvictor(override val maxEntryNumber: Long,
                 override val removeRatio: Float = 0.05f) extends Evictor {

  // remembers keys in arriving order. Ordered by first element: timestamp
  object ByFirstElement extends Ordering[(Long, Long)] {
    override def compare(a: (Long, Long), b: (Long, Long)): Int = a._1 compareTo b._2
  }

  var sortedSetOnTimeStamp: immutable.SortedSet[(Long, Long)] = immutable.SortedSet[(Long, Long)]()(ByFirstElement)
  var reverseMap: immutable.Map[Long, (Long, Long)] = immutable.Map[Long, (Long, Long)]()

  override def evict(map: mutable.Map[Long, Any], hash: Long, value: => Any): Unit = {
    if (map.size == maxEntryNumber) {
      // remove a part of map entries (part = REMOVE_RATIO), starting from the left (=smallest timestamps)
      sortedSetOnTimeStamp = sortedSetOnTimeStamp.dropWhile({
        case (_, h) =>
          if (map.size > removeThreshold){
            map.remove(h)
            reverseMap = reverseMap - h
            true
          }
          else{
            false
          }
      })
    }
    // add in timestamp ascending ordered set and reverseMap
    val pair = System.nanoTime() -> hash
    sortedSetOnTimeStamp = sortedSetOnTimeStamp + pair
    reverseMap = reverseMap + (hash -> pair)

  }

  override def onHit(map: mutable.Map[Long, Any], hash: Long, value: => Any): Unit = {
    val sortedSetKeyToRemove = reverseMap(hash)
    sortedSetOnTimeStamp = sortedSetOnTimeStamp - sortedSetKeyToRemove
    sortedSetOnTimeStamp = sortedSetOnTimeStamp + (System.nanoTime() -> hash)
  }
}