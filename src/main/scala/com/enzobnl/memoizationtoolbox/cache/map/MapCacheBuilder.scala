package com.enzobnl.memoizationtoolbox.cache.map

import Eviction.Eviction
import com.enzobnl.memoizationtoolbox.cache.{Cache, CacheBuilder}


/**
  * Design: Functional Builder Pattern allowing fluent customization of Scala Map based Cache.
  *
  * @param evictionPolicy: Policy used if maxEntryNumber size reached
  * @param maxEntryNumber: Maximum number of entries in the cache
  * @param removeRatio: Float in ]0, 1] indicating the size of the eviction bactches
  *                   (removeRatio*maxEntryNumber entries). If set to 1, the entire cache is purged
  *                   each time it reaches maxEntryNumber entries.
  */
class MapCacheBuilder private(val evictionPolicy: Eviction,
                              val maxEntryNumber: Option[Long],
                              val removeRatio: Float) extends CacheBuilder {

  def this() = this(Eviction.LRU, None, 0.05f)

  def withEviction(eviction: Eviction): MapCacheBuilder =
    new MapCacheBuilder(eviction, maxEntryNumber, removeRatio)

  def withMaxEntryNumber(size: Long): MapCacheBuilder =
    new MapCacheBuilder(evictionPolicy, Some(size), removeRatio)

  def withRemoveRation(ratio: Float): MapCacheBuilder =
    new MapCacheBuilder(evictionPolicy, maxEntryNumber, ratio)

  override def build(): Cache = evictionPolicy match {
    case Eviction.LRU => new LRUEvictorMapCacheAdapter(maxEntryNumber, removeRatio)
    case Eviction.FIFO => new FIFOEvictorMapCacheAdapter(maxEntryNumber, removeRatio)
    case Eviction.COST => new CostEvictorMapCacheAdapter(maxEntryNumber, removeRatio)

  }
}
