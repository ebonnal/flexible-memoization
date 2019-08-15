package com.enzobnl.memoizationtoolbox.cache.ignite

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

object OffHeapEviction extends Enumeration {
  type OffHeapEviction = Value
  /**
    * Pick randomly 5 entries and evicts least recently used entry
    */
  val RANDOM_LRU,

  /**
    * Pick randomly 5 entries and evicts the one with the oldest penultimate usage timestamp
    */
  RANDOM_2_LRU = Value
}