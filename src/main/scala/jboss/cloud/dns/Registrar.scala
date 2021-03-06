package jboss.cloud.dns


import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.Date
import org.apache.commons.io.IOUtils
import org.xbill.DNS._
import org.scala_tools.javautils.Imports._
import java.io.{FileInputStream, FileOutputStream, File}
import javax.ws.rs._
import jboss.cloud.config.Services

/**
 * Manage the database of name registrations for the cloud servers
 * Pass in the root of the directory to hold the zone files, and the IP of the DNS server itself
 */
@Path("/api/naming") class Registrar {

  val TTL = 60
  var rootDirectory = new File(Services.dnsZoneFolder)
  var primaryDNS = Services.dnsPrimary
  var secondaryDNS = Services.dnsSecondary

  @GET @Path("/") def root = <api><link rel="domains" href="/api/naming/domains"/></api>.toString
  @GET @Path("/domains") def domainListing = <domains>{listDomains.map(s => <link href={"/api/naming/domains/" + s} rel="domain"/>)}</domains>.toString
  @GET @Path("/domains/{name}") def getSubDomains(@PathParam("name") domainName: String) = {
    <domain name={domainName}>{listSubDomains(domainName).map(rec => <link href={"/api/naming/domains/" + domainName +"/" + rec} rel="address"/>)}
      <link href={"/api/naming/domains/" + domainName + "/default"} rel="address"/>
      <link href={"/api/naming/domains/" + domainName + "/zoneFile"} rel="file"/>
      <link href={"/api/naming/domains/" + domainName + "/services"} rel="services"/>
      <link href={"/api/naming/domains/" + domainName + "/txt"} rel="txt"/>
    </domain>.toString
  }

  @GET @Path("/domains/{name}/services") def serviceListing(@PathParam("name") domain : String) =
        <services>{listServices(domain).map(s => <link href={"/api/naming/domains/" + domain + "/services/" + s} rel="service"/>)}</services>
  @GET @Path("/domains/{name}/txt") def txtListing(@PathParam("name") domain : String) =
        <txt>{listServices(domain).map(s => <link href={"/api/naming/domains/" + domain + "/txt/" + s} rel="txt"/>)}</txt>


  def listDomains = rootDirectory.listFiles.map(_.getName)

  @POST @Path("/domains")
  def registerNewDomain(@FormParam("name") domain: String) = {
    val adminEmailAddress = Name.fromString("admin." + domain, Name.root)
    val domainName = Name.fromString(domain, Name.root)
    val serial = new SimpleDateFormat("yyyyMMdd").format(new Date).toInt
    val refresh = 86400 //1d
    val FOUR_WEEKS = 2419200
    val expiry = FOUR_WEEKS //4w
    val retry = 120
    val minimum = 60

    val primaryDNSName = if (isIP(primaryDNS)) Name.fromString("dns", domainName) else Name.fromString(primaryDNS, Name.root)


    /* start of authority */
    val soa = new SOARecord(domainName, DClass.IN, TTL, primaryDNSName, adminEmailAddress, serial, refresh, retry, expiry, minimum)

    /* The NS stuff (should really have 2 of them, but this is the primary) : */
    val primaryNSRecord = new NSRecord(domainName, DClass.IN, FOUR_WEEKS, primaryDNSName)

    val zone = new Zone(domainName, Array[Record](soa, primaryNSRecord))
    //may need to add A recs for in-zone DNS - I like the word "bailiwick"
    if (isIP(primaryDNS)) zone.addRecord(new ARecord(primaryDNSName, DClass.IN, FOUR_WEEKS, InetAddress.getByName(primaryDNS)))

    if (secondaryDNS != null) {
        val secondaryDNSName = if (isIP(secondaryDNS)) Name.fromString("dns2", domainName) else Name.fromString(secondaryDNS, Name.root)
        val secondaryNSRecord = new NSRecord(domainName, DClass.IN, FOUR_WEEKS, secondaryDNSName)
        zone.addRecord(secondaryNSRecord)
        if (isIP(secondaryDNS)) zone.addRecord(new ARecord(secondaryDNSName, DClass.IN, FOUR_WEEKS, InetAddress.getByName(secondaryDNS)))
    }


    IOUtils.write(zone.toMasterFile, new FileOutputStream(new File(rootDirectory, domain)))
    zone.toMasterFile
  }

