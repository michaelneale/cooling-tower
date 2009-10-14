package jboss.cloud


import java.util.ArrayList
import org.drools.builder.{KnowledgeBuilderFactory, ResourceType}
import org.drools.io.ResourceFactory
import org.drools.KnowledgeBaseFactory
import org.drools.runtime.StatefulKnowledgeSession
import org.testng.annotations.Test
import org.testng.Assert._

/**
 * 
 * @author Michael Neale
 */

@Test
class ServerManagementTest {

  private def newSession : StatefulKnowledgeSession = {
    val kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder
    kbuilder.add(ResourceFactory.newClassPathResource("server-management.drl"), ResourceType.DRL)
    println(kbuilder.getErrors)
    
    val conf = KnowledgeBaseFactory.newKnowledgeBaseConfiguration
    conf.setProperty("drools.removeIdentities", "true")
    val kb = KnowledgeBaseFactory.newKnowledgeBase(conf)
    kb.addKnowledgePackages(kbuilder.getKnowledgePackages)

    kb.newStatefulKnowledgeSession
  }

  @Test def findInstanceForColoApp = {
    val img = Image(1, "fedora11")
    val flv1 = Flavor(1, 256, 1024, "x86")
    val flv2 = Flavor(2, 512, 1024, "x86")
    val flv3 = Flavor(3, 1024, 10000, "x86")

    val app = Application("mike", "war", true, 360, 100, 1, 0, 0, 0)
    val sess = newSession
    sess.insert(img)
    sess.insert(flv1)
    sess.insert(flv2)
    sess.insert(flv3)
    sess.insert(app)
    val ls = new ArrayList[Any]
    sess.setGlobal("results", ls)

    sess.fireAllRules

    assertEquals(1, ls.size)
    assertEquals(ls.get(0).asInstanceOf[InstanceCreateRequest].flavor, flv2)
    assertEquals(ls.get(0).asInstanceOf[InstanceCreateRequest].application, app)


    sess.dispose
  }

  


  @Test def findInstanceForNonColoApp = {
    val img = Image(1, "fedora11")
    val flv1 = Flavor(1, 256, 1024, "x86")
    val flv2 = Flavor(2, 512, 1024, "x86")
    val flv3 = Flavor(3, 1024, 10000, "x86")

    val app = Application("mike", "war", false, 42, 100, 1, 0, 0, 0)
    val sess = newSession
    sess.insert(img)
    sess.insert(flv1)
    sess.insert(flv2)
    sess.insert(flv3)
    sess.insert(app)
    val ls = new ArrayList[Any]
    sess.setGlobal("results", ls)

    sess.fireAllRules
    assertEquals(1, ls.size)
    assertEquals(ls.get(0).asInstanceOf[InstanceCreateRequest].flavor, flv1)
    
    sess.dispose

  }


  @Test def someAppsAlreadyCoLocated = {
    val img = Image(1, "fedora11")
    val flv1 = Flavor(1, 256, 1024, "x86")
    val flv2 = Flavor(2, 512, 1024, "x86")
    val flv3 = Flavor(3, 1024, 10000, "x86")

    val app = Application("mike", "war", true, 200, 1000, 1, 0, 0, 0)
    val sess = newSession
    sess.insert(app)
    sess.insert(img)
    sess.insert(flv1)
    sess.insert(flv2)
    sess.insert(flv3)

    sess.insert(Instance(1, "mic22", img, flv3, "RUNNING", Array(Application("other", "war", true, 42, 100, 1, 0, 0, 0))))

    val ls = new ArrayList[Any]
    sess.setGlobal("results", ls)
    sess.fireAllRules

    assertEquals(1, ls.size)

    assertEquals(ls.get(0).asInstanceOf[InstanceCreateRequest].flavor, flv3)
    assertEquals(app, ls.get(0).asInstanceOf[InstanceCreateRequest].application)
    
    sess dispose
  }
  



}