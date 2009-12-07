package jboss.cloud.api


import config.Services
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.{HttpClient, HttpMethodBase}
import org.jboss.resteasy.plugins.server.servlet.{ResteasyBootstrap, HttpServletDispatcher}
import org.mortbay.jetty.servlet.Context
import org.testng.Assert._

/**
 * Wire up the mocks etc.
 * @author Michael Neale
 */
class ApiHelper {

  val HOST = "http://localhost:8888/api"

  Services.configure
  Services.deltaClient = new MockCloudClient
  Services.dep = new MockDeployer


  implicit def should(v: Any) = new Checker(v)

  def cleanDatabase = Services.database.clearDatabase
  def get(url: String) = Client.call(new GetMethod(HOST + url))
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

  case class TestResponse(m: HttpMethodBase) {
    def body = m.getResponseBodyAsString
  }


  /**
   * Embedded server for testing.
   */
    object ServerInstance {
      val server = startServer


      def startServer = {
        val server = new org.mortbay.jetty.Server(8888);
        val ctx = new Context(server, "/", Context.SESSIONS)
        ctx.setInitParams(params)
        ctx.addEventListener(new ResteasyBootstrap)
        ctx.addServlet(classOf[HttpServletDispatcher], "/api/*")
        server.setStopAtShutdown(true)
        server.start
        server
      }

      def params = {
        val hm = new java.util.HashMap[String, String]
        hm.put("resteasy.resources", "jboss.cloud.api.Server")
        hm
      }



    }

    object Client {
        val client = new HttpClient
        def call(method: HttpMethodBase) = {
          val s = ServerInstance.server
          client.executeMethod(method)
          TestResponse(method)
        }
    }
  

}