package jboss.cloud.deploy

import javax.ws.rs.core.{MediaType, Response, Context, Request}
import jboss.cloud.api.{Applications, NewApplication}
import java.net.URI
import javax.ws.rs.core.Response.Status
import java.io.InputStream
import javax.ws.rs.{PathParam, POST, Path, GET}
import jboss.cloud.config.Services

/**
 * This is the RESTful entry point for the deployment services
 */

@Path("/api/applications")
class DeployResource(@Context request: Request) {

    @GET @Path("/")
    def listing = Response.ok(<applications>{Services.database.listApplications.map(a => <application href={a.name}/>)}</applications>.toString, MediaType.APPLICATION_XML).build

    @POST @Path("/{appName}.{appType}")
    def uploadNew(@PathParam("appName") appName: String, @PathParam("appType") appType: String,  binary: InputStream) = {
      (new NewApplication).deploy(appName, appType, binary) match {
        case Some(error) => Response.status(Status.BAD_REQUEST).entity(error.message).build
        case None => Response.created(new URI("/api/applications/" + appName)).build
      }
    }

    @GET @Path("/{appName}")
    def appDetails(@PathParam("appName") name: String) = {
      val state = (new Applications).stateOfApp(name)
      <application name={name} state={state.state}><addresses>{state.addresses.map(ad => <address>{ad}</address>)}</addresses></application>
    }
}