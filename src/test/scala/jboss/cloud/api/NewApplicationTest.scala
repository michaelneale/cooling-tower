package jboss.cloud.api


import config.Services
import org.testng.annotations.Test
import org.testng.Assert._



class NewApplicationTest extends ApiHelper {

  
  @Test def another = {
    fail("here")
  }



  @Test def testCreate = {


    println("helo")
    cleanDatabase
    Services.database.listApplications.size shouldBe 0
    
    get("/applications") shouldBe("<applications/>")
    post("/applications/something.war", "data".getBytes) shouldMatch("<status>")
    get("/applications") shouldMatch("<application name=\"something\">")
    put("/applications/something.war", "data".getBytes)  shouldMatch  ("status")
    








    println("ok")

    //val appList = listApplications



    //check app listing
    //POST an app
    //check app listing for it
    //check status on it...
    //when ready...
    //PUT a new version...
    //check status...
    //DELETE it...
    //check app listing...
    
    


    //POST something... create that app - reserve the name...
    //POST app

  }
}