package com.enzobnl.scalablememoizer.core

import com.enzobnl.scalablememoizer.core.cache.MapMemoCacheBuilder
import com.enzobnl.scalablememoizer.core.memo.Memo
import com.enzobnl.scalablememoizer.ignite.cache.{IgniteMemoCacheBuilder, OnHeapEviction}
import com.enzobnl.sparkscalaexpe.util.QuickSparkSessionFactory
import org.apache.spark.sql.SparkSession
import org.scalatest.FlatSpec

class GeneralSuite extends FlatSpec{
  "fibo(20) no memo vs any memo within spark" should "take 21891 vs 21 runs" in {
    lazy val spark: SparkSession = QuickSparkSessionFactory.getOrCreate()
    lazy val sc = spark.sparkContext
    lazy val df = spark.createDataFrame(
      Seq(("Thin", "Cell", 6000, 1),
        ("Normal", "Tablet", 1500, 1),
        ("Mini", "Tablet", 5500, 1),
        ("Ultra thin", "Cell", 5000, 1),
        ("Very thin", "Cell", 6000, 1),
        ("Big", "Tablet", 2500, 2),
        ("Bendable", "Cell", 3000, 2),
        ("Foldable", "Cell", 3000, 2),
        ("Pro", "Tablet", 4500, 2),
        ("Pro2", "Tablet", 6500, 2))).toDF("product", "category", "revenue", "un")
    val igniteMemo = new Memo(new IgniteMemoCacheBuilder()
      .withEviction(OnHeapEviction.LRU)
      .build())
    lazy val igniteMemoFibo: Int => Int = igniteMemo {
      case 0 => 1
      case 1 => 1
      case n: Int => igniteMemoFibo(n - 1) + igniteMemoFibo(n - 2)
    }
    val mapMemo = new Memo(new MapMemoCacheBuilder())

    lazy val mapMemoFibo: Int => Int = mapMemo {
      case 0 => 1
      case 1 => 1
      case n: Int => mapMemoFibo(n - 1) + mapMemoFibo(n - 2)
    }

    spark.udf.register("f", igniteMemoFibo)
    assert(spark.createDataFrame(for (i <- 1 to 20) yield Tuple1(i))
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
  }
  System.gc()
}
