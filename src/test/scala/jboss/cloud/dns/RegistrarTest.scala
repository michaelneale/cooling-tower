package jboss.cloud.dns

import java.io.File
import org.testng.Assert._
import java.text.SimpleDateFormat
import java.util.Date
import org.testng.annotations._


class RegistrarTest {

  var zoneDirectory: File = new File("_test_zone_directory_")  //for naughty testing purposes


  @Test def createNewZone = {
    val reg = Registrar(zoneDirectory, "8.8.8.8")

    assertEquals(0, reg.listDomains.size)
    reg.registerNewDomain("samplezone.com", "4.4.4.4")
    val initialZoneFile = reg.zoneFileFor("samplezone.com")
    assertEquals(1, reg.listDomains.size)


    var doms = reg.listSubDomains("samplezone.com")
    assertEquals(doms.size, 2)
    assertEquals("samplezone.com", doms(0))

    reg.bindSubDomain("samplezone.com", "wee", "1.1.1.1")



    assertEquals(1, reg.listDomains.size)

    doms = reg.listSubDomains("samplezone.com")
    assertEquals(doms.size, 3)
    doms.contains("wee")

    assertEquals(reg.subDomainAddress("samplezone.com", "wee"), "1.1.1.1")

    reg.registerNewDomain("michael.org", "2.2.2.2")
    assertEquals(2, reg.listDomains.size)
    assertTrue(reg.listDomains.contains("michael.org"))

    doms = reg.listSubDomains("samplezone.com")
    assertEquals(doms.size, 3)
    assertTrue(doms.contains("wee.samplezone.com"))
  }

  @Test def subDomains = {
    val reg = Registrar(zoneDirectory, "8.8.8.8")
    assertEquals(0, reg.listDomains.size)
    val zone = reg.registerNewDomain("samplezone.com", "")
    assertEquals(1, reg.listDomains.size)

    reg.bindSubDomain("samplezone.com", "tom", "1.2.3.4")
    assertTrue(reg.listSubDomains("samplezone.com").contains("tom.samplezone.com"))

    reg.bindSubDomain("samplezone.com", "harry", "2.2.3.4")
    assertTrue(reg.listSubDomains("samplezone.com").contains("harry.samplezone.com"))
    assertTrue(reg.listSubDomains("samplezone.com").contains("tom.samplezone.com"))

    assertEquals(reg.subDomainAddress("samplezone.com", "tom"), "1.2.3.4")
    assertEquals(reg.subDomainAddress("samplezone.com", "harry"), "2.2.3.4")

    reg.bindSubDomain("samplezone.com", "tom", "1.2.3.5")
    assertEquals(reg.subDomainAddress("samplezone.com", "tom"), "1.2.3.5")



    reg.removeSubDomain("samplezone.com", "harry")
    assertFalse(reg.listSubDomains("samplezone.com").contains("harry.samplezone.com"))
    assertTrue(reg.listSubDomains("samplezone.com").contains("tom.samplezone.com"))

  }

  @Test def shouldUpdateSerial = {
    
    val reg = Registrar(zoneDirectory, "8.8.8.8")
    val serial = new SimpleDateFormat("yyyyMMdd").format(new Date).toInt
    val zoneFile = reg.registerNewDomain("foo.com", "1.1.1.1")
    assertEquals(reg.zoneFileFor("foo.com"), zoneFile)
    reg.bindSubDomain("foo.com", "bar", "1.2.3.4")
    val newZone = reg.zoneFileFor("foo.com")
    assertFalse(newZone == zoneFile)
    val newSerial = "" + (serial + 1)
    assertTrue(newZone.indexOf(newSerial) > 0, newZone)

  }


  @BeforeMethod def directory = {
    delete(zoneDirectory)
    if (!zoneDirectory.exists) zoneDirectory.mkdir
  }

  @AfterTest def cleanup = {
    delete(zoneDirectory)
    zoneDirectory.delete
  }



  private def delete(f: File) : Unit = if (f.isDirectory) f.listFiles.map(delete) else f.delete




  
}