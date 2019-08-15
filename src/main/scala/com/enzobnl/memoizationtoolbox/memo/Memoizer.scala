package com.enzobnl.memoizationtoolbox.memo

import com.enzobnl.memoizationtoolbox.cache.Cache

/**
  * Mixin mixed in FunctionX anonymous class implementation returned by Memoizer 'applies' methods.
  *
  * Design: It is the subject class in the Observer Pattern shared with core.cache.Cache.
  * It is registering the Cache observer inside sharedCache attribute.
  * The signals it sends to it are:
  * - notifyDependencyStart(): One time at the MemoizedFunc instanciation
  * - notifyDependencyEnd(): InMemoizedFunc's finalyze, when it is garbage collected
  *
  * @param sharedCache: cache shared among all the functions memoized by the same Memoizer
  * @param id: unique id corresponding to the non memoized function object
  */
private[memo] class MemoizedFunc(val sharedCache: Cache, val id: Int) {
  sharedCache.notifyDependencyStart()

  override def finalize(): Unit = sharedCache.notifyDependencyEnd()
}

/**
  * Design: "Decorating" Factory Pattern that exposes overloaded apply methods that together
  * can handle any scala function signature and return its memoized version: a MemoizedFunc instance
  * mixed with the function type taken in input.
  *
  * Each function has access to two overloaded apply methods:
  *
  * - The first one performs the standard memoization.
  *   Example: def apply[I, R](f: I => R): MemoizedFunc with (I => R)
  *
  * - The second one take a second function 'trigger' (with the same input signature as f,
  *   but returning a Boolean) as argument. The output is a memoized function that triggers its
  *   caching capabilities only if the result of 'trigger' on the inputs gives true.
  *   Example: def apply[I, R](f: I => R, trigger: I => Boolean): MemoizedFunc with (I => R)
  *
  *
  * The core implementation of this trait is core.memo.Memo
  */
trait Memoizer {

  def apply[I, R](f: I => R): MemoizedFunc with (I => R)

  def apply[I, R](f: I => R, trigger: I => Boolean): MemoizedFunc with (I => R)

  def apply[I1, I2, R](f: (I1, I2) => R): MemoizedFunc with ((I1, I2) => R)

  def apply[I1, I2, R](f: (I1, I2) => R, trigger: (I1, I2) => Boolean): MemoizedFunc with ((I1, I2) => R)

  def apply[I1, I2, I3, R](f: (I1, I2, I3) => R): MemoizedFunc with ((I1, I2, I3) => R)

  def apply[I1, I2, I3, R](f: (I1, I2, I3) => R, trigger: (I1, I2, I3) => Boolean): MemoizedFunc with ((I1, I2, I3) => R)

}
