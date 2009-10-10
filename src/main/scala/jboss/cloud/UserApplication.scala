package jboss.cloud


case class ApplicationRequirements(memory: Int, disk: Int, canCoExist: Boolean, cpu: Int)
case class Image(name: String, id: Int)
case class Flavor(id: Int, memory: Int, storage: Int)

case class RunningApplication(requirements: ApplicationRequirements, version: Int, dateCreated: Long, dateLastUpdated: Long)

case class Instance(id: Int, name: String, image: Image, flavor: Flavor, var applications: List[RunningApplication]) {
  def getSpareMemory = flavor.memory - applications.foldLeft(0)(_ + _.requirements.memory)
  def getSpareStorage = flavor.disk - applications.foldLeft(0)(_ + _.requirements.disk)
}





