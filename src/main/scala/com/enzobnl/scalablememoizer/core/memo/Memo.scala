package com.enzobnl.scalablememoizer.core.memo

import com.enzobnl.scalablememoizer.core.cache.{MemoCache, MemoCacheBuilder}

trait CacheNotifierMixin{
  val sharedCache: MemoCache
  override def finalize(): Unit = {
    println(s"${this.hashCode()} finalyzed")
    sharedCache.notifyDependencyEnd()
    super.finalize()
  }
}

class Memo(cache: MemoCache) extends MemoMixin {
  def this(cacheBuilder: MemoCacheBuilder) = this(cacheBuilder.build())
  override def apply[I, R](f: I => R): I => R = {
    cache.notifyDependencyStart()
    new (I => R) with CacheNotifierMixin {
      override  val sharedCache: MemoCache = cache
      override def apply(v1: I): R =
        sharedCache.getOrElseUpdate((f.hashCode(), v1).hashCode(), f.apply(v1)).asInstanceOf[R]
    }
  }

  override def apply[I1, I2, R](f: (I1, I2) => R): (I1, I2) => R = {
    cache.notifyDependencyStart()
    new ((I1, I2) => R) with CacheNotifierMixin{
      val sharedCache = cache
      override def apply(v1: I1, v2: I2): R=
        sharedCache.getOrElseUpdate((f.hashCode(), v1, v2).hashCode(), f.apply(v1, v2)).asInstanceOf[R]
    }
  }
}