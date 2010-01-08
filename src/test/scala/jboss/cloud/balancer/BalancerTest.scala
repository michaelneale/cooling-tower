package jboss.cloud.balancer


import org.drools.solver.config.XmlSolverConfigurer
import org.drools.solver.core.Solver
import org.testng.annotations.Test
import org.testng.Assert._
import org.scala_tools.javautils.Imports._

/**
 * 
 * @author Michael Neale
 */

class BalancerTest {
  @Test def initialSolution = {

        val initialSolution = BalanceSolution(List(
                                AppServerInstance(List(Application("foo"), Application("bar"))),
                                AppServerInstance(List(Application("wing"), Application("wang")))))

        assertEquals(initialSolution.getFacts.size, 2)

        val initialSolution_ = initialSolution.cloneSolution
        assertEquals(initialSolution_.getFacts.size, 2)
        assertNotSame(initialSolution, initialSolution_)

  }

  /**
   * Test the possible moves, that make sense
   * Note that the solver will compose these moves together in wonderful wacky ways...
   */
  @Test def permutationCity = {
      val initialSolution = BalanceSolution(List(
                            AppServerInstance(List(Application("foo"), Application("bar"))),
                            AppServerInstance(List(Application("foo"), Application("bar")))))

      val mm = new MoveMaker
      val list = mm.createCachedMoveList(initialSolution)

      //assertTrue(list.size > 0)
      assertEquals(list.size, 4) //only for combinations that make sense, not with itself.

      println(list)


      val move = list.get(0)
      assertTrue(move.isMoveDoable(null))
  }

  @Test def move = {
    val target = AppServerInstance(List(Application("wee"), Application("waa")))
    val source = AppServerInstance(List(Application("foo"), Application("bar")))
    val move = AppMove(target, source, Application("bar"))
    assertEquals(source.apps.size, 2)
    move.moveApp
    assertEquals(source.apps.size, 1)
    assertEquals(target.apps.size, 3)
  }


  @Test def basicSolutionTry = {
    val initialSolution = BalanceSolution(List(
                          AppServerInstance(List(Application("foo"), Application("bar"))),
                          AppServerInstance(List(Application("wing"), Application("wang")))))


    val configurer = new XmlSolverConfigurer
    configurer.configure("/jboss/cloud/balancer/plannerconf.xml");
    val solver = configurer.buildSolver
    solver.setStartingSolution(initialSolution)
    solver.solve

    val solution = solver.getBestSolution.asInstanceOf[BalanceSolution]
    assertNotNull(solution)



    assertTrue(solution.applicationServers.filter(_.apps.size == 0).size > 0)
    println("solution is: " + solution)
    println("OK")



    
  }
}