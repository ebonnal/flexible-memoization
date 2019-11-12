
package ca.ubc.ece.systems

import ca.ubc.ece.systems.Logging
import ca.ubc.ece.systems.SparkUtils

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectOutputStream, DataOutputStream}
import java.io.{ObjectStreamClass}
import java.io.{IOException}
import sun.misc.Unsafe
import java.lang.reflect.Modifier
import java.lang.reflect.{Array => ReflectArray};
import java.nio.ByteBuffer;
import scala.collection.mutable.{Map, Set, SortedSet, Queue, Stack, ListBuffer, HashMap}
import scala.collection.JavaConverters._

//for function serialization & encoding
import java.security.MessageDigest
import java.util.regex.Pattern
import org.apache.commons.codec.binary.Base64
//import org.apache.spark.SparkContext

//for JSON logging
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{write => WriteJson}

//for bytecode operations
import org.objectweb.asm.{ClassReader, ClassVisitor, MethodVisitor, Type, Label, Handle}
import org.objectweb.asm.tree.{ClassNode, MethodNode, InsnList, AbstractInsnNode}
import org.objectweb.asm.util.{TraceMethodVisitor, Textifier}
import org.objectweb.asm.Opcodes._

// include log4j logging so output can get logged into Spark
object ClosureHash extends Logging {
  /** Hash the given closure.
   * This hashes only the functional pieces of the closure
   *  - function signature
   *  - instructions
   * so that it's functionality can be summarized as a hash, used for
   * comparison between different spark contexts that may be in use
   */
  val preferredHashType = "SHA-256"
  var hashCache: Map[String, Array[Byte]] = Map.empty

  def hash(func: AnyRef): Option[String] = {
    /* ignore the hash trace return, if present */
    hashWithTrace(func,false).map( (x) => {x._1} )
  }
  def hashWithTrace(func: AnyRef, trace:Boolean): Option[(String, HashTraceMap)] = {
    try {
      hash_internal(func, trace)
    } catch {
      /* IOException happens when a class we need to hash can't be found.  We
       * need to access the class but can't, so we can't produce a valid hash,
       * so just return None.  Encountered when SparkContext has been stopped
       * in testing */
      case e: IOException => {
        logInfo(s"HLS Hash failed: ${e}")
        return None
      }
    }
  }
  // Check whether a class represents a Scala closure
  private def isClosure(cls: Class[_]): Boolean = {
    /* todo is this needed to do hashing in general? */
    cls.getName.contains("$anonfun$")
  }

