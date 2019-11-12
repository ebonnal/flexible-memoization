package com.enzobnl.flexiblememoization.cache.impl.ignite

object CacheMode extends Enumeration {
  type CacheMode = Value
  /**
    * Each node is independent and holds the cache entries it has generated.
    */

  val LOCAL,  // it is way worth it to use Caffeine instead of LOCAL mode.

  /**
    * Every nodes holds a copy of the entire cache: slow on PUT, fast on GET
    */
  REPLICATED,

  /**
    * Cache size scale across cluster nodes: fast on PUT, slow on GET
    */
  PARTITIONED = Value
}
