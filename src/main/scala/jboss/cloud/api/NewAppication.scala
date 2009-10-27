package jboss.cloud.api


import deploy.{TaskManager, CreateInstance, DeployApplication}
import java.io.InputStream
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
      "BAD"
    } else {
      rec map(processRecommendation)
      <status application={application.name}><link href={"status/" + application.name}>{application.name}</link></status>.toString  //need to return app URL etc...
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

  def status(name: String) = {
    println("OK")
    //search outstanding tasks - if not there, then check in instances, if there, cool, Otherwise, its AWOL.
  }





}