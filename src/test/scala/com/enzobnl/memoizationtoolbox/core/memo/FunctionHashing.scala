package com.enzobnl.memoizationtoolbox.core.memo

import ca.ubc.ece.systems.ClosureHash
import com.enzobnl.memoizationtoolbox.caffeine.cache.CaffeineCacheBuilder
import com.enzobnl.memoizationtoolbox.core.cache.{Eviction, MapCacheBuilder}
import com.enzobnl.memoizationtoolbox.ignite.cache.{IgniteMemoCacheBuilder, OnHeapEviction}
import com.enzobnl.memoizationtoolbox.util.Timeit
import org.scalatest._
import scalaz.Memo.mutableHashMapMemo

import scala.util.Random


class FunctionHashing extends FlatSpec {

  "two different function instances corresponding to same code" should
    "share thair result" in {
    val f = (i: Int) => {val x = 7; x*i}
    val hash:Option[String] = ca.ubc.ece.systems.ClosureHash.hash(f)
    println(hash.get, f.hashCode())

    val f2 = (i: Int) => {
      val y = 7; y*i
    }
    val hash2:Option[String] = ca.ubc.ece.systems.ClosureHash.hash(f2)
    println(hash2.get, f2.hashCode())

    val f3 = (i: Int) => i*i
    val hash3:Option[String] = ca.ubc.ece.systems.ClosureHash.hash(f3)
    println(hash3.get, f3.hashCode())

    assert(hash.get == hash2.get)
    assert(hash.get != hash3.get)
    assert(f2.hashCode() != f.hashCode())
    assert(f2.hashCode() != f3.hashCode())
    assert(f.hashCode() != f3.hashCode())
    def getClosure(x: Int)= {
      val a = Seq(1,2,x)
      (i:Int) => {val x = 5; i*x*a.sum}
    }
    assert(ClosureHash.hash(getClosure(1)).get == ClosureHash.hash(getClosure(1)).get)

    val memo = new Memo()
    val memoizedf1 = memo(getClosure(10))
    val memoizedf2 = memo(getClosure(10))
    val memoizedf3 = memo(getClosure(1))
    memoizedf1(1)
    memoizedf2(1)
    memoizedf3(1)
    assert(memoizedf1.sharedCache.getHitsAndMisses == (1, 2))
  }

}