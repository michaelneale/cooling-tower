package jboss.cloud.deploy

import config.Services
import scala.concurrent.ops._


sealed trait Task {
  var id = System.nanoTime
}

case class DeployApplication(appName: String, ins: Instance) extends Task {
  def application = ins.applications.filter(_.name == appName)(0)
}
case class CreateInstance(req: InstanceCreateRequest) extends Task

/**
 * For managing background tasks.
 * Should only be one instance of this running at a time ideally.
 *
 * TODO: have a startup task to load up any tasks before accepting more...
 *
 * @author Michael Neale
 */
class TaskManager {

    var WAIT_FOR_STATE = 30000

    def add(t: Task) : Unit = {
      Services.database.saveTask(t)
      t match {
        case a: DeployApplication => spawn { deployApp(a) }
        case b: CreateInstance => spawn { createInstance(b) }
      }
    }

    def remove(ts: Task) = Services.database.removeTask(ts)


    def createInstance(c: CreateInstance) = {
      val instance = Services.deltaCloud.createInstance(c.req.flavor, c.req.image, c.req.realm)
      instance.applications = Array(c.req.application)
      Services.database.saveInstance(instance)
      remove(c)
      add(DeployApplication(c.req.application.name, instance))
    }


    def deployApp(d: DeployApplication) = {
      d.ins.state match {
        case "RUNNING" =>  {
          Services.deployer.deploy(d.application, d.ins)
          remove(d)
        }
        case "PENDING" => {
          val (state, publicAddresses) = pollForRunning(d.ins)
          if (state == "RUNNING") {
              //TODO could run post startup script here??
              d.ins.state = state
              d.ins.publicAddresses = publicAddresses
              Services.database.saveInstance(d.ins)
              Services.deployer.deploy(d.application, d.ins)
           } else {
              println("UNABLE TO DEPLOY AS SERVER NOT WAKING UP")
          }
          remove(d)
        }
        case "STOPPED" => {
          println("WILL NOT BE ABLE TO DEPLOY")
          remove(d)
        }
      }
    }

    def pollForRunning(ins: Instance) = poll(0, ins)

    def poll(tries: Int, ins: Instance) : (String, Array[String]) = {
      val (state, addresses) = Services.deltaCloud.pollInstanceState(ins.id)
      if (state != "RUNNING" && tries < 100) {
        Thread.sleep(WAIT_FOR_STATE)
        poll(tries + 1, ins)
      } else {
        (state, addresses)
      }
    }

}