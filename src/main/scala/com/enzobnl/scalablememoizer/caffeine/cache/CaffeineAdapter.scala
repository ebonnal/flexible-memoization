package com.enzobnl.scalablememoizer.caffeine.cache

import com.enzobnl.scalablememoizer.core.cache.{MemoCache, MemoCacheBuilder}
import com.github.blemale.scaffeine.{Cache, Scaffeine}

private class CaffeineAdapter(scaffeine: Scaffeine[Any, Any]) extends MemoCache {
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

class CaffeineMemoCacheBuilder private(maxEntryNumber: Option[Long]) extends MemoCacheBuilder {
  def this() = this(None)
  def withMaxEntryNumber(number: Option[Long]): CaffeineMemoCacheBuilder =
    new CaffeineMemoCacheBuilder(number)
  override def build(): MemoCache = maxEntryNumber match {
    case Some(number) => new CaffeineAdapter(number)
    case None => new CaffeineAdapter()
  }
}