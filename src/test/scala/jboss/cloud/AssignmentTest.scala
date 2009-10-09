package jboss.cloud


import org.testng.annotations.Test

/**
 * 
 * @author Michael Neale
 */

@Test
class AssignmentTest {

  @Test def singleRunningInstance = {

    val flv = Flavor(1, 256, 1024)
    val img = Image("img", 2)
    val instance = Instance(42, "mike", img, flv)

    

    


    println("ok")
  }

}