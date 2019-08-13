package com.enzobnl.memoizationtoolbox.core.cache

import org.scalatest.FlatSpec


class EvictionSuite extends FlatSpec {
  "Fifo with 2 entries max, on keys stream <<-(1,2,3,4)<<-" should "give (3,4)" in {
    val cache: FIFOEvictorMapCacheAdapter = new MapCacheBuilder(2).withEvictor(Eviction.FIFO).build().asInstanceOf[FIFOEvictorMapCacheAdapter]
    cache.getOrElseUpdate(1, "a")
    cache.getOrElseUpdate(2, "b")
    cache.getOrElseUpdate(3, "c")
    cache.getOrElseUpdate(4, "d")
    println(cache.map == Map(3->"c", 4->"d"))
  }
  "Lru with 2 entries max, on keys stream <<-(1,2,2,1,3)<<-" should "give (3,1)" in {
    val cache: LRUEvictorMapCacheAdapter = new MapCacheBuilder(2).withEvictor(Eviction.LRU).build().asInstanceOf[LRUEvictorMapCacheAdapter]
    cache.getOrElseUpdate(1, "a")
    cache.getOrElseUpdate(2, "b")
    cache.getOrElseUpdate(2, "b")
    cache.getOrElseUpdate(1, "a")
    cache.getOrElseUpdate(3, "c")
    println(cache.map ==  Map(1->"a", 3->"c"))
  }
  "Cost with 2 entries max, on keys stream <<-(1,2,2,1,3)<<-" should "give (3,1)" in {
    val cache: LRUEvictorMapCacheAdapter = new MapCacheBuilder(2).withEvictor(Eviction.LRU).build().asInstanceOf[LRUEvictorMapCacheAdapter]
    cache.getOrElseUpdate(1, "a")
    cache.getOrElseUpdate({Thread.sleep(500); 2}, "b")
    cache.getOrElseUpdate(2, "b")
    cache.getOrElseUpdate(1, "a")
    cache.getOrElseUpdate({Thread.sleep(300); 3}, "c")
    cache.getOrElseUpdate(4, "d")
    println(cache.map ==  Map(2->"b", 4->"d"))
  }
}