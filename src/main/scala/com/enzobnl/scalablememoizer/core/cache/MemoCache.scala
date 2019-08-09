package com.enzobnl.scalablememoizer.core.cache

trait Notifiable {
  def notifyDependencyStart(): Unit = ()

  def notifyDependencyEnd(): Unit = ()
}

trait Closable {
  def close(): Unit = ()
}

trait GettableCache {
  def getOrElseUpdate(key: Long, value: => Any): Any
}

trait HitCounter{
  def getHitsMisses: (Long, Long)
}

/** Base trait for memoization caches
  *
  * Design: Observer pattern that listen on its notifyDependencyStart and
  * notifyDependencyEnd methods. Memo is charged to register it as
  * member of memoized functions.
  */
trait MemoCache extends Closable with Notifiable with GettableCache with HitCounter{
  protected[scalablememoizer] var hits = 0L
  protected[scalablememoizer] var misses = 0L
  private var nSubjects: Int = 0

  override def getHitsMisses: (Long, Long) = (hits, misses)

  //   MESSAGES FROM SUBJECTS
  override def notifyDependencyStart(): Unit = nSubjects += 1

  override def notifyDependencyEnd(): Unit = {
    nSubjects -= 1
    tryToClose()
  }
  def tryToClose(): Unit = if (nSubjects == 0) close()
}

