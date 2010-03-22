package jboss.cloud.elastic

import reflect.BeanProperty

/**
 * The elastic scaler which adds and removes nodes from an AS cluster.
 */
class ElasticScaler {


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
 * Represents the state of the node - from mod_cluster/JMX metrics
 */
case class NodeState(@BeanProperty name: String, @BeanProperty cluster: Cluster, @BeanProperty avgServerLoad: Float)

