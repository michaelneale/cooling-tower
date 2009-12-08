package jboss.cloud.api


import config.Services
import java.io.InputStream
import javax.ws.rs._
import javax.ws.rs.core.Response.Status
import javax.ws.rs.core.{MediaType, Response, Context, Request}
@Path("/api/applications") class Server(@Context request: Request) {

    @GET @Path("/")
    def listing = {
     Response.ok (<applications>{Services.database.listApplications.map(a => <link href={a.name}/>)}</applications>.toString, MediaType.APPLICATION_XML).build 
    }

    @POST @Path("/{applicationName}")
    def uploadNew(@PathParam("applicationName") appName: String, binary: InputStream) = {
      (new NewApplication).deploy(appName, binary) match {
        case Some(error) => Response.status(Status.BAD_REQUEST).entity(error.message).build
        case None => Response.ok(<application name={appName}><link href="status"/></application>.toString, MediaType.APPLICATION_XML).build
      }
    }


}