  @DELETE @Path("/domains/{name}") 
  def removeDomain(@PathParam("name") domain: String) = new File(rootDirectory, domain).delete

  @PUT @POST @Path("/domains/{name}/default")
  def updateDefaultAddress(@PathParam("name") domain: String, @FormParam("address")  address: String, addressBody: String) = {
    val zone = loadZone(domain)
    val name = Name.fromString(domain, Name.root)
    recordsFor(zone).find(r => r.getName == name && (r.isInstanceOf[ARecord] || r.isInstanceOf[CNAMERecord])).map(zone.removeRecord(_))
    zone.addRecord(makeRecord(name, if (address != null) address else addressBody))
    saveZone(zone, domain)
  }

  @GET @Path("/domains/{name}/default") @Produces(Array("text/plain"))
  def defaultAddressFor(@PathParam("name") domain: String) = {
    val zone = loadZone(domain)
    val name = Name.fromString(domain, Name.root)
    getAddress(recordsFor(zone).find(r => r.getName == name && (r.isInstanceOf[ARecord] || r.isInstanceOf[CNAMERecord])))
  }

  @GET @Path("/domains/{name}/zoneFile") @Produces(Array("text/plain"))
  def zoneFileFor(@PathParam("name") domain: String) :String = IOUtils.toString(new FileInputStream(new File(rootDirectory, domain)))



  /** Show a list of subdomains, excluding DNS and such */
  def listSubDomains(domain: String) : Seq[String] = {
    val zone = loadZone(domain)
    recordsFor(zone)
            .filter(r => r.isInstanceOf[ARecord] || r.isInstanceOf[CNAMERecord])
            .filter(_.getName.toString != domain)
            .map(_.getName.toString.split("\\.")(0))
            .filter(!_.startsWith("dns"))   //do not want DNS or default entries showing up
  }

  /** Probably can have optional params for TTL etc... */
  @POST @PUT @Path("/domains/{name}")
  def updateSubDomain(@PathParam("name") domain: String, @FormParam("subdomain") subDomain: String, @FormParam("address") address: String)  = {
    val zone = loadZone(domain)
    val name = Name.fromString(subDomain, zone.getOrigin)
    recordsFor(zone).find(_.getName == name) foreach(zone.removeRecord(_))
    zone.addRecord(makeRecord(name, address))
    saveZone(zone, domain)
  }


  /**
   * Create/update a SRV entry - for the given domain. The service "name" typically follows the convention of:
   *  _ServiceName._Protocol.Hostname
   *
   * eg:
   *
   *  _infinispan._https - would be passed in as the service parameter.
   *
   * A port number is provided for this service, as is a target name (which must be canonical name - ie have an A record, not a CNAME).
   * 
   * See here http://en.wikipedia.org/wiki/SRV_record1
   * 
   */
  @POST @PUT @Path("/domains/{name}/services")
  def updateService(@PathParam("name") domain: String, @FormParam("service") service: String, @FormParam("address") target: String, @FormParam("port") port: Int) = {
    val zone = loadZone(domain)
    val name = Name.fromString(service, zone.getOrigin)
    recordsFor(zone).find(r => r.isInstanceOf[SRVRecord] && r.getName == name) foreach(zone.removeRecord(_))
    zone.addRecord(new SRVRecord(name, DClass.IN, TTL, 1, 1, port, Name.fromString(target, Name.root)))
    saveZone(zone, domain)
  }

  def listServices(domain: String) : Seq[String] = recordsFor(loadZone(domain)).filter(_.isInstanceOf[SRVRecord]).map(name => name.getName.toString.split("\\.")(0) + "." + name.getName.toString.split("\\.")(1))

  @GET @Path("/domains/{name}/services/{service}")
  def serviceInfo(@PathParam("name") domain: String, @PathParam("service") service: String) = {
    val zone = loadZone(domain)
    val name = Name.fromString(service, Name.fromString(domain, Name.root))
    recordsFor(zone).find(_.getName == name) match {
      case Some(r) => r.toString
      case None => ""
    }
  }

