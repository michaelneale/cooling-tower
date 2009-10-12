package jboss.cloud


case class Image( id: Int, name: String)
case class Flavor(id: Int, memory: Int, storage: Int, architecture: String)

case class Application(name: String,
                       applicationType: String,
                       var canCoExist: Boolean,
                       var memory: Int,
                       var disk: Int,
                       var cpu: Int,
                       var version: Int,
                       var dateCreated: Long,
                       var dateLastUpdated: Long)

case class Instance(id: Int, name: String, image: Image, flavor: Flavor, var applications: Array[Application]) {
  def getSpareMemory = flavor.memory - applications.foldLeft(0)(_ + _.memory)
  def getSpareStorage = flavor.storage - applications.foldLeft(0)(_ + _.disk)
  def getNumberOfApps = applications.length
}


/** Control fact for tracking assignment to instances */
case class Assignment(application: Application, instance: Instance)

/** Control fact for tracking request for a new instance */
case class InstanceRequest(application: Application)









