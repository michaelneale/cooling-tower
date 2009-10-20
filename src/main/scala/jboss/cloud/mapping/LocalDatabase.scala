package jboss.cloud.mapping


import com.thoughtworks.xstream.XStream
import java.io.{FileInputStream, FileOutputStream, File, InputStream}
import org.apache.commons.io.IOUtils
/**
 * Local persistence of data.
 * A Very dumb database.
 * 
 * @author Michael Neale
 */

class LocalDatabase {
  var ROOT = new File(".")
  var applications = dir("applications")
  var instances = dir("instances")
  val xstream = new XStream

  def storeApplicationBinary(application: Application, appBinary: Array[Byte]) = {
    val f = new File(applications, application.name + "." + application.applicationType + "." + application.version)
    if (f.exists) f.delete
    val fout = new FileOutputStream(f)
    IOUtils.write(appBinary, fout)
    fout.close
  }

  def listInstances = instances.listFiles.map((f: File) => xstream.fromXML(new FileInputStream(f)).asInstanceOf[Instance])

  def saveInstance(ins: Instance) = xstream.toXML(ins, new FileOutputStream(new File(instances, ins.id + ".xml")))


  private def dir(sub: String) = {
    val f = new File(ROOT, sub)
    if (!f.exists) f.mkdir
    f
  }



}