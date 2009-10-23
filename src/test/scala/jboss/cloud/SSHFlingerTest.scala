package jboss.cloud


import ch.ethz.ssh2.{SCPClient, Connection, Session, StreamGobbler}
import deploy.SSHFlinger
import java.io.{BufferedReader, InputStreamReader}
import org.testng.annotations.Test




/**
 * 
 * @author Michael Neale
 */


class SSHFlingerTest {


  def sendAFile = {
    val sf = new SSHFlinger
    sf.putApp("hey ho".getBytes, "somethingElse.txt", "174.143.154.216", "deployer_acct", "yeahyeah")
  }


}

