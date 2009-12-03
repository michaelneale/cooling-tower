package jboss.cloud.deltacloud


import config.Services
import java.io.StringReader
import javax.servlet.http.HttpServletResponse
import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.methods.{PostMethod, GetMethod}
import org.apache.commons.httpclient.{UsernamePasswordCredentials, HttpClient}
import xml.{Node, XML, NodeSeq}
/**
 * 
 * @author Michael Neale
 */

class DeltaClient {


  implicit def valueFromNode(n: NodeSeq) : NodeVal = new NodeVal(n)

  def images: Seq[Image] = {
    val images = XML.load(doGet(lookup("images")).getResponseBodyAsStream) \\ "image"
    images.map(n => Image(n.string("id"), n.string("name")))
  }


  def flavors: Seq[Flavor] = {
    val flavors = XML.load(doGet(lookup("flavors")).getResponseBodyAsStream) \\ "flavor"
    flavors.map(n => Flavor(n.string("id"), n.float("memory"), n.float("storage"), n.string("architecture")))
  }

  def realms: Seq[Realm] = {
    val realms = XML.load(doGet(lookup("realms")).getResponseBodyAsStream) \\ "realm"
    realms.map(n => Realm(n.string("id"), n.string("name"), n.string("state")))
  }

  def createInstance(flavor: Flavor, image: Image, realm : Realm) : Instance = {
    val post = new PostMethod(lookup("instances"))
    post.setRequestHeader("Accept", "application/xml")
    post.setParameter("image_id", image.id)
    post.setParameter("realm_id", realm.id)
    post.setParameter("flavor_id", flavor.id)
    client.executeMethod(post)
    if (post.getStatusCode == HttpServletResponse.SC_UNAUTHORIZED) throw new SecurityException("Unable to authenticate with deltacloud")
    val resp = XML.load(post.getResponseBodyAsStream)
    val instance = Instance(resp.string("id"), resp.string("name"), image, flavor,resp.string("state"),  Array())
    instance.publicAddresses = (resp \\ "public-addresses").foldLeft(Array[String]())((list, node) => list ++ Array(node.string("address")))
    instance
  }
  

  def pollInstanceState(id: String) : (String, Array[String]) =  {
    val resp = XML.load(doGet(lookup("instances") + "/" + id).getResponseBodyAsStream)
    val publicAddresses = (resp \\ "public-addresses").foldLeft(Array[String]())((list, node) => list ++ Array(node.string("address")))
    val state = resp.string("state")
    (state, publicAddresses)
  }


  class NodeVal(n: NodeSeq) {
    def string(name: String) = (n \\ name).text
    def float(name: String) = (n \\ name).text.toFloat
  }

  lazy val resources : Map[String, String] = {
    val links = XML.load(doGet(Services.deltaCloudConfig.apiURL).getResponseBodyAsStream) \\ "link"
    links.foldLeft(Map[String, String]()) ( (m , n) => m ++ Map(n.string("@rel") -> n.string("@href")) )
  }

  def lookup(resourceName: String) = {
    resources.get(resourceName) match {
      case Some(s) => s
      case None => throw new IllegalArgumentException("I have no idea where to find " + resourceName)
    }
  }


  private def client = {
    var client = new HttpClient
    client.getState().setCredentials(new AuthScope(null, -1), new UsernamePasswordCredentials(Services.deltaCloudConfig.userName, Services.deltaCloudConfig.password))
    client
  }

  private def doGet(url: String) : GetMethod = {
     val get = new GetMethod(url)
     get.setRequestHeader("Accept", "application/xml")
     client.executeMethod(get)
     if (get.getStatusCode == HttpServletResponse.SC_UNAUTHORIZED) throw new SecurityException("Unable to authenticate with deltacloud")
     get
  }

  

  

}



