package jboss.cloud.dns


import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.Date
import org.apache.commons.io.IOUtils
import org.xbill.DNS._
import org.scala_tools.javautils.Imports._
import java.io.{FileInputStream, FileOutputStream, File}

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
    val serial = new SimpleDateFormat("yyyyMMdd").format(new Date).toInt
    val refresh = 86400 //1d
    val FOUR_WEEKS = 2419200
    val expiry = FOUR_WEEKS //4w
    val retry = 120
    val minimum = 60
    val primaryDNSName = Name.fromString("dns", domainName) 
    
    /* start of authority */
    val soa = new SOARecord(domainName, DClass.IN, TTL, primaryDNSName, adminEmailAddress, serial, refresh, retry, expiry, minimum)

    /* The NS stuff (should really have 2 of them, but this is the primary) : */
    val primaryDNSARecord = new ARecord(primaryDNSName, DClass.IN, FOUR_WEEKS, InetAddress.getByName(dnsServerAddress))
    val primaryNSRecord = new NSRecord(domainName, DClass.IN, FOUR_WEEKS, primaryDNSName)

    
    val zone = new Zone(domainName, Array[Record](soa, primaryNSRecord, primaryDNSARecord))
    if (defaultAddress != null && !defaultAddress.isEmpty)  zone.addRecord(new ARecord(domainName, DClass.IN, TTL, InetAddress.getByName(defaultAddress)))
    IOUtils.write(zone.toMasterFile, new FileOutputStream(new File(rootDirectory, domain)))
    zone.toMasterFile
  }

  def zoneFileFor(domain: String) :String = IOUtils.toString(new FileInputStream(new File(rootDirectory, domain)))
  def listDomains = rootDirectory.listFiles.map(_.getName)

  def listSubDomains(domain: String) = {
    val zone = loadZone(domain)
    recordsFor(zone).filter(_.isInstanceOf[ARecord]).map(_.getName.toString).map(n => n.substring(0, n.length -1))
  }



  /** Probably can have optional params for TTL etc... */
  def updateSubDomain(domain: String, subDomain: String, address: String)  = {
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
  private def saveZone(zone: Zone, domain: String) =  {
    //need to update serial !
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