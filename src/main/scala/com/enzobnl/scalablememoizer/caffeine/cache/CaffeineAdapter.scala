package com.enzobnl.scalablememoizer.caffeine.cache

import com.enzobnl.scalablememoizer.core.cache.MemoCache
import com.github.blemale.scaffeine.{Cache, Scaffeine}

class CaffeineAdapter(scaffeine: Scaffeine[Any, Any]) extends MemoCache {
  def this(maxEntryNumber: Long) = this(Scaffeine().maximumSize(maxEntryNumber))
  def this() = this(Scaffeine())

  lazy val caffeineCache: Cache[Long, Any] = scaffeine.build[Long, Any]()

  override def getOrElseUpdate(key: Long, value: => Any): Any = {
    println(caffeineCache.hashCode())
    caffeineCache.getIfPresent(key) match {
      case Some(v) =>
        hits += 1
        v
      case None =>
        misses += 1
        val v = value
        caffeineCache.put(key, v)
        v
    }
  }

  override def close(): Unit = {
    caffeineCache.cleanUp()
  }
}

