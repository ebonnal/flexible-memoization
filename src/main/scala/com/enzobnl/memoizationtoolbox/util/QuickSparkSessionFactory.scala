package com.enzobnl.memoizationtoolbox.util
import org.apache.spark.sql.SparkSession

object QuickSparkSessionFactory{
  private var appName: String = _
  private var logLevel: String = _
  private lazy val sqlContext: SparkSession = SparkSession.builder.master("local[*]").appName(appName).getOrCreate

  def getOrCreate(appName: String="default", logLevel: String="ERROR"): SparkSession = {
    this.appName = appName
    this.logLevel = logLevel
    this.sqlContext
  }
}
