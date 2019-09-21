package ca.ubc.ece.systems

import ca.ubc.ece.systems.ClosureHash
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{write => WriteJson}
import java.net.{URLClassLoader, URL}
import java.io.File;

import org.apache.log4j.{Logger, Level};
import org.apache.log4j.BasicConfigurator;

object HashCLI {
  implicit val jsonformats = Serialization.formats(NoTypeHints)
  val usage = """
    ./cli.sh <classfile>
  """
  def main(args: Array[String]){
    /* set up logging as if we were in spark */
    BasicConfigurator.configure();
    val logger = Logger.getLogger("ca.ubc.ece.systems.ClosureHash");
    //logger.setLevel(Level.DEBUG)
    //logger.setLevel(Level.INFO)
    logger.setLevel(Level.OFF)

    //val lambda = (x:Int) => {x*2+10}
    //val basic_hash = ClosureHash.hashWithTrace(lambda)
    //println(WriteJson(basic_hash.get._2))
    //return

    /* parse args */
    //println(s"args: ${args.length}")
    if(args.length != 1){
      println(usage)
      return;
    }
    val classfile = args(0)
    /* we need the full path for java to properly load the class */
    val jfile = new File(classfile)
    val dirparts = jfile.getCanonicalPath().split("/")
    //val classpath = args(0)
    //val cls = args(1)

    val cls = dirparts.takeRight(1)(0).replaceAll(".class$", "")
    val classpath = dirparts.slice(0,dirparts.length-1).mkString("/") + "/"
    
    //println(s"classpath: ${classpath} cls: ${cls}")

    /* load class and hash */
    val cl = URLClassLoader.newInstance(Array(new URL(s"file://${classpath}")))
    /* enable the thread to find this class loader in case we need more from
     * this directory */
    Thread.currentThread.setContextClassLoader(cl)
    val loaded = cl.loadClass(cls)

    /* so far this has been un-needed */
    //for(m <- loaded.getMethods){
      //m.setAccessible(true)
    //}

    val instance = loaded.newInstance()
    val anyref_instance = instance.asInstanceOf[AnyRef]

    val hash = ClosureHash.hashWithTrace(anyref_instance, true)
    println(WriteJson(hash.get._2))
  }
}
