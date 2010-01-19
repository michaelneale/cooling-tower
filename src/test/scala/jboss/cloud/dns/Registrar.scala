package jboss.cloud.dns


import java.io.{FileOutputStream, File}
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.Date
import org.apache.commons.io.IOUtils
import org.xbill.DNS._
import org.scala_tools.javautils.Imports._

/**
 * Manage the database of name registrations for the cloud servers
 * Pass in the root of the directory to hold the zone files, and the IP of the DNS server itself
 * TODO: MX records? 
 */
case class Registrar(rootDirectory: File, dnsServerAddress: String) {

  val TTL = 60

  def registerNewDomain(domain: String, defaultAddress: String) = {
    val adminEmailAddress = Name.fromString("admin." + domain, Name.root)
    val domainName = Name.fromString(domain, Name.root)
    val serial = new SimpleDateFormat("yyyymmdd").format(new Date).toInt
    val refresh = 86400 //1d
    val expiry = 2419200 //4w
    val retry = 120
    val minimum = 60
    val defaultDNSName = Name.fromString("dns", domainName)

    val soa = new SOARecord(domainName, DClass.IN, TTL, defaultDNSName, adminEmailAddress, serial, refresh, retry, expiry, minimum)
    val dnsRecord = new ARecord(defaultDNSName, DClass.IN, 2419200, InetAddress.getByName(dnsServerAddress))
    val nsEntry = new NSRecord(domainName, DClass.IN, 2419200, defaultDNSName)
    val zone = new Zone(domainName, Array[Record](soa, nsEntry, dnsRecord))
    if (defaultAddress != null && !defaultAddress.isEmpty)  zone.addRecord(new ARecord(domainName, DClass.IN, TTL, InetAddress.getByName(defaultAddress)))
    saveZone(zone, domain)
    zone.toMasterFile
  }

  def listDomains = rootDirectory.listFiles.map(_.getName)

  def listSubDomains(domain: String) = {
    val zone = loadZone(domain)
    recordsFor(zone).filter(_.isInstanceOf[ARecord]).map(_.getName.toString).map(n => n.substring(0, n.length -1))
  }



  /** Probably can have optional params for TTL etc... */
  def bindSubDomain(domain: String, subDomain: String, address: String)  = {
    //need to whip through RRset and Records to find the right one... then remove it, create a new one from it.
    val zone = loadZone(domain)
    val name = Name.fromString(subDomain, zone.getOrigin)
    recordsFor(zone).find(_.getName == name).map(zone.removeRecord(_))
    zone.addRecord(new ARecord(name, DClass.IN, TTL, InetAddress.getByName(address)))
    saveZone(zone, domain)
  }

  def subDomainAddress(domain: String, subDomain: String) = {
    //whoops - shouldn't be FQDN for subdomains? 
    val name = Name.fromString(subDomain, Name.fromString(domain, Name.root))
    recordsFor(loadZone(domain)).find(_.getName == name) match {
      case Some(record) => record match {
        case a: ARecord => a.getAddress.getHostAddress
        case c: CNAMERecord => c.getTarget.toString
        case _ => ""
      }
      case None => ""
    }
  }

  def removeSubDomain(domain: String, subDomain: String) = {
    val zone = loadZone(domain)
    val name = Name.fromString(subDomain, Name.fromString(domain, Name.root))    
    recordsFor(zone).filter(_.getName == name).map(zone.removeRecord(_))
    saveZone(zone, domain)
  }


  def aliasSubDomain(domain: String, subDomain: String, targetURI: String) = null
  





  private def loadZone(domain: String) : Zone = new Zone(Name.fromString(domain, Name.root), new File(rootDirectory, domain).getPath)
  private def saveZone(zone: Zone, domain: String) =  IOUtils.write(zone.toMasterFile, new FileOutputStream(new File(rootDirectory, domain)))


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