package com.enzobnl.memoizationtoolbox.core.cache

import com.enzobnl.memoizationtoolbox.util.Timeit

import scala.collection.{immutable, mutable}



private[cache] class FIFOEvictorMapCacheAdapter private[cache](val maxEntryNumber: Long,
                                                               val removeRatio: Float) extends Cache {

  val removeThreshold: Double = maxEntryNumber * (1 - removeRatio)
  // key list
  var fifoList: List[Int] = List[Int]()
  // key -> value map
  val map: mutable.Map[Int, Any] = mutable.Map[Int, Any]()

  override def getOrElseUpdate(hash: Int, value: => Any): Any = {
    var computed = false
    val result = map.getOrElseUpdate(hash, {
      computed = true
      evict(hash, value)
      value
    })
    if (computed) misses += 1
    else {
      hits += 1
    }
    result
  }

  def evict(hash: Int, value: => Any): Unit = {

    if (map.size == maxEntryNumber) {
      fifoList = fifoList.dropWhile(hash => {
        if (map.size > removeThreshold) {
          map.remove(hash)
          true
        }
        else {
          false
        }
      })
    }
    fifoList = hash :: fifoList
  }
}

private[cache] case class LRUSortedSetEntry(timestamp: Long, hash: Int)

class LRUEvictorMapCacheAdapter private[cache](val maxEntryNumber: Long, val removeRatio: Float) extends Cache {

  object ByFirstElement extends Ordering[LRUSortedSetEntry] {
    override def compare(a: LRUSortedSetEntry, b: LRUSortedSetEntry): Int = a.timestamp compareTo b.timestamp
  }

  val removeThreshold: Double = maxEntryNumber * (1 - removeRatio)
  // (timestamp, key) -> key map, sorted on timestamp
  var sortedSetOnTimeStamp: immutable.SortedSet[LRUSortedSetEntry] =
    immutable.SortedSet[LRUSortedSetEntry]()(ByFirstElement)
  // key -> ((timestamp, key), value) map
  var map: mutable.Map[Int, (LRUSortedSetEntry, Any)] = mutable.Map[Int, (LRUSortedSetEntry, Any)]()

  override def getOrElseUpdate(hash: Int, value: => Any): Any = {
    var computed = false
    val result = map.getOrElseUpdate(hash, {
      computed = true
      (evict(hash, value), value)
    })
    if (computed) misses += 1
    else {
      val sortedSetKeyToRemove = map(hash)
      sortedSetOnTimeStamp = sortedSetOnTimeStamp - sortedSetKeyToRemove._1
      sortedSetOnTimeStamp = sortedSetOnTimeStamp + LRUSortedSetEntry(System.nanoTime(), hash)
      hits += 1
    }
    result._2
  }

  def evict(hash: Int, value: => Any): LRUSortedSetEntry = {
    if (map.size == maxEntryNumber) {
      // remove a part of map entries (part = REMOVE_RATIO), starting from the left (=smallest timestamps)
      sortedSetOnTimeStamp = sortedSetOnTimeStamp.dropWhile({
        case LRUSortedSetEntry(_, h) =>
          if (map.size > removeThreshold) {
            map.remove(h)
            true
          }
          else {
            false
          }
      })
    }
    // add in timestamp ascending ordered set and reverseMap
    val pair = LRUSortedSetEntry(System.nanoTime(), hash)
    sortedSetOnTimeStamp = sortedSetOnTimeStamp + pair
    pair
  }
}

private[cache] class CostEvictorMapCacheAdapter private[cache](val maxEntryNumber: Long,
                                                               val removeRatio: Float) extends Cache {

  object ByFirstElement extends Ordering[(Long, Int)] {
    override def compare(a: (Long, Int), b: (Long, Int)): Int = a._1 compareTo b._1
  }

  val removeThreshold: Double = maxEntryNumber * (1 - removeRatio)
  var sortedSetOnCost: immutable.SortedSet[(Long, Int)] = immutable.SortedSet[(Long, Int)]()(ByFirstElement)
  val map: mutable.Map[Int, Any] = mutable.Map[Int, Any]()

  override def getOrElseUpdate(hash: Int, value: => Any): Any = {
    var computed = false
    val result = map.getOrElseUpdate(hash, {
      computed = true
      evict(hash, value)
      value
    })
    if (computed) misses += 1
    else {
      hits += 1
    }
    result
  }

  def evict(hash: Int, value: => Any): Unit = {
    if (map.size == maxEntryNumber) {
      // remove a part of map entries (part = REMOVE_RATIO), starting from the left (=smallest timestamps)
      sortedSetOnCost = sortedSetOnCost.dropWhile({
        case (_, h) =>
          if (map.size > removeThreshold) {
            map.remove(h)
            true
          }
          else {
            false
          }
      })
    }
    sortedSetOnCost = sortedSetOnCost + (Timeit.get(value) -> hash)

  }
}