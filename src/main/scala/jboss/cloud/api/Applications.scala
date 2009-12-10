package jboss.cloud.api


import config.Services
import deploy.{DeployApplication, CreateInstance}

/**
 * Deals with application resources.
 *
 * @author Michael Neale
 */
class Applications {
    def stateOfApp(appName: String) = {
      val outstandingTasks = Services.database.listTasks.filter(_ match  {
        case d: DeployApplication => d.appName == appName
        case c: CreateInstance => c.req.application.name == appName
      })
      if (outstandingTasks.size == 0) {
        val ins = Services.database.listInstances.filter(_.applications.filter(_.name == appName).size > 0)(0)
        ApplicationState(ins.state, ins.publicAddresses)
      } else ApplicationState("PENDING", Array())
  }
}

case class ApplicationState(state: String, addresses: Seq[String])