  /* Internal hash method that returns a trace */
  def hash_internal(func: AnyRef, trace:Boolean): Option[(String,HashTraceMap)] = {
    var hashStart = System.nanoTime

    //if (!isClosure(func.getClass)) {
      //logInfo("Expected a closure; got " + func.getClass.getName)
      //return None
    //}
    if (func == null) {
      return None
    }
    logDebug(s"+++ Hashing closure $func (${func.getClass.getName}) +++")

    /* first check to see if we've already hash the given ref */
    /* TODO potentially implement cache that stores callees seperately */
    
    var bytecodeHashTraces = if(trace){ Some(ListBuffer[HashTraceMap]()) } else { None }

    var bytecodeHashbytes:Array[Byte] = hashCache get func.getClass.getName match {
      case Some(result) => {
        logInfo("Hash Cache hit for: " + func.getClass.getName)
        bytecodeHashTraces = bytecodeHashTraces map { _ += HashMap(
          "hashCacheHit"->true, 
          "name"-> func.getClass.getName, 
          "localHash" -> Base64.encodeBase64URLSafeString(result)
        )}
        result
      }
      case None => {
          logInfo("Hash Cache miss for: " + func.getClass.getName)

          var classesToVisit = getFunctionsCalled(func)
          logDebug(s"+++ All classes to visit: $classesToVisit")

          /* hash the given function */
          var hash = MessageDigest.getInstance(preferredHashType)
          var bytecodeHashTrace = bytecodeHashTraces map((_)=>{ new HashTraceMap })
          hashClass(func, hash, bytecodeHashTrace) 
          bytecodeHashTraces map { _ += bytecodeHashTrace.get }

          /* hash the bytecode of all functions that this function calls */
          /* TODO rewrite this so we only call it once per class */
          for( cls <- classesToVisit ){
            var clzname = cls._1.replace('/', '.')
            var obj = Class.forName( clzname,
            false, Thread.currentThread.getContextClassLoader)
            logDebug(s"+++ hashing $clzname functions: ${ (cls._2, cls._3) }")

            var bytecodeHashTrace = bytecodeHashTraces map((_)=>{ new HashTraceMap })
            hashClass(obj, hash, bytecodeHashTrace, Set((cls._2, cls._3)) )
            bytecodeHashTraces map { _ += bytecodeHashTrace.get }
          }
          val digest = hash.digest
          hashCache(func.getClass.getName) = digest
          digest
      }
    }
    var hashBytecodeStop = System.nanoTime

    // Now we want to serialize & hash any referenced fields
    /* disable serialization hashing for now */
    /* 
    val serializationBytes = hashSerialization(func);
    var serial_hash = MessageDigest.getInstance("SHA-256")
    serial_hash.update(serializationBytes)
    var serial_hash64 = Base64.encodeBase64URLSafeString(serial_hash.digest)
    logInfo(s"Serialization hash: $serial_hash64")
    */

    //hash the primitives
    var hashPrimitivesStart = System.nanoTime
    val bos = new ByteArrayOutputStream()
    val dos = new DataOutputStream(bos)
    logTrace("Hash input primitives start for: " + func.getClass.getName)
    val primitiveHashTrace = if(trace){ Some(new HashTraceMap) } else { None }
    hashInputPrimitives( func, dos, primitiveHashTrace )
    dos.close()
    logTrace(s"Primitive Bytes: ${ bos.toByteArray.mkString(",") } ")

    var primitive_hash64 = if( bos.toByteArray.length > 0){
      var primitiveHash = MessageDigest.getInstance("SHA-256")
      primitiveHash.update(bos.toByteArray)
      Base64.encodeBase64URLSafeString(primitiveHash.digest)
    } else {
      "none"
    }
    logInfo(s"Primitive hash: $primitive_hash64")

    //finalize the hash
    var hashbytesenc = Base64.encodeBase64URLSafeString(bytecodeHashbytes)
    logInfo(s"Bytecode hash: $hashbytesenc")
    //Some(hashbytesenc)

    //merge bytecode and primitive hash
    var merged = s"bc:${hashbytesenc}_pr:${primitive_hash64}"
    logInfo(s"Merged hash: ${merged}")

    var hashStop = System.nanoTime
    logInfo(s"Hashing took: ${hashStop - hashStart} ns closure:${func.getClass.getName}")
    logInfo(s"Hashing Bytecode took: ${hashBytecodeStop - hashStart} ns closure:${func.getClass.getName}")
    logInfo(s"Hashing Primitives took: ${hashStop - hashPrimitivesStart} ns closure:${func.getClass.getName}")

    var hashedBytes = if(trace){ bos.toByteArray.mkString(",") } else { "" }

    var overallHashTrace = HashMap(
      "closureName" -> func.getClass.getName,
      "bytecode"-> Map(
        "trace"->(bytecodeHashTraces getOrElse ""),
        "hash"->hashbytesenc,
        "took_ns"->(hashBytecodeStop - hashStart)
      ),
      "primitives"-> Map(
        "trace"->(primitiveHashTrace getOrElse ""),
        "hash"->primitive_hash64,
        "hashed_bytes"->hashedBytes,
        "took_ns"->(hashStop - hashPrimitivesStart)
      ),
      "mergedHash"->merged,
      "took_ns"->(hashStop - hashStart)
    )

    implicit val jsonformats = Serialization.formats(NoTypeHints)
    logInfo(s"Hashing trace: ${WriteJson(overallHashTrace)}")

    Some((merged, overallHashTrace))
  }

