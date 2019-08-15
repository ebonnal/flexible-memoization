package com.enzobnl.flexiblememoization.cache

/**
  * Mimic the usefull getOrElseUpdate method from mutable.Map
  */
trait Gettable {
  def getOrElseUpdate(key: Int, value: => Any): Any
}

trait ClosableMixin {
  def close(): Unit = ()
}

/** Design: Observer Pattern that listen on its notifyDependencyStart and
  * notifyDependencyEnd methods.
  */
trait NotifiableMixin extends ClosableMixin {
  private[flexiblememoization] var nSubjects: Int = 0

  def notifyDependencyStart(): Unit = nSubjects += 1

  def notifyDependencyEnd(): Unit = {
    nSubjects -= 1
    tryToClose()
  }

  /**
    * If there is no more subjects observed, close
    */
  def tryToClose(): Unit = if (nSubjects == 0) close()
}

/**
  * Define two counters for hits and misses and provide access to their tuple and hit ratio.
  */
trait HitCounterMixin {
  protected[flexiblememoization] var hits = 0L
  protected[flexiblememoization] var misses = 0L

  def getHitsAndMisses: (Long, Long) = (hits, misses)
  def getHitRatio: Float = hits.toFloat/(hits + misses)

}

/**
  * Base trait needed by core.memo.Memo
  */
trait Cache extends Gettable with NotifiableMixin with HitCounterMixin

trait CacheBuilder {
  def build(): Cache
}