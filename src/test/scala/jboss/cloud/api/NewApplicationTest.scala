package jboss.cloud.api


import config.Services
import deltacloud.CloudClient
import org.testng.annotations.Test
import org.testng.Assert._



class NewApplicationTest extends ApiHelper {

  



  @Test def testCreate = {


    println("helo")

    Services.database.listApplications.size shouldBe 0

    var currentStatus = "PENDING"

    val cloud = MockCloud(List(Flavor("id", 42, 42, "x86")), List(Image("img1", "jboss-as")))
    Services.deltaClient = cloud
    val deployer = new MockDeployer
    Services.dep = deployer



    
    get("/applications").body shouldBe <applications/>

    val resp = post("/applications/something.war", "data".getBytes, "application/octet-stream")
    resp.statusCode shouldBe 201
    resp.header("Location") shouldBe "http://localhost:8888/api/applications/something"

    get("/applications").body shouldMatch <applications><application href="something"/></applications>
    get("/applications/something").body shouldMatch <application name="something" status="PENDING"><addresses/></application>


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