package jboss.cloud


import org.drools.builder.{KnowledgeBuilderFactory, ResourceType}
import org.drools.io.ResourceFactory
import org.drools.KnowledgeBaseFactory
import org.testng.annotations.Test

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







    println("ok")
  }

  @Test def mvelFunctions = {
    val kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder
    //kbuilder.add(ResourceFactory.newClassPathResource("functions.drl"), ResourceType.DRL)
    kbuilder.add(ResourceFactory.newClassPathResource("mylang.dsl"), ResourceType.DSL)
    kbuilder.add(ResourceFactory.newClassPathResource("simple-rules.drl"), ResourceType.DSLR)
    println(kbuilder.getErrors)

  }

}