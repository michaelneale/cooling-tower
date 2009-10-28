package jboss.cloud.deploy


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
    //load app binary, and fling it...
    println("deploying...")
  }

}