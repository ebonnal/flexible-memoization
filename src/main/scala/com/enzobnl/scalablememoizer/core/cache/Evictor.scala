package com.enzobnl.scalablememoizer.core.cache

import scala.collection.{immutable, mutable}


trait Evictor {
  def evict(map: mutable.Map[Long, Any], hash: Long, value: => Any): Unit

  def onHit(map: mutable.Map[Long, Any], hash: Long, value: => Any): Unit = ()
}

class NoEvictor extends Evictor {
  override def evict(map: mutable.Map[Long, Any], hash: Long, value: => Any): Unit = ()
}

trait LimitedEvictor extends Evictor {
  val maxEntryNumber: Long
  val removeRatio: Float
  val removeThreshold: Double = maxEntryNumber * (1 - removeRatio)
}

class FifoEvictor(override val maxEntryNumber: Long,
                  override val removeRatio: Float = 0.05f) extends LimitedEvictor {

  // remembers keys in arriving order
  var fifoList: List[Long] = List[Long]()

  override def evict(map: mutable.Map[Long, Any], hash: Long, value: => Any): Unit = {
    if (map.size == maxEntryNumber) {
      fifoList = fifoList.dropWhile(hash => {
        map.remove(hash)
        map.size > removeThreshold
      })
    }
    else {
      fifoList = hash :: fifoList
    }
  }
}

class LruEvictor(override val maxEntryNumber: Long,
                 override val removeRatio: Float = 0.05f) extends LimitedEvictor {

  // remembers keys in arriving order. Ordered by first element: timestamp
  object ByFirstElement extends Ordering[(Long, Long)] {
    override def compare(a: (Long, Long), b: (Long, Long)): Int = a._1 compareTo b._1
  }

  var sortedSetOnTimeStamp: immutable.SortedSet[(Long, Long)] = immutable.SortedSet[(Long, Long)]()(ByFirstElement)
  var reverseMap: immutable.Map[Long, (Long, Long)] = immutable.Map[Long, (Long, Long)]()

  override def evict(map: mutable.Map[Long, Any], hash: Long, value: => Any): Unit = {
    if (map.size == maxEntryNumber) {
      // remove a part of map entries (part = REMOVE_RATIO), starting from the left (=smallest timestamps)
      sortedSetOnTimeStamp = sortedSetOnTimeStamp.dropWhile({
        case (_, h) =>
          map.remove(h)
          reverseMap = reverseMap - h
          map.size > removeThreshold
      })
    }
    else {
      // add in timestamp ascending ordered set and reverseMap
      val pair = System.nanoTime() -> hash
      sortedSetOnTimeStamp = sortedSetOnTimeStamp + pair
      reverseMap = reverseMap + (hash -> pair)
    }
  }

  override def onHit(map: mutable.Map[Long, Any], hash: Long, value: => Any): Unit = {
    val sortedSetKeyToRemove = reverseMap(hash)
    sortedSetOnTimeStamp = sortedSetOnTimeStamp - sortedSetKeyToRemove
    sortedSetOnTimeStamp = sortedSetOnTimeStamp + (System.nanoTime() -> hash)
  }
}

abstract class FrequostEvictor(override val maxEntryNumber: Long,
                               override val removeRatio: Float = 0.05f) extends LimitedEvictor {

}