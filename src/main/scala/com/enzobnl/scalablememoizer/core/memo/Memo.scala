package com.enzobnl.scalablememoizer.core.memo

import com.enzobnl.scalablememoizer.core.cache.{GettableCache, Notifiable}


class Memo(memoCache: GettableCache with Notifiable) extends MemoMixin {
  override def apply[I, R](f: I => R, accessCache: Option[I => R]=None): I => R = {
    memoCache.notifyDependencyStart()

    new (I => R){
      val sharedMemoizationCache: GettableCache with Notifiable = memoCache

      override def apply(v1: I): R =
        sharedMemoizationCache.getOrElseUpdate((f.hashCode(), v1).hashCode(), f.apply(v1)).asInstanceOf[R]

      override def finalize(): Unit ={
        println(s"${this.hashCode()} finalyzed")
        sharedMemoizationCache.notifyDependencyEnd()
        super.finalize()
      }
    }
  }

  override def apply[I1, I2, R](f: (I1, I2) => R, accessCache: Option[(I1, I2) => R]=None): (I1, I2) => R = {
    memoCache.notifyDependencyStart()

    new ((I1, I2) => R){

      val sharedMemoizationCache: GettableCache with Notifiable = memoCache

      override def apply(v1: I1, v2: I2): R =
        sharedMemoizationCache.getOrElseUpdate((f.hashCode(), v1, v2).hashCode(), f.apply(v1, v2)).asInstanceOf[R]

      override def finalize(): Unit ={
        println(s"${this.hashCode()} finalyzed")
        sharedMemoizationCache.notifyDependencyEnd()
        super.finalize()
      }
    }
  }
  override def finalize(): Unit = {
    println("Memo finalyze")
    super.finalize()
  }
}