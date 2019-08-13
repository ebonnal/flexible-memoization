package com.enzobnl.scalablememoizer.ignite.cache

import com.enzobnl.scalablememoizer.core.cache.Cache
import org.apache.ignite.configuration._
import org.apache.ignite.{Ignite, IgniteCache, Ignition}

private class IgniteCacheAdapter(ignite: Ignite, cacheName: String) extends Cache {

  def this(icf: IgniteConfiguration, cacheName: String) = this(Ignition.start(icf), cacheName)

  val igniteCache: IgniteCache[Long, Any] = ignite.getOrCreateCache[Long, Any](cacheName)

  override def getOrElseUpdate(key: Long, value: => Any): Any = {
    igniteCache.get(key) match {
      case v: Any =>
        hits += 1
        v
      case _ =>
        misses += 1
        val v = value
        igniteCache.put(key, v)
        v
    }
  }

  override def close(): Unit = {
    ignite.close()
  }
}

