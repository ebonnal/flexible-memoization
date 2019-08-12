package com.enzobnl.scalablememoizer.core.cache

import com.enzobnl.sparkscalaexpe.util.Utils

import scala.collection.{immutable, mutable}

class LFU_wEvictor(override val maxEntryNumber: Long,
                   override val removeRatio: Float = 0.05f,
                   val decay: Long => Long = _ >>> 1) extends Evictor {
  var count: Int = 0

  // remembers keys in arriving order. Ordered by first element: timestamp
  object ByFirstElement extends Ordering[(Long, Long, Long)] {
    override def compare(a: (Long, Long, Long), b: (Long, Long, Long)): Int = if(a._2 > b._2) 1 else -1
  }

  var sortedSetOnProduct: immutable.SortedSet[(Long, Long, Long)] = immutable.SortedSet[(Long, Long, Long)]()(ByFirstElement)
  var reverseMap: immutable.Map[Long, (Long, Long, Long)] = immutable.Map[Long, (Long, Long, Long)]()

  override def evict(map: mutable.Map[Long, Any], hash: Long, value: => Any): Unit = {
    println("evict", hash)
    if (map.size > maxEntryNumber) {
      // remove a part of map entries (part = REMOVE_RATIO), starting from the left (=smallest timestamps)
      sortedSetOnProduct = sortedSetOnProduct.dropWhile({
        case (_, _, h) =>
          println(map.size, removeThreshold)
          if (map.size > removeThreshold){
            map.remove(h)
            reverseMap = reverseMap - h
            true
          }
          else{
            false
          }
      })

    }
    print(sortedSetOnProduct, map)
    // before new adding to set
    sortedSetOnProduct = sortedSetOnProduct.map(trio => (trio._1, purgeDecays(trio._2), trio._3))
    count = 0
    println(sortedSetOnProduct)

    val trio = (1L, Utils.timeit(value), hash)
    sortedSetOnProduct = sortedSetOnProduct + trio
    reverseMap = reverseMap + (hash -> trio)
    println("added: ", trio, sortedSetOnProduct, sortedSetOnProduct.lastKey, sortedSetOnProduct.firstKey)

  }

  override def onHit(map: mutable.Map[Long, Any], hash: Long, value: => Any): Unit = {
    val sortedSetKeyToRemove = reverseMap(hash)
    sortedSetOnProduct = sortedSetOnProduct - sortedSetKeyToRemove
    // update frequency on hit entry*
    val trio = ((sortedSetKeyToRemove._1 + 1L, sortedSetKeyToRemove._2, hash))
    sortedSetOnProduct = sortedSetOnProduct + trio
    reverseMap = reverseMap - hash + (hash -> trio)
    count += 1
    println("hit", hash,  sortedSetOnProduct)
  }

  private def purgeDecays(input: Long): Long = nDecays(input, count)

  private def nDecays(input: Long, n: Int) = (1 to n).foldLeft(input)((acc, _) => decay(acc))
}
