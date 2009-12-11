package jboss.cloud.mapping


import java.io.{ByteArrayOutputStream, ByteArrayInputStream, InputStream}
import java.util.zip.ZipInputStream
import org.apache.commons.io.IOUtils

/**
 * Peeks in side application deployment archives.
 * @author Michael Neale
 */
class AppAnalyser {


  /**
   * TODO: This should peer inside the war/ear for a cloud.config properties file. 
   */
  def parseApplication(appName: String, appType: String, ins: InputStream) : (Application, Array[Byte])   =
    (
     Application(appName, appType, true, 0.2f, 4, 0, 1, System.currentTimeMillis, System.currentTimeMillis),
     IOUtils.toByteArray(ins)
    )



}