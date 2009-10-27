package jboss.cloud


import com.thoughtworks.xstream.XStream
import java.util.ArrayList
import org.drools.builder.{KnowledgeBuilderFactory, ResourceType}
import org.drools.io.ResourceFactory
import org.drools.{ObjectFilter, KnowledgeBaseFactory}
import org.testng.annotations.Test
import org.testng.Assert._



/**
 * 
 * @author Michael Neale
 */

@Test
class AssignmentTest {

  private def newSession = {
    val kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder
    kbuilder.add(ResourceFactory.newClassPathResource("server-assignment.drl"), ResourceType.DRL)
    println(kbuilder.getErrors)
    val conf = KnowledgeBaseFactory.newKnowledgeBaseConfiguration
    conf.setProperty("drools.removeIdentities", "true")
    val kb = KnowledgeBaseFactory.newKnowledgeBase(conf)
    kb.addKnowledgePackages(kbuilder.getKnowledgePackages)
    kb.newStatefulKnowledgeSession
  }




  @Test def singleRunningInstance = {


    val img = Image("1", "fedora11")
    val flv1 = Flavor("1", 256, 1024, "x86")
    val flv2 = Flavor("2", 512, 1024, "x86")
    val flv3 = Flavor("3", 1024, 10000, "x86")

    val app = Application("mike", "war", true, 42, 100, 1, 0, 0, 0)

    val ins = Instance("1", "mic22", img, flv3, "RUNNING", Array(Application("other", "war", true, 42, 100, 1, 0, 0, 0)))


    val ks = newSession
    val results = new ArrayList[Any]
    ks.setGlobal("results", results)
    ks.insert(img)

    ks.insert(flv1)
    ks.insert(flv2)
    ks.insert(flv3)

    ks.insert(app)
    ks.insert(ins)
    ks.fireAllRules
    ks.dispose

    assertEquals(1, results.size)

    val assig = results.get(0).asInstanceOf[Assignment]
    assertEquals(app, assig.application)
    assertEquals(ins, assig.instance)

    println("ok")
  }

  @Test def noExistingInstances = {
    val ks = newSession
    val app = Application("mike", "war", true, 42, 100, 1, 0, 0, 0)
    ks.insert(app)       
    val ls = new ArrayList[Any]
    ks.setGlobal("results", ls)

    ks.fireAllRules
    ks.dispose

    assertEquals(ls.size, 1)
    val insr = ls.get(0).asInstanceOf[NewInstanceNeeded]
    assertEquals(app, insr.application)

    ks.dispose
  }


  



}