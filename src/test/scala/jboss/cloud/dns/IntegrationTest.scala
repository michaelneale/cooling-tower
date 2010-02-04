package jboss.cloud.dns

import jboss.cloud.api.ApiHelper
import jboss.cloud.config.Services
import org.testng.annotations.{BeforeTest, BeforeMethod, AfterTest, Test}
import java.io.File

class DNSIntegrationTest extends ApiHelper {
  var zoneDirectory: File = new File("_test_zone_directory_")  //for naughty testing purposes

   @Test def shouldHaveNamingRoot = {
     get("/naming").body shouldBe <api><link href="/api/naming/domains" rel="domains"></link></api>
   }

   @Test def shouldRegisterDomainNames = {
     get    ("/naming/domains").body shouldBe <domains/>
     post   ("/naming/domains",  "name=samplezone.org")
     get    ("/naming/domains").body shouldBe <domains><link href="/api/naming/domains/samplezone.org" rel="domain"></link></domains>
     delete ("/naming/domains/samplezone.org")
     get    ("/naming/domains").body shouldBe <domains/>


   }

   @Test def shouldBeAbleToSubDomain = {
     post("/naming/domains",  "name=samplezone.org")
     get ("/naming/domains/samplezone.org").body shouldContain <link href="/api/naming/domains/samplezone.org/default" rel="address" />
     get ("/naming/domains/samplezone.org").body shouldContain <link href="/api/naming/domains/samplezone.org/zoneFile" rel="file" />
     get ("/naming/domains/samplezone.org/zoneFile").body shouldContain "SOA"

     get ("/naming/domains/samplezone.org/default").body shouldBe ""
     post("/naming/domains/samplezone.org/default", "address=1.2.3.4")
     get ("/naming/domains/samplezone.org/default").body shouldBe "1.2.3.4"
     post("/naming/domains/samplezone.org/default", "address=1.2.3.5")
     post("/naming/domains/samplezone.org/default", "1.2.3.6".getBytes, "test/plain")
     get ("/naming/domains/samplezone.org/default").body shouldBe "1.2.3.6"

     post("/naming/domains/samplezone.org", "subdomain=www&address=2.2.2.2")
     get ("/naming/domains/samplezone.org/www").body shouldBe "2.2.2.2"
     post("/naming/domains/samplezone.org", "subdomain=www&address=2.2.2.3")
     get ("/naming/domains/samplezone.org/www").body shouldBe "2.2.2.3"
     post("/naming/domains/samplezone.org/www", "address=2.2.2.2")
     get ("/naming/domains/samplezone.org/www").body shouldBe "2.2.2.2"

     post("/naming/domains/samplezone.org/www", "2.2.2.9".getBytes, "text/plain")
     get ("/naming/domains/samplezone.org/www").body shouldBe "2.2.2.9"

     get ("/naming/domains/samplezone.org").body shouldContain <link href="/api/naming/domains/samplezone.org/www" rel="address" />
     delete("/naming/domains/samplezone.org/www")
     get ("/naming/domains/samplezone.org").body shouldNotContain <link href="/api/naming/domains/samplezone.org/www" rel="address" />

   }

   




  




  @BeforeTest def before = Services.dnsZoneFolder = zoneDirectory.getPath
  @BeforeMethod def directory = {
    delete(zoneDirectory)
    if (!zoneDirectory.exists) zoneDirectory.mkdir
  }
  @AfterTest def cleanup = {
    delete(zoneDirectory)
    zoneDirectory.delete
  }

}