  /* hash a given classes bytecode, optionally only the given functions,
   * will anonymize certain names to avoid false negatives due to:
   *  REPL line numbers
   *  names of called functions (redundant when the bytecode is included instead)
   *  outer classes of called functions
   * @param obj the object to hash (class or lambda)
   * @param hashObj the digest to add bytecode to
   * @param hashTrace map object that is used to store debug information about the hash procedure
   * @param funcList list of functions to hash of the class (if empty, all functions will be hashed)
   * */
  type HashTraceMap = HashMap[String,Any]
  private def hashClass(obj: AnyRef,
    hashObj: MessageDigest,
    hashTrace: Option[HashTraceMap],
    funcList: Set[(String,String)] = Set()
    ): Unit  = {
     val anonymizeClass = true; //always anonymize the class

     /* build classnode */
     val cn = new ClassNode()
     //we might get passed a function of a class, or a class itself, so we disambiguate
     val objcls:Class[_] = if (obj.isInstanceOf[Class[_]]){
       obj.asInstanceOf[Class[_]]
     } else {
       obj.getClass
     }
     SparkUtils.getClassReader(objcls).accept(cn, 0)

     /* build debug trace */
     var localHash = MessageDigest.getInstance(preferredHashType)
     hashTrace map { _ += (
         ("name" -> cn.name), 
         ("outerClass" -> cn.outerClass),
         ("outerMethod" -> cn.outerMethod),
         ("outerMethodDesc" -> cn.outerMethodDesc),
         ("signature" -> cn.signature),
         ("superName" -> cn.superName)
       )
     }
     
     for( f <- objcls.getDeclaredFields ){
       if (f.getName == "serialVersionUID"){
         try {
           hashTrace map { _ += ("serialVersionUID" -> f.getLong(null).toString) }
         } catch {
           case e: Exception => hashTrace map { _ += ("serialVersionUID" -> null) }
         }
       }
     }

     /* read class using a visitor we can override
      * make sure to rewrite any non-identifying symbols that could cause
      * differences in the hash but aren't functionally relevant, such as:
      *  - Scala REPL adds 'lineXX' package for each lambda
      *  - class self-references and references to it's outer class
      */
     val re_line = (s:String) => {
       /* this transformation ensures that any classes that reference a scala
        * REPL line number are modified to remove the line number 
        * this should be applied last because it may disrupt other (more
        * important) transformations
        */
       if(anonymizeClass){
         val quoted_name = "\\$line\\d+".r
         var ret = quoted_name.replaceAllIn(s, "\\$lineXX")
         ret
       } else {
         s
       }
     }
     val re_name = (s:String) => {
       /* this transformation ensures that when a class references itself, it
        * uses a relative name. this avoids problems where similar funcions are
        * written in different contexts, and thus their names are different but
        * functionality is equivalent.
        */
       if(anonymizeClass && cn.name != null){
         val quoted_name = Pattern.quote(cn.name).r
         re_line(quoted_name.replaceAllIn(s, "THISCLASS"))
       } else {
         s
       }
     }
     val re_outer = (s:String) => {
       /* this transformation turns an absolute reference to an outer class
        * into a relative reference
        */
       if(anonymizeClass && cn.outerClass != null){
         val quoted_outer = Pattern.quote(cn.outerClass).r
         re_line(quoted_outer.replaceAllIn(s, "THISOUTER"))
       } else {
         s
       }
     }

     // A methodvisitor to rename strings in the bytecode output
     val p = new Textifier(ASM5) {
       override def visitLocalVariable(name:String, desc:String,
         signature:String, start:Label, end:Label, index:Int){
           var newdesc = re_outer(re_name(desc))
           /* emit a new name to avoid issues with changes in variable names */
           super.visitLocalVariable(s"localvar${index}", newdesc, signature,
             start, end, index)
       }
       /* deprecated by ASM5, kept here to ensure renaming works */
      override def visitMethodInsn(opcode:Int, owner:String,
        name:String, desc:String){
          var newname = re_name(name)
          var newowner = re_name(owner)
          var newdesc = re_outer(re_name(desc))
          super.visitMethodInsn(opcode, newowner, newname, newdesc);
        }
       override def visitMethodInsn(opcode:Int, owner:String,
         name:String, desc:String, itf:Boolean){
          var newname = re_name(name)
          var newowner = re_name(owner)
          var newdesc = re_outer(re_name(desc))

          if(newowner.startsWith("$line")){
            newname = """VAL\d+""".r.replaceAllIn(newname, "VALXX")
          }

          //call super to print method
          super.visitMethodInsn(opcode, newowner, newname, newdesc, itf);
       }
       override def visitLineNumber(line:Int, start:Label){
          //this is a no-op to suppress line number output
       }
       override def visitFieldInsn( opcode:Int, owner:String,
         name:String, desc:String){
           /* rename references to fields */
           val newowner = re_name(owner)
           val newdesc = re_name(desc)
           var newname = re_name(name)
           if (opcode == GETFIELD && owner.startsWith("$line")) {
             newname = """VAL\d+""".r.replaceAllIn(newname, "VALXX")
           }
           super.visitFieldInsn(opcode, newowner, newname, newdesc)
       }
     }
     val tm = new TraceMethodVisitor(p)

     var bytecode_string = new StringBuilder

     val methods = cn.methods.asInstanceOf[java.util.ArrayList[MethodNode]].asScala
     val filter = funcList.size > 0
     for(m:MethodNode <- methods){
       //println(s"checking if ${(m.name,m.desc)} in ${funcList}: ${ funcList contains (m.name,m.desc) }");
       if( !filter || (funcList contains (m.name,m.desc)) ){
         val newdesc = re_outer(re_name(m.desc))
         var newname = re_name(m.name)
         if (newdesc.startsWith("()L$line")) {
           newname = """VAL\d+""".r.replaceAllIn(newname, "VALXX")
         }
         var methodHeader = s"${newname} ${newdesc} ${m.signature}"

         bytecode_string.append(methodHeader)
         hashObj.update(methodHeader.getBytes)
         localHash.update(methodHeader.getBytes)

         m.accept(tm) //visit method

         //read text from method visitor
         for(o <- p.getText.asScala){
           var s = o.toString()
           bytecode_string.append(s)
           hashObj.update(s.getBytes)
           localHash.update(s.getBytes)
         }
         p.getText.clear
       }
     }

    hashTrace map { _ += ("bytecode" -> bytecode_string.toString) }
    hashTrace map { _ += ("localHash" -> Base64.encodeBase64URLSafeString(localHash.digest)) }

    //recurse into callees of the given function
  }

