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
class NewAppication {

  val advisor = new Advisor

  /**
   * Deploy an app.
   * This should return a URL to request the state of the app deployment (as it is asynchronous, if a new
   * server is required it will typically take a fair bit longer).
   * OR: should this only take config, and take an app later on? 
   */
  def deploy(name: String, app: InputStream) = {
    val instances = Services.database.listInstances
    val images = Services.deltaCloud.images
    val realms = Services.deltaCloud.realms
    val flavors = Services.deltaCloud.flavors
    val (application, appBinary) = Services.analyser.parseApplication(name, app)

    Services.database.saveApplication(application)
    Services.database.saveApplicationBinary(application, appBinary) //stores it as version 1

    val rec = advisor.allocateApplication(application, instances, images, flavors, realms)
    if (rec.filter(_.isInstanceOf[Assignment]).size == 0 && rec.filter(_.isInstanceOf[InstanceCreateRequest]).size == 0) {
      Response.status(HttpServletResponse.SC_BAD_REQUEST).entity("Unable find an instance to run that application.").build
    } else {
      rec map(processRecommendation)
      Response.ok.entity(<status application={application.name}><link href={application.name}>{application.name}</link></status>.toString).build 
    }
  }


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
      //val instance: Instance = Services.database.listInstances.filter(_.applications.filter(_.name == appName).size == 1)(0)

    }
    println("OK")
  }





}