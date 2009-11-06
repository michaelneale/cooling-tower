package jboss.cloud.api


import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.{NameValuePair, HttpClient}

/**
 * 
 * @author Michael Neale
 */

class NewApplicationTest {
  def testCreate = {
    //setup mocks...
    //POST /applications/myapp.war
    //GET /applications/mywap.war
    //GET /applications
    val x = new HttpClient
    val p = new PostMethod
    val vp = new NameValuePair
 //   p.setRequestBody(vp)

    println("OK")

  }
}