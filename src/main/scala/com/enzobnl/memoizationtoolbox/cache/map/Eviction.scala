package com.enzobnl.memoizationtoolbox.cache.map

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
