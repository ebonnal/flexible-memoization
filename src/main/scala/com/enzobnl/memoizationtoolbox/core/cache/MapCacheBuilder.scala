package com.enzobnl.memoizationtoolbox.core.cache


import com.enzobnl.memoizationtoolbox.core.cache.Eviction.Eviction



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
                              val maxEntryNumber: Long,
                              val removeRatio: Float) extends CacheBuilder {

  def this() = this(Eviction.LRU, 10000, 0.05f)

  def withEvictor(eviction: Eviction): MapCacheBuilder =
    new MapCacheBuilder(eviction, maxEntryNumber, removeRatio)

  def withMaxEntryNumber(size: Long): MapCacheBuilder =
    new MapCacheBuilder(evictionPolicy, size, removeRatio)

  def withRemoveRation(ratio: Float): MapCacheBuilder =
    new MapCacheBuilder(evictionPolicy, maxEntryNumber, ratio)

  override def build(): Cache = evictionPolicy match {
    case Eviction.LRU => new LRUEvictorMapCacheAdapter(maxEntryNumber, removeRatio)
    case Eviction.FIFO => new FIFOEvictorMapCacheAdapter(maxEntryNumber, removeRatio)
    case Eviction.COST => new CostEvictorMapCacheAdapter(maxEntryNumber, removeRatio)

  }
}
