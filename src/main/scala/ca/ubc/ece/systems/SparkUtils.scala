
package ca.ubc.ece.systems


/* Since this project originated from within Spark, there are a variety of
 * helper tools we borrow from the Spark source
 *
 */
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io._
import ca.ubc.ece.systems.Logging
import java.nio.channels.{Channels, FileChannel}
import org.objectweb.asm.{ClassReader, ClassVisitor, MethodVisitor, Type, Label, Handle}

private[systems] object SparkUtils extends Logging {
  // Get an ASM class reader for a given class from the JAR that loaded it
  def getClassReader(cls: Class[_]): ClassReader = {
    // Copy data over, before delegating to ClassReader - else we can run out of open file handles.
    val className = cls.getName.replaceFirst("^.*\\.", "") + ".class"
    val resourceStream = cls.getResourceAsStream(className)
    // todo: Fixme - continuing with earlier behavior ...
    if (resourceStream == null) return new ClassReader(resourceStream)

    val baos = new ByteArrayOutputStream(128)
    copyStream(resourceStream, baos, true)
    new ClassReader(new ByteArrayInputStream(baos.toByteArray))
  }
  /**
   * Copy all data from an InputStream to an OutputStream. NIO way of file stream to file stream
   * copying is disabled by default unless explicitly set transferToEnabled as true,
   * the parameter transferToEnabled should be configured by spark.file.transferTo = [true|false].
   */
  def copyStream(
      in: InputStream,
      out: OutputStream,
      closeStreams: Boolean = false,
      transferToEnabled: Boolean = false): Long = {
    tryWithSafeFinally {
      if (in.isInstanceOf[FileInputStream] && out.isInstanceOf[FileOutputStream]
        && transferToEnabled) {
        // When both streams are File stream, use transferTo to improve copy performance.
        val inChannel = in.asInstanceOf[FileInputStream].getChannel()
        val outChannel = out.asInstanceOf[FileOutputStream].getChannel()
        val size = inChannel.size()
        copyFileStreamNIO(inChannel, outChannel, 0, size)
        size
      } else {
        var count = 0L
        val buf = new Array[Byte](8192)
        var n = 0
        while (n != -1) {
          n = in.read(buf)
          if (n != -1) {
            out.write(buf, 0, n)
            count += n
          }
        }
        count
      }
    } {
      if (closeStreams) {
        try {
          in.close()
        } finally {
          out.close()
        }
      }
    }
  }

  def copyFileStreamNIO(
      input: FileChannel,
      output: FileChannel,
      startPosition: Long,
      bytesToCopy: Long): Unit = {
    val initialPos = output.position()
    var count = 0L
    // In case transferTo method transferred less data than we have required.
    while (count < bytesToCopy) {
      count += input.transferTo(count + startPosition, bytesToCopy - count, output)
    }
    assert(count == bytesToCopy,
      s"request to copy $bytesToCopy bytes, but actually copied $count bytes.")

    // Check the position after transferTo loop to see if it is in the right position and
    // give user information if not.
    // Position will not be increased to the expected length after calling transferTo in
    // kernel version 2.6.32, this issue can be seen in
    // https://bugs.openjdk.java.net/browse/JDK-7052359
    // This will lead to stream corruption issue when using sort-based shuffle (SPARK-3948).
    val finalPos = output.position()
    val expectedPos = initialPos + bytesToCopy
    assert(finalPos == expectedPos,
      s"""
         |Current position $finalPos do not equal to expected position $expectedPos
         |after transferTo, please check your kernel version to see if it is 2.6.32,
         |this is a kernel bug which will lead to unexpected behavior when using transferTo.
         |You can set spark.file.transferTo = false to disable this NIO feature.
           """.stripMargin)
  }
  /**
   * Execute a block of code, then a finally block, but if exceptions happen in
   * the finally block, do not suppress the original exception.
   *
   * This is primarily an issue with `finally { out.close() }` blocks, where
   * close needs to be called to clean up `out`, but if an exception happened
   * in `out.write`, it's likely `out` may be corrupted and `out.close` will
   * fail as well. This would then suppress the original/likely more meaningful
   * exception from the original `out.write` call.
   */
  def tryWithSafeFinally[T](block: => T)(finallyBlock: => Unit): T = {
    var originalThrowable: Throwable = null
    try {
      block
    } catch {
      case t: Throwable =>
        // Purposefully not using NonFatal, because even fatal exceptions
        // we don't want to have our finallyBlock suppress
        originalThrowable = t
        throw originalThrowable
    } finally {
      try {
        finallyBlock
      } catch {
        case t: Throwable if (originalThrowable != null && originalThrowable != t) =>
          originalThrowable.addSuppressed(t)
          logWarning(s"Suppressing exception in finally: ${t.getMessage}", t)
          throw originalThrowable
      }
    }
  }
}