  /* Hash all referenced fields of an object, ignoring everything except bytes
   * of primitives, making Array[Int,Int] = (Int,Int). Alone this might
   * introduce ambiguity but when combined with the bytecode hash there is no
   * ambiguity.
   *
   * use java reflection and sun.misc.unsafe to introspect the memory directly
   * copy the serialization code and walk the objects ourselves
   * this will only serialize non-transitive, non-static primitive values
   *
   * this routine assumes the object has been passed through the clean method
   * so there are no irrelevant fields to hash that remain on the object
   *
   * @param func the function to walk to hash the primitives
   * @param dos the stream where the bytes will be written to
   * @param visited a set of previously visited objects, to avoid duplicating work (used for recursion)
   * @param clz class 
   */
  def hashValue( value: Any, trace:Boolean = false ): Option[String] = {
    /* external helper to hash values, used *maybe* for Broadcast values */
    val bos = new ByteArrayOutputStream()
    val dos = new DataOutputStream(bos)
    val primitiveHashTrace = if(trace){ Some(new HashTraceMap) } else { None }

    /* hash input primitives needs an object wrapped around an array */
    case class ArrayHackByte(v:Array[Byte])
    case class ArrayHackChar(v:Array[Char])
    case class ArrayHackShort(v:Array[Short])
    case class ArrayHackInt(v:Array[Int])
    case class ArrayHackLong(v:Array[Long])
    case class ArrayHackFloat(v:Array[Float])
    case class ArrayHackDouble(v:Array[Double])
    case class ArrayHackBoolean(v:Array[Boolean])
    var vToHash = if(value.getClass().isArray){ 
      value match {
        case _:Array[Byte] => ArrayHackByte(value.asInstanceOf[Array[Byte]])
        case _:Array[Char] => ArrayHackChar(value.asInstanceOf[Array[Char]])
        case _:Array[Short] => ArrayHackShort(value.asInstanceOf[Array[Short]])
        case _:Array[Int] => ArrayHackInt(value.asInstanceOf[Array[Int]])
        case _:Array[Long] => ArrayHackLong(value.asInstanceOf[Array[Long]])
        case _:Array[Float] => ArrayHackFloat(value.asInstanceOf[Array[Float]])
        case _:Array[Double] => ArrayHackDouble(value.asInstanceOf[Array[Double]])
        case _:Array[Boolean] => ArrayHackBoolean(value.asInstanceOf[Array[Boolean]])
      }
    } else { 
      value.asInstanceOf[AnyRef]
    }

    //hashInputPrimitives( value.asInstanceOf[AnyRef], dos, primitiveHashTrace )
    hashInputPrimitives( vToHash, dos, primitiveHashTrace )
    dos.close()
    var primitive_hash64:Option[String] = if( bos.toByteArray.length > 0){
      var primitiveHash = MessageDigest.getInstance("SHA-256")
      primitiveHash.update(bos.toByteArray)
      Some(Base64.encodeBase64URLSafeString(primitiveHash.digest))
    } else {
      None
    }
    if(trace){
      implicit val jsonformats = Serialization.formats(NoTypeHints)
      var overallHashTrace = HashMap(
        "closureName" -> value.getClass.getName,
        "primitives"-> Map(
          "trace"->(primitiveHashTrace getOrElse ""),
          "hash"->primitive_hash64
          //"hashed_bytes"->hashedBytes,
        )
      )
      logInfo(s"Value Hashing trace: ${WriteJson(overallHashTrace)}")
    }
    primitive_hash64
  }
  def hashInputPrimitives(func: AnyRef,
    dos: DataOutputStream,
    hashTrace:Option[HashTraceMap],
    visited:Set[Object] = Set[Object](),
    clz:Class[_] = null
  ):Unit = {
    /* get unsafe handle */
    //var unsafe = Unsafe.getUnsafe();
    val field = classOf[Unsafe].getDeclaredField("theUnsafe");
    field.setAccessible(true);
    val unsafe = field.get(null).asInstanceOf[Unsafe];
    
    //avoid loops
    if(visited contains func){
      logTrace(s"returning early, ${func} already visited");
      return;
    }
    visited += func;

    /* queue of objects to visit */
    val toVisit = Queue[AnyRef]() //Queue is ordered?

    var cl = clz;
    if(cl == null){
      cl = func.getClass()
    }
    logTrace(s"hashInputPrimitives: class: ${cl.getName}")
    /* trace enough so we can attribute differences in hashing to a FIELD */
    //var hashTrace:HashTraceMap = Map(
    hashTrace map { _ += (
        "class"->cl.getName, 
        "fields"->ListBuffer[HashTraceMap](),
        "children"->ListBuffer[HashTraceMap]()
      )
    }

    for( f <- cl.getDeclaredFields ){
      f.setAccessible(true)
      val transient = Modifier.isTransient(f.getModifiers)
      val static = Modifier.isStatic(f.getModifiers)
      val fldtype = f.getType()
      val primitive = fldtype.isPrimitive()

      logTrace(s"hashInputPrimitives:\tfield ${f.getName} type: ${fldtype.getName}")
      logTrace(s"hashInputPrimitives:\t\tstatic: ${static} primitive: ${primitive} type: ${fldtype.getName} transient: ${transient}")

      var fieldTrace:Option[HashTraceMap] = hashTrace map((_) => { new HashTraceMap })
      fieldTrace map { _ += ( "name"->f.getName, "type"->fldtype.getName,
        "static"->static, "primitive"->primitive, "transient"->transient, "array"->fldtype.isArray ) }

      val fieldBytesTrace = hashTrace map ((_)=>{ListBuffer[String]()})

      val writeBytes = (output:DataOutputStream, thing:AnyRef, offset:Long, size:Integer) => {
        for(i <- 0 until size){
             val b = unsafe.getByte(thing.asInstanceOf[Object], offset+i)
             output.writeByte( b )
             fieldBytesTrace map { _ += b.toString }
             logTrace(s"hashInputPrimitives:\t\twriteBytes up to: ${output.size}")
        }
      }

      val skipped = (
        f.getName == "org$apache$spark$broadcast$TorrentBroadcast$$checksums" /* uninitialized memory when checksum false */
        || (cl.getName == "org.apache.spark.storage.BroadcastBlockId") /* ignore blockids of broadcast variables, we rely on their checkums and hls_value being equal */
        || (cl.getName == "org.apache.spark.sql.catalyst.expressions.ExprId") /* avoid randomly generated id and UUID by spark */
        || (cl.getName == "org.apache.spark.sql.execution.metric.SQLMetric") /* skip all fields of sql metrics */
        || (cl.getName == "org.apache.spark.ShuffleDependency" && f.getName == "shuffleId") /* ignore shuffle IDs */
      )

      if( !( static || transient || skipped ) ){
        /* only read fields that are not static and not transient */
        val offset = unsafe.objectFieldOffset( f )
        //if primitive, grab value
        if(primitive && !fldtype.isArray()){
          logTrace(s"hashInputPrimitives:\t\tadding to hash (primitive)")
          fieldTrace map { _ += ("hashedAs"->"primitive") }
          if( fldtype == classOf[Byte] ){
            //dos.writeByte( unsafe.getByte(func.asInstanceOf[Object], offset) )
            writeBytes(dos, func, offset, 1)
          } else if( fldtype == classOf[Char] ){
            //dos.writeChar( unsafe.getChar(func.asInstanceOf[Object], offset) )
            writeBytes(dos, func, offset, 2)
          } else if( fldtype == classOf[Int] ){
            //dos.writeInt( unsafe.getInt(func.asInstanceOf[Object], offset) )
            //dos.writeByte( unsafe.getByte(func.asInstanceOf[Object], offset) )
            //dos.writeByte( unsafe.getByte(func.asInstanceOf[Object], offset+1) )
            //dos.writeByte( unsafe.getByte(func.asInstanceOf[Object], offset+2) )
            //dos.writeByte( unsafe.getByte(func.asInstanceOf[Object], offset+3) )
            writeBytes(dos, func, offset, 4)
          } else if( fldtype == classOf[Long] ){
            //dos.writeLong( unsafe.getLong(func.asInstanceOf[Object], offset) )
            writeBytes(dos, func, offset, 8)
          } else if( fldtype == classOf[Float] ){
            //dos.writeFloat( unsafe.getFloat(func.asInstanceOf[Object], offset) )
            writeBytes(dos, func, offset, 4)
          } else if( fldtype == classOf[Double] ){
            //dos.writeDouble( unsafe.getDouble(func.asInstanceOf[Object], offset) )
            writeBytes(dos, func, offset, 8)
          } else if( fldtype == classOf[Boolean] ){
            //dos.writeBoolean( unsafe.getBoolean(func.asInstanceOf[Object], offset) )
            writeBytes(dos, func, offset, 1)
          } else {
            logError("hashInputPrimitives: Error could not determine primitive type")
          }
        }
        else if(fldtype.isArray()){
          val Value:Array[_] = f.get(func).asInstanceOf[Array[_]]
          if(Value != null){
            val length = ReflectArray.getLength(Value)
            val baseOffset = unsafe.arrayBaseOffset( fldtype );
            val indexScale = unsafe.arrayIndexScale( fldtype );
            logTrace(s"hashInputPrimitives:\tfield is array, value:${Value.mkString(",")} length: ${length} baseOffset: ${baseOffset} indexScale: ${indexScale}");
            logTrace(s"hashInputPrimitives:\t\tadding to hash (array)")
            fieldTrace map { _ += ("hashedAs"->"array", "length"->length, "baseOffset"->baseOffset,
              "indexScale"->indexScale, "value"->Value.mkString) }

            // if this is an array of objects (not primitives),
            // make sure we visit each one
            if(Value.isInstanceOf[Array[Object]]){
              logTrace(s"hashInputPrimitives:\t\twriting objects")
              fieldTrace map { _ += ("elementsHashedAs"->"objects") }
              for(p <- Value.asInstanceOf[Array[Object]]){
                if(p != null){
                  toVisit += p
                }
              }
            } else {
              logTrace(s"hashInputPrimitives:\t\twriting raw bytes")
              fieldTrace map { _ += ("elementsHashedAs"->"bytes") }
              //primitive arrays get visited directly??
              //read the bytes of the array so that we don't have to infer type
              // this has a problem in that reading bytes gives different byte ordering
              // than reading int, double, etc, due to endianness
              //val byteArray:Array[Byte] = new Array[Byte](indexScale*length);
              for(i <- 0 to (indexScale*length-1) ){
                //byteArray(i) = unsafe.getByte( Value, baseOffset + i );
                val b = unsafe.getByte( Value, baseOffset + i )
                dos.writeByte( b )
                fieldBytesTrace map { _ += b.toString }
              }
            }
          }
        } else {
          //if not primitive, recurse into and find it's primitives
          // we don't care about the value of the reference really
          // or the type, as this is encoded in the bytecode hash! :D
          val p = f.get(func)
          if(p != null){
            logTrace(s"hashInputPrimitives:\t\tadding to visit list")
            fieldTrace map { _ += ("hashedAs"->"visited") }
            toVisit += p
          }
        }
      } else {
        //trace non visited fields
        fieldTrace map { _ += ("hashedAs" -> "skipped") }
      }

      //add field trace to the hash trace
      fieldTrace map { _ += ("hashed_bytes" -> fieldBytesTrace.mkString(",")) }
      hashTrace map { _("fields").asInstanceOf[ListBuffer[HashTraceMap]] += fieldTrace.get }
    }

    logTrace(s"visit list: ${toVisit.mkString(",")}")
    for( p <- toVisit ){
      logTrace(s"visiting ${p}")
      val childTrace = hashTrace map((_) =>{new HashTraceMap})
      hashInputPrimitives(p, dos, childTrace, visited)
      hashTrace map { _("children").asInstanceOf[ListBuffer[HashTraceMap]] += childTrace.get }
    }

    //TODO support externalizable objects?

    //follow the parent class desc
    if( cl.getSuperclass() != null ){
      val childTrace = hashTrace map((_) =>{new HashTraceMap})
      hashInputPrimitives(func, dos, childTrace, visited, cl.getSuperclass() )
      hashTrace map { _ += ("superclass"->childTrace.get) }
    }
  }

