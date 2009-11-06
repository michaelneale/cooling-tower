package jboss.cloud.mapping


import com.thoughtworks.xstream.XStream
import deploy.Task
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
  
  def applications = dir("applications")
  def instances = dir("instances")
  def tasks = dir("tasks")
  val xstream = new XStream

  def saveApplicationBinary(application: Application, appBinary: Array[Byte]) = {
    val f = new File(applications, application.name + "." + application.applicationType + "." + application.version)
    if (f.exists) f.delete
    val fout = new FileOutputStream(f)
    IOUtils.write(appBinary, fout)
    fout.close
  }

  def saveApplication(a: Application) = xstream.toXML(a, new FileOutputStream(new File(applications, a.name + ".xml")))
  def listApplications = applications.listFiles.map((f: File) => xstream.fromXML(new FileInputStream(f)).asInstanceOf[Application])


  def loadApplicationBinary(application: Application) : Array[Byte] = {
    val f = new File(applications, application.name + "." + application.applicationType + "." + application.version)
    IOUtils.toByteArray(new FileInputStream(f))      
  }

  def listInstances = instances.listFiles.map((f: File) => xstream.fromXML(new FileInputStream(f)).asInstanceOf[Instance])
  def saveInstance(ins: Instance) = xstream.toXML(ins, new FileOutputStream(new File(instances, ins.id + ".xml")))
  def loadInstance(id: String) = xstream.fromXML(new FileInputStream(new File(instances, id + ".xml"))).asInstanceOf[Instance]

  def saveTask(ts: Task) = xstream.toXML(ts, new FileOutputStream(new File(tasks, ts.id + ".xml")));
  def removeTask(ts: Task) = (new File(tasks, ts.id + ".xml")).delete
  def listTasks = tasks.listFiles.map((f: File) => xstream.fromXML(new FileInputStream(f)).asInstanceOf[Task])



  private def dir(sub: String) = {
    if (!ROOT.exists) ROOT.mkdir
    val f = new File(ROOT, sub)
    if (!f.exists) f.mkdir
    f
  }



}