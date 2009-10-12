package jboss.cloud


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

  @Test def singleRunningInstance = {

    val kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder
    kbuilder.add(ResourceFactory.newClassPathResource("server-assignment.drl"), ResourceType.DRL)
    println(kbuilder.getErrors)

    val kb = KnowledgeBaseFactory.newKnowledgeBase
    kb.addKnowledgePackages(kbuilder.getKnowledgePackages)


    val img = Image(1, "fedora11")
    val flv1 = Flavor(1, 256, 1024, "x86")
    val flv2 = Flavor(2, 512, 1024, "x86")
    val flv3 = Flavor(3, 1024, 10000, "x86")

    val app = Application("mike", "war", true, 42, 100, 1, 0, 0, 0)

    val ins = Instance(1, "mic22", img, flv3, Array(Application("other", "war", true, 42, 100, 1, 0, 0, 0)))


    val ks = kb.newStatefulKnowledgeSession
    val results = new ArrayList[Any]
    ks.setGlobal("results", results)
    ks.insert(img)
    ks.insert(flv1)
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


}