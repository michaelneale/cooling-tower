package jboss.cloud.dns

import java.io.{FileOutputStream, File}
import org.apache.commons.io.IOUtils
import org.testng.annotations.Test
import org.testng.Assert._
import org.xbill.DNS.{RRset, ARecord, Name, Zone}
/**
 * Some basics of DNS lib.
 * @author Michael Neale
 */
class ExploratoryTest {
  @Test def zones = {
    val zoneFile = new File("/Users/Shared/samplezone.org")
    val origin = Name.fromString(zoneFile.getName, Name.root)
    val zone:Zone = new Zone(origin, zoneFile.getPath)
    println(zone.toMasterFile)
    IOUtils.write(zone.toMasterFile, new FileOutputStream("/Users/Shared/samplezone.org.new"))

    val zoneFile2 = new File("/Users/Shared/samplezone.org")
    val origin2 = Name.fromString(zoneFile2.getName, Name.root)
    val zone2:Zone = new Zone(origin2, zoneFile2.getPath)
    println(zone2.toMasterFile)

    val it = zone2.iterator
    while (it.hasNext) {
      val rec = it.next.asInstanceOf[RRset]
      val iter = rec.rrs
      while(iter.hasNext) {
        println("some record: ")        
        println(iter.next.asInstanceOf[Object].getClass.getName)  
      }

    }
  }


}