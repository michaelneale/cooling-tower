package jboss.cloud.api


import java.io.InputStream
import java.net.URI
import javax.ws.rs._
import javax.ws.rs.core.Response.Status
import javax.ws.rs.core.{MediaType, Response, Context, Request}
import jboss.cloud.config.Services

/**
 * The root resource for the RESTful api
 */
@Path("/api") class Server {


    @GET @Path("/")
    def root = <api version="1.0"><link href="/api/applications" rel="resource"/><link href="/api/naming" rel="resource"/></api>


}