package jboss.cloud.api


import config.Services
import deploy.{TaskManager, CreateInstance, DeployApplication}
import java.io.InputStream
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status

/**
 * This is for creating apps.  
 * @author Michael Neale
 */
class NewApplication {

  val advisor = new Advisor

  /**
   * Deploy an app.
   */
  def deploy(appName: String, appType: String, app: InputStream) : Option[Failed] = {
    val instances = Services.database.listInstances
    val images = Services.deltaCloud.images
    val realms = Services.deltaCloud.realms
    val flavors = Services.deltaCloud.flavors
    val (application, appBinary) = Services.analyser.parseApplication(appName, appType, app)

    Services.database.saveApplication(application)
    Services.database.saveApplicationBinary(application, appBinary) //stores it as version 1

    val rec = advisor.allocateApplication(application, instances, images, flavors, realms)
    if (rec.filter(_.isInstanceOf[Assignment]).size == 0 && rec.filter(_.isInstanceOf[InstanceCreateRequest]).size == 0) {
      Some(Failed("Unable find an instance to run that application."))
    } else {
      rec map(processRecommendation)
      None 
    }
  }

  case class Failed(message: String)


  def processRecommendation(rec: Recommendation) = {
    rec match {
      case a: Assignment => {
        a.instance.applications = a.instance.applications ++ Array(a.application)
        Services.database.saveInstance(a.instance)
        Services.tasks.add(DeployApplication(a.application.name, a.instance))
      }
      case c: InstanceCreateRequest => Services.tasks.add(CreateInstance(c))
      case _ => //ignore for now
    }
  }

  def status(appName: String) = {
    val outstandingTasks = Services.database.listTasks.filter(_ match  {
      case d: DeployApplication => d.appName == appName
      case c: CreateInstance => c.req.application.name == appName
    })

    if (outstandingTasks.size == 0) {
      //it is ok and running??
    } else {
      //it is pending??
    }
    println("OK")
  }





}