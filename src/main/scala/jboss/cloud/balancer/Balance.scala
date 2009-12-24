package jboss.cloud.balancer


import reflect.BeanProperty

/**
 * To use for intelligent balancing - a simple model to start with. 
 * @author Michael Neale
 */
class Balance

case class Server(@BeanProperty var appServers: List[AppServerInstance])
case class AppServerInstance(@BeanProperty var apps: List[Application])
case class Application(name: String)



