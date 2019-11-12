package com.enzobnl.flexiblememoization.memo

import com.enzobnl.flexiblememoization.cache.HitCounterMixin
import com.enzobnl.flexiblememoization.cache.impl.caffeine.CaffeineCacheBuilder
import com.enzobnl.flexiblememoization.cache.impl.ignite.{IgniteCacheBuilder, OnHeapEviction}
import com.enzobnl.flexiblememoization.cache.impl.map.{Eviction, MapCacheBuilder}
import com.enzobnl.flexiblememoization.util.Timeit
import org.scalatest._
import scalaz.Memo.mutableHashMapMemo

import scala.util.Random


class MemoSuite extends FlatSpec {


  "(defaults, retrieves)" should "(3, 7) then (3, 17)" in {
    val memoCache = new IgniteCacheBuilder()
      .withOnHeapEviction(OnHeapEviction.LRU)
      .build()

    val memo = new Memo(memoCache)
    val f = (i: Int) => i * 4
    val memoizedf = memo(f)
    for (i <- 1 to 10) memoizedf(i % 3)
    assert(memoCache.asInstanceOf[HitCounterMixin].getHitsAndMisses == (7, 3)) // 3 7


    val memo2 = new Memo(memoCache)
    val memoizedf2 = memo2(f)
    for (i <- 1 to 10) println(memoizedf2(i % 3), i % 3, Memo.getHashCode(i % 3))
    assert(memoCache.asInstanceOf[HitCounterMixin].getHitsAndMisses == (17, 3)) // 3 17

    val memoCacheMap = new MapCacheBuilder().build()
    val memo3 = new Memo(memoCacheMap)
    for (i <- 1 to 10) memo3(f)(i % 3)
    assert(memoCacheMap.asInstanceOf[HitCounterMixin].getHitsAndMisses == (7, 3)) // 3 7
    val memoized3 = memo3(f)
    for (i <- 1 to 10) memoized3(i % 3)
    assert(memoCacheMap.asInstanceOf[HitCounterMixin].getHitsAndMisses == (17, 3)) // 3,17
  }
  "cache access condition (I1, I2) => R" should "give consistent hits and misses" in {
    val cache = new MapCacheBuilder().build()
    val f = (i: Int, j: Int) => Math.log(i * j)
    val condition = (i: Int, j: Int) => i == j
    val memoized = new Memo(cache)(f, condition)
    for (i <- 1 to 20; j <- 1 to 20) memoized(i, j)
    assert(cache.asInstanceOf[HitCounterMixin].getHitsAndMisses._2 == 20)
  }
  "cache access condition on I => R" should "give consistent hits and misses" in {
    val cache = new MapCacheBuilder().build()
    val f = (i: Int) => Math.log(i)
    val condition = (i: Int) => i > 10
    val memoized = new Memo(cache)(f, condition)
    (1 to 20).map(memoized)
    assert(cache.asInstanceOf[HitCounterMixin].getHitsAndMisses._2 == 10)
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
    val igniteMemo = new Memo(new IgniteCacheBuilder()
      .withOnHeapEviction(OnHeapEviction.LRU)
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
    System.gc()
  }
  "fibo(20) caffeine memo" should "take 21 runs" in {
    val caffeineMemo = new Memo(new CaffeineCacheBuilder().withMaxEntryNumber(10000))
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
    val mapMemo = new Memo(new MapCacheBuilder())
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

  "bench between MapLRU, Caffeine and Ignite on small dataset" should
    "give: " in {
    val bench = (N: Int) => {
      val maxSize = N / 100
      val data: Seq[Int] = for (i <- 1 to N) yield i
      val naiveIsPrime = (i: Int) => (2 to Math.sqrt(i).toInt + 1).dropWhile(i % _ != 0).nonEmpty
      val mapF = new Memo(new MapCacheBuilder().withMaxEntryNumber(maxSize))(naiveIsPrime)
      val caffeineF = new Memo(new CaffeineCacheBuilder().withMaxEntryNumber(maxSize))(naiveIsPrime)
      val igniteCache = new IgniteCacheBuilder().build()
      val igniteF = new Memo(igniteCache)(naiveIsPrime)
      val scalazF = scalaz.Memo.mutableHashMapMemo(naiveIsPrime)
      val mapBench = Timeit.get(data.foreach(mapF))
      val caffeineBench = Timeit.get(data.foreach(caffeineF))
      val igniteBench = Timeit.get(data.foreach(igniteF))
      val scalazBench = Timeit.get(data.foreach(scalazF))
      println("BENCH cross frameworks:", N)
      println(mapBench)
      println(caffeineBench.toDouble / mapBench)
      println(igniteBench.toDouble / mapBench)
      println(scalazBench.toDouble / mapBench)
      igniteCache.close()
      println("passed close")
      assert(igniteBench > mapBench)
      assert(igniteBench > caffeineBench)
      assert(mapBench > scalazBench)
      assert(caffeineBench > scalazBench)
      // scalaz is very fast because no eviction, caffeine faster than my scala
    }
    Seq(100, 1000).foreach(bench)
  }

  "COST bench between MapLRU, MapFIFO, and MapCost on isPrime" should
    "give Cost winner, then LRU" in {
    val bench = (N: Int, cacheSize: Int, rangeSize: Int) => {
      val maxSize = N / 100
      println("maxSize=", maxSize)
      val data: Seq[Int] = for (_ <- 1 to N) yield Random.nextLong().toInt % rangeSize * 100
      val naiveIsPrime = (i: Int) => (2 to Math.sqrt(i).toInt + 1).dropWhile(i % _ != 0).nonEmpty
      val mapBuilder = new MapCacheBuilder().withMaxEntryNumber(maxSize)
      val mapLRUf = new Memo(mapBuilder.withEviction(Eviction.LRU))(naiveIsPrime)
      val mapFIFOf = new Memo(mapBuilder.withEviction(Eviction.FIFO))(naiveIsPrime)
      val mapCostf = new Memo(mapBuilder.withEviction(Eviction.COST))(naiveIsPrime)

      val mapBenchLRU = Timeit.get(data.foreach(mapLRUf))
      val mapBenchFIFO = Timeit.get(data.foreach(mapFIFOf))
      val mapBenchCOST = Timeit.get(data.foreach(mapCostf))
      println("BENCH MAPS cost:", N, cacheSize, rangeSize)
      println(mapBenchCOST)
      println(mapBenchFIFO.toDouble / mapBenchCOST)
      println(mapBenchLRU.toDouble / mapBenchCOST)
      //      assert(mapBenchCOST < mapBenchLRU && mapBenchLRU < mapBenchFIFO)
      (mapBenchCOST.toDouble, mapBenchLRU.toDouble / mapBenchCOST, mapBenchFIFO.toDouble / mapBenchCOST)
    }
    val n = 3
    val results = (1 to n).foldLeft(Array(Seq[Double](), Seq[Double](), Seq[Double]()))((acc, _) => {
      val res = bench(100000, 500, 1000)
      Array(acc(0) :+ res._1, acc(1) :+ res._2, acc(2) :+ res._3)
    })
    val sums = results.map(_.sum)
    val means = sums.map(_ / n)
    println("MEANS", means.toSeq)
    assert(means(1) > 1)
    assert(means(2) > 1)
    assert(means(2) > means(1))
  }
  "HIT-RATIO bench between MapLRU, MapFIFO, and MapCost on isPrime" should
    "give about 0.5 for everyone because input random in 2 times the size of caches" in {
    val bench = (N: Int, cacheSize: Int, rangeSize: Int) => {
      val maxSize = N / 100
      println("maxSize=", maxSize)
      val data: Seq[Int] = for (_ <- 1 to N) yield Random.nextLong().toInt % rangeSize * 100
      val naiveIsPrime = (i: Int) => (2 to Math.sqrt(i).toInt + 1).dropWhile(i % _ != 0).nonEmpty
      val mapBuilder = new MapCacheBuilder().withMaxEntryNumber(maxSize)
      val mapLRUf = new Memo(mapBuilder.withEviction(Eviction.LRU))(naiveIsPrime)
      val mapFIFOf = new Memo(mapBuilder.withEviction(Eviction.FIFO))(naiveIsPrime)
      val mapCostf = new Memo(mapBuilder.withEviction(Eviction.COST))(naiveIsPrime)
      data.foreach(mapLRUf)
      data.foreach(mapFIFOf)
      data.foreach(mapCostf)
      val mapBenchLRU = mapLRUf.sharedCache.asInstanceOf[HitCounterMixin].getHitRatio
      val mapBenchFIFO = mapFIFOf.sharedCache.asInstanceOf[HitCounterMixin].getHitRatio
      val mapBenchCOST = mapCostf.sharedCache.asInstanceOf[HitCounterMixin].getHitRatio
      println("BENCH MAPS hit-ratio:", N, cacheSize, rangeSize)
      println(mapBenchCOST)
      println(mapBenchFIFO)
      println(mapBenchLRU)
      (mapBenchCOST, mapBenchLRU, mapBenchFIFO)
    }
    val n = 3
    val results = (1 to n).foldLeft(Array(Seq[Float](), Seq[Float](), Seq[Float]()))((acc, _) => {
      val res = bench(100000, 500, 1000)
      Array(acc(0) :+ res._1, acc(1) :+ res._2, acc(2) :+ res._3)
    })
    val sums = results.map(_.sum)
    val means = sums.map(_ / n)
    println("MEANS", means.toSeq)

    (0 to 2).foreach(n => {
      assert(means(n) > 0.45)
      assert(means(n) < 0.52)
    })
  }
  "memo on high arity" should "work just fine" in {
    val iLastCharacters = (i: Int, s: String, ii: Int, si: String, iii: Int, siiii: String, iiiii: Int, siiiiiiii: String) => s.substring(Math.max(0, s.length - i))
    val m = new Memo()(iLastCharacters, (i: Int, _: String, _: Int, _: String, _: Int, _: String, _: Int, _: String) => i > 1000)
    assert(m(10, "123456", 10, "123456", 10, "123456", 10, "123456") == "123456")
    assert(m(1, "123456", 10, "123456", 10, "123456", 10, "123456") == "6")
  }

}