package com.enzobnl.memoizationtoolbox.core.memo

trait MemoMixin {
  def apply[I, R](f: I => R): I => R
  def apply[I, R](f: I => R, accessCache: I => Boolean): I => R
  def apply[I1, I2, R](f: (I1, I2) => R): (I1, I2) => R
  def apply[I1, I2, R](f: (I1, I2) => R, accessCache: (I1, I2) => Boolean): (I1, I2) => R
  override def finalize(): Unit = {
    println("Memo finalyze")
    super.finalize()
  }
}
