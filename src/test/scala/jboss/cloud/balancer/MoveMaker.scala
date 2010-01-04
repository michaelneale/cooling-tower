package jboss.cloud.balancer


import org.drools.solver.core.move.factory.CachedMoveFactory
import org.drools.solver.core.move.Move
import org.drools.solver.core.solution.Solution
import org.drools.WorkingMemory
import reflect.BeanProperty

/**
 * 
 * @author Michael Neale
 */



class MoveMaker extends CachedMoveFactory {
  def createCachedMoveList(sol: Solution) : java.util.List[Move] = null

}

case class AppMove(@BeanProperty var target: AppServerInstance,
                  @BeanProperty var source: AppServerInstance,
                  @BeanProperty var app: Application) extends Move {
  def doMove(wm: WorkingMemory) = {}

  def isMoveDoable(wm: WorkingMemory) = false

  def createUndoMove(wm: WorkingMemory) = null
}