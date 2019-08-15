package com.enzobnl.flexiblememoization.memo

import ca.ubc.ece.systems.ClosureHash
import org.scalatest._

import scala.collection.immutable.SortedSet
import scala.collection.{Iterable, mutable}


class Hashing extends FlatSpec {

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

  "2 different references for the same func, passing through different" +
    " memos, getting different attributes types" should "reuse results" in {
    val mf1 = new Memo()((f: Int=>Int, iter: Iterable[Int], set: Set[Int]) => set.contains(iter.map(f).last))
    val mf2 = new Memo()((g: Int=>Int, iterable: Iterable[Int], s: Set[Int]) => s.contains(iterable.map(g).last))
    mf1((i: Int)=> i*2, mutable.LinkedList(1,2), Set(1,2))
    mf2((x: Int)=> x*2, Array(1,2), SortedSet(1,2))
    println(mf1.sharedCache.getHitsAndMisses, (1, 1))
  }


}