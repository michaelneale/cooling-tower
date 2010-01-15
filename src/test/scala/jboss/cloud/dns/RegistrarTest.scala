package jboss.cloud.dns

import java.io.File
import org.testng.annotations.Test
import org.testng.Assert._


class RegistrarTest {

  @Test def createNewZone = {

    val reg = Registrar(new File("temp_test_zones"), "8.8.8.8")
    println(reg.registerNewDomain("foo.com", "4.4.4.4"))

  }

  
}