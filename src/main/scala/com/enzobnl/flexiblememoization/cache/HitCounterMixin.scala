package com.enzobnl.flexiblememoization.cache

/**
  * Define two counters for hits and misses and provide access to their tuple and hit ratio.
  */
trait HitCounterMixin {
  protected[flexiblememoization] var hits = 0L
  protected[flexiblememoization] var misses = 0L

  def getHitsAndMisses: (Long, Long) = (hits, misses)
  def getHitRatio: Float = hits.toFloat/(hits + misses)

}
