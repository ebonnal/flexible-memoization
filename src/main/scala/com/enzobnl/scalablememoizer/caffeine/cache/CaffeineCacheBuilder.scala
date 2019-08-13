package com.enzobnl.scalablememoizer.caffeine.cache

import com.enzobnl.scalablememoizer.core.cache.{Cache, CacheBuilder}

class CaffeineCacheBuilder private(maxEntryNumber: Option[Long]) extends CacheBuilder {
  def this() = this(None)

  def withMaxEntryNumber(number: Option[Long]): CaffeineCacheBuilder =
    new CaffeineCacheBuilder(number)

  override def build(): Cache = maxEntryNumber match {
    case Some(number) => new CaffeineCacheAdapter(number)
    case None => new CaffeineCacheAdapter()
  }
}
