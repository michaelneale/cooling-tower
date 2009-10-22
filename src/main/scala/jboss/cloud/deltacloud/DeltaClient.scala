package jboss.cloud.deltacloud

/**
 * TODO: implement
 * @author Michael Neale
 */

class DeltaClient {
  def images: Seq[Image] = List()
  def flavors: Seq[Flavor] = List()
  def realms: Seq[Realm] = List()

  def createInstance(flavor: Flavor, image: Image, realm : Realm) : Instance = {
    null
  }
  
  def stopInstance(id: Int) = {
    println("stopping")
  }

  def pollInstanceState(id: Int) : String = {
    null
  }
}



