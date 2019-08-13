package com.enzobnl.memoizationtoolbox.core.memo

import com.enzobnl.memoizationtoolbox.core.cache.{Cache, CacheBuilder, MapCacheBuilder}


/**
  * Core class of the framework.
  *
  * @param cache: default is set to new core.cache.MapCacheBuilder().build()
  */
class Memo(cache: Cache=new MapCacheBuilder().build()) extends Memoizer {
  def this(cacheBuilder: CacheBuilder) = this(cacheBuilder.build())

  override def apply[I, R](f: I => R): MemoizedFunc with (I => R) = {
    new MemoizedFunc(cache, f.hashCode()) with (I => R) {
      override def apply(v1: I): R = sharedCache.getOrElseUpdate((id, v1).hashCode(), f.apply(v1)).asInstanceOf[R]
    }
  }


  override def apply[I1, I2, R](f: (I1, I2) => R): MemoizedFunc with ((I1, I2) => R) = {
    new MemoizedFunc(cache, f.hashCode()) with ((I1, I2) => R) {
      override def apply(v1: I1, v2: I2): R =
        sharedCache.getOrElseUpdate((id, v1, v2).hashCode(), f.apply(v1, v2)).asInstanceOf[R]
    }
  }

  override def apply[I, R](f: I => R, trigger: I => Boolean): MemoizedFunc with (I => R) = {
    new MemoizedFunc(cache, f.hashCode()) with (I => R) {
      override def apply(v1: I): R = {
        if (trigger(v1)) sharedCache.getOrElseUpdate((id, v1).hashCode(), f.apply(v1)).asInstanceOf[R]
        else f.apply(v1)
      }
    }
  }

  override def apply[I1, I2, R](f: (I1, I2) => R, trigger: (I1, I2) => Boolean): MemoizedFunc with ((I1, I2) => R) = {
    new MemoizedFunc(cache, f.hashCode()) with ((I1, I2) => R) {
      override def apply(v1: I1, v2: I2): R = {
        if (trigger(v1, v2)) sharedCache.getOrElseUpdate((id, v1, v2).hashCode(), f.apply(v1, v2)).asInstanceOf[R]
        else f.apply(v1, v2)
      }
    }
  }
}