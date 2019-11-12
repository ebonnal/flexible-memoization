package com.enzobnl.flexiblememoization.memo.impl

import ca.ubc.ece.systems.ClosureHash
import com.enzobnl.flexiblememoization.cache.impl.caffeine.{CaffeineCacheAdapter, CaffeineCacheBuilder}
import com.enzobnl.flexiblememoization.cache.{Cache, CacheBuilder, HitCounterMixin, NotifiableMixin}
import com.enzobnl.flexiblememoization.cache.impl.ignite.{IgniteCacheAdapter, IgniteCacheBuilder, OnHeapEviction}
import com.enzobnl.flexiblememoization.memo.{MemoizedFunction, Memoizer}

import scala.collection.Iterable


/**
  * Core class of the framework.
  *
  * @param cache : cache.Cache instance, taken as a Strategy Pattern dependency injection.
  */
case class Memo(cache: Cache) extends Memoizer {

  def this(cacheBuilder: CacheBuilder) = this(cacheBuilder.build())

  def this() = this(Memo.DEFAULT_CACHE)

  override def apply[I, R](f: I => R): MemoizedFunction with (I => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f)) with (I => R) {
      override def apply(v1: I): R = sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1), f.apply(v1)).asInstanceOf[R]
    }
  }

  override def apply[I, R](f: I => R, trigger: I => Boolean): MemoizedFunction with (I => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f, trigger)) with (I => R) {
      override def apply(v1: I): R = {
        if (trigger(v1)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1), f.apply(v1)).asInstanceOf[R]
        else f.apply(v1)
      }
    }
  }

  override def apply[I1, I2, R](f: (I1, I2) => R): MemoizedFunction with ((I1, I2) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f)) with ((I1, I2) => R) {
      override def apply(v1: I1, v2: I2): R =
        sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2), f.apply(v1, v2)).asInstanceOf[R]
    }
  }


  override def apply[I1, I2, R](f: (I1, I2) => R, trigger: (I1, I2) => Boolean): MemoizedFunction with ((I1, I2) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f, trigger)) with ((I1, I2) => R) {
      override def apply(v1: I1, v2: I2): R = {
        if (trigger(v1, v2)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2), f.apply(v1, v2)).asInstanceOf[R]
        else f.apply(v1, v2)
      }
    }
  }

  override def apply[I1, I2, I3, R](f: (I1, I2, I3) => R): MemoizedFunction with ((I1, I2, I3) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f)) with ((I1, I2, I3) => R) {
      override def apply(v1: I1, v2: I2, v3: I3): R =
        sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2), f.apply(v1, v2, v3)).asInstanceOf[R]

    }
  }

  override def apply[I1, I2, I3, R](f: (I1, I2, I3) => R, trigger: (I1, I2, I3) => Boolean): MemoizedFunction with ((I1, I2, I3) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f, trigger)) with ((I1, I2, I3) => R) {
      override def apply(v1: I1, v2: I2, v3: I3): R = {
        if (trigger(v1, v2, v3)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2), f.apply(v1, v2, v3)).asInstanceOf[R]
        else f.apply(v1, v2, v3)
      }
    }
  }

  override def apply[I1, I2, I3, I4, R](f: (I1, I2, I3, I4) => R): MemoizedFunction with ((I1, I2, I3, I4) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f)) with ((I1, I2, I3, I4) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4): R = sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4), f.apply(v1, v2, v3, v4)).asInstanceOf[R]
    }
  }

  override

  def apply[I1, I2, I3, I4, R](f: (I1, I2, I3, I4) => R, trigger: (I1, I2, I3, I4) => Boolean): MemoizedFunction with ((I1, I2, I3, I4) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f, trigger)) with ((I1, I2, I3, I4) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4): R = {
        if (trigger(v1, v2, v3, v4)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4), f.apply(v1, v2, v3, v4)).asInstanceOf[R] else f.apply(v1, v2, v3, v4)
      }
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, R](f: (I1, I2, I3, I4, I5) => R): MemoizedFunction with ((I1, I2, I3, I4, I5) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f)) with ((I1, I2, I3, I4, I5) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5): R = sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5), f.apply(v1, v2, v3, v4, v5)).asInstanceOf[R]
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, R](f: (I1, I2, I3, I4, I5) => R, trigger: (I1, I2, I3, I4, I5) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f, trigger)) with ((I1, I2, I3, I4, I5) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5): R = {
        if (trigger(v1, v2, v3, v4, v5)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5), f.apply(v1, v2, v3, v4, v5)).asInstanceOf[R] else f.apply(v1, v2, v3, v4, v5)
      }
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, R](f: (I1, I2, I3, I4, I5, I6) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f)) with ((I1, I2, I3, I4, I5, I6) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6): R = sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6), f.apply(v1, v2, v3, v4, v5, v6)).asInstanceOf[R]
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, R](f: (I1, I2, I3, I4, I5, I6) => R, trigger: (I1, I2, I3, I4, I5, I6) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f, trigger)) with ((I1, I2, I3, I4, I5, I6) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6): R = {
        if (trigger(v1, v2, v3, v4, v5, v6)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6), f.apply(v1, v2, v3, v4, v5, v6)).asInstanceOf[R] else f.apply(v1, v2, v3, v4, v5, v6)
      }
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, R](f: (I1, I2, I3, I4, I5, I6, I7) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f)) with ((I1, I2, I3, I4, I5, I6, I7) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7): R = sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7), f.apply(v1, v2, v3, v4, v5, v6, v7)).asInstanceOf[R]
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, R](f: (I1, I2, I3, I4, I5, I6, I7) => R, trigger: (I1, I2, I3, I4, I5, I6, I7) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f, trigger)) with ((I1, I2, I3, I4, I5, I6, I7) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7): R = {
        if (trigger(v1, v2, v3, v4, v5, v6, v7)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7), f.apply(v1, v2, v3, v4, v5, v6, v7)).asInstanceOf[R] else f.apply(v1, v2, v3, v4, v5, v6, v7)
      }
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, R](f: (I1, I2, I3, I4, I5, I6, I7, I8) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f)) with ((I1, I2, I3, I4, I5, I6, I7, I8) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8): R = sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8), f.apply(v1, v2, v3, v4, v5, v6, v7, v8)).asInstanceOf[R]
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, R](f: (I1, I2, I3, I4, I5, I6, I7, I8) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f, trigger)) with ((I1, I2, I3, I4, I5, I6, I7, I8) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8): R = {
        if (trigger(v1, v2, v3, v4, v5, v6, v7, v8)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8), f.apply(v1, v2, v3, v4, v5, v6, v7, v8)).asInstanceOf[R] else f.apply(v1, v2, v3, v4, v5, v6, v7, v8)
      }
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9): R = sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9)).asInstanceOf[R]
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f, trigger)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9): R = {
        if (trigger(v1, v2, v3, v4, v5, v6, v7, v8, v9)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9)).asInstanceOf[R] else f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9)
      }
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10): R = sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10)).asInstanceOf[R]
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f, trigger)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10): R = {
        if (trigger(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10)).asInstanceOf[R] else f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10)
      }
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11): R = sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11)).asInstanceOf[R]
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f, trigger)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11): R = {
        if (trigger(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11)).asInstanceOf[R] else f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11)
      }
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11, v12: I12): R = sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12)).asInstanceOf[R]
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f, trigger)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11, v12: I12): R = {
        if (trigger(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12)).asInstanceOf[R] else f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12)
      }
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11, v12: I12, v13: I13): R = sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13)).asInstanceOf[R]
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f, trigger)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11, v12: I12, v13: I13): R = {
        if (trigger(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13)).asInstanceOf[R] else f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13)
      }
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11, v12: I12, v13: I13, v14: I14): R = sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14)).asInstanceOf[R]
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f, trigger)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11, v12: I12, v13: I13, v14: I14): R = {
        if (trigger(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14)).asInstanceOf[R] else f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14)
      }
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11, v12: I12, v13: I13, v14: I14, v15: I15): R = sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15)).asInstanceOf[R]
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f, trigger)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11, v12: I12, v13: I13, v14: I14, v15: I15): R = {
        if (trigger(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15)).asInstanceOf[R] else f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15)
      }
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11, v12: I12, v13: I13, v14: I14, v15: I15, v16: I16): R = sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16)).asInstanceOf[R]
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f, trigger)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11, v12: I12, v13: I13, v14: I14, v15: I15, v16: I16): R = {
        if (trigger(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16)).asInstanceOf[R] else f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16)
      }
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11, v12: I12, v13: I13, v14: I14, v15: I15, v16: I16, v17: I17): R = sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17)).asInstanceOf[R]
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f, trigger)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11, v12: I12, v13: I13, v14: I14, v15: I15, v16: I16, v17: I17): R = {
        if (trigger(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17)).asInstanceOf[R] else f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17)
      }
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11, v12: I12, v13: I13, v14: I14, v15: I15, v16: I16, v17: I17, v18: I18): R = sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18)).asInstanceOf[R]
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f, trigger)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11, v12: I12, v13: I13, v14: I14, v15: I15, v16: I16, v17: I17, v18: I18): R = {
        if (trigger(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18)).asInstanceOf[R] else f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18)
      }
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11, v12: I12, v13: I13, v14: I14, v15: I15, v16: I16, v17: I17, v18: I18, v19: I19): R = sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19)).asInstanceOf[R]
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f, trigger)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11, v12: I12, v13: I13, v14: I14, v15: I15, v16: I16, v17: I17, v18: I18, v19: I19): R = {
        if (trigger(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19)).asInstanceOf[R] else f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19)
      }
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11, v12: I12, v13: I13, v14: I14, v15: I15, v16: I16, v17: I17, v18: I18, v19: I19, v20: I20): R = sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20)).asInstanceOf[R]
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f, trigger)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11, v12: I12, v13: I13, v14: I14, v15: I15, v16: I16, v17: I17, v18: I18, v19: I19, v20: I20): R = {
        if (trigger(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20)).asInstanceOf[R] else f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20)
      }
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11, v12: I12, v13: I13, v14: I14, v15: I15, v16: I16, v17: I17, v18: I18, v19: I19, v20: I20, v21: I21): R = sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21)).asInstanceOf[R]
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f, trigger)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11, v12: I12, v13: I13, v14: I14, v15: I15, v16: I16, v17: I17, v18: I18, v19: I19, v20: I20, v21: I21): R = {
        if (trigger(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21)).asInstanceOf[R] else f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21)
      }
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, I22, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, I22) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, I22) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, I22) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11, v12: I12, v13: I13, v14: I14, v15: I15, v16: I16, v17: I17, v18: I18, v19: I19, v20: I20, v21: I21, v22: I22): R = sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22)).asInstanceOf[R]
    }
  }

  override

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, I22, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, I22) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, I22) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, I22) => R) = {
    new MemoizedFunction(cache, Memo.getHashCode(f, trigger)) with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, I22) => R) {
      override def apply(v1: I1, v2: I2, v3: I3, v4: I4, v5: I5, v6: I6, v7: I7, v8: I8, v9: I9, v10: I10, v11: I11, v12: I12, v13: I13, v14: I14, v15: I15, v16: I16, v17: I17, v18: I18, v19: I19, v20: I20, v21: I21, v22: I22): R = {
        if (trigger(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22), f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22)).asInstanceOf[R] else f.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22)
      }
    }
  }
}

