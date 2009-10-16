package jboss.cloud.deploy


import ch.ethz.ssh2.{Connection, StreamGobbler, SCPClient}
import java.io.{BufferedReader, InputStreamReader}
/**
 * Flings files via SSH !
 * @author Michael Neale
 */
class SSHFlinger {


  def putApp(file: Array[Byte], fileName : String, host: String, userName: String, password: String) = {
    val conn = new Connection(host)

    conn.connect

    /* Authenticate.
     * If you get an IOException saying something like
     * "Authentication method password not supported by the server at this stage."
     * then please check the FAQ.
     */
    val isAuthenticated = conn.authenticateWithPassword(userName, password)

    val sess = conn.openSession

    sess.execCommand("uname -a && date && uptime && who")

			/*
			 * This basic example does not handle stderr, which is sometimes dangerous
			 * (please read the FAQ).
			 */

			val stdout = new StreamGobbler(sess.getStdout())
			val br = new BufferedReader(new InputStreamReader(stdout));

      var line = ""
			while (true && line != null)
			{
				line = br.readLine();
				if (line != null) {
				  println(line)
        }
			}


			println("ExitCode: " + sess.getExitStatus())


			sess.close();


      val scp = new SCPClient(conn)
      scp.put(file, fileName, "deployments")

			conn.close
  }

}