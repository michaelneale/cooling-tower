package jboss.cloud.config


//import deltacloud.{CloudClient, DeltaClient}
//import deploy.{SSHDeployer, Deployer, TaskManager}
import java.io.{File, FileInputStream}
import java.util.Properties
import jboss.cloud.deploy.{SSHDeployer, Deployer, TaskManager}
import jboss.cloud.deltacloud.{DeltaClient, CloudClient}
//import mapping.{LocalDatabase, AppAnalyser}
import org.apache.commons.io.IOUtils
import jboss.cloud.mapping._

/**
 * Launching point for getting access to services.
 *
 * Design notes:
 *  Config items:
 *      creds for ∂-cloud, creds for installation, pre/post install scripts, root of local db.
 *      These are specified in a config file, which is either root of classpath, called cooling-tower.config, or else
 *      set a System property called  cooling.tower.conf to the path to the config file. 
 *
 *  Pluggable items: Deployer, Cloud Client. 
 *
 *
 * @author Michael Neale
 */
object Services {

  //TODO: Need to have LISTENER context for startup - configure services? or can be lazy? REFACTOR !
  //TODO: refactor config info from actual services - still need to be able to mock out.
  var deltaClient : CloudClient = new DeltaClient
  var appAnalyser = new AppAnalyser
  var db = new LocalDatabase
  var taskManager = new TaskManager
  var dep : Deployer  = new SSHDeployer
  var deltaCloudConfig = new DeltaCloudConfig
  var newInstanceConfig = new NewInstanceConfig

  var dnsPrimary = properties.getProperty("dns-primary")
  var dnsSecondary = properties.getProperty("dns-secondary")
  var dnsZoneFolder = properties.getProperty("dns-zone-folder")


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



  def properties = {
    val props = new Properties
    System.getProperty("cooling.tower.conf", "NONE") match {
      case "NONE" => {
        println("Loading config from classpath")
        props.load(Services.getClass.getResourceAsStream("/cooling-tower.config"))
      }
      case path => {
        println("Loading config from file: " + path)
        props.load(new FileInputStream(new File(path)))
      }
    }
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

