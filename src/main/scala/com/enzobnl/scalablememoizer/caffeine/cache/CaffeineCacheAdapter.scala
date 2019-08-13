package com.enzobnl.scalablememoizer.caffeine.cache

import com.enzobnl.scalablememoizer.core.cache.Cache
import com.github.blemale.scaffeine.Scaffeine

private class CaffeineCacheAdapter(scaffeine: Scaffeine[Any, Any]) extends Cache {
  def this(maxEntryNumber: Long) = this(Scaffeine().maximumSize(maxEntryNumber))

  def this() = this(Scaffeine())

  lazy val caffeineCache: com.github.blemale.scaffeine.Cache[Long, Any] =
    scaffeine.build[Long, Any]()

  override def getOrElseUpdate(key: Long, value: => Any): Any = {
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

