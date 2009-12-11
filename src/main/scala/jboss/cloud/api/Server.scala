package jboss.cloud.api


import config.Services
import java.io.InputStream
import java.net.URI
import javax.ws.rs._
import javax.ws.rs.core.Response.Status
import javax.ws.rs.core.{MediaType, Response, Context, Request}
@Path("/api") class Server(@Context request: Request) {


    @GET @Path("/")
    def root = <api version="1.0"><link href="applications"/></api>

    @GET @Path("/applications/")
    def listing = Response.ok(<applications>{Services.database.listApplications.map(a => <application href={a.name}/>)}</applications>.toString, MediaType.APPLICATION_XML).build 

    @POST @Path("/applications/{appName}.{appType}")
    def uploadNew(@PathParam("appName") appName: String, @PathParam("appType") appType: String,  binary: InputStream) = {
      (new NewApplication).deploy(appName, appType, binary) match {
        case Some(error) => Response.status(Status.BAD_REQUEST).entity(error.message).build
        case None => Response.created(new URI("/api/applications/" + appName)).build
      }
    }

    @GET @Path("/applications/{appName}")
    def appDetails(@PathParam("appName") name: String) = {
      val state = (new Applications).stateOfApp(name)
      <application name={name} state={state.state}><addresses>{state.addresses.map(ad => <address>{ad}</address>)}</addresses></application>
    }




}