  @DELETE @Path("/domains/{name}/services/{service}")
  def removeService(@PathParam("name") domain: String, @PathParam("service") service: String) = {
    val zone = loadZone(domain)
    val name = Name.fromString(service, Name.fromString(domain, Name.root))    
    recordsFor(zone).filter(r => r.isInstanceOf[SRVRecord] && r.getName == name) foreach(zone.removeRecord(_))
    saveZone(zone, domain)
  }


  /** TXT is for random human readable info. name is the subdomain style name - one day...  */
  def updateTxt(domain: String, rname: String, text: String) = {
    val zone = loadZone(domain)
    val name = Name.fromString(rname, zone.getOrigin)
    recordsFor(zone).find(r => r.isInstanceOf[TXTRecord] && r.getName == name) foreach(zone.removeRecord(_))
    zone.addRecord(new TXTRecord(name, DClass.IN, TTL, text))
    saveZone(zone, domain)
  }

  def listTxt(domain: String) : Seq[String] = recordsFor(loadZone(domain)).filter(_.isInstanceOf[TXTRecord]).map(_.getName.toString.split("\\.")(0))
  def removeTxt(domain: String, rname: String) = {
    val zone = loadZone(domain)
    val name = Name.fromString(rname, Name.fromString(domain, Name.root))
    recordsFor(zone).filter(r => r.isInstanceOf[TXTRecord] && r.getName == name) foreach(zone.removeRecord(_))
    saveZone(zone, domain)
  }




  @POST @PUT @Path("/domains/{name}/{subdomain}")
  def updateViaPath(@PathParam("name") name: String, @PathParam("subdomain") sub: String, @FormParam("address") addressParam: String, addressBody: String) =
    updateSubDomain(name, sub, if (addressParam != null) addressParam else addressBody)



  @GET @Path("/domains/{name}/{subdomain}")
  def subDomainAddress(@PathParam("name") domain: String, @PathParam("subdomain") subDomain: String) = {
    val name = Name.fromString(subDomain, Name.fromString(domain, Name.root))
    getAddress(recordsFor(loadZone(domain)).find(_.getName == name))
  }

  


  @DELETE @Path("/domains/{name}/{subdomain}")
  def removeSubDomain(@PathParam("name") domain: String, @PathParam("subdomain") subDomain: String) = {
    val zone = loadZone(domain)
    val name = Name.fromString(subDomain, Name.fromString(domain, Name.root))
    recordsFor(zone).filter(_.getName == name).map(zone.removeRecord(_))
    saveZone(zone, domain)
  }


  private def makeRecord(name: Name, address: String) = {
    if (isIP(address))  new ARecord(name, DClass.IN, TTL, InetAddress.getByName(address))
    else                new CNAMERecord(name, DClass.IN, TTL, Name.fromString(address, Name.root))
  }

  /** Return true if it is an IP address, false means it is a domain name and should be treated via CNAME etc not A record */
  def isIP(address: String) = address.matches("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+") || address.indexOf(":") > -1



  private def getAddress(rec: Option[Record]) : String = {
    rec match {
      case Some(record) => record match {
        case a: ARecord => a.getAddress.getHostAddress
        case c: CNAMERecord => {
          val nm = c.getTarget.toString
          nm.substring(0, nm.size - 1)
        }
        case _ => ""
      }
      case None => ""
    }
  }




  private def loadZone(domain: String) : Zone = new Zone(Name.fromString(domain, Name.root), new File(rootDirectory, domain).getPath)

  /** Update serial and save */
  private def saveZone(zone: Zone, domain: String) =  {
    val old = zone.getSOA
    zone.removeRecord(old)
    val soa = new SOARecord(old.getName, DClass.IN, TTL, old.getHost, old.getAdmin, old.getSerial + 1, old.getRefresh, old.getRetry, old.getExpire, old.getMinimum)
    zone.addRecord(soa)
    IOUtils.write(zone.toMasterFile, new FileOutputStream(new File(rootDirectory, domain)))
  }



  private def recordsFor(z: Zone) : Seq[Record] = {
    val ls = new java.util.ArrayList[Record]
    val it = z.iterator
    while (it.hasNext) {
      val rec = it.next.asInstanceOf[RRset]
      val recs = rec.rrs
      while(recs.hasNext) {
        val rec = recs.next      
        ls.add(rec.asInstanceOf[Record])
      }
    }
    ls.asScala
  }

  


}