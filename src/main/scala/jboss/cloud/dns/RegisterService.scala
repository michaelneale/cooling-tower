package jboss.cloud.dns

import javax.ws.rs.ext.MessageBodyWriter
import java.io.OutputStream
import java.lang.{String, Class}
import javax.ws.rs.core.{MultivaluedMap, MediaType}
import java.lang.annotation.Annotation
import java.lang.reflect.Type
import javax.xml.ws.Provider
import javax.ws.rs.Produces
import org.apache.commons.io.IOUtils

/**
 * The RESTful service for DNS 
 */


class DomainList(val domains: Seq[String])



