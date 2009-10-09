package jboss.cloud


case class ApplicationRequirements(memory: Int, disk: Int, canCoExist: Boolean)
case class Image(name: String, id: Int)
case class Flavor(id: Int, memory: Int, storage: Int)
case class Instance(id: Int, name: String, image: Image, flavor: Flavor)





