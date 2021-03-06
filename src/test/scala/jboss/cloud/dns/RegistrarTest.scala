package jboss.cloud.dns

import java.io.File
import org.testng.Assert._
import java.text.SimpleDateFormat
import java.util.Date
import org.testng.annotations._
import org.jboss.logging.Logger


class RegistrarTest  {
  var zoneDirectory: File = new File("_test_zone_directory_")  //for naughty testing purposes
  var reg = new Registrar

  val log = Logger.getLogger(getClass)

  @Test def createNewZone = {

    log.infov("Here we go {0} !", 42)

    assertEquals(0, reg.listDomains.size)
    reg.registerNewDomain("samplezone.com")
    val initialZoneFile = reg.zoneFileFor("samplezone.com")
    assertEquals(1, reg.listDomains.size)


    var doms = reg.listSubDomains("samplezone.com")
    assertEquals(doms.size, 0)

    reg.updateSubDomain("samplezone.com", "wee", "1.1.1.1")


    assertEquals(1, reg.listDomains.size)

    doms = reg.listSubDomains("samplezone.com")
    assertEquals(doms.size, 1)
    doms.contains("wee")

    assertEquals(reg.subDomainAddress("samplezone.com", "wee"), "1.1.1.1")

    reg.registerNewDomain("michael.org")
    assertEquals(2, reg.listDomains.size)
    assertTrue(reg.listDomains.contains("michael.org"))

    doms = reg.listSubDomains("samplezone.com")
    assertEquals(doms.size, 1)
    assertTrue(doms.contains("wee"))

    assertTrue(reg.zoneFileFor("samplezone.com").contains("wee.samplezone.com"))

  }


  @Test def services = {
    reg.registerNewDomain("samplezone.com")
    reg.updateService("samplezone.com", "wallet.open", "www.apple.com", 443)
    assertEquals(1, reg.listServices("samplezone.com").size)
    reg.updateService("samplezone.com", "wallet.open", "www.apple.com", 80)
    assertEquals(1, reg.listServices("samplezone.com").size)
    assertTrue(reg.zoneFileFor("samplezone.com").indexOf("www.apple.com") > -1)
    assertTrue(reg.zoneFileFor("samplezone.com").indexOf("SRV") > -1)
    reg.removeService("samplezone.com", "wallet.open")
    assertEquals(0, reg.listServices("samplezone.com").size)
  }

  @Test def txt = {
    reg.registerNewDomain("samplezone.com")
    reg.updateTxt("samplezone.com", "foo", "some text here")
    assertEquals(1, reg.listTxt("samplezone.com").size)
    assertTrue(reg.zoneFileFor("samplezone.com").indexOf("some text here") > -1)
    reg.removeTxt("samplezone.com", "foo")
    assertEquals(0, reg.listTxt("samplezone.com").size)

  }


  @Test def subDomains = {
    assertEquals(0, reg.listDomains.size)
    val zone = reg.registerNewDomain("samplezone.com")
    assertEquals(1, reg.listDomains.size)

    reg.updateSubDomain("samplezone.com", "tom", "1.2.3.4")
    assertTrue(reg.listSubDomains("samplezone.com").contains("tom"))

    reg.updateSubDomain("samplezone.com", "harry", "2.2.3.4")
    assertTrue(reg.listSubDomains("samplezone.com").contains("harry"))
    assertTrue(reg.listSubDomains("samplezone.com").contains("tom"))

    assertEquals(reg.subDomainAddress("samplezone.com", "tom"), "1.2.3.4")
    assertEquals(reg.subDomainAddress("samplezone.com", "harry"), "2.2.3.4")

    reg.updateSubDomain("samplezone.com", "tom", "1.2.3.5")
    assertEquals(reg.subDomainAddress("samplezone.com", "tom"), "1.2.3.5")
    reg.removeSubDomain("samplezone.com", "harry")
    assertFalse(reg.listSubDomains("samplezone.com").contains("harry"))
    assertTrue(reg.listSubDomains("samplezone.com").contains("tom"))

    //should store it as a CNAME
    reg.updateSubDomain("samplezone.com", "boo", "www.smh.com.au")
    assertEquals(reg.subDomainAddress("samplezone.com", "boo"), "www.smh.com.au")
    reg.updateDefaultAddress("samplezone.com", "2.5.4.3", null)
    assertTrue(reg.listSubDomains("samplezone.com") contains ("boo"))
    assertFalse(reg.listSubDomains("samplezone.com") contains ("dns"))

    println(reg.listSubDomains("samplezone.com"))



  }


  @Test def shouldUpdateSerial = {
    val serial = new SimpleDateFormat("yyyyMMdd").format(new Date).toInt
    val zoneFile = reg.registerNewDomain("foo.com")
    reg.updateSubDomain("foo.com", "bar", "1.2.3.4")
    val newZone = reg.zoneFileFor("foo.com")
    assertFalse(newZone == zoneFile)
    val newSerial = "" + (serial + 1)
    assertTrue(newZone.indexOf(newSerial) > 0, newZone)
  }

  /** For the default address of something.com */
  @Test def defaultAddress = {
    reg.registerNewDomain("blah.com")
    reg.updateDefaultAddress("blah.com", null, "1.1.1.1")
    assertEquals(reg.defaultAddressFor("blah.com"), "1.1.1.1")
    reg.updateDefaultAddress("blah.com", "2.2.2.2", null)
    assertEquals(reg.defaultAddressFor("blah.com"), "2.2.2.2")
  }

  @Test def shouldHandleCNamesTransparently = {
    reg.registerNewDomain("something.com")
    reg.updateSubDomain("something.com", "news", "www.smh.com.au")
    assertEquals(reg.subDomainAddress("something.com", "news"), "www.smh.com.au")
  }

  @Test def removeDomain = {
    reg.registerNewDomain("blah.com")
    reg.registerNewDomain("rlah.com")
    reg.removeDomain("blah.com")
    assertEquals(1, reg.listDomains.size)
    assertTrue(reg.listDomains.contains("rlah.com"))
  }
  



  @Test def noSecondary = {
    reg.secondaryDNS = null
    reg.registerNewDomain("blah.com")
    reg.registerNewDomain("rlah.com")
    reg.removeDomain("blah.com")
    assertEquals(1, reg.listDomains.size)
    assertTrue(reg.listDomains.contains("rlah.com"))

  }



  @BeforeTest def before = {
    reg.primaryDNS = "8.8.8.8"
    reg.secondaryDNS = "8.4.4.4"
    reg.rootDirectory = zoneDirectory
    
  }

  @BeforeMethod def directory = {
    delete(zoneDirectory)
    if (!zoneDirectory.exists) zoneDirectory.mkdir
  }

  @AfterSuite def cleanup = {
    delete(zoneDirectory)
    zoneDirectory.delete
  }



  def delete(f: File) : Unit = if (f.isDirectory) f.listFiles.map(delete) else f.delete




  
}