  /* serialize the entire function to bytes, and fix/blank out
   * any fields that we don't need, then return the bytes for hashing
   * we just serialize the top-level class which should capture everything
   * required 
   *
   * this function is very deprecated and it contains rather specific spark
   * details
   * */
  def hashSerialization(func:AnyRef): Array[Byte] = {
    var serialize_func = (f:AnyRef) => {
      val bos = new ByteArrayOutputStream()
      val out = new ObjectOutputStream(bos)
      out.writeObject(func)
      out.close()
      //logTrace(s"serializationHash: unmangled serialized func byte array:\n${bos.toByteArray.mkString(",")}")
      new String(bos.toByteArray, "ISO-8859-1") //iso 8859-1 is 1-1 character to byte
    }
    /* fix up the serialization to avoid some non-determinism from the REPL */
    val re_serialized = (s:String) => {
     /* serialversionUID */
     // python re.sub(r'sr..lineXX.\$read(\$\$iwC)*........\x02', 'sr\x00\x09.REPLCLASS\x02', s)
     val all_res = "res\\d+".r.replaceAllIn(s, "resXX")
     val all_vals = "VAL\\d+".r.replaceAllIn(all_res, "valXX")
     val lineXX = "\\$line\\d+".r.replaceAllIn(all_vals, "lineXX")
     /* replace the serialversion ID with a dummy value
      * (?s) needed to get the dot matching newlines, etc.
      * */
     val replclass = "(?s)sr..lineXX.\\$read(\\$\\$iwC)*........\\x02\\x00".r.replaceAllIn(lineXX, "sr..REPLCLASS\\x02\\x00")

     /* replace the RDD ID with a dummy value
      * lots of context is included here, up to the start of the RDD id serialization, so we're sure we got it right
      * as time goes on we may need to refine this so that we don't miss a case
      * we need to mask the rddid off, because all parents have a numerical rdd, even if they also have RDDUnique
      * */
     val rddid = "r\\x00\\x18org.apache.spark.rdd.RDD........\\x02\\x00\\x05I\\x00\\x02idL\\x00\\x0echeckpointDatat\\x00\\x0eLscala/Option;L\\x00'org\\$apache\\$spark\\$rdd\\$RDD\\$\\$dependencies_t\\x00\\x16Lscala/collection/Seq;L\\x00\\$org\\$apache\\$spark\\$rdd\\$RDD\\$\\$evidence\\$1q\\x00~\\x00.L\\x00\\x0cstorageLevelt\\x00'Lorg/apache/spark/storage/StorageLevel;xp....s"
     .r.replaceAllIn(replclass, "RDDIDMASK")
     rddid
    }

    //testing that serialization is deterministic
    val serial1 = re_serialized(serialize_func(func));
    //logTrace(s"serializationHash: mangled serialized func byte array:\n${serial1.getBytes("ISO-8859-1").mkString(",")}")
    serial1.getBytes("ISO-8859-1")
  }

