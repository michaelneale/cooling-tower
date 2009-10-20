package jboss.cloud.deploy

/**
 * For managing background tasks. 
 * @author Michael Neale
 */

sealed trait Task
case class DeployApplication(app: Application, ins: Instance) extends Task
case class CreateInstance(req: InstanceCreateRequest) extends Task


class TaskManager {



    def add(ts: Task) = {
      //have a global task register - which can actually be files...
      //but then each task gets its own actor instance
      println("OK")
    }

    //for create - need to call deltacloud, and then poll
    //then when its RUNNING, create a DeployApplication task, and delete itself


    //when its a DeployApplication, load up the appropriate app binary, and fling it to the instance specified
    //when its ACKed, delete the task



}