package com.enzobnl.memoizationtoolbox.core.cache


trait Gettable {
  def getOrElseUpdate(key: Long, value: => Any): Any
}

trait ClosableMixin {
  def close(): Unit = ()
}

trait NotifiableMixin extends ClosableMixin {
  private var nSubjects: Int = 0

  def notifyDependencyStart(): Unit = nSubjects += 1

  def notifyDependencyEnd(): Unit = {
    nSubjects -= 1
    tryToClose()
  }

  def tryToClose(): Unit = if (nSubjects == 0) close()
}

trait HitCounterMixin {
  protected[memoizationtoolbox] var hits = 0L
  protected[memoizationtoolbox] var misses = 0L

  def getHitsAndMisses: (Long, Long) = (hits, misses)
}

/** Base trait for memoization caches
  *
  * Design: Observer pattern that listen on its notifyDependencyStart and
  * notifyDependencyEnd methods. Memo is charged to register it as
  * member of memoized functions.
  */
trait Cache extends Gettable with NotifiableMixin with HitCounterMixin

trait CacheBuilder {
  def build(): Cache
}