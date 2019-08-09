package com.enzobnl.scalablememoizer.ignite.cache

object OnHeapEvictionPolicy extends Enumeration {
  type OnHeapEvictionPolicy = Value
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