package jboss.cloud.api


import deltacloud.DeltaClient
import deploy.{Deployer, TaskManager}
import mapping.{LocalDatabase, AppAnalyser}
/**
 * Launching point for getting access to services.
 * @author Michael Neale
 */
object Services {
  var deltaClient = new DeltaClient
  var appAnalyser = new AppAnalyser
  var db = new LocalDatabase
  var taskManager = new TaskManager
  var dep = new Deployer

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
    dep = new Deployer
  }

}