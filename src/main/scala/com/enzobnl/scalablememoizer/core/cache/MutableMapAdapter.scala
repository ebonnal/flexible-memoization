package com.enzobnl.scalablememoizer.core.cache

import scala.collection.mutable


private[cache] class MutableMapAdapter(evictor: Evictor) extends MemoCache {
  val map: mutable.Map[Long, Any] = mutable.Map[Long, Any]()

  override def getOrElseUpdate(hash: Long, value: => Any): Any = {
    var computed = false
    val result = map.getOrElseUpdate(hash, {
      computed = true
      evictor.evict(map, hash, value)
      value
    })
    if (computed) misses += 1
    else {
      evictor.onHit(map, hash, value)
      hits += 1
    }
    result
  }
}

class MapMemoCacheBuilder private(val evictor: Evictor) extends MemoCacheBuilder {
  def this() = this(new Evictor)
  def withEvictor(evictor: Evictor): MapMemoCacheBuilder = new MapMemoCacheBuilder(evictor)

  override def build(): MemoCache = new MutableMapAdapter(evictor)
}
