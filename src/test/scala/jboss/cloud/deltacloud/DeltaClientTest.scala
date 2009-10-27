package jboss.cloud.deltacloud


import java.io.StringReader
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.{HostConfiguration, HttpClient}
import org.testng.annotations.Test
import xml.{NodeSeq, Node, XML}
/**
 * 
 * @author Michael Neale
 */

class DeltaClientTest {

  implicit def valueFromNode(n: NodeSeq) : NodeVal = new NodeVal(n)

  @Test def listImages = {
    var client = new HttpClient
    val hostConfig = new HostConfiguration

    hostConfig.setHost("localhost", 3000)
    client.setHostConfiguration(hostConfig)

    val get = new GetMethod("/api/flavors")
    get.setRequestHeader("Accept", "application/xml")
    client.executeMethod(get)

    val dom = XML.load(new StringReader(get.getResponseBodyAsString))
    val flavors =  dom \\ "flavor"
    val result = flavors.map((n: Node) => Flavor(n.string("id"), n.float("memory"), n.float("storage"), n.string("architecture") ))
    println(result)

  }


  class NodeVal(n: NodeSeq) {
    def string(name: String) = (n \\ name).text
    def float(name: String) = (n \\ name).text.toFloat
  }

  @Test def testFindAPI = {
    var client = new HttpClient
    val hostConfig = new HostConfiguration

    hostConfig.setHost("localhost", 3000)
    client.setHostConfiguration(hostConfig)

    val get = new GetMethod("/api")
    get.setRequestHeader("Accept", "application/xml")
    client.executeMethod(get)

    println(get.getResponseBodyAsString)


  }
}