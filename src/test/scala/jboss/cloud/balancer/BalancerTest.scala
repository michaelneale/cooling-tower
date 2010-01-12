package jboss.cloud.balancer


import org.drools.solver.config.XmlSolverConfigurer
import org.drools.solver.core.Solver
import org.testng.annotations.Test
import org.testng.Assert._
import org.scala_tools.javautils.Imports._

/**
 * Test the balance algorithm (which uses drools planner).
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
                            AppServerInstance(List(Application("wee"), Application("waa")))))

      val mm = new MoveMaker
      val list = mm.createCachedMoveList(initialSolution)

      assertEquals(list.size, 4) //only for combinations that make sense, not with itself.

      println(list)


      val move = list.get(0)
      assertTrue(move.isMoveDoable(null))

      //should be zero, as no point in moving anything... 
      assertEquals(0, mm.createCachedMoveList(BalanceSolution(List(
                            AppServerInstance(List(Application("foo"), Application("bar"))),
                            AppServerInstance(List(Application("foo"), Application("bar")))))).size)

  }

  @Test def bulkMove = {
    val initialSolution = BalanceSolution(List(
                          AppServerInstance(List(Application("foo"), Application("bar"))),
                          AppServerInstance(List(Application("wee"), Application("waa")))))

    val mm = new BulkMoveMaker
    val list = mm.createCachedMoveList(initialSolution)
    assertEquals(2, list.size)


    assertTrue(BulkAppMove(AppServerInstance(List(Application("wah"))), AppServerInstance(List(Application("wee"))), List(Application("wee"))).isMoveDoable(null))
    assertFalse(BulkAppMove(AppServerInstance(List(Application("wah"))), AppServerInstance(List(Application("baz"), Application("wah"))), List(Application("baz"), Application("wah"))).isMoveDoable(null))    


    val as1 = AppServerInstance(List(Application("wah")))
    val as2 = AppServerInstance(List(Application("wee")))
    val move = BulkAppMove(as1, as2, List(Application("wee")))
    move.moveApp
    assertEquals(2, as1.apps.size)
    assertEquals(0, as2.apps.size)
  }



  @Test def move = {
    val target = AppServerInstance(List(Application("wee"), Application("waa")))
    val source = AppServerInstance(List(Application("foo"), Application("bar")))
    val move = AppMove(target, source, Application("bar"))
    assertEquals(source.apps.size, 2)
    assertTrue(move.isMoveDoable(null))
    move.moveApp
    assertEquals(source.apps.size, 1)
    assertEquals(target.apps.size, 3)


    val nilMove = AppMove(AppServerInstance(List(Application("bar"))), AppServerInstance(List(Application("baz"))), Application("bar"))
    assertFalse(nilMove.isMoveDoable(null), "Shouldn't move as already has bar !")


    

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
    assertTrue(solution.applicationServers.filter(_.apps.size == 0).size > 0, solution.toString)
    System.err.println(solution.toString)

  }

  @Test def lessBasicSolutionTry = {
    val initialSolution = BalanceSolution(List(
                          AppServerInstance(List(Application("foo"), Application("bar"))),
                          AppServerInstance(List(Application("wing"), Application("wang"))),
                          AppServerInstance(List(Application("mee"))),
                          AppServerInstance(List(Application("bang"), Application("bing"))),
                          AppServerInstance(List(Application("mee2"))),
                          AppServerInstance(List(Application("mee3"))),
                          AppServerInstance(List(Application("mee4")))
                          ))


    val configurer = new XmlSolverConfigurer
    configurer.configure("/jboss/cloud/balancer/plannerconf.xml");
    val solver = configurer.buildSolver
    solver.setStartingSolution(initialSolution)
    solver.solve

    val solution = solver.getBestSolution.asInstanceOf[BalanceSolution]
    assertTrue(solution.applicationServers.filter(_.apps.size == 0).size > 0, solution.toString)
    System.err.println("SOLUTION: \n" + solution)

    System.err.println("BEST SCORE:" + solver.getBestScore)
    


  }

  @Test def prettyLargeNumber = {

    //every 10th server will have 2 apps on it
    val list = (i: Int) => if (i % 10 == 0) List(Application("app1_" + i), Application("app2_" + i)) else List(Application("app_" + i))
    val servers = (1 to 300).map(i => AppServerInstance(list(i))).toList

    val initialSolution = BalanceSolution(servers)

    val configurer = new XmlSolverConfigurer
    configurer.configure("/jboss/cloud/balancer/plannerconf.xml");
    val solver = configurer.buildSolver
    solver.setStartingSolution(initialSolution)
    solver.solve

    val solution = solver.getBestSolution.asInstanceOf[BalanceSolution]
    assertTrue(solution.applicationServers.filter(_.apps.size == 0).size > 0, solution.toString)

    System.err.println("BEST SCORE:" + solver.getBestScore)
    System.err.println("NUMBER OF FREE: " + solution.applicationServers.filter(_.apps.size == 0).size)



    
  }
}