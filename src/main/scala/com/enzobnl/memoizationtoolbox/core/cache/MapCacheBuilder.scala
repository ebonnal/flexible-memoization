package com.enzobnl.memoizationtoolbox.core.cache


import com.enzobnl.memoizationtoolbox.core.cache.Eviction.Eviction

object Eviction extends Enumeration {
  type Eviction = Value
  /**
    * Evicts least recently used entry
    */
  val LRU,

  /**
    * Evicts oldest entry in the cache
    */
  FIFO,

  /**
    * Evicts entry with smaller computation time cost
    */
  COST = Value
}


class MapCacheBuilder private(val evictionPolicy: Eviction,
                              val maxEntryNumber: Long,
                              val removeRatio: Float) extends CacheBuilder {

  def this(maxEntryNumber: Long) = this(Eviction.LRU, maxEntryNumber, 0.05f)

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
