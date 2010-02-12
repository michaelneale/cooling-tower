package jboss.cloud.elastic

/**
 * The elastic scaler which adds and removes nodes from an AS cluster.
 */
class ElasticScaler


case class AppCluster(id: String)
case class AppServerInstance(id: String,
                             busyConnectors: Int,
                             heapUsage: Long,
                             sessionCount: Int,
                             cpuLoad: Int,
                             server: Server,
                             cluster: AppCluster)
case class Server(id: String)
