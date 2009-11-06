package jboss.cloud.deploy


import api.Services
import java.io.InputStream

/**
 * @author Michael Neale
 */

trait Deployer {

  /** Will be called and expects to push the app to the given instance, and run any post install scripts */
  def deploy(application: Application, instance: Instance)
}

class DefaultDeployer extends Deployer {


  def deploy(application: Application, instance: Instance) = {
    val scl = new SSHClient
    scl.connect(instance.publicAddresses(0), "root", "bar")
    val appFileName = application.name + "." + application.applicationType
    scl.putFile(Services.database.loadApplicationBinary(application), appFileName, "cooling-deployments")
    val installScript: String = ""
    if (installScript != "") {
 //      scl.runScript(installScript.replace(("$APPLICATION", appFileName)).replace("$VERSION", application.version))
    }
    scl.disconnect
  }

}