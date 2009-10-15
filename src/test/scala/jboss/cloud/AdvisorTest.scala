package jboss.cloud


import org.testng.annotations.Test
import org.testng.Assert._

/**
 * 
 * @author Michael Neale
 */

@Test
class AdvisorTest {

  @Test def shouldWork = {
    val img = Image(1, "fedora11")
    val flv1 = Flavor(1, 256, 1024, "x86")
    val flv2 = Flavor(2, 512, 1024, "x86")
    val flv3 = Flavor(3, 1024, 10000, "x86")

    val app = Application("mike", "war", true, 42, 100, 1, 0, 0, 0)

    val ins = Instance(1, "mic22", img, flv3, "RUNNING", Array(Application("other", "war", true, 42, 100, 1, 0, 0, 0)))

    val a = new Advisor
    val res = a.allocateApplication(app, List(ins), List(img), List(flv1, flv2, flv3), List(Realm(1, "hey", "RUNNING", 0)))
    assertEquals(1, res.size)
    assertTrue(res(0).isInstanceOf[Assignment])

  }
}