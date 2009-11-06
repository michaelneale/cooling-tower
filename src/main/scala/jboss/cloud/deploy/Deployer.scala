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

class SSHDeployer extends Deployer {
  def deploy(application: Application, instance: Instance) = {
    val scl = new SSHClient
    val instanceConfig = Services.newInstanceConfig
    if (instanceConfig.privateKey != null) {
      scl.connect(instance.publicAddresses(0), instanceConfig.userName, instanceConfig.privateKey.toCharArray, null )
    } else {
      scl.connect(instance.publicAddresses(0), instanceConfig.userName, instanceConfig.password)
    }
    val appFileName = application.name + "." + application.applicationType
    scl.runScript("mkdir ct-temp")
    scl.putFile(Services.database.loadApplicationBinary(application), appFileName, "ct-temp")
    scl.runScript("mv ct-temp/" + appFileName + " " + instanceConfig.targetDir)
    scl.disconnect
  }

}