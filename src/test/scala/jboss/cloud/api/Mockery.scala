package jboss.cloud.api


import deltacloud.CloudClient
import deploy.Deployer
import java.security.SecureRandom
import java.util.Random

/**
 * Mocks for testing purposes.
 *  
 * @author Michael Neale
 */
class MockDeployer extends Deployer {
  var app: Application = null
  var ins: Instance = null
  def deploy(application: Application, instance: Instance) = {
    app = application
    ins = instance
  }
}

case class MockCloud(flvs: Seq[Flavor], imgs: Seq[Image]) extends CloudClient {
  var instances: Map[String, Instance] = Map()
  def images = imgs
  def flavors = flvs
  def pollInstanceState(id: String) = (instances(id).state, instances(id).publicAddresses)
  def createInstance(flavor: Flavor, image: Image, realm: Realm) = {
    var id = "X" + (new SecureRandom).nextInt
    val ins = Instance(id, "NAME_" + id, image, flavor, "PENDING", Array())
    instances = instances ++ Map(id -> ins)
    ins
  }
  def realms = List(Realm("1", "AU", "AVAILABLE"))
}