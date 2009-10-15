package jboss.cloud


case class Image( id: Int, name: String)
case class Flavor(id: Int, memory: Float, storage: Float, architecture: String)
case class Realm(id: Int, name: String, state: String /* AVAILABLE or UNAVAILABLE */, limit: Int)

case class Application(name: String,
                       applicationType: String,
                       var canCoExist: Boolean,
                       var memory: Float, /* in GB */
                       var disk: Float, /* In GB */
                       var cpu: Int,
                       var version: Int,
                       var dateCreated: Long,
                       var dateLastUpdated: Long)

case class Instance(id: Int,
                    name: String, 
                    image: Image,
                    flavor: Flavor,
                    state: String /* PENDING, STOPPED, RUNNING */,
                    var applications: Array[Application]) {
  var createdOn = System.currentTimeMillis
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









