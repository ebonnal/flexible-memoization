package com.enzobnl.flexiblememoization.cache.impl.map

import com.enzobnl.flexiblememoization.cache.{Cache, HitCounterMixin}
import com.enzobnl.flexiblememoization.util.Timeit

import scala.collection.{immutable, mutable}

private[cache] case class SortedSetEntry(toSortOn: Long, hash: Int)

private[cache] object ByToSortOn extends Ordering[SortedSetEntry] {
  override def compare(a: SortedSetEntry, b: SortedSetEntry): Int = a.toSortOn compareTo b.toSortOn
}

private[cache] class FIFOEvictorMapCacheAdapter(val maxEntryNumber: Option[Long],
                                                val removeRatio: Float) extends Cache with HitCounterMixin{

  val removeThreshold: Double = maxEntryNumber.getOrElse(0L) * (1 - removeRatio)
  // key FIFO, in right, out left
  var fifoList: List[Int] = List[Int]()
  // key -> value map
  val map: mutable.Map[Int, Any] with mutable.SynchronizedMap[Int, Any] =
    new mutable.HashMap[Int, Any]() with mutable.SynchronizedMap[Int, Any]

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
    if (maxEntryNumber match {
      case Some(size) => map.size == size
      case None => false
    }) {
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
    fifoList = fifoList :+ hash
  }
}


private[cache] class LRUEvictorMapCacheAdapter(val maxEntryNumber: Option[Long],
                                               val removeRatio: Float) extends Cache with HitCounterMixin{


  val removeThreshold: Double = maxEntryNumber.getOrElse(0L) * (1 - removeRatio)
  // (timestamp, key) -> key map, sorted on timestamp
  var sortedSetOnTimeStamp: immutable.SortedSet[SortedSetEntry] =
    immutable.SortedSet[SortedSetEntry]()(ByToSortOn)
  // key -> ((timestamp, key), value) map
  val map: mutable.Map[Int, (SortedSetEntry, Any)] with mutable.SynchronizedMap[Int, (SortedSetEntry, Any)] =
    new mutable.HashMap[Int, (SortedSetEntry, Any)]() with mutable.SynchronizedMap[Int, (SortedSetEntry, Any)]

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
      sortedSetOnTimeStamp = sortedSetOnTimeStamp + SortedSetEntry(System.nanoTime(), hash)
      hits += 1
    }
    result._2
  }

  def evict(hash: Int, value: => Any): SortedSetEntry = {
    if (maxEntryNumber match {
      case Some(size) => map.size == size
      case None => false
    }) {

      // remove a part of map entries (part = REMOVE_RATIO), starting from the left (=smallest timestamps)
      sortedSetOnTimeStamp = sortedSetOnTimeStamp.dropWhile({
        case SortedSetEntry(_, h) =>
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
    val pair = SortedSetEntry(System.nanoTime(), hash)
    sortedSetOnTimeStamp = sortedSetOnTimeStamp + pair
    pair
  }
}

private[cache] class CostEvictorMapCacheAdapter(val maxEntryNumber: Option[Long],
                                                val removeRatio: Float) extends Cache with HitCounterMixin {


  val removeThreshold: Double = maxEntryNumber.getOrElse(0L) * (1 - removeRatio)
  var sortedSetOnCost: immutable.SortedSet[SortedSetEntry] = immutable.SortedSet[SortedSetEntry]()(ByToSortOn)
  val map: mutable.Map[Int, Any] with mutable.SynchronizedMap[Int, Any] =
    new mutable.HashMap[Int, Any]() with mutable.SynchronizedMap[Int, Any]

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
    if (maxEntryNumber match {
      case Some(size) => map.size == size
      case None => false
    }) {
      // remove a part of map entries (part = REMOVE_RATIO), starting from the left (=smallest timestamps)
      sortedSetOnCost = sortedSetOnCost.dropWhile({
        case SortedSetEntry(_, h) =>
          if (map.size > removeThreshold) {
            map.remove(h)
            true
          }
          else {
            false
          }
      })
    }
    sortedSetOnCost = sortedSetOnCost + SortedSetEntry(Timeit.get(value), hash)

  }
}
