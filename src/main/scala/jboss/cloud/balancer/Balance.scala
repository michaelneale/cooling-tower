package jboss.cloud.balancer


import reflect.BeanProperty

/**
 * To use for intelligent balancing - a simple model to start with. 
 * @author Michael Neale
 */
class Balance

case class AppServerInstance(@BeanProperty var apps: List[Application]) {
  def copy = AppServerInstance(apps.map(_.copy))
}
case class Application(name: String) {
  def copy = Application(name)
}



