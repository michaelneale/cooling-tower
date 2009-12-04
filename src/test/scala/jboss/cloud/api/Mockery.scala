package jboss.cloud.api


import deltacloud.CloudClient
import deploy.Deployer

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

class MockCloudClient extends CloudClient {
  def images = null

  def flavors = null

  def pollInstanceState(id: String) = null

  def createInstance(flavor: Flavor, image: Image, realm: Realm) = null

  def realms = null
  
}