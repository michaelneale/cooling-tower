package jboss.cloud


import com.jcraft.jsch.{JSch, ChannelSftp}
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

  def testJSCH = {

            //
            //First Create a JSch session
            //
            System.out.println("Creating session.");
            val jsch = new JSch()
            jsch.addIdentity("path/to/pem")
            val session = jsch.getSession("root", "ec2-174-129-173-78.compute-1.amazonaws.com")
            val config = new java.util.Properties
            config.put("StrictHostKeyChecking", "no")
            session.setConfig(config)
            session.connect
            println("connected !")
            //Session session = null;
            //Channel channel = null;
            //ChannelSftp c = null;
            session.disconnect
  }

}