package jboss.cloud.api


import config.Services
import deltacloud.CloudClient
import org.testng.annotations.Test
import org.testng.Assert._



class NewApplicationTest extends ApiHelper {


  @Test def testCreate = {

    Services.taskManager.WAIT_FOR_STATE = 50
    Services.database.listApplications.size shouldBe 0

    val cloud = MockCloud(List(Flavor("id", 42, 500, "x86")), List(Image("img1", "jboss-as")))
    val deployer = new MockDeployer

    Services.deltaClient = cloud
    Services.dep = deployer

    get("/").body shouldBe <api version="1.0"><link href="applications"/></api>
    get("/applications").body shouldBe <applications/>

    cloud.instances.size shouldBe 0
    
    val resp = post("/applications/something.war", "data".getBytes, "application/octet-stream")
    resp.statusCode shouldBe 201
    resp.header("Location") shouldBe "http://localhost:8888/api/applications/something"

    get("/applications").body shouldMatch <applications><application href="something"/></applications>
    get("/applications/something").body shouldMatch <application state="PENDING" name="something"><addresses/></application>


    cloud.instances.size shouldBe 1

    val ins = Services.database.listInstances.filter(_.applications.filter(_.name == "something").size > 0)(0)

    cloud.instances(ins.id).publicAddresses = Array("foo.bar.com")
    cloud.instances(ins.id).state = "RUNNING"


    Thread.sleep(500)

    get("/applications/something").body shouldMatch <application state="RUNNING" name="something"><addresses><address>foo.bar.com</address></addresses></application>



    



    




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