object Memo {
  val DEFAULT_CACHE: CacheBuilder = new CaffeineCacheBuilder()

  /**
    * Compute the hashCode of any number of elems.
    * Order matters: Same elems in different order leads to different hashes
    * Elem are treated in a different manner given their type:
    * - Arrays are casted to Seq before calling hashCode() to avoid reference default comparison.
    * - Iterables are catched in oreder to prevent them to be casted and considered as
    * functions, thus leading to invalid value comparison, at least for Sets and Maps
    * - Function are compared by compilation equality using KeyChain's ClosureHash.hash
    *
    * @param args
    * @tparam I1
    * @tparam I2
    * @tparam R
    * @return
    */
  def getHashCode[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, I22, R](args: Any*): Int = {
    args.toSeq.map({
      case iter: Iterable[_] => iter.hashCode // trait Set[A] extends (A => Boolean)
      case array: Array[_] => array.toSeq.hashCode // Array are compared by reference by default
      case elem: (I1 => R) => ClosureHash.hash(elem).get.hashCode
      case elem: ((I1, I2) => R) => ClosureHash.hash(elem).get.hashCode
      case elem: ((I1, I2, I3) => R) => ClosureHash.hash(elem).get.hashCode
      case elem: ((I1, I2, I3, I4) => R) => ClosureHash.hash(elem).get.hashCode
      case elem: ((I1, I2, I3, I4, I5) => R) => ClosureHash.hash(elem).get.hashCode
      case elem: ((I1, I2, I3, I4, I5, I6) => R) => ClosureHash.hash(elem).get.hashCode
      case elem: ((I1, I2, I3, I4, I5, I6, I7) => R) => ClosureHash.hash(elem).get.hashCode
      case elem: ((I1, I2, I3, I4, I5, I6, I7, I8) => R) => ClosureHash.hash(elem).get.hashCode
      case elem: ((I1, I2, I3, I4, I5, I6, I7, I8, I9) => R) => ClosureHash.hash(elem).get.hashCode
      case elem: ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10) => R) => ClosureHash.hash(elem).get.hashCode
      case elem: ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11) => R) => ClosureHash.hash(elem).get.hashCode
      case elem: ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12) => R) => ClosureHash.hash(elem).get.hashCode
      case elem: ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13) => R) => ClosureHash.hash(elem).get.hashCode
      case elem: ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14) => R) => ClosureHash.hash(elem).get.hashCode
      case elem: ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15) => R) => ClosureHash.hash(elem).get.hashCode
      case elem: ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16) => R) => ClosureHash.hash(elem).get.hashCode
      case elem: ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17) => R) => ClosureHash.hash(elem).get.hashCode
      case elem: ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18) => R) => ClosureHash.hash(elem).get.hashCode
      case elem: ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19) => R) => ClosureHash.hash(elem).get.hashCode
      case elem: ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20) => R) => ClosureHash.hash(elem).get.hashCode
      case elem: ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21) => R) => ClosureHash.hash(elem).get.hashCode
      case elem: ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, I22) => R) => ClosureHash.hash(elem).get.hashCode
      case elem => elem.hashCode()
    }).hashCode()
  }
}
