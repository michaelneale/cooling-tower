package jboss.cloud


import deploy.SSHClient
import java.io.{FileInputStream, File}
import org.apache.commons.io.IOUtils
import org.testng.annotations.Test
import org.testng.Assert._

/**
 * 
 * @author Michael Neale
 */

class SSHClientTest {
  def testPwd = {
    val c = new SSHClient
    c.connect("XXX", "root", "aed")
    println(c.runScript("mkdir deployment-uploads\nls -l"))
    c.putFile("Some content".getBytes, "mycode.txt", "deployment-uploads")
    c.runScript("mv deployment-uploads/mycode.txt ./mycode.txt")
    println(c.runScript("ls -l"))
    c.disconnect
  }


  def testKey = {
    val c = new SSHClient
    val f = new File("abc")
    val key = IOUtils.toString(new FileInputStream(f))
    assertTrue(f.exists)
    println(key.trim)

    
    c.connect("abc.compute-1.amazonaws.com", "root", key.trim.toCharArray, null)
    println(c.runScript("mkdir deployment-uploads\nls -l"))
    c.putFile("Some content".getBytes, "mycode.txt", "deployment-uploads")
    c.runScript("mv deployment-uploads/mycode.txt ./mycode.txt")
    println(c.runScript("ls -l"))           
    c.disconnect
  }

}