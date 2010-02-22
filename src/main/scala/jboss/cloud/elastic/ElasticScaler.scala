package jboss.cloud.elastic

import reflect.BeanProperty

/**
 * The elastic scaler which adds and removes nodes from an AS cluster.
 */
class ElasticScaler {

  var nodes : Map[String, ClusterNode] = null
  var servers : Map[String, ServerInstance] = null
  var clusters : Map[String, Cluster] = null

  //TODO: can refactor this to be more functional
  def updateMetric(nodeName: String, name: String, value: Double) = {
    //nodes get(nodeName) map(/* update metric here */)
    
  }




  def addClusterNode(serverName: String, clusterName: String, nodeName: String) = {
    println("DO THIS")
  }

  

}


/**
 * We have clusters, as well as domains - a domain is like a slice of a cluster - replication generally only within a domain,
 * domains can be upgraded independently. For most cloud purposes, may only have one domain per up, unless it gets large then expand
 * to multiple domains as needed (if > 100 nodes) - just a suggestion. 
 */
case class Cluster(name: String)


/**
 * Metrics are the readings of values from the cluster:
 *
 *  ActiveSessions - number of sessions active.
 *  BusyConnectors - percentage of connectors busy
 *  ReceiveTraffic - inbound traffic in KB/s
 *  SendTraffic    - outbound traffic in KB/s
 *  RequestCount   - requests per sec
 *  AverageSystemLoad - CPU load
 *  SystemMemoryUsage
 *  HeapMemoryUsage
 *
 * This and more from:  http://www.jboss.org/mod_cluster/java/load.html
 * (should use names derived from the JMX MBeans).
 *  
 */
case class Metric(@BeanProperty val node: ClusterNode, name: String, value: Double) {
  def getCluster = node.cluster
}
case class ClusterNode(server: ServerInstance, cluster: Cluster, name: String) //belongs to cluster and a ServerInstance
case class ServerInstance(name: String) 

