package jboss.cloud.balancer


import org.drools.solver.core.solution.Solution
import org.scala_tools.javautils.Imports._

/**
 * A solution.
 * @author Michael Neale
 */
case class BalanceSolution(val applicationServers: Seq[AppServerInstance]) extends Solution {
  def cloneSolution = BalanceSolution(applicationServers.map(_.copy))
  def getFacts = applicationServers.asJava
}