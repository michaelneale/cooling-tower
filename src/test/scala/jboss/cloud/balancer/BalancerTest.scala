package jboss.cloud.balancer


import org.testng.annotations.Test
import org.testng.Assert._

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

  @Test def permutationCity = {
    
  }
}