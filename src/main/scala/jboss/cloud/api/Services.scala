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
}