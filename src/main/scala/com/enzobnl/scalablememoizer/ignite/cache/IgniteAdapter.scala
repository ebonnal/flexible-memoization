package com.enzobnl.scalablememoizer.ignite.cache

import com.enzobnl.scalablememoizer.core.cache.{MemoCache, MemoCacheBuilder}
import com.enzobnl.scalablememoizer.ignite.cache.OnHeapEviction.OnHeapEviction
import org.apache.ignite.cache.eviction.fifo.FifoEvictionPolicyFactory
import org.apache.ignite.cache.eviction.lru.LruEvictionPolicyFactory
import org.apache.ignite.cache.eviction.sorted.SortedEvictionPolicyFactory
import org.apache.ignite.configuration._
import org.apache.ignite.{Ignite, IgniteCache, Ignition}

object OnHeapEviction extends Enumeration {
  type OnHeapEviction = Value
  /**
    * Evicts least recently used entry
    */
  val LRU,

  /**
    * Evicts oldest entry in the cache
    */
  FIFO,

  /**
    * Evicts entry with smaller value (can use user comparison func)
    */
  SORTED = Value
}

private class IgniteAdapter(ignite: Ignite, cacheName: String) extends MemoCache {
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

class IgniteMemoCacheBuilder private
(onHeapMaxSize: Option[Long],
 offHeapMaxSize: Option[Long],
 onHeapEvictionPolicy: OnHeapEviction) extends MemoCacheBuilder {

  def this() = this(Some(1L * 1024 * 1024 * 1024), None, OnHeapEviction.LRU)

  val cacheName = s"cache$hashCode"
  val storageRegionName = s"region$hashCode"
  val igniteInstanceName = s"instance$hashCode"

  def withOnHeapMaxSize(size: Option[Long]): IgniteMemoCacheBuilder = {
    new IgniteMemoCacheBuilder(size, offHeapMaxSize, onHeapEvictionPolicy)
  }

  def withOffHeapMaxSize(size: Option[Long]): IgniteMemoCacheBuilder = {
    new IgniteMemoCacheBuilder(onHeapMaxSize, size, onHeapEvictionPolicy)
  }

  def withEviction(eviction: OnHeapEviction): IgniteMemoCacheBuilder = {
    new IgniteMemoCacheBuilder(onHeapMaxSize, offHeapMaxSize, eviction)
  }

  override def build(): MemoCache = {

    // Creating a new data region.
    val regionCfg = new DataRegionConfiguration()
      .setName(this.storageRegionName)
      .setInitialSize(IgniteMemoCacheBuilder.OFF_HEAP_INITIAL_SIZE)
      .setPageEvictionMode(DataPageEvictionMode.RANDOM_2_LRU)

    this.offHeapMaxSize match {
      case Some(size) => regionCfg.setMaxSize(size) // 500 MB initial size (RAM).
      case None => ()
    }

    val storageCfg = new DataStorageConfiguration().setDataRegionConfigurations(regionCfg)

    val cacheConfig: CacheConfiguration[Long, Any] = new CacheConfiguration(this.cacheName)
      .setOnheapCacheEnabled(true) // on heap cache backed by off-heap + eviction

    val evictionPolicyFactory = this.onHeapEvictionPolicy match {
      case OnHeapEviction.LRU =>
        new LruEvictionPolicyFactory()
          .asInstanceOf[LruEvictionPolicyFactory[Long, Any]]
      case OnHeapEviction.FIFO =>
        new FifoEvictionPolicyFactory()
          .asInstanceOf[FifoEvictionPolicyFactory[Long, Any]]
      case OnHeapEviction.SORTED =>
        new SortedEvictionPolicyFactory()
          .asInstanceOf[SortedEvictionPolicyFactory[Long, Any]]
    }
    this.onHeapMaxSize match {
      case Some(size) => evictionPolicyFactory.setMaxMemorySize(size)
      case None => ()
    }

    cacheConfig.setEvictionPolicyFactory(evictionPolicyFactory)

    val icf: IgniteConfiguration = new IgniteConfiguration()
      .setCacheConfiguration(cacheConfig)
      .setIgniteInstanceName(this.igniteInstanceName) // ensures 2 nodes can be started on same JVM
      .setDataStorageConfiguration(storageCfg)

    new IgniteAdapter(icf, this.cacheName)
  }
}

object IgniteMemoCacheBuilder {
  val OFF_HEAP_INITIAL_SIZE: Long = 500L * 1024 * 1024
}