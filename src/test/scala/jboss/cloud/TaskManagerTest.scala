package jboss.cloud


import api.Services
import deltacloud.DeltaClient
import deploy.{TaskManager, Deployer}
import org.testng.annotations.Test

/**
 * 
 * @author Michael Neale
 */

class TaskManagerTest {

  @Test def canMockOut = {
    class MockDC extends DeltaClient {
      override def createInstance(flavor: Flavor, image: Image, realm: Realm) = null

      override def pollInstanceState(id: Int) = "RUNNING"

      override def images = null

      override def stopInstance(id: Int) = null

      override def realms = null

      override def flavors = null
    }

    class MockDeployer extends Deployer {
      override def deploy(application: Application, instance: Instance) = null
    }

    Services.deltaClient = new MockDC
    Services.dep = new MockDeployer

    




  }
}