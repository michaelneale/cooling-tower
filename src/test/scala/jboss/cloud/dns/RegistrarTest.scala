package jboss.cloud.dns

import java.io.File
import org.testng.annotations.{BeforeTest, AfterSuite, BeforeSuite, Test}
import org.testng.Assert._



class RegistrarTest {


  var zoneDirectory: File = new File("_test_zone_directory_")

  @BeforeTest def directory = {
    delete(zoneDirectory )
    if (!zoneDirectory.exists) zoneDirectory.mkdir
  }

  @AfterSuite def cleanup = delete(zoneDirectory)

  @Test def createNewZone = {
    val reg = Registrar(zoneDirectory, "8.8.8.8")

    assertEquals(0, reg.listDomains.size)
    val zone = reg.registerNewDomain("samplezone.com", "4.4.4.4")
    assertEquals(1, reg.listDomains.size)

    var doms = reg.listSubDomains("samplezone.com")
    assertEquals(doms.size, 2)
    assertEquals("samplezone.com", doms(0))

    reg.bindSubDomain("samplezone.com", "wee.samplezone.com", "1.1.1.1")
    assertEquals(1, reg.listDomains.size)

    doms = reg.listSubDomains("samplezone.com")
    assertEquals(doms.size, 3)
    doms.contains("wee.samplezone.com")
    

  }




  private def delete(f: File) : Unit = if (f.isDirectory) f.listFiles.map(delete) else f.delete




  
}