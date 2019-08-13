package com.enzobnl.memoizationtoolbox.ignite.cache

import com.enzobnl.memoizationtoolbox.core.cache.{Cache, CacheBuilder}
import com.enzobnl.memoizationtoolbox.ignite.cache.OffHeapEviction.OffHeapEviction
import com.enzobnl.memoizationtoolbox.ignite.cache.OnHeapEviction.OnHeapEviction
import org.apache.ignite.cache.eviction.fifo.FifoEvictionPolicyFactory
import org.apache.ignite.cache.eviction.lru.LruEvictionPolicyFactory
import org.apache.ignite.cache.eviction.sorted.SortedEvictionPolicyFactory
import org.apache.ignite.configuration._

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

object OffHeapEviction extends Enumeration {
  type OffHeapEviction = Value
  /**
    * Pick randomly 5 entries and evicts least recently used entry
    */
  val RANDOM_LRU,

  /**
    * Pick randomly 5 entries and evicts the one with the oldest penultimate use
    */
  RANDOM_2_LRU = Value
}

class IgniteMemoCacheBuilder private(onHeapMaxSize: Option[Long],
                                     offHeapMaxSize: Option[Long],
                                     offHeapInitialSize: Option[Long],
                                     onHeapEvictionPolicy: OnHeapEviction,
                                     offHeapEvictionPolicy: OffHeapEviction) extends CacheBuilder {

  def this() = this(
    Some(1L * 1024 * 1024 * 1024),
    Some(500L * 1024 * 1024),
    None,
    OnHeapEviction.LRU,
    OffHeapEviction.RANDOM_2_LRU
  )

  val cacheName = s"cache$hashCode"
  val storageRegionName = s"region$hashCode"
  val igniteInstanceName = s"instance$hashCode"

  def withOnHeapMaxSize(size: Option[Long]): IgniteMemoCacheBuilder = {
    new IgniteMemoCacheBuilder(size, offHeapMaxSize, offHeapInitialSize, onHeapEvictionPolicy, offHeapEvictionPolicy)
  }

  def withOffHeapMaxSize(size: Option[Long]): IgniteMemoCacheBuilder = {
    new IgniteMemoCacheBuilder(onHeapMaxSize, size, offHeapInitialSize, onHeapEvictionPolicy, offHeapEvictionPolicy)
  }

  def withOnHeapEviction(eviction: OnHeapEviction): IgniteMemoCacheBuilder = {
    new IgniteMemoCacheBuilder(onHeapMaxSize, offHeapMaxSize, offHeapInitialSize, eviction, offHeapEvictionPolicy)
  }

  def withOffHeapEviction(eviction: OffHeapEviction): IgniteMemoCacheBuilder = {
    new IgniteMemoCacheBuilder(onHeapMaxSize, offHeapMaxSize, offHeapInitialSize, onHeapEvictionPolicy, eviction)
  }

  override def build(): Cache = {

    // Creating a new data region.
    val regionCfg = new DataRegionConfiguration()
      .setName(this.storageRegionName)

    this.offHeapInitialSize match {
      case Some(size) => regionCfg.setInitialSize(size) // 500 MB initial size (RAM).
      case None => ()
    }

    regionCfg.setPageEvictionMode(
      this.offHeapEvictionPolicy match {
        case OffHeapEviction.RANDOM_LRU => DataPageEvictionMode.RANDOM_2_LRU
        case OffHeapEviction.RANDOM_2_LRU => DataPageEvictionMode.RANDOM_LRU
      }
    )

    this.offHeapMaxSize match {
      case Some(size) => regionCfg.setMaxSize(size)
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

    new IgniteCacheAdapter(icf, this.cacheName)
  }
}
