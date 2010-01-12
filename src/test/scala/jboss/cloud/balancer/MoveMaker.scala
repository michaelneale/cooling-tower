package jboss.cloud.balancer


import org.drools.solver.core.move.factory.CachedMoveFactory
import org.drools.solver.core.move.Move
import org.drools.solver.core.solution.Solution
import org.drools.WorkingMemory
import reflect.BeanProperty
import org.scala_tools.javautils.Imports._

/**
 * The move factory for the planner/solver engine - suggests moves. 
 * @author Michael Neale
 */
class MoveMaker extends CachedMoveFactory {
  def createCachedMoveList(sol: Solution) : java.util.List[Move] = {
    val solution = sol.asInstanceOf[BalanceSolution]
    val moves = for (source <- solution.applicationServers;
                     app <- source.apps;
                     target <- solution.applicationServers filter(_ != source))
                yield (AppMove(target, source, app).asInstanceOf[Move])
    moves.asJava
  }

}

case class AppMove(@BeanProperty var target: AppServerInstance,
                  @BeanProperty var source: AppServerInstance,
                  @BeanProperty var app: Application) extends Move {
  def doMove(wm: WorkingMemory) = {
    wm.retract(wm.getFactHandle(source))
    wm.retract(wm.getFactHandle(target))
    moveApp
    wm.insert(source)
    wm.insert(target)
  }

  def moveApp = {
    source.apps = source.apps.remove(_ == app)
    target.apps = app :: target.apps
  }

  def isMoveDoable(wm: WorkingMemory) = !target.apps.contains(app) && source.apps.contains(app)

  def createUndoMove(wm: WorkingMemory) = AppMove(source, target, app)
}

class BulkMoveMaker extends CachedMoveFactory {
  def createCachedMoveList(sol: Solution) = {
    val solution = sol.asInstanceOf[BalanceSolution]
    val moves = for (source <- solution.applicationServers;
                     target <- solution.applicationServers filter(_ != source))
                yield (BulkAppMove(target, source, source.apps).asInstanceOf[Move])
    moves.asJava
  }
}

case class BulkAppMove(val target: AppServerInstance, val source: AppServerInstance, val appsToMove: List[Application]) extends Move {
  def doMove(wm: WorkingMemory) = {
    wm.retract(wm.getFactHandle(source))
    wm.retract(wm.getFactHandle(target))
    moveApp
    wm.insert(source)
    wm.insert(target)
  }

  def moveApp = {
      source.apps = source.apps.remove(appsToMove.contains(_))
      target.apps = appsToMove ++ target.apps
  }

  def createUndoMove(wm: WorkingMemory) = BulkAppMove(source, target, appsToMove)

  def isMoveDoable(wm: WorkingMemory) = {
    target.apps.filter(appsToMove.contains(_)).size == 0 &&
    source.apps.filter(appsToMove.contains(_)).size == appsToMove.size
  }
}