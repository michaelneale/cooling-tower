package jboss.cloud.elastic

/**
 * The elastic scaler which adds and removes nodes from an AS cluster.
 */
class ElasticScaler

case class AppCluster(appServers: Seq[AppServerInstance])
case class AppServerInstance(sessionCount: Int, cpuUsage: Int)
