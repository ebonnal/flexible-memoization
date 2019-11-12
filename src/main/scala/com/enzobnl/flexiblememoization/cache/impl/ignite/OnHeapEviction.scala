package com.enzobnl.flexiblememoization.cache.impl.ignite

object OnHeapEviction extends Enumeration {
  type OnHeapEviction = Value
  /**
    * Evicts least recently used entry
    */
  val LRU,

  /**
    * Evicts oldest entry in the cache
    */
  FIFO,

  /**
    * Evicts entry with smaller value (can use user comparison func)
    */
  SORTED = Value
}

