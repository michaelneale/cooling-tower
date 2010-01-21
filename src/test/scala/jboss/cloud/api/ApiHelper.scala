package jboss.cloud.api


import java.io.ByteArrayInputStream
import org.apache.commons.httpclient.methods.{PostMethod, GetMethod}
import org.apache.commons.httpclient.{HttpClient, HttpMethodBase}
import org.jboss.resteasy.plugins.server.servlet.{ResteasyBootstrap, HttpServletDispatcher}
import org.mortbay.jetty.servlet.Context
import org.testng.Assert._
import xml.Elem
import jboss.cloud.TestDB
import jboss.cloud.config.Services

/**
 * Wire up the mocks etc.
 * @author Michael Neale
 */
class ApiHelper {

  val HOST = "http://localhost:8888/api"

  Services.configure
  Services.db = TestDB.getDB
  Services.dep = new MockDeployer


  implicit def should(v: Any) = new Checker(v)

  def get(url: String) = Client.call(new GetMethod(HOST + url))
  def post(url: String, data: Array[Byte], contentType: String) = {
    val pm = new PostMethod(HOST + url)
    pm.setRequestBody(new ByteArrayInputStream(data))
    pm.setRequestHeader("Content-Type", contentType)
    Client.call(pm)
  }
  def put(url: String, data: Array[Byte]) = ""

  def deployer = Services.deployer.asInstanceOf[MockDeployer]


  /** Provide checking trickery */
  class Checker(v: Any) {
    def shouldBe(xml: Elem) :Unit = shouldBe(xml.toString)
    def shouldBe(a: Any) = if (v != a) fail("expected " + a.toString + " but was " + v.toString)
    def shouldMatch(pattern: String) = {
      if (!v.isInstanceOf[String]) fail("Can only match on string")
      if (!v.asInstanceOf[String].matches(pattern)) fail(v + " does not match with " + pattern)
    }

    def shouldMatch(xml: Elem) : Unit = shouldMatch(xml.toString)
  }

  case class TestResponse(m: HttpMethodBase) {
    def body = m.getResponseBodyAsString
    def statusCode = m.getStatusCode
    def header(name: String) = m.getResponseHeader(name).getValue
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