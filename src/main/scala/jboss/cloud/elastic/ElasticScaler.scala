package jboss.cloud.elastic

/**
 * The elastic scaler which adds and removes nodes from an AS cluster.
 */
class ElasticScaler {

  def receiveMetric(name: String, value: Double, clusterNodeName: String) = {
    
  }

}

case class Cluster(name: String)
case class Metric(node: ClusterNode, name: String, value: Double) //belongs to a cluster node
case class ClusterNode(server: ServerInstance, cluster: Cluster, name: String) //belongs to cluster and a ServerInstance
case class ServerInstance(name: String) 

