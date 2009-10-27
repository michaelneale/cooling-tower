package jboss.cloud


import org.testng.annotations.Test
import org.testng.Assert._
import jboss.cloud.FutureProcess._

/**
 * 
 * @author Michael Neale
 */

class FutureTest {
  
    @Test def fromFuture = {


          val someExpensiveComputation = future {
            println("sleeping")
            Thread.sleep(100000)
            println("awake")
            1
          }

          val someResult = future {
            Thread.sleep(100000)
            1
          }

          val cheapComputation = 1


          process(1, someResult, someExpensiveComputation + 1, cheapComputation)
    }

    @Test def fromNow = {

      val someExpensiveComputation = {
        println("sleeping")
        Thread.sleep(1000)
        println("awake")
        1
      }

      val someResult = {
        Thread.sleep(1000)
        1
      }


      val cheapComputation = 1

      process(1, someResult, someExpensiveComputation + 1, cheapComputation)

    }

    def process(arg1: Int, arg2: Int, arg3: Int, arg4: Int) = {
      val x = arg1 + arg2 + arg3 + arg4
      assertEquals(x, 5)
      println("OK")
      println(x)
    }
}