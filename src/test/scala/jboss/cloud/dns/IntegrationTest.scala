package jboss.cloud.dns

import jboss.cloud.api.ApiHelper
import jboss.cloud.config.Services
import org.testng.annotations.{BeforeTest, BeforeMethod, AfterTest, Test}
import java.io.File

class DNSIntegrationTest extends ApiHelper {
  var zoneDirectory: File = new File("_test_zone_directory_")  //for naughty testing purposes
  /**
   * get("/naming") returns list of domains under management, and links to zone info, current default address
   * get("/naming/domain.com") returns list of subdomains
   * get("/naming/domain.com/zone") returns its IP or CNAME
   * get("/naming/domain.com/zone") returns its IP or CNAME
   *
   * post("/naming") - post a new zone to manage
   * post("/naming/domain") - post a new subdomain
   * put....
   * delete....
   *
   * Architectural Change:
   *
   *  Also refactor test helper for HTTP tests into a trait to mix in to the test?
   *  Need to think what top level 4 features are, and how they are URL structured...
   *
   *
   */

   @Test def shouldList = {
      get("/naming").body shouldBe <api><link href="/api/naming/domains" rel="domains"></link></api>
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