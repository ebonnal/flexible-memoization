package com.enzobnl.flexiblememoization.cache

import com.enzobnl.flexiblememoization.cache.map.{CostEvictorMapCacheAdapter, Eviction, FIFOEvictorMapCacheAdapter, LRUEvictorMapCacheAdapter, MapCacheBuilder}
import org.scalatest.FlatSpec


class EvictionSuite extends FlatSpec {
  "Fifo with 2 entries max, on keys stream <<-(1,2,3,4)<<-" should "give (3,4)" in {
    val cache: FIFOEvictorMapCacheAdapter =
      new MapCacheBuilder()
        .withMaxEntryNumber(2)
        .withEviction(Eviction.FIFO)
        .build()
        .asInstanceOf[FIFOEvictorMapCacheAdapter]
    cache.getOrElseUpdate(1, "a")
    cache.getOrElseUpdate(2, "b")
    cache.getOrElseUpdate(2, "b")
    cache.getOrElseUpdate(1, "a")
    cache.getOrElseUpdate(3, "c")
    assert(cache.map.keys == Set(2,3))
  }
  "Lru with 2 entries max, on keys stream <<-(1,2,2,1,3)<<-" should "give (3,1)" in {
    val cache: LRUEvictorMapCacheAdapter =
      new MapCacheBuilder()
        .withMaxEntryNumber(2)
        .withEviction(Eviction.LRU)
        .build()
        .asInstanceOf[LRUEvictorMapCacheAdapter]
    cache.getOrElseUpdate(1, "a")
    cache.getOrElseUpdate(2, "b")
    cache.getOrElseUpdate(2, "b")
    cache.getOrElseUpdate(1, "a")
    cache.getOrElseUpdate(3, "c")
    assert(cache.map.keys ==  Set(1, 3))
  }
  "Cost with 2 entries max, on keys stream <<-(1,2,2,1,3)<<-" should "give (3,1)" in {
    val cache: CostEvictorMapCacheAdapter =
      new MapCacheBuilder()
        .withMaxEntryNumber(2)
        .withEviction(Eviction.COST)
        .build()
        .asInstanceOf[CostEvictorMapCacheAdapter]
    cache.getOrElseUpdate(1, {Thread.sleep(10); "a"})

    cache.getOrElseUpdate(2, "b")

    cache.getOrElseUpdate(2, "b")

    cache.getOrElseUpdate(1, "a")

    cache.getOrElseUpdate(3, "c")

    cache.getOrElseUpdate(4, "d")
    assert(cache.map.keys ==  Set(1, 4))
  }
}