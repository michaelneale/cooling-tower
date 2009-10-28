package jboss.cloud


import bsh.commands.dir
import deploy.DeployApplication
import java.io.File
import mapping.LocalDatabase
import org.apache.commons.io.IOUtils
import org.testng.annotations.Test
import org.testng.Assert._


@Test class LocalDatabaseTest {


    def checkAppBinary = {
      val db = TestDB.getDB
      val app = Application("foo", "war", true, 0,0,0,0,0,0)
      db.saveApplicationBinary(app, "some data".getBytes)
      val result = db.loadApplicationBinary(app)
      assertEquals(new String(result), "some data")
    }

    def checkInstances = {
      val ldb = TestDB.getDB
      println(ldb.ROOT.getAbsolutePath)
      assert(ldb.listInstances.length == 0)
      ldb.saveInstance(Instance("42", "blah", Image("42", "blah"), Flavor("42", 42, 42, "x86"), "RUNNING", Array(Application("foo", "war", true, 0,0,0,0,0,0))))
      assertEquals(1, ldb.listInstances.size)

      var ins: Instance = ldb.listInstances(0)
      assertEquals("42", ins.id)
      assertEquals(1, ins.applications.size)

      assertEquals("RUNNING", ins.state)

      ldb.updateInstanceState(ins.id, "BLAH")
      ins = ldb.listInstances(0)
      assertEquals("42", ins.id)
      assertEquals(1, ins.applications.size)


      val saveIns = Instance("43", "blah", Image("42", "blah"), Flavor("42", 42, 42, "x86"), "RUNNING", Array(Application("foo", "war", true, 0,0,0,0,0,0)))
      saveIns.publicAddresses = Array("foo.bar")
      ldb.saveInstance(saveIns)
      assertEquals(ldb.listInstances.size, 2)
      assertTrue(ldb.listInstances.filter(_.id == "43").size == 1)
      assertTrue(ldb.listInstances.filter(_.id == "42").size == 1)

      val loadedIns : Instance = ldb.listInstances.filter(_.id == "43")(0)
      assertEquals("foo.bar", loadedIns.publicAddresses(0))



      assertEquals("BLAH", ins.state)
    }

    def checkTasks = {
      val db = TestDB.getDB
      var app = Application("foo", "war", true, 0,0,0,0,0,0)
      var ins = Instance("42", "blah", Image("42", "blah"), Flavor("42", 42, 42, "x86"), "RUNNING", Array(app))
      var d = DeployApplication(app.name, ins)
      db.saveTask(d)
      assertEquals(db.listTasks.size, 1)
      db.removeTask(d)
      assertEquals(0, db.listTasks.size)

      app = Application("foo", "war", true, 0,0,0,0,0,0)
      ins = Instance("42", "blah", Image("42", "blah"), Flavor("42", 42, 42, "x86"), "RUNNING", Array(app))
      d = DeployApplication(app.name, ins)
      db.saveTask(d)

      app = Application("foo", "war", true, 0,0,0,0,0,0)
      ins = Instance("43", "blah", Image("42", "blah"), Flavor("42", 42, 42, "x86"), "RUNNING", Array(app))
      d = DeployApplication(app.name, ins)
      db.saveTask(d)

      val ls = db.listTasks
      assertTrue(ls.size == 2)


      db.removeTask(d)
      assertTrue(db.listTasks.size == 1)
    }

    def checkApplications = {
      val db = TestDB.getDB
      var app = Application("foo", "war", true, 0,0,0,0,0,0)
      assertEquals(0, db.listApplications.size)
      db.saveApplication(app)
      assertEquals(1, db.listApplications.size)
      

    }

}