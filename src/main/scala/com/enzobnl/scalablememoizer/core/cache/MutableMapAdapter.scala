package com.enzobnl.scalablememoizer.core.cache

import scala.collection.mutable


class MutableMapAdapter(evictor: Evictor = new NoEvictor) extends MemoCache {
  val map: mutable.Map[Long, Any] = mutable.Map[Long, Any]()

  override def getOrElseUpdate(hash: Long, value: => Any): Any = {
    var computed = false
    val result = map.getOrElseUpdate(hash, {
      computed = true; evictor.evict(map, hash, value); value
    })
    if (computed) misses += 1
    else {
      evictor.onHit(map, hash, value)
      hits += 1
    }
    result
  }
}

