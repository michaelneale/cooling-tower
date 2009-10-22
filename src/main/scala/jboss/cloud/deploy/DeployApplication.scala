package jboss.cloud.deploy

import api.Services
import scala.actors.Actor
import scala.actors.Actor._

sealed trait Task {
  var id = System.nanoTime
}

case class DeployApplication(app: Application, ins: Instance) extends Task
case class CreateInstance(req: InstanceCreateRequest) extends Task

/**
 * For managing background tasks.
 * Should only be one instance of this running at a time ideally.
 *
 * @author Michael Neale
 */
class TaskManager {

    var WAIT_FOR_STATE = 30000

    def add(ts: Task) : Unit = {
      Services.database.addTask(ts)
      performTaskAsync(ts)
    }

    def remove(ts: Task) = {
      Services.database.removeTask(ts)  
    }

    def performTaskAsync(tsk: Task) = {
      val t = new Thread(new Runnable {
        def run = {
          tsk match {
            case d: DeployApplication => deployApp(d)
            case c: CreateInstance => requestInstance(c)
          }
        }
      })
      t.start
    }

    def requestInstance(c: CreateInstance) = {
      val instance = Services.deltaCloud.createInstance(c.req.flavor, c.req.image, c.req.realm)
      instance.applications = Array(c.req.application)
      Services.database.saveInstance(instance)
      remove(c)
      add(DeployApplication(c.req.application, instance))
    }


    def deployApp(d: DeployApplication) = {
      d.ins.state match {
        case "RUNNING" =>  {
          Services.deployer.deploy(d.app, d.ins)
          remove(d)
        }
        case "PENDING" => {
          var state = "PENDING"
          var i = 0
          while (state != "RUNNING" && i < 100) {
            Thread.sleep(WAIT_FOR_STATE)
            state = Services.deltaCloud.pollInstanceState(d.ins.id)
            if (state == "RUNNING") {
              d.ins.state = state
              Services.database.updateInstanceState(d.ins.id, state)
              Services.deployer.deploy(d.app, d.ins)
            }
            i = i + 1
          }
          remove(d)
        }
        case "STOPPED" => {
          println("WILL NOT BE ABLE TO DEPLOY")
          remove(d)
        }
      }
    }

}