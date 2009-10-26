package jboss.cloud


import org.testng.annotations.Test

/**
 * 
 * @author Michael Neale
 */

class ParaTest {
  
    @Test def fromFuture = {
          import Para._

          val someExpensiveComputation = spawn {
            println("sleeping")
            Thread.sleep(1000)
            println("awake")
            1
          }

          val someResult = spawn {
            Thread.sleep(1000)
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
      println("OK")
      println(x)
    }
}