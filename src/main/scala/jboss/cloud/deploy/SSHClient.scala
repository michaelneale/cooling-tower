package jboss.cloud.deploy


import ch.ethz.ssh2.{Connection, SCPClient}
import java.io.File
import org.apache.commons.io.IOUtils
/**
 * Flings files via SSH !
 * @author Michael Neale
 */
class SSHClient {

  var conn: Connection = null

  def connect(host: String, userName: String, password: String) = {
    conn = new Connection(host)
    conn.connect
    conn.authenticateWithPassword(userName, password)
  }

  def connect(host: String, userName: String, key: Array[Char], passphrase: String)  = {
    conn = new Connection(host)
    conn.connect
    conn.authenticateWithPublicKey(userName, key, passphrase)    
  }

  def disconnect = conn.close

  def putFile(file: Array[Byte], fileName: String, path: String) = {
    val scp = new SCPClient(conn)
    scp.put(file, fileName, path)
  }

  def runScript(script: String) = {
    val sess = conn.openSession
    sess.execCommand(script)
    val result = (IOUtils.toString(sess.getStdout), IOUtils.toString(sess.getStderr), sess.getExitStatus)
		sess.close
    result
  }

}