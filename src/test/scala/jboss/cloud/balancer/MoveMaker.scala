package jboss.cloud.balancer


import org.drools.solver.core.move.factory.CachedMoveFactory
import org.drools.solver.core.move.Move
import org.drools.solver.core.solution.Solution

/**
 * 
 * @author Michael Neale
 */

class MoveMaker extends CachedMoveFactory {
  def createCachedMoveList(p1: Solution) : java.util.List[Move] = null

}