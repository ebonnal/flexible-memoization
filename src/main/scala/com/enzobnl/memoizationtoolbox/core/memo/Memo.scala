package com.enzobnl.memoizationtoolbox.core.memo

import com.enzobnl.memoizationtoolbox.core.cache.{Cache, CacheBuilder}

trait CacheNotifierMixin {
  val sharedCache: Cache

  override def finalize(): Unit = {
    println(s"${this.hashCode()} finalyzed")
    sharedCache.notifyDependencyEnd()
    super.finalize()
  }
}

class Memo(cache: Cache) extends MemoMixin {
  def this(cacheBuilder: CacheBuilder) = this(cacheBuilder.build())

  override def apply[I, R](f: I => R): I => R = {
    cache.notifyDependencyStart()
    new (I => R) with CacheNotifierMixin {
      override val sharedCache: Cache = cache

      override def apply(v1: I): R =
        sharedCache.getOrElseUpdate((f.hashCode(), v1).hashCode(), f.apply(v1)).asInstanceOf[R]
    }
  }

  override def apply[I1, I2, R](f: (I1, I2) => R): (I1, I2) => R = {
    cache.notifyDependencyStart()
    new ((I1, I2) => R) with CacheNotifierMixin {
      override val sharedCache = cache

      override def apply(v1: I1, v2: I2): R =
        sharedCache.getOrElseUpdate((f.hashCode(), v1, v2).hashCode(), f.apply(v1, v2)).asInstanceOf[R]
    }
  }

  override def apply[I, R](f: I => R, accessCache: I => Boolean): I => R = {
    cache.notifyDependencyStart()
    new (I => R) with CacheNotifierMixin {
      override val sharedCache: Cache = cache

      override def apply(v1: I): R = {
        if (accessCache(v1))
          sharedCache.getOrElseUpdate((f.hashCode(), v1).hashCode(), f.apply(v1)).asInstanceOf[R]
        else
          f.apply(v1)
      }
    }
  }

  override def apply[I1, I2, R](f: (I1, I2) => R, accessCache: (I1, I2) => Boolean): (I1, I2) => R = {
    cache.notifyDependencyStart()
    new ((I1, I2) => R) with CacheNotifierMixin {
      override val sharedCache: Cache = cache

      override def apply(v1: I1, v2: I2): R = {
        if (accessCache(v1, v2))
          sharedCache.getOrElseUpdate((f.hashCode(), v1, v2).hashCode(), f.apply(v1, v2)).asInstanceOf[R]
        else
          f.apply(v1, v2)
      }
    }
  }
}