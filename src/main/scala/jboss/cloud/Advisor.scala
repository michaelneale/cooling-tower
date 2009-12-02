package jboss.cloud


import java.util.{Arrays, ArrayList}
import org.drools.builder.{KnowledgeBuilderFactory, ResourceType}
import org.drools.io.ResourceFactory
import org.drools.KnowledgeBaseFactory
import org.drools.runtime.{StatelessKnowledgeSession, StatefulKnowledgeSession}
import org.scala_tools.javautils.Imports._

/**
 * Provides an API for getting recommendations of what instance to run a given application on, or what to start up etc.
 * Uses Drools knowledge base to decide this.
 *
 * @author Michael Neale
 */
                                                                                       
class Advisor {


  /**
   * Return a list of recommendations. If empty returned, that is a problem as no suitable matches were found. 
   */
  def allocateApplication(application: Application,
                          instances: Seq[Instance],
                          images: Seq[Image],
                          flavors: Seq[Flavor],
                          realms: Seq[Realm]) : Seq[Recommendation] = {
    val allocationSession = newAllocationSession
    val ls = new ArrayList[Recommendation]
    allocationSession.setGlobal("results", ls)
    val data = (instances ++ images ++ flavors ++ realms)
    data.map(allocationSession.insert(_))
    allocationSession.insert(application)
    allocationSession.fireAllRules
    allocationSession.dispose

    val recommendations = ls.asScala// convertList(ls)

    return recommendations.filter(!_.isInstanceOf[NewInstanceNeeded]) ++ recommendInstances(recommendations.filter(_.isInstanceOf[NewInstanceNeeded]), data)


  }


  def recommendInstances(needed: Seq[Recommendation], data: Seq[Any]) : Seq[Recommendation] = {
    val session = newServerSession
    val ls = new ArrayList[Recommendation]
    session.setGlobal("results", ls)
    
    data.map(session.insert(_))
    data.map(println(_))

    session.insert(Realm("1", "hey", "RUNNING"))
    val instanceNeeds = needed.map(_.asInstanceOf[NewInstanceNeeded].application)
    instanceNeeds.map(session.insert(_))
    session.fireAllRules
    session.dispose
    ls.asScala
  }




  




  private def newAllocationSession : StatefulKnowledgeSession = newSession("server-assignment.drl")
  private def newServerSession : StatefulKnowledgeSession = newSession("server-management.drl")

  private def newSession(drl: String) = {
    val kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder
    kbuilder.add(ResourceFactory.newClassPathResource(drl), ResourceType.DRL)
    println(kbuilder.getErrors)
    val conf = KnowledgeBaseFactory.newKnowledgeBaseConfiguration
    conf.setProperty("drools.removeIdentities", "true")
    val kb = KnowledgeBaseFactory.newKnowledgeBase(conf)
    kb.addKnowledgePackages(kbuilder.getKnowledgePackages)
    kb.newStatefulKnowledgeSession

  }




}