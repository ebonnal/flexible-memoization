package com.enzobnl.memoizationtoolbox.memo

import ca.ubc.ece.systems.ClosureHash
import com.enzobnl.memoizationtoolbox.cache.caffeine.CaffeineCacheBuilder
import com.enzobnl.memoizationtoolbox.cache.{Cache, CacheBuilder}

import scala.collection.immutable.SortedSet
import scala.collection.{Iterable, mutable}


/**
  * Core class of the framework.
  *
  * @param cache : default is set to new core.cache.MapCacheBuilder().build()
  */
class Memo(cache: Cache) extends Memoizer {
  def this() = this(Memo.DEFAULT_CACHE)

  def this(cacheBuilder: CacheBuilder) = this(cacheBuilder.build())

  override def apply[I, R](f: I => R): MemoizedFunc with (I => R) = {
    new MemoizedFunc(cache, Memo.getHashCode(f)) with (I => R) {
      override def apply(v1: I): R = sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1), f.apply(v1)).asInstanceOf[R]
    }
  }


  override def apply[I1, I2, R](f: (I1, I2) => R): MemoizedFunc with ((I1, I2) => R) = {
    new MemoizedFunc(cache, Memo.getHashCode(f)) with ((I1, I2) => R) {
      override def apply(v1: I1, v2: I2): R =
        sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2), f.apply(v1, v2)).asInstanceOf[R]
    }
  }

  override def apply[I1, I2, I3, R](f: (I1, I2, I3) => R): MemoizedFunc with ((I1, I2, I3) => R) = {
    new MemoizedFunc(cache, Memo.getHashCode(f)) with ((I1, I2, I3) => R) {
      override def apply(v1: I1, v2: I2, v3: I3): R =
        sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2), f.apply(v1, v2, v3)).asInstanceOf[R]

    }
  }

  override def apply[I, R](f: I => R, trigger: I => Boolean): MemoizedFunc with (I => R) = {
    new MemoizedFunc(cache, Memo.getHashCode(f)) with (I => R) {
      override def apply(v1: I): R = {
        if (trigger(v1)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1), f.apply(v1)).asInstanceOf[R]
        else f.apply(v1)
      }
    }
  }

  override def apply[I1, I2, R](f: (I1, I2) => R, trigger: (I1, I2) => Boolean): MemoizedFunc with ((I1, I2) => R) = {
    new MemoizedFunc(cache, Memo.getHashCode(f)) with ((I1, I2) => R) {
      override def apply(v1: I1, v2: I2): R = {
        if (trigger(v1, v2)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2), f.apply(v1, v2)).asInstanceOf[R]
        else f.apply(v1, v2)
      }
    }
  }

  override def apply[I1, I2, I3, R](f: (I1, I2, I3) => R, trigger: (I1, I2, I3) => Boolean): MemoizedFunc with ((I1, I2, I3) => R) = {
    new MemoizedFunc(cache, Memo.getHashCode(f)) with ((I1, I2, I3) => R) {
      override def apply(v1: I1, v2: I2, v3: I3): R = {
        if (trigger(v1, v2, v3)) sharedCache.getOrElseUpdate(Memo.getHashCode(id, v1, v2), f.apply(v1, v2, v3)).asInstanceOf[R]
        else f.apply(v1, v2, v3)
      }
    }
  }
}

object Memo {
  val DEFAULT_CACHE: Cache = new CaffeineCacheBuilder().build()

  /**
    * Compute the hashCode of any number of elems.
    * Order matters: Same elems in different order leads to different hashes
    * Elem are treated in a different manner given their type:
    * - Arrays are casted to Seq before calling hashCode() to avoid reference default comparison.
    * - Iterables are catched in oreder to prevent them to be casted and considered as
    * functions, thus leading to invalid value comparison, at least for Sets and Maps
    * - Function are compared by compilation equality using KeyChain's ClosureHash.hash
    *
    * @param elems
    * @tparam I1
    * @tparam I2
    * @tparam R
    * @return
    */
  def getHashCode[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, I22, R](elems: Any*): Int = {
    elems.toSeq.map({
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