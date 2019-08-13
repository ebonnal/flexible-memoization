package com.enzobnl.memoizationtoolbox.ignite.cache

import com.enzobnl.memoizationtoolbox.core.cache.Cache
import org.apache.ignite.{Ignite, IgniteCache, Ignition, configuration}

/**
  * Design: Adapter Pattern wrapping Apache Ignite (fast in-memory datagrid allowing seemless
  * cache sharing among a cluster) under memoizationtoolbox.core.cache.Cache interface.
  *
  * It is not part of the public API: Client must use IgniteCacheAdapterBuilder to instanciate it.
  *
  * @param ignite
  */
private class IgniteCacheAdapter(ignite: Ignite) extends Cache {

  def this(icf: configuration.IgniteConfiguration) = this(Ignition.start(icf))

  var isClosed = false
  val igniteCache: IgniteCache[Int, Any] = ignite.getOrCreateCache[Int, Any](IgniteMemoCacheBuilder.CACHE_NAME)

  override def getOrElseUpdate(key: Int, value: => Any): Any = {
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
    if (!isClosed) {
      ignite.close()
      isClosed = true
    }
  }

  override def finalize(): Unit = close()

}

