package com.bonnalenzo.flexiblememoization.cache

/**
  * Mimic the useful getOrElseUpdate method from mutable.Map
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
    if (nSubjects == 0) close()  
  }
}


/**
  * Base trait needed by memo.Memo
  */
trait Cache extends Gettable with NotifiableMixin



trait CacheBuilder {
  def build(): Cache
}

