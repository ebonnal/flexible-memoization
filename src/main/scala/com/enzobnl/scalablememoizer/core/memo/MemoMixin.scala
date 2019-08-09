package com.enzobnl.scalablememoizer.core.memo


trait MemoMixin{
  def apply[I, R](f: I => R): I => R
  def apply[I1, I2, R](f: (I1, I2) => R): (I1, I2) => R
}