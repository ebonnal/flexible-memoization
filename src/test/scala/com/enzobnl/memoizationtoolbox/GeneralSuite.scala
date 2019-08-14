package com.enzobnl.memoizationtoolbox

import com.enzobnl.memoizationtoolbox.core.cache.MapCacheBuilder
import com.enzobnl.memoizationtoolbox.core.memo.Memo
import com.enzobnl.memoizationtoolbox.ignite.cache.{IgniteMemoCacheBuilder, OnHeapEviction}
import com.enzobnl.memoizationtoolbox.util.QuickSparkSessionFactory
import org.apache.spark.sql.SparkSession
import org.scalatest.FlatSpec

class GeneralSuite extends FlatSpec {
  lazy val spark: SparkSession = QuickSparkSessionFactory.getOrCreate()
  "fibo(20) no memo vs any memo within spark" should "take 21891 vs 21 runs" in {

    val igniteMemo = new Memo(new IgniteMemoCacheBuilder().withOnHeapEviction(OnHeapEviction.LRU))
    lazy val igniteMemoFibo: Int => Int = igniteMemo {
      case 0 => 1
      case 1 => 1
      case n: Int => igniteMemoFibo(n - 1) + igniteMemoFibo(n - 2)
    }

    spark.udf.register("f", igniteMemoFibo)
    val data = for (i <- 1 to 20) yield Tuple1(i)
    assert(spark.createDataFrame(data)
      .toDF("n")
      .selectExpr("f(n)")
      .collect().last.getAs[Int](0) == 10946
    )

  }
  "cache" should "be shared among jobs" in {

    val mapCache = new MapCacheBuilder().build()
    val mapMemo = new Memo(mapCache)

    lazy val mapMemoFibo: Int => Int = mapMemo {
      case 0 => 1
      case 1 => 1
      case n: Int => mapMemoFibo(n - 1) + mapMemoFibo(n - 2)
    }

    spark.udf.register("f", mapMemoFibo)
    import org.apache.spark.sql.functions.udf
    assert(spark.createDataFrame(for (i <- 1 to 20) yield Tuple1(i))
      .toDF("n")
      .selectExpr("f(n)")
      .collect().last.getAs[Int](0) == 10946
    )
    assert((37, 21) == mapCache.getHitsAndMisses)
    assert(spark.createDataFrame(for (i <- 1 to 20) yield Tuple1(i))
      .toDF("n")
      .selectExpr("f(n)")
      .collect().last.getAs[Int](0) == 10946
    )
    assert((57, 21) == mapCache.getHitsAndMisses)

  }
  "2 different ignite caches on two different ignite nodes used in two different Memos for THE SAME func" should
    "share their result, especially among jobs" in {
    // this is ok if their is no garbage collection between jobs

    val igniteCache1 = new IgniteMemoCacheBuilder().withOnHeapEviction(OnHeapEviction.LRU).build()
    val igniteCache2 = new IgniteMemoCacheBuilder().withOnHeapEviction(OnHeapEviction.LRU).build()

    val f = (i: Int, j: Int) => i * j

    val mf1 = new Memo(igniteCache1)(f)
    // 2 different caches used with two different Memos:if work it's ready for cluster
    val mf2 = new Memo(igniteCache2)(f)

    spark.udf.register("f", mf1)
    assert(spark.createDataFrame(for (i <- 1 to 200) yield Tuple2(i, i))
      .toDF("n", "m")
      .selectExpr("f(n, m)")
      .collect().last.getAs[Int](0) == 40000
    )
    assert(igniteCache1.getHitsAndMisses == (0, 200))
    spark.udf.register("f", mf2)

    assert(spark.createDataFrame(for (i <- 1 to 200) yield Tuple2(i, i))
      .toDF("n", "m")
      .selectExpr("f(n, m)")
      .collect().last.getAs[Int](0) == 40000
    )
    assert(igniteCache2.getHitsAndMisses == (200, 0))
    spark.stop()
    //    System.gc()
  }


}


