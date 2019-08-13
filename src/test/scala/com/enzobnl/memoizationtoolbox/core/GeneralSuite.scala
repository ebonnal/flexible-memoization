package com.enzobnl.memoizationtoolbox.core

import com.enzobnl.memoizationtoolbox.core.cache.MapCacheBuilder
import com.enzobnl.memoizationtoolbox.core.memo.Memo
import com.enzobnl.memoizationtoolbox.ignite.cache.{IgniteMemoCacheBuilder, OnHeapEviction}
import com.enzobnl.memoizationtoolbox.util.QuickSparkSessionFactory
import org.apache.spark.sql.SparkSession
import org.scalatest.FlatSpec
class GeneralSuite extends FlatSpec{
  lazy val spark: SparkSession = QuickSparkSessionFactory.getOrCreate()
  "fibo(20) no memo vs any memo within spark" should "take 21891 vs 21 runs" in {

    val igniteMemo = new Memo(new IgniteMemoCacheBuilder()
      .withOnHeapEviction(OnHeapEviction.LRU)
      .build())
    lazy val igniteMemoFibo: Int => Int = igniteMemo {
      case 0 => 1
      case 1 => 1
      case n: Int => igniteMemoFibo(n - 1) + igniteMemoFibo(n - 2)
    }
    val mapMemo = new Memo(new MapCacheBuilder(10000))

    lazy val mapMemoFibo: Int => Int = mapMemo {
      case 0 => 1
      case 1 => 1
      case n: Int => mapMemoFibo(n - 1) + mapMemoFibo(n - 2)
    }

    spark.udf.register("f", igniteMemoFibo)
    val data = for (i <- 1 to 20) yield Tuple1(i)
    assert(spark.createDataFrame(data)
      .toDF("n")
      .selectExpr("f(n)")
      .collect().last.getAs[Int](0) == 10946
    )

    spark.udf.register("f", mapMemoFibo)
    assert(spark.createDataFrame(for (i <- 1 to 20) yield Tuple1(i))
      .toDF("n")
      .selectExpr("f(n)")
      .collect().last.getAs[Int](0) == 10946
    )
    spark.stop()
    System.gc()
  }

}
