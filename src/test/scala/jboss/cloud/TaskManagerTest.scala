package jboss.cloud


import api.Services
import deltacloud.DeltaClient
import deploy.{CreateInstance, DeployApplication, TaskManager, Deployer}
import java.security.SecureRandom
import java.util.concurrent.{Future, Callable, Executors, Executor}
import java.util.Random
import org.testng.annotations.{AfterTest, BeforeTest, Test}
import org.testng.Assert._
/**
 * 
 * @author Michael Neale
 */

class TaskManagerTest {

  @AfterTest def resetDependencies = Services.configure

  @Test def checkDeployTask = {
    var instanceState = "RUNNING"
    var publicAddresses = Array("foo.bar")
    var createdInstance: Instance = null
    class MockDC extends DeltaClient {
      override def createInstance(flavor: Flavor, image: Image, realm: Realm) = {
        val id = "" + (new SecureRandom).nextInt
        createdInstance = Instance(id, "faux", image, flavor, "PENDING", Array())
        createdInstance
      }
      override def pollInstanceState(id: String) = (instanceState, publicAddresses)
      override def images = null
      override def realms = null
      override def flavors = null
    }

    var deployedApp: Application = null
    var deployedInstance: Instance = null

    class MockDeployer extends Deployer {
      override def deploy(application: Application, instance: Instance) = {
        deployedApp = application
        deployedInstance = instance
      }
    }

    Services.deltaClient = new MockDC
    Services.dep = new MockDeployer
    Services.db = TestDB.getDB


    //Test simple deploy
    val tm = new TaskManager
    tm.WAIT_FOR_STATE = 500 

    assertEquals(Services.database.listTasks.length, 0)
    

    val app = Application("mic", "war", true, 1, /* in GB */ 5, /* In GB */1, 1,System.currentTimeMillis, System.currentTimeMillis)

    val instance = Instance("42", "ins1", Image("42", "my image"), Flavor("42", 42, 42, "x86"), "RUNNING", Array(app))
    Services.database.saveInstance(instance)
    tm.add(DeployApplication("mic", instance))
    Thread.sleep(100)

    assertEquals(Services.database.listTasks.length, 0)


    assertNotNull(deployedApp)
    assertNotNull(deployedInstance)

    assertSame(app, deployedApp)
    assertSame(instance, deployedInstance)

    //and now deploy another
    val app2 = Application("jo", "war", true, 1, /* in GB */ 5, /* In GB */1, 1,System.currentTimeMillis, System.currentTimeMillis)
    instance.applications = instance.applications ++ Array(app2)
    Services.database.saveInstance(instance)


    tm.add(DeployApplication("jo", instance))
    Thread.sleep(100)
    assertSame(app2, deployedApp)
    assertSame(instance, deployedInstance)
    deployedApp = null
    assertEquals(Services.database.listTasks.length, 0)


    //check a unready instance
    instance.state = "PENDING"
    instanceState = "PENDING"
    Services.database.saveInstance(instance)    

    tm.add(DeployApplication("jo", instance))
    Thread.sleep(100)

    assertEquals(Services.database.listTasks.length, 1)
    assertEquals(deployedApp, null)

    instanceState = "RUNNING"
    Thread.sleep(1000)


    assertEquals(Services.database.listTasks.length, 0)
    assertEquals(app2, deployedApp)
    assertEquals("foo.bar", Services.database.loadInstance(instance.id).publicAddresses(0))


    deployedApp = null
    deployedInstance = null
    instanceState = "RUNNING"

    //and now for creating a new instance to host an application
    createdInstance = null
    val appNew = Application("chloe", "war", true, 1, /* in GB */ 5, /* In GB */1, 1,System.currentTimeMillis, System.currentTimeMillis)
    val flv = Flavor("42", 42, 42, "x86")
    val icr = InstanceCreateRequest(flv, Image("42", "my image"), Realm("42", "foo", "AVAILABLE"), appNew)
    tm.add(CreateInstance(icr))

    assertEquals(Services.database.listTasks.length, 1)
    Thread.sleep(1000)
    assertEquals(Services.database.listTasks.length, 0)
    assertNotNull(deployedInstance)
    assertNotNull(createdInstance)
    assertSame(deployedInstance, createdInstance)
    assertSame(createdInstance.flavor, flv)
    assertSame(appNew, deployedApp)

    assertEquals(1, Services.database.listInstances.filter(_.id == createdInstance.id).size)
    val loadedInstance = Services.database.listInstances.filter(_.id == createdInstance.id)(0)
    assertEquals(flv.id, loadedInstance.flavor.id)
    assertEquals(1, loadedInstance.applications.size)
    
    assertEquals(loadedInstance.applications(0).name, "chloe")



  }
}