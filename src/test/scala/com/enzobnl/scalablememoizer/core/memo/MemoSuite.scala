package com.enzobnl.scalablememoizer.core.memo

import com.enzobnl.scalablememoizer.caffeine.cache.CaffeineMemoCacheBuilder
import com.enzobnl.scalablememoizer.core.cache.MapMemoCacheBuilder
import com.enzobnl.scalablememoizer.ignite.cache.{IgniteMemoCacheBuilder, OnHeapEviction}
import org.scalatest._
import scalaz.Memo.mutableHashMapMemo


class MemoSuite extends FlatSpec {


  "(defaults, retrieves)" should "(3, 7) then (3, 17)" in {
    val memoCache = new IgniteMemoCacheBuilder()
      .withEviction(OnHeapEviction.LRU)
      .build()

    val memo = new Memo(memoCache)
    val f = (i: Int) => i * 4
    val memoizedf = memo(f)
    for (i <- 1 to 10) memoizedf(i % 3)
    assert((memoCache.misses, memoCache.hits) == (3, 7)) // 3 7

    val memo2 = new Memo(memoCache)
    val memoizedf2 = memo2(f)
    for (i <- 1 to 10) memoizedf2(i % 3)
    assert((memoCache.misses, memoCache.hits) == (3, 17)) // 3 17

    val memoCacheMap = new MapMemoCacheBuilder().build()
    val memo3 = new Memo(memoCacheMap)
    for (i <- 1 to 10) memo3(f)(i % 3)
    assert((memoCacheMap.misses, memoCacheMap.hits) == (3, 7)) // 3 7
    val memoized3 = memo3(f)
    for (i <- 1 to 10) memoized3(i % 3)
    assert((memoCacheMap.misses, memoCacheMap.hits) == (3, 17)) // 3,17
  }
  "cache access condition (I1, I2) => R" should "give consistent hits and misses" in {
    val cache = new MapMemoCacheBuilder().build()
    val f = (i: Int, j: Int) => Math.log(i * j)
    val condition = (i: Int, j: Int) => i == j
    val memoized = new Memo(cache)(f, condition)
    for (i <- 1 to 20; j <- 1 to 20) memoized(i, j)
    assert(cache.getHitsAndMisses._2 == 20)
  }
  "cache access condition on I => R" should "give consistent hits and misses" in {
    val cache = new MapMemoCacheBuilder().build()
    val f = (i: Int) => Math.log(i)
    val condition = (i: Int) => i > 10
    val memoized = new Memo(cache)(f, condition)
    (1 to 20).map(memoized)
    assert(cache.getHitsAndMisses._2 == 10)
  }
  var i = 0

  "fibo(20) no memo" should "take 21891 runs" in {

    lazy val fibo: Int => Int = n => {
      i += 1
      n match {
        case 0 => 1
        case 1 => 1
        case _ => fibo(n - 1) + fibo(n - 2)
      }
    }
    assert(fibo(20) == 10946)
    assert(i == 21891)


  }
  "fibo(20) ignite memo" should "take 21 runs" in {
    val igniteMemo = new Memo(new IgniteMemoCacheBuilder()
      .withEviction(OnHeapEviction.LRU)
      .build())
    lazy val igniteMemoFibo: Int => Int = igniteMemo(n => {
      i += 1
      println(f"fibo IGNITE run$i")
      n match {
        case 0 => 1
        case 1 => 1
        case _ => igniteMemoFibo(n - 1) + igniteMemoFibo(n - 2)
      }
    })
    i = 0

    assert(igniteMemoFibo(20) == 10946)
    assert(i == 21)

  }
  "fibo(20) caffeine memo" should "take 21 runs" in {
    val caffeineMemo = new Memo(new CaffeineMemoCacheBuilder().withMaxEntryNumber(Some(10000)))
    lazy val caffeineMemoFibo: Int => Int = caffeineMemo(n => {
      i += 1
      println(f"fibo Caffeine run$i")
      n match {
        case 0 => 1
        case 1 => 1
        case _ => caffeineMemoFibo(n - 1) + caffeineMemoFibo(n - 2)
      }
    })
    i = 0

    assert(caffeineMemoFibo(20) == 10946)
    assert(i == 21)

  }
  "fibo(20) MutableMap memo" should "take 21 runs" in {
    val mapMemo = new Memo(new MapMemoCacheBuilder())
    lazy val mapMemoFibo: Int => Int = mapMemo(n => {
      i += 1
      n match {
        case 0 => 1
        case 1 => 1
        case _ => mapMemoFibo(n - 1) + mapMemoFibo(n - 2)
      }
    })
    i = 0


    assert(mapMemoFibo(20) == 10946)
    assert(i == 21)
  }
  "fibo(20) scalaz memo" should "take 21 runs" in {
    lazy val scalazMemoFibo: Int => Int = mutableHashMapMemo {
      n =>
        i += 1
        n match {
          case 0 => 1
          case 1 => 1
          case _ => scalazMemoFibo(n - 2) + scalazMemoFibo(n - 1)
        }
    }

    i = 0
    assert(scalazMemoFibo(20) == 10946)
    assert(i == 21)
  }

  System.gc()
}