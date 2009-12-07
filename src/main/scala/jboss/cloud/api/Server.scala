package jboss.cloud.api


import javax.ws.rs.core.{Context, Request}
import javax.ws.rs.{GET, Path}

@Path("/api") class Server(@Context request: Request) {

    @GET @Path("/applications")
    def listing = <applications/> toString


}