package com.enzobnl.memoizationtoolbox.caffeine.cache

import com.enzobnl.memoizationtoolbox.core.cache.{Cache, CacheBuilder}

/**
  * Design: Functional Builder Pattern allowing fluent customization of caffeine based Cache.
  *
  * @param maxEntryNumber: Maximum number of entries in the cache
  */
class CaffeineCacheBuilder private(maxEntryNumber: Option[Long]) extends CacheBuilder {
  def this() = this(None)

  def withMaxEntryNumber(number: Option[Long]): CaffeineCacheBuilder =
    new CaffeineCacheBuilder(number)

  override def build(): Cache = maxEntryNumber match {
    case Some(number) => new CaffeineCacheAdapter(number)
    case None => new CaffeineCacheAdapter()
  }
}
