package jboss.cloud


case class Image(id: String, name: String)
case class Flavor(id: String, memory: Float, storage: Float, architecture: String)
case class Realm(id: String, name: String, state: String /* AVAILABLE or UNAVAILABLE */)

/** Represents an application 'bundle' */
case class Application(name: String,
                       applicationType: String,
                       var canCoExist: Boolean,
                       var memory: Float, /* in GB */
                       var disk: Float, /* In GB */
                       var cpu: Int,
                       var version: Int,
                       var dateCreated: Long,
                       var dateLastUpdated: Long) 

case class Instance(id: String,
                    name: String, 
                    image: Image,
                    flavor: Flavor,
                    var state: String /* PENDING, STOPPED, RUNNING */,
                    var applications: Array[Application]) {
  var createdOn = System.currentTimeMillis
  var publicAddresses: Array[String] = Array()
  def getSpareMemory = flavor.memory - applications.foldLeft(0: Float)(_ + _.memory)
  def getSpareStorage = flavor.storage - applications.foldLeft(0: Float)(_ + _.disk)
  def getNumberOfApps = applications.length
  def getAge = System.currentTimeMillis - createdOn


}

/** The result of asking what you should do - is a Recommendation ! */
sealed trait Recommendation

/** Control fact for tracking assignment to instances */
case class Assignment(application: Application, instance: Instance) extends Recommendation

/** Control fact for tracking request for a new instance */
case class NewInstanceNeeded(application: Application) extends Recommendation

/** Control fact for creating an instance of a certain type, and then binding the application to it */
case class InstanceCreateRequest(flavor: Flavor, image: Image, realm : Realm, application: Application) extends Recommendation

/** Control fact for disposing an instance */
case class InstanceDestroyRequest(instance: Instance) extends Recommendation








