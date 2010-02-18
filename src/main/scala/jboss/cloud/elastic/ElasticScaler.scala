package jboss.cloud.elastic

/**
 * The elastic scaler which adds and removes nodes from an AS cluster.
 */
class ElasticScaler {

  def updateMetric(serverName: String, name: String, value: Double, clusterNodeName: String) = {
    //lookup which clusterNode it applies to... need a map of them...
  }


  def addCluster(clusterName: String) = {}

  def addServerInstance(serverName: String) = {}

  def addClusterNode(serverName: String, clusterName: String, nodeName: String) = {}

  

}

case class Cluster(name: String)
case class Metric(node: ClusterNode, name: String, value: Double) //belongs to a cluster node
case class ClusterNode(server: ServerInstance, cluster: Cluster, name: String) //belongs to cluster and a ServerInstance
case class ServerInstance(name: String) 

