package com.enzobnl.sparkscalaexpe.util

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext

object QuickKafkaServerFactory {
  private var appName: String = _
  private var logLevel: String = _
  private lazy val sqlContext: SQLContext =
  {val sc = new SparkContext(new SparkConf().setAppName(appName).setMaster("local[*]").set("spark.driver.memory", "3g"))
    sc.setLogLevel(logLevel)
    new SQLContext(sc)}
  def getOrCreate(appName: String="default", logLevel: String="ERROR"): SQLContext = {
    this.appName = appName
    this.logLevel = logLevel
    this.sqlContext
}}
