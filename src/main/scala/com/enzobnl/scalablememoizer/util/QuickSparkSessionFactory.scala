package com.enzobnl.sparkscalaexpe.util
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SQLContext, SparkSession}

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
