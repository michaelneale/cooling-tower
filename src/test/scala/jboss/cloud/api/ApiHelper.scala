package jboss.cloud.api


import config.Services
import org.testng.Assert._

/**
 * Wire up the mocks etc.
 * @author Michael Neale
 */
class ApiHelper {

  Services.configure
  Services.deltaClient = new MockCloudClient
  Services.dep = new MockDeployer


  implicit def should(v: Any) = new Checker(v)

  def cleanDatabase = Services.database.clearDatabase
  def get(url: String) = ""
  def post(url: String, data: Array[Byte]) = ""
  def put(url: String, data: Array[Byte]) = ""

  def cloudClient = Services.deltaClient.asInstanceOf[MockCloudClient]
  def deployer = Services.deployer.asInstanceOf[MockDeployer]


  /** Provide checking trickery */
  class Checker(v: Any) {
    def shouldBe(a: Any) = if (v != a) fail("expected " + a.toString + " but was " + v.toString)
    def shouldMatch(pattern: String) = {
      if (!v.isInstanceOf[String]) fail("Can only match on string")
      if (!v.asInstanceOf[String].matches(pattern)) fail(v + " does not match with " + pattern)
    }
  }
  

}