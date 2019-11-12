package com.enzobnl.flexiblememoization.cache.impl.ignite

import com.enzobnl.flexiblememoization.cache.HitCounterMixin
import com.enzobnl.flexiblememoization.cache.Cache
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.{Ignite, IgniteCache, Ignition}

/**
  * Design: Adapter Pattern wrapping Apache Ignite (fast in-memory datagrid allowing seemless
  * cache sharing among a cluster) under memoizationtoolbox.core.cache.Cache interface.
  *
  * It is not part of the public API: Client must use IgniteCacheAdapterBuilder to instanciate it.
  *
  * @param node : Ignite instance lazy param, can be derived from IgniteConfiguration
  *             by alternative constructor
  */
private[flexiblememoization] class IgniteCacheAdapter(icf: Option[IgniteConfiguration],
                                 node: => Option[Ignite]) extends Cache with HitCounterMixin {

  def this(providedIgnite: => Ignite) = this(None, Some(providedIgnite))

  def this(icf: IgniteConfiguration) = this(Some(icf), None)

  var isClosed = false

  lazy val ignite: Ignite = node match {
    // if ignite node provided, use it
    case Some(ign) => ign
    case None =>
      icf match {
        // else if config provided, use it to start ignite node with unique name
        case Some(conf) =>
          Ignition.start(conf.setIgniteInstanceName(s"instance$hashCode"))
        // else start ignite node with unique name and default config
        case None =>
          Ignition.start(new IgniteConfiguration().setIgniteInstanceName(s"instance$hashCode"))
      }
  }

  lazy val igniteCache: IgniteCache[Int, Any] = ignite.getOrCreateCache[Int, Any](IgniteCacheBuilder.CACHE_NAME)

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

