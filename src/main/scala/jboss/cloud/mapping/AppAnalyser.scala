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
   * name should include file extension.
   */
  def parseApplication(name: String, ins: InputStream) : (Application, Array[Byte])   = {
    val nm = name.split('.')
    val bytes = IOUtils.toByteArray(ins)
    val app = Application(nm(0), nm(1), true, 0.2f, 4, 0, 1, System.currentTimeMillis, System.currentTimeMillis)
    (app, bytes)
  }


}