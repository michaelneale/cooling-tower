package jboss.cloud.deltacloud


import java.io.StringReader
import java.net.{URL, URI}
import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.methods.{PostMethod, GetMethod}
import org.apache.commons.httpclient.{UsernamePasswordCredentials, HostConfiguration, HttpClient}
import org.testng.annotations.Test
import xml.{NodeSeq, Node, XML}


/**
 * More a smoke test then anything.  
 * @author Michael Neale
 */
@Test
class DeltaClientTest {


  def listStuff = {
    val dc = new DeltaClient
    val res = dc.images
    println(res)
  }


  def instance = {
    val dc = new DeltaClient
    val ins = dc.createInstance(Flavor("m1-small", 42, 42, "x"), Image("img1", "mic"), Realm("us", "US of A", "AVAILABLE"))
    println(ins)
    println(dc.pollInstanceState(ins.id))
  }

}