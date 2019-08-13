package com.enzobnl.memoizationtoolbox.ignite.cache

import com.enzobnl.memoizationtoolbox.core.cache.{Cache, CacheBuilder}
import com.enzobnl.memoizationtoolbox.ignite.cache.OffHeapEviction.OffHeapEviction
import com.enzobnl.memoizationtoolbox.ignite.cache.OnHeapEviction.OnHeapEviction
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.cache.eviction.fifo.FifoEvictionPolicyFactory
import org.apache.ignite.cache.eviction.lru.LruEvictionPolicyFactory
import org.apache.ignite.cache.eviction.sorted.SortedEvictionPolicyFactory
import org.apache.ignite.configuration._


/**
  * Design: Functional Builder Pattern allowing fluent customization of ignite based Cache.
  *
  * @param onHeapMaxSize         : in Bytes
  * @param offHeapMaxSize        : in Bytes
  * @param offHeapInitialSize    : in Bytes
  * @param onHeapEvictionPolicy  : Policy used to move entries from on-heap to off-heap when on-heap
  *                              reaches onHeapMaxSize.
  * @param offHeapEvictionPolicy : Policy used to evict entries from off-heap when offHeapMaxSize reached.
  */
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

  val igniteInstanceName = s"instance$hashCode" // One per IgniteCacheAdapterBuilder

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
      .setName(IgniteMemoCacheBuilder.STORAGE_REGION_NAME)

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

    val cacheConfig: CacheConfiguration[Long, Any] = new CacheConfiguration(IgniteMemoCacheBuilder.CACHE_NAME)
      .setOnheapCacheEnabled(true) // on heap cache backed by off-heap + eviction
      .setCacheMode(CacheMode.PARTITIONED) // already default

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
      // ensures 2 nodes can be started on same JVM, thus close of one node triggered by end of use of*
      // one IgniteCacheAdapter still let others have their lives
      .setIgniteInstanceName(this.igniteInstanceName)
      .setDataStorageConfiguration(storageCfg)

    new IgniteCacheAdapter(icf)
  }
}

object IgniteMemoCacheBuilder {
  val CACHE_NAME = "MemoizationCache" // always same name to have a sharing among cluster
  val STORAGE_REGION_NAME = s"MemoizationStorageRegion"
}