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

    val kb = KnowledgeBaseFactory.newKnowledgeBase
    kb.addKnowledgePackages(pkgs)


    val img = Image("fedora11", 1)
    val flv1 = Flavor(1, 256, 1024)
    val flv2 = Flavor(2, 512, 1024)
    val flv3 = Flavor(3, 1024, 10000)

    val requirements = ApplicationRequirements(128, 256, 1)
    val runningApplication = RunningApplication(ApplicationRequirements(128, 256, 1), 1, 10, 10)
    




    println("ok")
  }

}