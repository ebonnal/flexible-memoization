package com.enzobnl.flexiblememoization.cache.impl.caffeine

import com.enzobnl.flexiblememoization.cache.CacheBuilder
import com.enzobnl.flexiblememoization.cache.Cache

/**
  * Design: Functional Builder Pattern allowing fluent customization of caffeine based Cache.
  *
  * @param maxEntryNumber: Maximum number of entries in the cache
  */
class CaffeineCacheBuilder private(maxEntryNumber: Option[Long]) extends CacheBuilder {
  def this() = this(None)

  def withMaxEntryNumber(number: Long): CaffeineCacheBuilder =
    new CaffeineCacheBuilder(Some(number))

  override def build(): Cache = maxEntryNumber match {
    case Some(number) => new CaffeineCacheAdapter(number)
    case None => new CaffeineCacheAdapter()
  }
}
