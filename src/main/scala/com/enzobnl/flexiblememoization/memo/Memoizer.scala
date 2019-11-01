package com.enzobnl.flexiblememoization.memo

import com.enzobnl.flexiblememoization.cache.Cache

/**
  * Mixin mixed in FunctionX anonymous class implementation returned by Memoizer 'applies' methods.
  *
  * Design: It is the subject class in the Observer Pattern shared with cache.Cache.
  * It is registering the Cache observer inside sharedCache attribute.
  * The signals it sends to it are:
  * - notifyDependencyStart(): One time at the MemoizedFunc instanciation
  * - notifyDependencyEnd(): InMemoizedFunc's finalyze, when it is garbage collected
  *
  * @param sharedCache : cache shared among all the functions memoized by the same Memoizer
  * @param id          : unique id corresponding to the non memoized function object
  */
private[flexiblememoization] class MemoizedFunction(val sharedCache: Cache, val id: Int) {
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
  * Example: def apply[I, R](f: I => R): MemoizedFunc with (I => R)
  *
  * - The second one take a second function 'trigger' (with the same input signature as f,
  * but returning a Boolean) as argument. The output is a memoized function that triggers its
  * caching capabilities only if the result of 'trigger' on the inputs gives true.
  * Example: def apply[I, R](f: I => R, trigger: I => Boolean): MemoizedFunc with (I => R)
  *
  *
  * The core implementation of this trait is core.memo.Memo
  */
trait Memoizer {

  def apply[I, R](f: I => R): MemoizedFunction with (I => R)

  def apply[I, R](f: I => R, trigger: I => Boolean): MemoizedFunction with (I => R)

  def apply[I1, I2, R](f: (I1, I2) => R): MemoizedFunction with ((I1, I2) => R)

  def apply[I1, I2, R](f: (I1, I2) => R, trigger: (I1, I2) => Boolean): MemoizedFunction with ((I1, I2) => R)

  def apply[I1, I2, I3, R](f: (I1, I2, I3) => R): MemoizedFunction with ((I1, I2, I3) => R)

  def apply[I1, I2, I3, R](f: (I1, I2, I3) => R, trigger: (I1, I2, I3) => Boolean): MemoizedFunction with ((I1, I2, I3) => R)

  def apply[I1, I2, I3, I4, R](f: (I1, I2, I3, I4) => R): MemoizedFunction with ((I1, I2, I3, I4) => R)

  def apply[I1, I2, I3, I4, R](f: (I1, I2, I3, I4) => R, trigger: (I1, I2, I3, I4) => Boolean): MemoizedFunction with ((I1, I2, I3, I4) => R)

  def apply[I1, I2, I3, I4, I5, R](f: (I1, I2, I3, I4, I5) => R): MemoizedFunction with ((I1, I2, I3, I4, I5) => R)

  def apply[I1, I2, I3, I4, I5, R](f: (I1, I2, I3, I4, I5) => R, trigger: (I1, I2, I3, I4, I5) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5) => R)

  def apply[I1, I2, I3, I4, I5, I6, R](f: (I1, I2, I3, I4, I5, I6) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6) => R)

  def apply[I1, I2, I3, I4, I5, I6, R](f: (I1, I2, I3, I4, I5, I6) => R, trigger: (I1, I2, I3, I4, I5, I6) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, R](f: (I1, I2, I3, I4, I5, I6, I7) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, R](f: (I1, I2, I3, I4, I5, I6, I7) => R, trigger: (I1, I2, I3, I4, I5, I6, I7) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, R](f: (I1, I2, I3, I4, I5, I6, I7, I8) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, R](f: (I1, I2, I3, I4, I5, I6, I7, I8) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, I22, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, I22) => R): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, I22) => R)

  def apply[I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, I22, R](f: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, I22) => R, trigger: (I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, I22) => Boolean): MemoizedFunction with ((I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, I17, I18, I19, I20, I21, I22) => R)
}
