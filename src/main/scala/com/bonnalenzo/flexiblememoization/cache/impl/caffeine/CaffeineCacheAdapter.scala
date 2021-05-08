package com.bonnalenzo.flexiblememoization.cache.impl.caffeine

import com.bonnalenzo.flexiblememoization.cache.{Cache, HitCounterMixin}
import com.github.blemale.scaffeine.Scaffeine

/**
  * Design: Adapter Pattern wrapping Caffeine (fast mono node cache with versatile w_TinyLFU
  * eviction) under memoizationtoolbox.core.cache.Cache interface.
  *
  * It is not part of the public API: Client must use CaffeineCacheAdapterBuilder to instanciate it.
  * @param scaffeine: scala wrapper around caffeine.cache.Caffeine[K, V]
  */
private[flexiblememoization] class CaffeineCacheAdapter(scaffeine: Scaffeine[Any, Any]) extends Cache with HitCounterMixin{
  def this(maxEntryNumber: Long) = this(Scaffeine().maximumSize(maxEntryNumber))

  def this() = this(Scaffeine())

  lazy val caffeineCache: com.github.blemale.scaffeine.Cache[Int, Any] = scaffeine.build[Int, Any]()

  override def getOrElseUpdate(key: Int, value: => Any): Any = {
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

