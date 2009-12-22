package jboss.cloud.config


import deltacloud.{CloudClient, DeltaClient}
import deploy.{SSHDeployer, Deployer, TaskManager}
import java.io.{File, FileInputStream}
import java.util.Properties
import mapping.{LocalDatabase, AppAnalyser}
import org.apache.commons.io.IOUtils
/**
 * Launching point for getting access to services.
 *
 * Design notes:
 *  Config items: creds for ∂-cloud, creds for installation, pre/post install scripts
 *  Pluggable items: Deployer, Cloud Client. 
 *
 * @author Michael Neale
 */
object Services {
  var deltaClient : CloudClient = new DeltaClient
  var appAnalyser = new AppAnalyser
  var db = new LocalDatabase
  var taskManager = new TaskManager
  var dep : Deployer  = new SSHDeployer
  var deltaCloudConfig = new DeltaCloudConfig
  var newInstanceConfig = new NewInstanceConfig

  def deltaCloud = deltaClient
  def analyser = appAnalyser
  def database = db
  def tasks = taskManager
  def deployer = dep


  /** Load up the dependencies */
  def configure = {
    deltaClient = new DeltaClient
    appAnalyser = new AppAnalyser
    db = new LocalDatabase
    taskManager = new TaskManager
    dep = new SSHDeployer
    deltaCloudConfig = new DeltaCloudConfig
    newInstanceConfig = new NewInstanceConfig

    deltaCloudConfig.apiURL = properties.getProperty("deltacloud-url")
    deltaCloudConfig.userName = properties.getProperty("deltacloud-username")
    deltaCloudConfig.password = properties.getProperty("deltacloud-password")

    newInstanceConfig.userName = properties.getProperty("deploy-username")
    newInstanceConfig.password = properties.getProperty("deploy-password")
    newInstanceConfig.targetDir = properties.getProperty("deploy-target-dir")
    val key = properties.getProperty("deploy-private-key")
    if (key != null) newInstanceConfig.privateKey = IOUtils.toString(new FileInputStream(key))

    db.ROOT = new File(properties.getProperty("database-root"))

  }



  val properties = {
    val props = new Properties
    props.load(Services.getClass.getResourceAsStream("/cooling-tower.config"))
    props
  }


}

class DeltaCloudConfig {
  var userName = "mockuser"
  var password = "mockpassword"
  var apiURL = "http://localhost:3000/api"
}

class NewInstanceConfig {
  var userName = "mockuser"
  var password = "mockpassword"
  var privateKey: String = null
  var targetDir = "/somewhere"
}

