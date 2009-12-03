package jboss.cloud


import config.Services
import org.testng.annotations.Test
import org.testng.Assert._

/**
 * 
 * @author Michael Neale
 */

@Test
class ServicesTest {
  def checkProps = {
    Services.configure
    val dc = Services.deltaCloudConfig
    assertEquals("http://localhost:3000/api", dc.apiURL)
    assertEquals("mockuser", dc.userName)
    assertEquals("mockpassword", dc.password)

    val ins = Services.newInstanceConfig
    assertEquals("michael", ins.userName)
    assertEquals("/etc/jboss/5.1/server/default/deploy", ins.targetDir)
    assertEquals("foo", ins.password)
    assertNull(ins.privateKey)

    assertEquals(Services.database.ROOT.getAbsolutePath, "/users/michaelneale/scratch/cooling-tower-db")

  }
}
