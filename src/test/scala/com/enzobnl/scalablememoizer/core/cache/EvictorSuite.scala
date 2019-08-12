package com.enzobnl.scalablememoizer.core.cache

import org.scalatest.FlatSpec


class EvictorSuite extends FlatSpec {


  "FifoEviction(2) on keys stream <<-(1,2,3,4)<<-" should "give (3,4)" in {
    val cache: MutableMapAdapter = new MutableMapAdapter(new FIFOEvictor(2))
    cache.getOrElseUpdate(1, "a")
    cache.getOrElseUpdate(2, "b")
    cache.getOrElseUpdate(3, "c")
    cache.getOrElseUpdate(4, "d")
    println(cache.map == Map(3->"c", 4->"d"))
  }
  "LruEviction(2) on keys stream <<-(1,2,2,1,3)<<-" should "give (3,1)" in {
    val cache: MutableMapAdapter = new MutableMapAdapter(new LRUEvictor(2))
    cache.getOrElseUpdate(1, "a")
    cache.getOrElseUpdate(2, "b")
    cache.getOrElseUpdate(2, "b")
    cache.getOrElseUpdate(1, "a")
    cache.getOrElseUpdate(3, "c")
    println(cache.map ==  Map(1->"a", 3->"c"))
  }
}