package jboss.cloud


import com.thoughtworks.xstream.XStream
import org.drools.builder.{ResourceType, KnowledgeBuilderFactory}
import org.drools.io.ResourceFactory
import org.drools.KnowledgeBaseFactory
import org.testng.annotations.Test
import org.testng.Assert._

/**
 * 
 * @author Michael Neale
 */

@Test
class DroolsTest {

  /**
   * Scenario: upload a war - no cloud info:
   *   decide what it needs
   *   look for any suitable homes
   *   spin one up if needed
   *   deploy.
   */

  def testMe = {
    val kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder
    kbuilder.add(ResourceFactory.newClassPathResource("basic.drl"), ResourceType.DRL)

    val pkgs = kbuilder.getKnowledgePackages
    assertEquals(1, pkgs.size)

    val kb = KnowledgeBaseFactory.newKnowledgeBase
    kb.addKnowledgePackages(pkgs)

    val i = Image2(1, "mic")
    kb.newStatelessKnowledgeSession.execute(i) //w00t, case classes work a treat !
    assertEquals("dave", i.name)
  }

  @Test def xstreamIt = {
    val ri = Instance(1, "mic22", Image("foo", 1, Flavor(1, 1, 1)), "ACTIVE")

    val xs = new XStream
    val s = xs.toXML(ri)
    println(s)

    val ri_ = xs.fromXML(s).asInstanceOf[Instance]
    assertEquals(ri.name, ri_.name)
    println("ok")
  }

}