  /* returns a map of (class to (funcname, funcsig) ) of all functions called by the given class, recursively */
  type Class2Func = (String,String,String)
  private def getFunctionsCalled(obj: AnyRef): Set[Class2Func] = {
    logDebug(s"Finding functions called by ${obj.getClass.getName}")
    var cls = obj.getClass

    /* read first set */
    val seen = SortedSet[Class2Func]()
    SparkUtils.getClassReader(obj.getClass).accept(new CalledFunctionsFinder(seen), 0)

    /* add all called functions to visit stack */
    var stack = SortedSet[Class2Func]()
    SparkUtils.getClassReader(obj.getClass).accept(new CalledFunctionsFinder(stack), 0)

    /* visit all other functions */
    while(!stack.isEmpty){
      val tup = stack.head
      stack = stack.tail
      val clazz = Class.forName(tup._1.replace('/', '.'), false, Thread.currentThread.getContextClassLoader)
      logDebug(s"Recursively finding functions called by ${clazz.getName}")
      val cr = SparkUtils.getClassReader(clazz)

      val set = SortedSet[Class2Func]()
      val sigtup = (tup._2, tup._3)
      cr.accept(new CalledFunctionsFinder(set, Some(Set(sigtup))), 0)

      for (newtups <- set -- seen){
        seen += newtups
        stack += newtups
      }
    }
    logDebug(s"Found ${seen.size} functions called by ${obj.getClass.getName}")

    seen
  }

  /* Finds all functions called from the given class, returning a list */
  private class CalledFunctionsFinder(
      output: Set[(String,String,String)],
      funcsToRead: Option[Set[(String,String)]] = None) extends ClassVisitor(ASM5) {
    var myName: String = null
    var filterFunctions = !funcsToRead.isEmpty
    var followFunctions = funcsToRead getOrElse Set()

    override def visit(version: Int, access: Int, name: String, sig: String,
        superName: String, interfaces: Array[String]) {
      myName = name
    }

    override def visitMethod(access: Int, name: String, desc: String,
        sig: String, exceptions: Array[String]): MethodVisitor = {

          /* only visit this method if it's in the list */
         var sig = (name, desc)
         if(filterFunctions && !(followFunctions contains sig)){
           //return noop method visitor
           new MethodVisitor(ASM5) { }
         } else {
           new MethodVisitor(ASM5) {
             override def visitMethodInsn(
               op: Int, owner: String, name: String, desc: String, itf: Boolean) {
                 val argTypes = Type.getArgumentTypes(desc)

                 if(owner != myName){
                   output += new Tuple3(owner, name, desc)
                 }
             }
           }
         }
    }
  }

}
