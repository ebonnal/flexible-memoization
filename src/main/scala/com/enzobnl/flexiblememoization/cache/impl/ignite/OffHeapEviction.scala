package com.enzobnl.flexiblememoization.cache.impl.ignite

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
